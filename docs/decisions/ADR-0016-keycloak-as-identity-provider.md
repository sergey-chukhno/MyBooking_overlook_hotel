# ADR-0016: Keycloak as Centralized Identity Provider & OAuth2/OIDC Authority

## Status
**Accepted**

## Context & Problem Statement
As MyBooking evolves toward a multi-service, multi-tenant distributed architecture with diverse clients (Next.js web portal, Rust/Tauri desktop workstation, Go edge gateway, AI agents, IoT devices), the platform requires robust identity, credential management, session lifecycle, and authentication capabilities.

Implementing a custom authentication and authorization server (handling PKCE, token refresh rotation, session revocation, brute-force lockout, MFA, WebAuthn passkeys, and JWKS key rotation) inside application code is error-prone and distracts from core hospitality domain engineering. Conversely, delegating domain business identity, user-hotel assignments, and fine-grained business authorization entirely to an external IAM vendor creates vendor lock-in and violates domain-driven design principles.

What identity provider should MyBooking adopt, what are the precise boundaries of responsibility between the IdP and MyBooking domain services, and how should user account migration and token verification be executed?

## Decision Drivers
* **Enterprise Security Standards**: Support standard OpenID Connect (OIDC) Core 1.0, OAuth 2.1, RFC 7517 (JWKS), RFC 7636 (PKCE), and RFC 9449 (DPoP).
* **Clear Architectural Separation of Concerns**:
  - Keycloak owns *authentication, credentials, and token issuance*.
  - MyBooking owns *business identity, user profiles, tenant assignments, and domain authorization*.
* **Preservation of Domain Integrity**: Existing internal IDs (`app_user.id`), historical reservations, audit logs, and employee-property relationships must remain stable and unaffected by the IdP adoption.
* **Modern Phishing-Resistant Authentication**: Native support for MFA (TOTP) and FIDO2 / WebAuthn passkeys for hotel staff and administrators.
* **Operational Control & Cloud-Native Deployment**: Must run via Docker Compose in local development and Helm in production Kubernetes (Phase 15), with declarative version-controlled configuration (realm-as-code).
* **Traceability**: Fulfills `SEC-IAM-003`, `SEC-IAM-004`, `SEC-IAM-005`, `SEC-CRYPTO-002`, and `SEC-CLIENT-001` in `docs/security/security-engineering.md`.

## Considered Options
1. **Option 1: Custom Home-Grown Auth Server (`identity-service` with jjwt)**: Implement OIDC token issuance, refresh rotation, and MFA entirely from scratch in Spring Boot.
2. **Option 2: Spring Authorization Server Library**: Build an in-house IdP using Spring's framework components.
3. **Option 3: Managed SaaS Identity (Auth0 / Okta / AWS Cognito)**: Delegate IAM to a commercial cloud vendor.
4. **Option 4: Keycloak Open-Source IAM (Chosen)**: Deploy Keycloak as the centralized, self-hosted OIDC Identity Provider with realm-as-code and clear domain boundaries.

## Decision Outcome
**Chosen Option**: **Option 4: Keycloak Open-Source IAM**.

### 1. Responsibility Boundaries
```
┌──────────────────────────────────────────────────────────┐
│ Keycloak (Identity Provider / OIDC Authority)            │
│ • Credential storage (Argon2 / PBKDF2)                   │
│ • Login, Registration, Password Reset flows              │
│ • MFA (TOTP) & FIDO2 / WebAuthn Passkeys                 │
│ • Session management & Back-Channel Logout               │
│ • OAuth2 / OIDC token issuance & JWKS key rotation       │
└────────────────────────────┬─────────────────────────────┘
                             │ OIDC Tokens (sub = Keycloak UUID)
                             ▼
┌──────────────────────────────────────────────────────────┐
│ MyBooking Backend (Resource Servers / Identity Domain)   │
│ • Internal User Identity (app_user.id = BIGINT)          │
│ • User profiles, phone, address, preferences             │
│ • Hotel property assignments (employee <-> property_id)  │
│ • Business authorization (RBAC + ReBAC + ABAC)           │
│ • Object-level authorization & BOLA prevention           │
└──────────────────────────────────────────────────────────┘
```

- **Keycloak Owns**: Credentials (passwords, WebAuthn keys), authentication protocols, MFA enforcement, session lifecycle, token signing, and public JWKS rotation.
- **MyBooking Owns**: Business identity, user profiles, staff job titles, room status updates, reservations, loyalty balances, and multi-tenant authorization. Passwords leave the MyBooking database entirely.
- **Identity Mapping**:
  - `app_user` retains its immutable `id` (bigserial) as the foreign key target for all business tables (`reservation`, `employee`, `loyalty_account`, etc.).
  - A new unique column `keycloak_id varchar(64) unique` is added to `app_user` to map Keycloak's external subject identifier (`sub`) to internal `app_user.id`.
  - The `password` column in `app_user` is deprecated and dropped in migration.

### 2. Realm & Tenant Architecture
- **Single Platform Realm**: The platform will operate a single unified realm (`mybooking-platform`).
- Hotel property multi-tenancy is modeled within the application domain, avoiding multi-realm operational drift and complex cross-realm identity reconciliation.
- **Keycloak Organizations Evaluation**: Keycloak Organizations will be evaluated as a tenant grouping mechanism; however, **Organizations will not replace application-owned tenant authorization**. A token issued for a user in an Organization remains subject to domain-level property checks.

### 3. Password Migration Strategy: Forced Password Reset at Cut-Over
- Existing user records (`app_user`) will be provisioned into Keycloak via a controlled migration script mapping email, name, and role.
- **Decision**: To maintain clean cryptographic standards and avoid complex, fragile custom BCrypt User Storage SPI providers, users will undergo a **forced password reset** upon first login following Keycloak cut-over.
- Internal IDs, profiles, reservations, hotel assignments, loyalty accounts, and historical audit trails remain 100% intact.
- Identity linking is verified strictly via cryptographic confirmation tokens sent to registered email addresses, never by trusting unverified client input.

### 4. Token Cryptography & Verification
- Tokens are signed asymmetrically using **RS256** (RSA 2048-bit) or **ES256** (ECDSA P-256).
- Distributed backend services (Spring Boot monolith/microservices, Go edge gateway) verify tokens autonomously by querying Keycloak's `/protocol/openid-connect/certs` (JWKS) endpoint.
- Services cache JWKS keys in memory and rotate them dynamically when a new `kid` (Key ID) header is encountered.

### 5. Client Security & Next.js BFF Integration
- The Next.js guest web portal eliminates token storage in browser `localStorage`.
- A Backend-for-Frontend (BFF) node executes the Authorization Code flow with PKCE server-side, storing access/refresh tokens in server memory/Redis and issuing encrypted `HttpOnly; Secure; SameSite=Strict` session cookies to the browser.

## Consequences & Operational Impact
* **Positive**:
  - Enterprise-grade, battle-tested IAM infrastructure with zero custom crypto/auth code.
  - Phishing-resistant WebAuthn passkey support for hotel staff and administrators.
  - Complete elimination of passwords from the MyBooking application database.
  - Asymmetric token verification enables any microservice (Java, Go, Rust, C++) to verify identity locally without database coupling.
  - Zero disruption to existing domain foreign key relationships (`app_user.id`).
* **Negative / Trade-offs**:
  - Introduces Keycloak as an infrastructure component requiring Docker Compose orchestration in development and dedicated database/clustering in production.
  - Forced password reset requires active user re-authentication upon cut-over.

