# ADR-0003: Zero-Trust Principal Identity Resolution & Deprecation of Client-Supplied Identity

## Status
**Accepted**

## Context & Problem Statement
In the initial baseline implementation of the MyBooking platform, endpoints in `EmployeeController` relied on an unauthenticated client-supplied HTTP header (`X-User-Id: <id>`) to identify the calling employee. This created a critical Broken Object Level Authorization (BOLA / IDOR, OWASP API1:2023) vulnerability: any authenticated user could read or modify another employee's shifts, tasks, profile, and leave requests simply by tampering with the header.

Furthermore, across multiple guest and administration controllers (`ClientReservationController`, `EventBookingController`, `FeedbackController`, `FeedbackReplyController`, `LoyaltyController`, `AuthController`, `RoomController`), controllers manually parsed raw HTTP `Authorization: Bearer <token>` headers using ad-hoc `substring(7)` string operations and executed redundant database queries (`getUserIdByEmail`) on every request to resolve caller IDs. The default HMAC secret key was an 88-bit string (`mySecretKey`), violating RFC 7518 requirements (minimum 256 bits for HS256), unauthenticated requests incorrectly returned HTTP 403 instead of HTTP 401, and tokens lacking valid subject/claims were not cleanly rejected.

How do we establish a secure, zero-trust identity resolution model that eliminates client-supplied identity, optimizes performance, and prepares the platform for centralized OAuth2/OIDC (Keycloak) adoption without introducing throwaway code?

## Decision Drivers
* **Security & OWASP Compliance**: Eliminate BOLA/IDOR vulnerabilities completely. Enforce Zero Trust: "Never trust client headers; always verify cryptographic identity."
* **Architectural Decoupling**: Application domain code must depend on a stable, application-owned `UserPrincipal` abstraction rather than vendor-specific or library-specific token structures.
* **Standardization & Long-Term Viability**: Align with Spring Security's standard OAuth2 Resource Server rather than maintaining fragile custom servlet filters.
* **Seamless Evolution to Keycloak (Milestone IdP)**: The transition from symmetric interim tokens (HS256) to Keycloak asymmetric tokens (RS256/ES256 via JWKS) must be a declarative configuration update rather than an application-wide code rewrite.
* **Performance & Clean Code**: Eliminate redundant database round-trips and duplicate token-parsing routines across controllers.
* **Traceability**: Satisfies requirements `SEC-IAM-001`, `SEC-IAM-002`, `SEC-AUTHZ-001`, and `SEC-AUTHZ-003` from `docs/security/security-engineering.md`.

## Considered Options
1. **Option 1: Retain `X-User-Id` with Manual Controller Verification**: Keep the header and require every controller method to manually compare `X-User-Id` against `SecurityContextHolder`.
2. **Option 2: Custom Servlet Filter & JWT Handler**: Extend the existing `JwtAuthenticationFilter` with a custom `UserPrincipal` and jjwt parsing.
3. **Option 3: Spring Security OAuth2 Resource Server with Decoupled `UserPrincipal` (Chosen)**: Adopt Spring Security's standard `spring-boot-starter-oauth2-resource-server` (`NimbusJwtDecoder`). Configure a custom `JwtAuthenticationConverter` that extracts claims and maps the token subject (`sub` / `userId`) to an application-owned `UserPrincipal`. Enforce fail-fast secret validation (>= 256 bits), explicit algorithm restrictions, and standard HTTP 401/403 entry points.

## Decision Outcome
**Chosen Option**: **Option 3: Spring Security OAuth2 Resource Server with Decoupled `UserPrincipal`**.

### Detailed Implementation Specifications

```
  Incoming Request (Bearer JWT)
               │
               ▼
  ┌────────────────────────────────────────────────────────┐
  │ Spring Security Filter Chain                           │
  │ • BearerTokenAuthenticationFilter                      │
  │ • NimbusJwtDecoder (Validates exp, nbf, iss, alg)     │
  └────────────────────────┬───────────────────────────────┘
                           │ Valid JWT Claims
                           ▼
  ┌────────────────────────────────────────────────────────┐
  │ JwtAuthenticationConverter                             │
  │ • Maps sub / userId / role / email                     │
  │ • Constructs application-owned UserPrincipal           │
  │ • Grants authorities (ROLE_CLIENT, ROLE_EMPLOYEE, ...) │
  └────────────────────────┬───────────────────────────────┘
                           │ Authentication(principal=UserPrincipal)
                           ▼
  ┌────────────────────────────────────────────────────────┐
  │ Controller Endpoint                                    │
  │ • @AuthenticationPrincipal UserPrincipal principal     │
  │ • principal.getId(), principal.getEmail()              │
  └────────────────────────────────────────────────────────┘
```

1. **Interim Authentication Infrastructure (Phase 2)**:
   - Include `org.springframework.boot:spring-boot-starter-oauth2-resource-server`.
   - Configure `NimbusJwtDecoder` with explicit HS256 algorithm restriction and HMAC secret key.
   - Enforce **fail-fast startup validation**: if `JWT_SECRET` is unset, blank, or shorter than 256 bits (32 bytes), the application refuses to start.
   - Eliminate hardcoded default secrets from code and production `application.yml`. Dedicated test secrets reside exclusively in `src/test/resources/application-test.yml`.
   - Validate standard claims: `exp` (with max 30s clock skew), `nbf`, `iat`, and `iss`.
   - Reject malformed tokens, expired tokens, or tokens lacking user identification with `401 Unauthorized` and `WWW-Authenticate: Bearer error="invalid_token"`.
2. **Application-Level `UserPrincipal`**:
   - Location: `backend/src/main/java/com/MyBooking/common/security/UserPrincipal.java`.
   - Implements `UserDetails`. Represents internal application invariants (`Long id`, `String email`, `String role`, `Collection<GrantedAuthority> authorities`).
   - Completely decoupled from external IdP or library tokens.
3. **Deprecation & Elimination of `X-User-Id`**:
   - Refactor all 9 endpoints in `EmployeeController` to inject `@AuthenticationPrincipal UserPrincipal principal` and resolve caller ID via `principal.getId()`.
   - Remove `X-User-Id` from frontend API client (`frontend/lib/api.ts`).
   - Enforce that client-supplied `X-User-Id` headers are completely ignored and never used for authorization.
4. **Centralize Identity Injection Across Controllers**:
   - Refactor 25 endpoints across 7 controllers (`ClientReservationController`, `EventBookingController`, `FeedbackController`, `FeedbackReplyController`, `LoyaltyController`, `AuthController`, `RoomController`) to inject `@AuthenticationPrincipal UserPrincipal principal`.
   - Delete manual `extractUserIdFromToken` / `substring(7)` methods.
   - Deprecate/remove redundant `getUserIdByEmail` database lookup methods in domain services.
5. **Target Evolution to Keycloak (Milestone IdP)**:
   - When Keycloak is introduced, the transition to asymmetric RS256/ES256 verification requires **zero controller code changes**.
   - The `SecurityConfig` simply switches from `NimbusJwtDecoder.withSecretKey(...)` to `NimbusJwtDecoder.withJwkSetUri(...)` or `issuer-uri` in application configuration.
   - The `JwtAuthenticationConverter` will map Keycloak's `sub` UUID to the internal `UserPrincipal.id` via domain lookup or Keycloak token mapper claims.

## Consequences & Operational Impact
* **Positive**:
  - Completely closes the critical BOLA / IDOR vulnerability in `EmployeeController`.
  - Standards-compliant Spring Security OAuth2 architecture with clean 401 vs 403 error semantics.
  - Eliminates 1 database query per authenticated request across 25 controller endpoints.
  - Decoupled `UserPrincipal` ensures that future migration to Keycloak or other IdPs requires zero changes to business logic or controllers.
  - Enforces RFC 7518 cryptographic compliance and fail-fast configuration safety.
* **Negative / Trade-offs**:
  - Existing client tokens issued without `userId` claims will be rejected; clients must re-authenticate (acceptable in dev/test baseline).
  - Requires updating existing unit/mock tests to mock `UserPrincipal` and OAuth2 security contexts rather than raw strings.

