# ADR-0019: Gateway Internal Delegation Token Architecture (Downstream Service Identity Propagation)

## Status
**Accepted**

## Context & Problem Statement
When external clients (web browsers via BFF or staff desktop workstations) authenticate through the Go Edge API Gateway, they present OAuth2 access tokens (optionally sender-constrained via DPoP proofs). The Edge Gateway validates incoming transport TLS, rate limits, verifies the access token against Keycloak's JWKS endpoint, and validates client DPoP proofs.

However, propagating external client tokens directly across internal service-to-service networks introduces several architectural and security vulnerabilities:
1. **Audience Ambiguity**: External access tokens issued by Keycloak typically carry broad audience scopes (e.g. `aud: mybooking-api`). If Service A invokes Service B using the original external token, a compromised Service B could replay that broad token against Service C (confused deputy vulnerability).
2. **DPoP Incompatibility Across Internal Hops**: DPoP proofs are cryptographically bound to the client's private key, the external HTTP method, and the external URI. Forwarding a DPoP-bound token internally requires either stripping the proof (turning it into a weak bearer token and violating DPoP invariants) or requiring downstream services to know external client keys and rewritten internal URIs.
3. **Excessive Keycloak Token Exchange Latency**: Calling Keycloak's token endpoint on every inbound API call to execute an OAuth2 Token Exchange (RFC 8693) adds significant network roundtrip latency (50–150ms per request) and transforms the IdP into a high-concurrency runtime bottleneck.

How should the Go Edge API Gateway authenticate and propagate user identity to downstream backend domain services while preserving strict audience scoping, microsecond-level performance, and zero-trust service autonomy?

## Decision Drivers
* **Confused Deputy Prevention**: Tokens received by internal services must be strictly audience-scoped to that specific downstream service.
* **Low Latency & High Concurrency**: Ingress identity propagation must not introduce synchronous HTTP roundtrips to Keycloak for every incoming request.
* **Zero-Trust Downstream Autonomy**: Backend resource servers must remain fully responsible for object-level authorization, tenant isolation, and business rule enforcement; they must not blindly trust the gateway.
* **Traceability**: Satisfies `SEC-CRYPTO-003`, `SEC-DIST-001`, and `SEC-AUTHZ-001` in `docs/security/security-engineering.md`.

## Considered Options
1. **Option 1: Direct Forwarding of External Bearer/DPoP Tokens**: Forward the raw external Keycloak JWT to all downstream services.
   - *Rejected*: Confused deputy risk; incompatible with DPoP semantics across internal hops.
2. **Option 2: Keycloak-Brokered OAuth2 Token Exchange (RFC 8693)**: The Gateway invokes Keycloak's `/token` endpoint using `urn:ietf:params:oauth:grant-type:token-exchange` on every incoming request to exchange the external token for an internal, audience-restricted token.
   - *Pros*: Standards-based token exchange managed entirely by the central IdP.
   - *Cons*: Introduces prohibitive network latency (50–150ms per request) and creates a single point of failure at Keycloak under peak booking traffic (5,000 req/s).
3. **Option 3: Gateway-Signed Asymmetric Internal Delegation JWTs (Chosen)**:
   - The Go Edge API Gateway acts as a narrowly scoped, high-performance internal Security Token Service (STS).
   - Upon validating the incoming external token and DPoP proof, the Gateway locally mints a short-lived, audience-restricted internal delegation JWT signed with a dedicated Gateway private key.

## Decision Outcome
**Chosen Option**: **Option 3: Gateway-Signed Asymmetric Internal Delegation JWTs**.

### 1. Architectural Model & Token Flow
```
External Client                 Go Edge API Gateway                    Downstream Resource Service
      │                                  │                                         │
      │── 1. Request + Token + Proof ───>│                                         │
      │   (DPoP-bound JWT or Bearer)     │── 2. Validate external token & DPoP     │
      │                                  │── 3. Mint Internal Delegation JWT       │
      │                                  │      (aud: reservation-service,         │
      │                                  │       sub: user_123, act: gateway)      │
      │                                  │── 4. Forward with Internal Token (mTLS) │
      │                                  │────────────────────────────────────────>│
      │                                  │                                         │── 5. Validate Gateway JWT
      │                                  │                                         │── 6. Enforce Tenant / BOLA
```

1. **Gateway Local Minting**:
   - The Go Edge Gateway validates incoming TLS, external JWT signatures via Keycloak JWKS, and DPoP proofs.
   - The Gateway mints an internal delegation token locally using an in-memory asymmetric private key (ECDSA P-256 / ES256 or RSA 2048 / RS256).
   - Minting executes in sub-millisecond time (< 1ms) with zero synchronous network calls to Keycloak.

2. **Internal Delegation Token Profile**:
   The internal token carries a canonical claims profile:
   ```json
   {
     "iss": "https://gateway.mybooking.internal",
     "sub": "keycloak-user-uuid-or-opaque-id",
     "aud": "reservation-service",
     "act": {
       "iss": "https://gateway.mybooking.internal",
       "sub": "edge-api-gateway"
     },
     "user_id": 12345,
     "role": "EMPLOYEE",
     "exp": 1730500060,
     "iat": 1730500000,
     "jti": "8e3c1a2b-4d5e-6f7a-8b9c-0d1e2f3a4b5c"
   }
   ```
   - `iss`: Authoritative internal Gateway issuer URL (`https://gateway.mybooking.internal`).
   - `sub`: Preserves the authenticated caller's external subject identifier.
   - `aud`: Strictly restricted to the intended downstream target service (e.g., `reservation-service`).
   - `act` (Actor Claim): Explicitly identifies the Edge Gateway as the delegating actor representing the subject (aligning with RFC 8693 actor semantics without requiring an IdP exchange).
   - `user_id` / `role`: Carries minimal, verified identity metadata.
   - `exp`: Ultra short-lived (configurable between **30 to 60 seconds**).
   - `jti`: Unique token identifier for tracing and replay prevention.

3. **Downstream Service Validation**:
   - Backend resource services (Spring Boot microservices) configure Spring Security OAuth2 Resource Server to validate incoming internal tokens against the Gateway's internal JWKS endpoint (`https://gateway.mybooking.internal/.well-known/jwks.json`).
   - Services verify:
     - Signature against Gateway's published public key.
     - `iss == "https://gateway.mybooking.internal"`.
     - `aud == "<current-service-name>"` (rejecting tokens minted for other microservices).
     - `exp` within valid 30–60s window.

4. **Cryptographic Key Management & Rotation**:
   - The Gateway uses a **dedicated signing keypair**, completely separated from Keycloak's signing keys and session encryption keys.
   - The Gateway public key is published internally via an in-cluster JWKS endpoint.
   - Key rotation executes with overlapping `kid` (Key ID) validity.
   - Key compromise recovery: Rotating the Gateway signing key immediately invalidates all active internal tokens across the entire cluster within 60 seconds.

### 2. Strict Security Boundaries
- **No Arbitrary Business Authority**: The internal delegation token proves *who authenticated at the edge and which service was invoked*. It does **not** grant arbitrary business permissions.
- **Downstream Domain Ownership**: Downstream microservices remain strictly responsible for:
  - Enforcing object-level authorization (BOLA defense).
  - Enforcing multi-tenant property isolation (`property_id` validation).
  - Checking staff active shift states and business aggregate invariants.
- **Zero Trust on Client Headers**: Downstream services **MUST NEVER** trust client-supplied identity or role headers (`X-User-Id`, `X-Role`).
- **Transport Security**: Internal traffic between the Edge Gateway and backend services **MUST** utilize internal transport encryption (TLS 1.3 / mTLS via SPIFFE/SPIRE).

### 3. Future Evolution
- **RFC 8693 Token Exchange through Keycloak**: Documented as a potential future alternative if centralized token auditing at the IdP is mandated by regulatory compliance. However, for initial and high-throughput production baselines, Gateway-signed delegation is the approved architectural standard.
- *Clarification*: The selected Gateway-signed token architecture utilizes RFC 8693 `act` claim conventions, but is locally minted by the Gateway; it **must not** be described as an RFC 8693 token exchange.

## Consequences & Operational Impact
* **Positive**:
  - Eliminates confused deputy vulnerabilities across internal microservice hops.
  - Sub-millisecond identity propagation with zero network overhead on Keycloak.
  - Full compatibility with external DPoP: external DPoP proofs terminate cleanly at the Gateway, while downstream services receive audience-bound internal tokens.
  - Ultra-short token lifetimes (30–60s) strictly bound the attack window if an internal token is intercepted.
* **Negative / Trade-offs**:
  - The Edge Gateway assumes the responsibility of an internal Security Token Service (STS).
  - Requires maintaining the Gateway's internal signing key lifecycle and internal JWKS distribution.

