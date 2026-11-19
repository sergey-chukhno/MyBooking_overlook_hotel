#!/usr/bin/env bash
# ==============================================================================
# MyBooking — Static Container/Compose, 5-Network Isolation & Keycloak Realm
# Policy Linter (CICD-010 / IDP-T01 / IDP-T02)
# ==============================================================================
# Usage:
#   ./scripts/ci/lint-infra-policy.sh [--compose <path>] [--realm <path>] [--dev-users <path>] [--run-self-tests]
#
# Enforces:
#   1. Docker Compose schema validity, pinned patch tags (zero :latest), 127.0.0.1-only
#      host port bindings, 5-network segmentation matrix (kc_db_net, bff_idp_net,
#      bff_redis_net, backend_db_net, bff_backend_net), and Redis RAM-only persistence
#      (--save "" --appendonly no) + scoped ~bff:* ACL invariants (T-02, SEC-IAM-006).
#   2. Keycloak realm-mybooking.json security invariants (T-01, T-09, T-10):
#      - Zero literal localhost URLs or hardcoded secrets in base realm template
#      - Zero wildcard redirectUris (*)
#      - Confidential mybooking-bff client (publicClient=false, standardFlowEnabled=true,
#        implicitFlowEnabled=false, directAccessGrantsEnabled=false, serviceAccountsEnabled=false)
#      - Mandatory PKCE S256 (pkce.code.challenge.method=S256)
#      - Single-use refresh token rotation (revokeRefreshToken=true, refreshTokenMaxReuse=0)
#      - Token lifespans (accessTokenLifespan=300, ssoSessionIdleTimeout=1800, ssoSessionMaxLifespan=28800)
#      - RS256 defaultSignatureAlgorithm and brute-force protection enabled (permanentLockout=false)
#      - Access-token-only mybooking-api audience mapper (access.token.claim=true, id.token.claim=false)
#      - Zero embedded seed users in base realm template (isolated to realm-mybooking-dev-users.json)
# ==============================================================================
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
COMPOSE_FILE="${ROOT_DIR}/docker-compose.yml"
REALM_FILE="${ROOT_DIR}/infra/keycloak/realm-mybooking.json"
DEV_USERS_FILE="${ROOT_DIR}/infra/keycloak/realm-mybooking-dev-users.json"
RUN_SELF_TESTS="false"

while [[ $# -gt 0 ]]; do
  case "$1" in
    --compose)
      COMPOSE_FILE="$2"
      shift 2
      ;;
    --realm)
      REALM_FILE="$2"
      shift 2
      ;;
    --dev-users)
      DEV_USERS_FILE="$2"
      shift 2
      ;;
    --run-self-tests)
      RUN_SELF_TESTS="true"
      shift
      ;;
    *)
      echo "[lint-infra-policy] Unknown argument: $1" >&2
      exit 2
      ;;
  esac
done

fail() {
  echo "[lint-infra-policy] FAIL: $*" >&2
  return 1
}

pass() {
  echo "[lint-infra-policy] PASS: $*"
}

lint_compose() {
  local target_compose="$1"
  echo "[lint-infra-policy] Validating Docker Compose policy on ${target_compose}..."

  if [[ ! -f "${target_compose}" ]]; then
    fail "Compose file not found: ${target_compose}"
  fi

  # 1. Validate compose syntax and render canonical JSON AST
  docker compose -f "${target_compose}" config --quiet || fail "docker compose config --quiet failed on ${target_compose}"
  local compose_json
  compose_json="$(docker compose -f "${target_compose}" config --format json)"

  # 2. Verify all 5 required logical networks exist and internal flags are set
  # Note: docker compose config prunes unused top-level networks (`bff_backend_net` has no container attached until containerized BFF/Backend),
  # so we verify top-level declaration in the raw compose YAML AND verify active networks in the rendered config.
  for net in kc_db_net bff_idp_net bff_redis_net backend_db_net bff_backend_net; do
    grep -Eq "^[[:space:]]+${net}:[[:space:]]*$" "${target_compose}" \
      || fail "Missing required network '${net}' declaration in ${target_compose}"
  done

  for active_net in kc_db_net bff_idp_net bff_redis_net backend_db_net; do
    echo "${compose_json}" | jq -e --arg n "${active_net}" '.networks[$n] != null' >/dev/null \
      || fail "Missing required active network '${active_net}' in rendered ${target_compose}"
  done

  echo "${compose_json}" | jq -e '.networks["kc_db_net"].internal == true' >/dev/null \
    || fail "Network 'kc_db_net' MUST set internal: true"

  awk '
    /^  bff_backend_net:[[:space:]]*$/ { in_block=1; next }
    /^(  [a-z_]+:|[a-z_]+:)/ { if (in_block) in_block=0 }
    in_block && /^    internal:[[:space:]]*true[[:space:]]*$/ { found=1 }
    END { exit !found }
  ' "${target_compose}" || fail "Network 'bff_backend_net' MUST set internal: true"

  # 3. Verify image pinning (zero :latest or unpinned tags)
  local unpinned_images
  unpinned_images="$(echo "${compose_json}" | jq -r '.services | to_entries[] | select((.value.image // "") == "" or (.value.image | test(":latest$")) or (.value.image | contains(":") | not)) | "\(.key)=\(.value.image)"')"
  if [[ -n "${unpinned_images}" ]]; then
    fail "Unpinned or :latest container image detected: ${unpinned_images}"
  fi

  # 4. Verify host port bindings bind strictly to 127.0.0.1 (and keycloak-db exposes zero host ports)
  local kcdb_ports
  kcdb_ports="$(echo "${compose_json}" | jq '.services["keycloak-db"].ports // [] | length')"
  if [[ "${kcdb_ports}" -ne 0 ]]; then
    fail "keycloak-db MUST NOT expose any host ports (found ${kcdb_ports})"
  fi

  local non_loopback_ports
  non_loopback_ports="$(echo "${compose_json}" | jq -r '.services | to_entries[] | .key as $svc | (.value.ports // [])[] | select((.host_ip // "") != "127.0.0.1") | "\($svc):\(.published)"')"
  if [[ -n "${non_loopback_ports}" ]]; then
    fail "All host ports MUST bind strictly to 127.0.0.1; offending bindings: ${non_loopback_ports}"
  fi

  # 5. Enforce 5-Network Service Attachment Matrix (SEC-IAM-006 / T-02)
  # keycloak-db -> ONLY kc_db_net
  echo "${compose_json}" | jq -e '(.services["keycloak-db"].networks | keys | sort) == ["kc_db_net"]' >/dev/null \
    || fail "keycloak-db must attach strictly to [kc_db_net] alone"

  # keycloak -> ONLY [bff_idp_net, kc_db_net]
  echo "${compose_json}" | jq -e '(.services["keycloak"].networks | keys | sort) == ["bff_idp_net", "kc_db_net"]' >/dev/null \
    || fail "keycloak must attach strictly to [bff_idp_net, kc_db_net]"

  # bff-redis -> ONLY [bff_redis_net]
  echo "${compose_json}" | jq -e '(.services["bff-redis"].networks | keys | sort) == ["bff_redis_net"]' >/dev/null \
    || fail "bff-redis must attach strictly to [bff_redis_net] alone"

  # app-db -> ONLY [backend_db_net]
  echo "${compose_json}" | jq -e '(.services["app-db"].networks | keys | sort) == ["backend_db_net"]' >/dev/null \
    || fail "app-db must attach strictly to [backend_db_net] alone"

  # If spring-backend service is defined in compose, it MUST NOT attach to kc_db_net, bff_idp_net, or bff_redis_net
  if echo "${compose_json}" | jq -e '.services["spring-backend"] != null' >/dev/null; then
    echo "${compose_json}" | jq -e '
      (.services["spring-backend"].networks | keys) as $nets |
      (($nets | index("kc_db_net")) == null) and
      (($nets | index("bff_idp_net")) == null) and
      (($nets | index("bff_redis_net")) == null)
    ' >/dev/null || fail "spring-backend is forbidden from attaching to kc_db_net, bff_idp_net, or bff_redis_net"
  fi

  # If nextjs-bff service is defined in compose, it MUST NOT attach to kc_db_net or backend_db_net
  if echo "${compose_json}" | jq -e '.services["nextjs-bff"] != null' >/dev/null; then
    echo "${compose_json}" | jq -e '
      (.services["nextjs-bff"].networks | keys) as $nets |
      (($nets | index("kc_db_net")) == null) and
      (($nets | index("backend_db_net")) == null)
    ' >/dev/null || fail "nextjs-bff is forbidden from attaching to kc_db_net or backend_db_net"
  fi

  # 6. Enforce bff-redis persistence disablement (--save "" --appendonly no) and scoped ACLs (T-02)
  local redis_cmd
  redis_cmd="$(echo "${compose_json}" | jq -c '.services["bff-redis"].command')"

  echo "${compose_json}" | jq -e '
    (.services["bff-redis"].command) as $cmd |
    ($cmd | index("--save") != null) and
    ($cmd[($cmd | index("--save")) + 1] == "") and
    ($cmd | index("--appendonly") != null) and
    ($cmd[($cmd | index("--appendonly")) + 1] == "no") and
    (($cmd | map(tostring) | join(" ")) | test("--appendonly\\s+yes") | not)
  ' >/dev/null || fail "bff-redis MUST disable disk persistence (--save \"\" --appendonly no); found command: ${redis_cmd}"

  echo "${compose_json}" | jq -e '
    (.services["bff-redis"].command | map(tostring) | join(" ")) as $cmd_str |
    ($cmd_str | contains("--requirepass")) and
    ($cmd_str | contains("~bff:*")) and
    ($cmd_str | contains("-flushall")) and
    ($cmd_str | contains("-config")) and
    ($cmd_str | contains("-keys"))
  ' >/dev/null || fail "bff-redis MUST enforce --requirepass and scoped ~bff:* ACLs denying flushall/config/keys"

  pass "Docker Compose 5-network isolation & Redis persistence/ACL policy verified (${target_compose})"
}

lint_realm() {
  local target_realm="$1"
  local target_dev_users="${2:-}"
  echo "[lint-infra-policy] Validating Keycloak Realm policy on ${target_realm}..."

  if [[ ! -f "${target_realm}" ]]; then
    fail "Realm file not found: ${target_realm}"
  fi

  jq -e '.' "${target_realm}" >/dev/null || fail "Invalid JSON in ${target_realm}"

  # 1. Base realm template MUST NOT contain literal localhost or 127.0.0.1 URLs
  if grep -Eqi '(localhost|127\.0\.0\.1|host\.docker\.internal)' "${target_realm}"; then
    fail "Base realm template (${target_realm}) contains hardcoded localhost/127.0.0.1/host.docker.internal URL; use \${KC_BFF_*} parameters"
  fi

  # 2. Base realm template MUST NOT bundle embedded seed users
  jq -e '(.users // []) | length == 0' "${target_realm}" >/dev/null \
    || fail "Base realm template (${target_realm}) MUST NOT contain embedded users; isolate dev accounts in realm-mybooking-dev-users.json"

  # 3. Realm-level security baselines
  jq -e '
    .realm == "mybooking" and
    .enabled == true and
    .registrationAllowed == true and
    .verifyEmail == true and
    .loginWithEmailAllowed == true and
    .duplicateEmailsAllowed == false and
    .bruteForceProtected == true and
    .permanentLockout == false and
    .failureFactor == 5 and
    .defaultSignatureAlgorithm == "RS256" and
    .revokeRefreshToken == true and
    .refreshTokenMaxReuse == 0 and
    .accessTokenLifespan == 300 and
    .ssoSessionIdleTimeout == 1800 and
    .ssoSessionMaxLifespan == 28800
  ' "${target_realm}" >/dev/null || fail "Realm baseline settings (RS256, bruteForceProtected, revokeRefreshToken=true, refreshTokenMaxReuse=0, 300s/1800s/28800s lifespans) violated in ${target_realm}"

  # 4. Password policy must enforce pbkdf2-sha512 (>=210000) or argon2
  jq -e '(.passwordPolicy // "") | (test("hashAlgorithm\\(pbkdf2-sha512\\)") and test("hashIterations\\(210000\\)")) or test("argon2")' "${target_realm}" >/dev/null \
    || fail "Realm passwordPolicy must enforce pbkdf2-sha512 (210000 iterations) or argon2"

  # 5. Realm roles (ROLE_CLIENT, ROLE_EMPLOYEE, ROLE_ADMIN; default role composite assigns ONLY ROLE_CLIENT)
  jq -e '
    ([.roles.realm[].name] | contains(["ROLE_CLIENT", "ROLE_EMPLOYEE", "ROLE_ADMIN"])) and
    ((.roles.realm[] | select(.name == "default-roles-mybooking") | .composites.realm) == ["ROLE_CLIENT"])
  ' "${target_realm}" >/dev/null || fail "Realm roles must define ROLE_CLIENT, ROLE_EMPLOYEE, ROLE_ADMIN and default-roles-mybooking must grant ONLY [\"ROLE_CLIENT\"]"

  # 6. Confidential client mybooking-bff invariants
  jq -e '
    (.clients[] | select(.clientId == "mybooking-bff")) as $bff |
    $bff != null and
    $bff.enabled == true and
    $bff.publicClient == false and
    $bff.standardFlowEnabled == true and
    $bff.implicitFlowEnabled == false and
    $bff.directAccessGrantsEnabled == false and
    $bff.serviceAccountsEnabled == false and
    $bff.secret == "${KC_CLIENT_SECRET_BFF}" and
    $bff.attributes."pkce.code.challenge.method" == "S256" and
    $bff.attributes."backchannel.logout.session.required" == "true" and
    (($bff.attributes."backchannel.logout.url" // "") | length > 0) and
    ($bff.attributes."access.token.signed.response.alg" == "RS256")
  ' "${target_realm}" >/dev/null || fail "Client 'mybooking-bff' violated confidential client, PKCE S256, ROPC/implicit disablement, or backchannel logout policy"

  # 7. Zero wildcard redirectUris on any enabled client
  jq -e '
    [.clients[] | select(.enabled == true) | (.redirectUris // [])[] | select(contains("*"))] | length == 0
  ' "${target_realm}" >/dev/null || fail "Enabled client contains forbidden wildcard (*) in redirectUris"

  # 8. Protocol Mappers on mybooking-bff: access-only mybooking-api audience mapper (id.token.claim=false) + realm_access.roles
  jq -e '
    (.clients[] | select(.clientId == "mybooking-bff") | .protocolMappers) as $mappers |
    ($mappers[] | select(.protocolMapper == "oidc-audience-mapper" and .config."included.custom.audience" == "mybooking-api") |
      (.config."access.token.claim" == "true" and .config."id.token.claim" == "false")) and
    (($mappers | map(select(.protocolMapper == "oidc-usermodel-realm-role-mapper" and .config."claim.name" == "realm_access.roles")) | length) == 1)
  ' "${target_realm}" >/dev/null || fail "mybooking-bff must include access-token-only 'mybooking-api' audience mapper (id.token.claim=false, access.token.claim=true) and realm_access.roles mapper"

  # 9. Deferred/Reserved clients (mybooking-service-worker, mybooking-mobile) must remain disabled in Milestone IdP
  jq -e '
    ([.clients[] | select(.clientId == "mybooking-service-worker" or .clientId == "mybooking-mobile") | .enabled] | all(. == false))
  ' "${target_realm}" >/dev/null || fail "Deferred clients (mybooking-service-worker, mybooking-mobile) must have enabled=false in Milestone IdP"

  # 10. Validate dev-only users fixture if provided
  if [[ -n "${target_dev_users}" && -f "${target_dev_users}" ]]; then
    jq -e '(.users | length >= 3) and ([.users[].credentials[].value] | all(. == "${KC_DEV_SEED_PASSWORD}"))' "${target_dev_users}" >/dev/null \
      || fail "Dev users fixture (${target_dev_users}) must parameterize credentials via \${KC_DEV_SEED_PASSWORD}"
  fi

  pass "Keycloak realm security policy verified (${target_realm})"
}

run_negative_self_tests() {
  echo "[lint-infra-policy] Running fail-closed negative fixture self-tests (CICD-010 §8)..."
  local tmp_dir
  tmp_dir="$(mktemp -d)"
  trap 'rm -rf "${tmp_dir}"' RETURN

  # Negative Test 1: directAccessGrantsEnabled=true in realm JSON must fail
  local bad_realm_ropc="${tmp_dir}/bad-realm-ropc.json"
  jq '(.clients[] | select(.clientId == "mybooking-bff")).directAccessGrantsEnabled = true' "${REALM_FILE}" > "${bad_realm_ropc}"
  if "${BASH_SOURCE[0]}" --realm "${bad_realm_ropc}" >/dev/null 2>&1; then
    fail "Self-test 1 failed: directAccessGrantsEnabled=true was NOT rejected"
  fi
  pass "Negative Self-Test 1: directAccessGrantsEnabled=true rejected as expected"

  # Negative Test 2: wildcard redirectUris (*) in realm JSON must fail
  local bad_realm_wildcard="${tmp_dir}/bad-realm-wildcard.json"
  jq '(.clients[] | select(.clientId == "mybooking-bff")).redirectUris = ["https://app.mybooking.example/*"]' "${REALM_FILE}" > "${bad_realm_wildcard}"
  if "${BASH_SOURCE[0]}" --realm "${bad_realm_wildcard}" >/dev/null 2>&1; then
    fail "Self-test 2 failed: wildcard redirectUris (*) was NOT rejected"
  fi
  pass "Negative Self-Test 2: wildcard redirectUris (*) rejected as expected"

  # Negative Test 3: id.token.claim="true" on mybooking-api audience mapper must fail
  local bad_realm_aud="${tmp_dir}/bad-realm-aud.json"
  jq '(.clients[] | select(.clientId == "mybooking-bff") | .protocolMappers[] | select(.protocolMapper == "oidc-audience-mapper")).config."id.token.claim" = "true"' "${REALM_FILE}" > "${bad_realm_aud}"
  if "${BASH_SOURCE[0]}" --realm "${bad_realm_aud}" >/dev/null 2>&1; then
    fail "Self-test 3 failed: id.token.claim=true on mybooking-api audience mapper was NOT rejected"
  fi
  pass "Negative Self-Test 3: id.token.claim=true on mybooking-api audience mapper rejected as expected"

  # Negative Test 4: --appendonly yes on bff-redis in docker-compose.yml must fail
  local bad_compose_aof="${tmp_dir}/bad-compose-aof.yml"
  sed 's/"no"/"yes"/' "${COMPOSE_FILE}" > "${bad_compose_aof}"
  if "${BASH_SOURCE[0]}" --compose "${bad_compose_aof}" >/dev/null 2>&1; then
    fail "Self-test 4 failed: --appendonly yes on bff-redis was NOT rejected"
  fi
  pass "Negative Self-Test 4: --appendonly yes on bff-redis rejected as expected"

  # Negative Test 5: attaching app-db or spring-backend to bff_idp_net in docker-compose.yml must fail
  local bad_compose_net="${tmp_dir}/bad-compose-net.yml"
  awk '{print} /- backend_db_net/{print "      - bff_idp_net"}' "${COMPOSE_FILE}" > "${bad_compose_net}"
  if "${BASH_SOURCE[0]}" --compose "${bad_compose_net}" >/dev/null 2>&1; then
    fail "Self-test 5 failed: cross-network violation on bff_idp_net was NOT rejected"
  fi
  pass "Negative Self-Test 5: 5-network isolation violation rejected as expected"
}

lint_compose "${COMPOSE_FILE}"
lint_realm "${REALM_FILE}" "${DEV_USERS_FILE}"

if [[ "${RUN_SELF_TESTS}" == "true" ]]; then
  run_negative_self_tests
fi

echo "[lint-infra-policy] All infrastructure, 5-network, Redis, and Keycloak realm policies passed."
