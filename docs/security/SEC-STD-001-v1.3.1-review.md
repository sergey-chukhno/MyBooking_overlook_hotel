# SEC-STD-001 v1.3.1-draft — Security Review Report

- **Document ID**: `SEC-REV-001-v1.3.1`
- **Related Standard**: `docs/security/security-engineering.md` (`SEC-STD-001 v1.3.1-draft`)
- **Review Date**: 2026-11-01
- **Role**: Principal Security Architect & Senior Application Security Engineer
- **Review Objective**: Resolve outstanding security review findings (SEC-FINDING-01 through SEC-FINDING-07) and establish an authoritative, implementation-ready baseline for Phase 2.

---

## 1. Executive Summary of Changes

The security standard `SEC-STD-001` has been updated to **version `1.3.1-draft`** (`Draft — Pending Final Review`). This targeted corrective revision directly addresses all seven findings from the latest security architecture review:

1. **Authoritative Password Policy Reconciliation (SEC-FINDING-01)**: Consolidated all credential ownership exclusively under Keycloak. Eliminated conflicting application-level hashing requirements. Documented Keycloak 26.x PBKDF2-HMAC-SHA512 default/FIPS baseline and Argon2id configurable realm policy, while confirming zero application-managed password storage.
2. **Distributed Multi-Replica DPoP Replay Protection (SEC-FINDING-02)**: Replaced single-instance memory cache with a globally coordinated Redis replay cache across all Gateway replicas. Formally specified atomic registration (`SET ... NX EX`), key derivation (`dpop:replay:{cnf.jkt}:{jti}`), 90-second TTL, fail-closed mechanics, canonical URI validation via trusted reverse proxy configurations, strict RFC 9449 §4.3 query/fragment exclusion, and prohibition of silent downgrade to `Bearer`.
3. **Normative Gateway Internal Delegation JWT Profile (SEC-FINDING-03)**: Fully specified the internal delegation token profile (`typ: mybooking-internal+jwt`, asymmetric `ES256`/`RS256`, `kid`, `iss`, `sub`, `aud`, `act`, `user_id`, `roles`, `scope`, `iat`, `exp`, `jti`) for independent implementation across Spring Boot, Go, C++, and Rust. Explicitly codified audience validation, 5s clock skew, mTLS transport boundaries, and receiving-service authorization responsibility.
4. **GDPR Erasure & Immutable Audit Retention Interplay (SEC-FINDING-04)**: Clarified that WORM storage does not override GDPR Article 17 rights. Established audit data minimization, classified client IP/user-agent as personal data, defined retention categories by legal basis, outlined statutory exception assessments (Art. 17(3)(b)/(e)), documented automated retention expiry, and clarified the legal limitations of crypto-shredding and tombstoning.
5. **WORM/Object Lock Guarantees & Audit Integrity (SEC-FINDING-05)**: Sharpened boundaries between storage immutability, tamper detection (Merkle trees), event completeness (sequence monitoring), original event authenticity (service signatures), and legal non-repudiation. Detailed signed Archival Manifests, dedicated audit signing keys, proactive upload lag alerting (> 15 min), Governance vs. Compliance mode staging, and development MinIO vs. production cloud provider guarantees.
6. **Session Encryption Key Lifecycle Architecture (SEC-FINDING-06)**: Defined the complete cryptographic lifecycle for Redis-backed Next.js BFF session envelope encryption (`SessionEncryptionKeyProvider`, KEK in KMS/Vault, DEK AES-256-GCM). Specified zero-downtime key rotation via versioned `key_id`, read-only retention of prior keys, key retirement policies, key separation invariants, and fail-closed handling.
7. **Security Verification & Traceability Status Taxonomy (SEC-FINDING-07)**: Introduced a formal 5-tier status taxonomy (**Target**, **Designed**, **Implemented**, **Verified**, **Accepted Risk**). Replaced informal labels ("Interim", "Current") and systematically downgraded unsubstantiated "Verified Implementation" claims for Phase 2 controls to **Designed**, requiring traceable automated test evidence.

---

## 2. Findings Resolution Matrix

| Finding ID | Priority | Original Issue | Corrective Action Taken | Document Section(s) | Status |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **SEC-FINDING-01** | **Critical** | Conflicting password hashing policies across IAM prose (Argon2 / PBKDF2-SHA512) and crypto table (PBKDF2-SHA256). Unclear distinction between Keycloak and application credential storage. | Established authoritative policy: Keycloak owns credential management exclusively. Documented PBKDF2-HMAC-SHA512 as Keycloak baseline default / FIPS and Argon2id as configurable realm policy. Removed application-level hashing requirements. Mandated forced password reset on migration. Reconciled all tables and prose. | §2.1, §6.1 Table | **RESOLVED** |
| **SEC-FINDING-02** | **Critical** | Single-instance/local in-memory DPoP replay cache fails across multiple Gateway replicas in K8s. Missing atomic semantics, key derivation, fail-closed rules, canonical URI reconstruction, and query/fragment handling. | Mandated globally shared Redis replay protection across Gateway replicas using atomic `SET key value NX EX ttl`. Defined key derivation `dpop:replay:{cnf.jkt}:{jti}`, 90s TTL, cross-replica rejection, and fail-closed behavior. Required URI reconstruction only from trusted reverse proxies. Mandated RFC 9449 §4.3 query/fragment exclusion. Enforced zero silent downgrade. Added multi-replica test matrix. | §5.3, §12 (`SEC-CRYPTO-003`) | **RESOLVED** |
| **SEC-FINDING-03** | **High** | Internal Gateway delegation JWT profile lacked normative precision for polyglot implementations (Java, Go, C++, Rust). Missing explicit `typ`, `kid`, audience validation rules, and authorization boundaries. | Specified complete JWS header (`typ: mybooking-internal+jwt`, `alg: ES256/RS256`, `kid`) and claims (`iss`, `sub`, `aud`, `act`, `user_id`, `roles`, `scope`, `iat`, `exp`, `jti`). Mandated per-service audience validation, 5s clock skew, internal JWKS distribution, mTLS transport boundary, and receiving-service domain authorization ownership. Clarified custom design (not RFC 8693). | §5.3 | **RESOLVED** |
| **SEC-FINDING-04** | **High** | Risk of implying immutable WORM storage overrides GDPR Article 17 erasure obligations. Undefined audit data minimization, IP/user-agent privacy classification, legal basis, retention expiry, and crypto-shredding limits. | Explicitly stated WORM does not override GDPR. Mandated audit data minimization. Classified client IP and user-agent as personal data. Defined 3 audit categories by legal basis. Specified erasure assessment under Art. 17(3)(b)/(e) statutory exceptions. Mandated automated lifecycle expiry deletion. Clarified that crypto-shredding and tombstoning do not constitute full anonymization. | §8.1 | **RESOLVED** |
| **SEC-FINDING-05** | **High** | Conflation of Object Lock storage immutability with event authenticity, completeness, and legal non-repudiation. Unclear verification procedures, manifest integrity, and MinIO vs production guarantees. | Distinctly defined guarantees: WORM (storage immutability), Merkle trees (tamper detection), sequence counters (completeness), service signatures (authenticity), and legal non-repudiation boundaries. Specified signed Archival Manifests with dedicated signing key, automated background verification, upload lag alerts (> 15 min), Governance vs Compliance staging, and MinIO vs cloud semantics. | §10.1 | **RESOLVED** |
| **SEC-FINDING-06** | **High** | Next.js BFF session and Redis token-encryption requirements lacked explicit key lifecycle, rotation without mass logout, key retirement, and key provider mechanics. | Formalized `SessionEncryptionKeyProvider` abstraction (`EnvSecretKeyManagementProvider` in dev, `KmsSecretKeyManagementProvider` in prod). Defined envelope encryption (KEK/DEK AES-256-GCM). Specified zero-downtime rotation using versioned `key_id` with active/retained keys. Defined retirement policy (8h session max + 1h grace), key separation, and fail-closed handling. | §2.7 | **RESOLVED** |
| **SEC-FINDING-07** | **High** | Inconsistent and unsubstantiated verification status claims ("Verified Implementation", "Interim", "Current") across ASVS table and requirements matrix without committed repository test evidence. | Introduced formal 5-tier status taxonomy (**Target**, **Designed**, **Implemented**, **Verified**, **Accepted Risk**). Systematically downgraded unverified Phase 2 controls to **Designed**, linking them to explicit required test evidence. Retained **Verified** only for controls backed by active automated evidence (e.g. GitLeaks scan `SEC-CRYPTO-001`, controller slice validation `SEC-API-001`). | §11.1, §12 | **RESOLVED** |

---

## 3. Assumptions Made

1. **Keycloak 26.x Baseline**: Assumed Keycloak 26.4.x remains the selected Identity Provider, deploying in a dedicated PostgreSQL container with realm-as-code configuration (`mybooking-platform`).
2. **Redis as Shared Platform Cache**: Assumed the shared Redis cluster approved for Next.js BFF session storage (ADR-0004) is also utilized by the Edge API Gateway for distributed DPoP replay caching and rate limiting.
3. **Internal NTP Clock Synchronization**: Assumed Kubernetes node clocks and internal service runtimes maintain tight clock synchronization via NTP, allowing a strict 5-second clock skew window for internal delegation tokens.
4. **Ingress Topology**: Assumed external traffic enters via a trusted cloud load balancer / ingress controller before reaching the Edge API Gateway, enabling trusted proxy IP filtering and reliable external canonical URI reconstruction.

---

## 4. Unresolved Architectural Questions (Open Decisions Required)

There are **zero blocking architectural questions** preventing the approval of the baseline or the execution of Phase 2. However, the following architectural choices will require formal ADRs in future phases:

1. **Dedicated KMS / Secret Management Platform (Phase 08 / Phase 15)**:
   - *Status*: **OPEN — DECISION REQUIRED PRIOR TO PHASE 08**.
   - *Description*: While the `SessionEncryptionKeyProvider` and Vault/KMS envelope encryption patterns are defined, the specific enterprise secret store product (HashiCorp Vault vs. AWS KMS vs. GCP KMS vs. Kubernetes External Secrets Operator) remains a Candidate requiring an ADR before microservices extraction.
2. **Fine-Grained ReBAC/ABAC Policy Decision Point (Phase 08+)**:
   - *Status*: **OPEN — CANDIDATE UNDER EVALUATION**.
   - *Description*: ADR-0017 evaluates OpenFGA (Google Zanzibar model) and Cedar/OPA sidecars. This remains an architectural candidate; native Java/Spring Security evaluators govern authorization through Phase 07.

---

## 5. Decisions Requiring Legal and Compliance Validation

The following items are technical specifications that **must undergo formal legal and regulatory review** before production deployment:

1. **Statutory Audit Retention Schedules (§8.1, §10.1)**:
   - The technical architecture provides 7–10 year retention for tax/accounting records, 1–3 years for security access logs, and 30–90 days for operational diagnostics. The exact statutory retention periods and legal basis citations must be formally validated by corporate legal counsel and tax advisors for each operating jurisdiction.
2. **Compliance Mode S3 Object Lock Classification (§10.1)**:
   - Enabling Compliance Mode Object Lock creates legally immutable, non-erasable data buckets. Corporate legal counsel and DPO must formally approve the specific evidence classes permitted to enter Compliance Mode buckets to prevent statutory conflicts with future legal rulings.
3. **PCI DSS v4.0.1 SAQ A Eligibility Review (§8.2)**:
   - While the backend architecture achieves Zero Cardholder Data Environment (Zero CDE) by outsourcing cardholder entry to a PSP hosted iframe, formal SAQ A eligibility requires merchant website compliance (script integrity controls, CSP, vulnerability scans) and an annual formal Attestation of Compliance (AOC) verified by a Qualified Security Assessor (QSA) or internal compliance officer.

---

## 6. Remaining Security Risks

1. **Pre-Keycloak Interim Security (Phase 02 & Phase 03)**:
   - Until Keycloak is deployed in Milestone IdP, the platform relies on interim Spring Security OAuth2 Resource Server symmetric HS256 tokens. Multi-factor authentication (MFA) and passkeys are not enforced during this interim window. This is an accepted developmental risk documented in the staged roadmap.
2. **Replay Cache Network Partition Risk (§5.3)**:
   - If the Redis replay cluster experiences a network partition, the Edge Gateway fails closed for DPoP requests. While this preserves security integrity, it impacts availability for native desktop clients during Redis outages. High-availability Redis clustering (multi-node Sentinel / Redis Cluster) in Kubernetes will mitigate this risk in Phase 07.
3. **GDPR Linkability in Relational Stores (§8.1)**:
   - Overwriting operational guest profile fields with tombstones (`[ERASED_GUEST]`) while preserving foreign keys maintains relational database consistency, but legally remains pseudonymized data. If auxiliary transaction data contains linkable behavioral clues, full anonymization is not achieved. Legal holds and restricted retention archives must be strictly enforced.

---

## 7. Confirmation of Scope Adherence

- **Documentation Only**: This review pass modified documentation files exclusively (`docs/security/security-engineering.md` and `docs/security/SEC-STD-001-v1.3.1-review.md`).
- **Zero Code Modifications**: No Java backend code, React/Next.js frontend code, test classes, Maven pom.xml files, Dockerfiles, or Docker Compose manifests were created or modified.
- **Zero Premature Infrastructure**: No Keycloak, Redis, or Kafka instances were deployed.
- **Traceability Preserved**: All requirement identifiers (`SEC-*`), ADR references, and section structures were preserved and cross-referenced.

---

```
================================================================================
REVIEW STATUS:
SEC-STD-001 v1.3.1-draft REVIEW COMPLETE — ALL 7 FINDINGS RESOLVED
READY FOR FINAL USER REVIEW & APPROVAL
================================================================================
```

