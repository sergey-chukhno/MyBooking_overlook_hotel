#!/usr/bin/env bash
# ==============================================================================
# MyBooking — Keycloak Realm Import Initialization Helper (IDP-T02)
# ==============================================================================
# Renders /tmp/keycloak-import/mybooking-realm.json from declarative templates:
#   - Base realm template: /opt/keycloak/realm-templates/realm-mybooking.json
#   - Optional dev seed fixture: /opt/keycloak/realm-templates/realm-mybooking-dev-users.json
#     (merged ONLY when KC_IMPORT_DEV_USERS=true)
# All ${KC_*} placeholders are resolved at container startup so zero plaintext
# secrets or hardcoded localhost URLs reside in version-controlled realm JSON.
# ==============================================================================
set -euo pipefail

TEMPLATE_DIR="${KC_REALM_TEMPLATE_DIR:-/opt/keycloak/realm-templates}"
BASE_REALM_FILE="${TEMPLATE_DIR}/realm-mybooking.json"
DEV_USERS_FILE="${TEMPLATE_DIR}/realm-mybooking-dev-users.json"
IMPORT_DIR="${KC_REALM_IMPORT_DIR:-/opt/keycloak/data/import}"
OUTPUT_FILE="${IMPORT_DIR}/mybooking-realm.json"

mkdir -p "${IMPORT_DIR}"

if [[ ! -f "${BASE_REALM_FILE}" ]]; then
  echo "[kc-entrypoint] ERROR: Base realm template not found at ${BASE_REALM_FILE}" >&2
  exit 1
fi

: "${KC_SSL_REQUIRED:?KC_SSL_REQUIRED is required}"
: "${KC_CLIENT_SECRET_BFF:?KC_CLIENT_SECRET_BFF is required}"
: "${KC_BFF_REDIRECT_URI:?KC_BFF_REDIRECT_URI is required}"
: "${KC_BFF_WEB_ORIGIN:?KC_BFF_WEB_ORIGIN is required}"
: "${KC_BFF_POST_LOGOUT_REDIRECT_URI:?KC_BFF_POST_LOGOUT_REDIRECT_URI is required}"
: "${KC_BFF_BACKCHANNEL_LOGOUT_URL:?KC_BFF_BACKCHANNEL_LOGOUT_URL is required}"

substitute_vars() {
  local content="$1"
  content="${content//\$\{KC_SSL_REQUIRED\}/${KC_SSL_REQUIRED}}"
  content="${content//\$\{KC_CLIENT_SECRET_BFF\}/${KC_CLIENT_SECRET_BFF}}"
  content="${content//\$\{KC_BFF_REDIRECT_URI\}/${KC_BFF_REDIRECT_URI}}"
  content="${content//\$\{KC_BFF_WEB_ORIGIN\}/${KC_BFF_WEB_ORIGIN}}"
  content="${content//\$\{KC_BFF_POST_LOGOUT_REDIRECT_URI\}/${KC_BFF_POST_LOGOUT_REDIRECT_URI}}"
  content="${content//\$\{KC_BFF_BACKCHANNEL_LOGOUT_URL\}/${KC_BFF_BACKCHANNEL_LOGOUT_URL}}"
  if [[ -n "${KC_DEV_SEED_PASSWORD:-}" ]]; then
    content="${content//\$\{KC_DEV_SEED_PASSWORD\}/${KC_DEV_SEED_PASSWORD}}"
  fi
  printf '%s\n' "${content}"
}

base_rendered="$(substitute_vars "$(cat "${BASE_REALM_FILE}")")"

if [[ "${KC_IMPORT_DEV_USERS:-false}" == "true" ]]; then
  if [[ ! -f "${DEV_USERS_FILE}" ]]; then
    echo "[kc-entrypoint] ERROR: KC_IMPORT_DEV_USERS=true but ${DEV_USERS_FILE} not found" >&2
    exit 1
  fi
  : "${KC_DEV_SEED_PASSWORD:?KC_DEV_SEED_PASSWORD is required when KC_IMPORT_DEV_USERS=true}"
  users_rendered="$(substitute_vars "$(cat "${DEV_USERS_FILE}")")"
  users_array="$(printf '%s\n' "${users_rendered}" | sed -n '/"users"[[:space:]]*:/,$p' | sed '1s/^[^{[]*//; $s/}[[:space:]]*$//')"
  base_without_closing="$(printf '%s\n' "${base_rendered}" | sed '$d')"
  printf '%s,\n  "users": %s\n}\n' "${base_without_closing}" "${users_array}" > "${OUTPUT_FILE}"
  echo "[kc-entrypoint] Rendered mybooking realm with dev seed users into ${OUTPUT_FILE}"
else
  printf '%s\n' "${base_rendered}" > "${OUTPUT_FILE}"
  echo "[kc-entrypoint] Rendered production-clean mybooking realm (zero dev seed users) into ${OUTPUT_FILE}"
fi

exec /opt/keycloak/bin/kc.sh "$@" --import-realm
