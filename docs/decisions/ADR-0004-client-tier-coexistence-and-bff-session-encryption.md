# ADR-0004: Client Tier Coexistence & Next.js BFF Session Encryption Key Management

## Status
**Accepted**

## Context & Problem Statement
MyBooking serves two primary interactive client user groups with fundamentally different operational profiles:
1. **Hotel Guests**: Access the platform primarily via consumer web browsers on desktops and mobile devices to discover properties, execute room reservations, manage loyalty accounts, and provide stay feedback.
2. **Hotel Operational Staff & Administrators**: Front desk receptionists, night auditors, housekeeping managers, and facility administrators require low-latency interaction, hardware peripheral access (keycard encoders, passport scanners, receipt printers), and offline/flaky-network resilience.

To satisfy these requirements, MyBooking adopted a dual-client strategy: a Next.js 14 web application for guests and a Rust + Tauri desktop application for staff workstations.

For the Next.js web application, storing OAuth2 access tokens and refresh tokens in browser-accessible storage (`localStorage`, `sessionStorage`) exposes guest credentials to Cross-Site Scripting (XSS) exfiltration. To prevent this, the architecture adopts a Backend-for-Frontend (BFF) pattern where the Next.js server handles OAuth2 flows and manages user sessions server-side.

However, in a multi-instance, scaled Next.js deployment, sessions must be shared across BFF replicas. Furthermore, storing sensitive OAuth2 tokens in a shared session store (Redis) requires a rigorous cryptographic key-management and envelope-encryption architecture. How should the BFF manage sessions, encrypt token payloads, and handle cryptographic keys across development and production environments?

## Decision Drivers
* **Defense in Depth**: Eliminating tokens from the browser DOM reduces XSS exposure; however, tokens stored server-side in Redis must not be stored in plaintext.
* **Separation of Concerns**: Session storage and token coordination must remain decoupled from domain authorization truth.
* **Cryptographic Lifecycle & Zero Downtime**: Key rotation must not invalidate all active user sessions or require risky offline database migrations.
* **Vendor Decoupling**: Application code must not be tightly coupled to a specific cloud KMS (AWS KMS, Google Cloud KMS, HashiCorp Vault).
* **Operational Pragmatism**: Development environments must remain lightweight and productive without mandatory enterprise KMS infrastructure, while production enforces strict envelope encryption.
* **Traceability**: Satisfies `SEC-CLIENT-001` and `SEC-CRYPTO-001` in `docs/security/security-engineering.md`.

## Considered Options
1. **Option 1: Browser-Stored Encrypted JWE Cookies**: Next.js emits an encrypted JWE cookie containing access and refresh tokens directly to the browser.
   - *Pros*: Completely stateless on the server; zero Redis dependency.
   - *Cons*: Cookie size exceeds HTTP header limits (4KB) with multiple token scopes; revocation is difficult without a distributed blacklist.
2. **Option 2: PostgreSQL-Backed BFF Session Store**: Store BFF sessions in relational tables.
   - *Pros*: Reuses existing relational database.
   - *Cons*: High I/O contention on session read/write per HTTP request; unnecessary database connection pool exhaustion.
3. **Option 3: Redis Shared Session Store with Envelope Encryption & Key-Management Provider (Chosen)**:
   - High-entropy opaque session identifier in `HttpOnly`, `Secure`, `SameSite=Lax` cookies.
   - Redis stores session state with application-level envelope encryption for token payloads.
   - A key-management provider abstraction provides environment-injected keys in development and Vault/KMS envelope encryption in production.

## Decision Outcome
**Chosen Option**: **Option 3: Redis Shared Session Store with Envelope Encryption & Key-Management Provider**.

### 1. BFF Session Architecture
```
  Browser (Guest)                     Next.js BFF Server                   Shared Redis Store
         │                                    │                                    │
         │── GET /api/reservations ──────────>│                                    │
         │   Cookie: mybooking_session=<id>   │── 1. Read session by ID ──────────>│
         │                                    │<── 2. Return Encrypted Session ────│
         │                                    │── 3. Decrypt DEK via KEK           │
         │                                    │── 4. Decrypt OAuth Token Payload   │
         │                                    │── 5. Forward Bearer JWT to Gateway>│
         │                                    │      (Authorization: Bearer <jwt>) │
```

- The browser receives strictly a high-entropy, cryptographically random opaque session identifier in a cookie:
  `mybooking_session=<session_id>; HttpOnly; Secure; SameSite=Lax; Path=/; Max-Age=28800`
- `SameSite=Lax` is the initial setting to ensure reliable completion of the OIDC authorization-code redirect callback flow across all browser vendors. Transition to `SameSite=Strict` requires explicit end-to-end testing.
- State-changing requests (`POST`, `PUT`, `DELETE`, `PATCH`) **MUST** enforce anti-CSRF verification (Synchronizer Token or Double-Submit HMAC cookie).
- Session identifiers **MUST** be rotated upon successful authentication and significant privilege transitions.

### 2. Envelope Encryption Architecture for Token Payloads
Application-level encryption of OAuth token payloads in Redis is a **mandatory production requirement**, distinct from Redis disk/volume encryption at rest:

```
  ┌────────────────────────────────────────────────────────┐
  │ Key-Management Provider (Vault / Cloud KMS)            │
  │ • Holds Master Key-Encryption-Key (KEK)                 │
  └──────────────────────────┬─────────────────────────────┘
                             │ Unwraps / Generates
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │ Data-Encryption-Key (DEK)                              │
  │ • Generated per session or rotated period              │
  │ • Encrypts OAuth token payload with AES-256-GCM        │
  └──────────────────────────┬─────────────────────────────┘
                             │
                             ▼
  ┌────────────────────────────────────────────────────────┐
  │ Stored in Redis (Opaque Key: session:<session_id>)     │
  │ • Metadata: key_id (KEK reference), iv/nonce           │
  │ • Encrypted DEK (wrapped by KEK)                       │
  │ • Ciphertext: AES-256-GCM encrypted tokens JSON        │
  │ • Auth Tag: GCM authentication tag                     │
  └────────────────────────────────────────────────────────┘
```

1. **Envelope Encryption Mechanics**:
   - A **Key Encryption Key (KEK)** is managed securely in the key-management provider (Vault/KMS).
   - A **Data Encryption Key (DEK)** (256-bit AES) encrypts the OAuth tokens JSON payload using **AES-256-GCM** (authenticated encryption).
   - Redis stores the wrapped DEK, initialization vector (IV), GCM authentication tag, key identifier (`key_id`), and ciphertext.
2. **Key-Management Provider Abstraction**:
   - Application code interfaces with a vendor-neutral provider interface:
     ```typescript
     interface KeyManagementProvider {
       getCurrentKeyId(): Promise<string>;
       encrypt(plaintext: Buffer): Promise<{ ciphertext: Buffer; keyId: string; iv: Buffer; tag: Buffer }>;
       decrypt(ciphertext: Buffer, keyId: string, iv: Buffer, tag: Buffer): Promise<Buffer>;
     }
     ```
   - **Local Development**: `EnvSecretKeyManagementProvider` uses environment-injected 256-bit master keys.
   - **Production**: `KmsKeyManagementProvider` integrates with HashiCorp Vault or cloud KMS, supporting envelope encryption.
3. **Key Rotation & Session Compatibility**:
   - The provider resolves previous keys by `key_id`.
   - During key rotation windows, existing sessions encrypted under older KEKs are decrypted and re-encrypted with the active KEK on the next session read/write cycle, avoiding session invalidation or unsafe offline plaintext migrations.
4. **Cryptographic Key Separation**:
   - Separate, dedicated keys **MUST** be used for:
     - BFF session token encryption.
     - Gateway internal delegation token signing.
     - DPoP client proof keys.
   - Keys **MUST** be segregated strictly between environments (dev, test, staging, prod).

### 3. Session Concurrency, Expiration & Failure Modes
- **Refresh Concurrency Lock**: Simultaneous browser requests entering the BFF must not trigger duplicate refresh calls against Keycloak. The BFF implements a Redis distributed mutex during token refresh; concurrent requests await the refresh completion or reuse the newly refreshed token.
- **Expiration Policies**:
  - Idle session expiration: 30 minutes.
  - Absolute session expiration: 8 hours.
- **Fail-Closed Principle**: If Redis is unreachable or session decryption fails, the BFF **MUST fail closed**, returning an error without granting access.
- **Domain Independence**: Redis is exclusively an operational session cache and token coordinator; it **MUST NOT** be treated as a source of domain authorization truth.

## Consequences & Operational Impact
* **Positive**:
  - Eliminates OAuth access and refresh tokens from browser DOM storage, mitigating XSS token exfiltration.
  - Opaque session cookies conceal internal system architectures and token lifetimes from client inspection.
  - Application-level envelope encryption prevents token theft even if Redis persistence files or snapshots are compromised.
  - Decoupled key provider enables local productivity without cloud dependencies while satisfying enterprise KMS compliance in production.
* **Negative / Trade-offs**:
  - Introduces Redis as an operational runtime dependency for the Next.js web application.
  - Requires maintaining the key provider abstraction and handling distributed refresh mutex locking.

