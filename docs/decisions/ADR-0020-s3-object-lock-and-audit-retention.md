# ADR-0020: Two-Layer Audit Architecture (ClickHouse Operational Analytics & S3 Object Lock WORM Archival)

## Status
**Accepted**

## Context & Problem Statement
The MyBooking platform processes security-sensitive identity events, financial transactions, room status updates, guest access authorizations, and physical IoT/robotic command dispatches. To satisfy hospitality accounting standards, security incident forensics, and regulatory data governance, the platform requires a robust audit logging and evidence preservation architecture.

Traditional centralized logging solutions (e.g. standard Elasticsearch, CloudWatch, or relational database logs) create significant operational and compliance conflicts:
1. **Search vs. Immutability Trade-off**: Systems optimized for high-speed indexing, filtering, and aggregation (like ClickHouse or Elasticsearch) are mutable by privileged database administrators or storage operators; they do not inherently provide Write-Once-Read-Many (WORM) statutory guarantees.
2. **Accidental Premature Deletion & Ransomware Risks**: Standard cloud storage buckets or database tables can be accidentally truncated, dropped, or encrypted by ransomware if administrator credentials are compromised.
3. **Statutory Retention vs. Right to Erasure**: Fiscal, tax, and hotel accounting regulations mandate retaining financial and stay records for up to 5–10 years. Conversely, GDPR Article 17 grants guests the Right to Erasure for operational data.

How should MyBooking architect its audit logging system to support sub-second operational investigation while guaranteeing retention-protected, tamper-resistant archival evidence?

## Decision Drivers
* **Separation of Operational Search from Archival Evidence**: Operational analytics must not compromise the integrity of immutable evidence, and immutable archives must not degrade everyday query performance.
* **Tamper-Resistant Retention**: Protect archival evidence against deletion, truncation, or modification during the mandatory retention window, even by privileged administrators.
* **Controlled Operational Flexibility**: Provide safe, non-destructive testing in development and staging before locking down irreversible production retention policies.
* **Regulatory Compliance Integrity**: Align statutory accounting retention with GDPR data minimization without claiming unwarranted legal guarantees.
* **Traceability**: Satisfies `SEC-OPS-001` and `SEC-DATA-002` in `docs/security/security-engineering.md`.

## Considered Options
1. **Option 1: Relational Database Audit Tables**: Store audit logs in PostgreSQL audit tables with trigger-based append-only rules.
   - *Rejected*: Massive write amplification; high storage cost; vulnerable to database superuser modification (`TRUNCATE`, `DROP`).
2. **Option 2: Elasticsearch / OpenSearch Cluster Only**: Route all logs to Elasticsearch with read-only index aliases.
   - *Rejected*: Indices can still be deleted by cluster administrators or snapshot policies; does not constitute certified WORM storage.
3. **Option 3: Two-Layer Audit Architecture (ClickHouse + S3 Object Lock) (Chosen)**:
   - **Layer 1 (Operational Analytics)**: ClickHouse provides sub-second search, filtering, RevPAR aggregations, and administrative dashboards.
   - **Layer 2 (Tamper-Resistant Archival)**: Versioned S3-compatible object storage configured with **S3 Object Lock** provides WORM retention-protected archival storage.

## Decision Outcome
**Chosen Option**: **Option 3: Two-Layer Audit Architecture (ClickHouse Operational Analytics + S3 Object Lock WORM Archival)**.

### 1. Two-Layer Architectural Model
```
┌────────────────────────────────────────────────────────┐
│ Application Services & Edge Gateway (JSON Audit Events) │
└───────────────────────────┬────────────────────────────┘
                            │ Kafka / Async Ingestion Stream
                            ▼
┌────────────────────────────────────────────────────────┐
│ Reliable Ingestion & Archival Engine                    │
├───────────────────────────┬────────────────────────────┤
│ Ingest Stream (Real-Time) │ Batch Export & Manifest    │
▼                           ▼                            │
┌─────────────────────────┐ ┌───────────────────────────┐│
│ Layer 1: ClickHouse     │ │ Layer 2: S3 Object Lock   ││
│ Operational Analytics   │ │ WORM Archival Storage     ││
│ • Fast filtering & SQL  │ │ • Retention-protected     ││
│ • Incident forensics    │ │ • Non-erasable WORM       ││
│ • Admin dashboards      │ │ • Hash-linked manifests   ││
└─────────────────────────┘ └───────────────────────────┘│
```

### 2. S3 Object Lock Retention Policy Strategy
The platform adopts a staged retention model distinguishing development from production:

| Deployment Environment | Selected Object Lock Mode | Operational Characteristics & Justification |
| :--- | :--- | :--- |
| **Development & Controlled Staging** | **Governance Mode** | Authorized principals holding the explicit `s3:BypassGovernanceRetention` IAM permission can delete or alter objects. Enables rapid testing, integration validation, and test dataset cleanup without permanent bucket lockouts. |
| **Production Target** | **Compliance Mode** | Protected object versions **CANNOT** be overwritten, truncated, or deleted before retention expiration by **ANY** user, including AWS account root or cluster administrators. Enforced for statutory audit records. |

- **Decoupled Mode and Duration**: Object Lock mode (Governance vs. Compliance) and retention duration are independent configuration parameters.
- **Explicit Retention Classes**:
  1. *Financial & Accounting Records* (Invoices, payment references, guest folios): **7 to 10 years** (per statutory commercial tax law).
  2. *Security & Access Logs* (Authentication failures, role changes, door unlock events): **1 to 3 years**.
  3. *Operational Diagnostics* (Routine housekeeping shifts, sensor heartbeats): **90 days**.
- **Legal Holds**: Independent of duration-based retention. An administrative legal hold (`s3:PutObjectLegalHold`) can be placed on specific object batches during an active regulatory investigation, freezing deletion indefinitely until the legal hold is formally lifted.

### 3. WORM Guarantees & Evidentiary Boundaries
- **Evidentiary Clarification**: WORM storage guarantees **tamper-resistant audit evidence, integrity verification, accountability, and incident forensics**. It proves that *the stored log record has not been altered or truncated since the moment of writing*.
- **Boundaries**: WORM storage does **not** independently prove the absolute objective truth of an event, nor does it prove that the originating system clock was synchronized, nor does Object Lock alone guarantee statutory compliance or legal non-repudiation.
- The standard explicitly differentiates:
  - *Event Authenticity*: Verified by application HMAC/asymmetric event signatures.
  - *Event Integrity*: Verified by hash-linked batch manifests (SHA-256 Merkle roots).
  - *Actor Attribution*: Verified by cryptographically resolved `(iss, sub)` identities.
  - *Immutable Retention*: Guaranteed by S3 Object Lock WORM enforcement.

### 4. Operational Ingestion, Integrity & Privacy Safeguards
1. **Batching & Hash-Linked Manifests**:
   - The archival engine writes canonical JSONL audit batches to S3 (e.g., hourly batches).
   - Each batch includes a signed manifest containing the SHA-256 hash of the batch object and a pointer to the previous batch hash (hash-chaining), guaranteeing detection of missing or dropped batches.
2. **Detection of Failed Archival**:
   - Ingestion lag and S3 upload failures generate high-priority alerting probes (`SEV-2`). If archival falls behind by more than 15 minutes, operations teams are notified immediately.
3. **Log Sanitation Invariant**:
   - Security audit events **MUST NEVER** contain plaintext passwords, raw JWT tokens, session cookies, payment card numbers (PAN/CVV), or unmasked passport numbers.
   - PII is strictly limited to verified system identifiers and operational metadata.
4. **Local Development / Testing Implementation**:
   - **MinIO** is utilized in local development (`docker-compose.yaml`) as an S3-compatible object store supporting Object Lock APIs.
   - *Notice*: Local MinIO testing validates API integration and application error-handling; it does not automatically certify production statutory compliance without provider-specific configuration audits in production.

## Consequences & Operational Impact
* **Positive**:
  - Eliminates the conflict between fast operational querying (ClickHouse) and statutory immutability (S3 WORM).
  - Protects compliance audit trails against ransomware, accidental admin deletion, and malicious log truncation.
  - Governance mode provides operational safety during initial development, avoiding permanent storage deadlocks.
  - Hash-linked manifests enable mathematical verification of audit log continuity.
* **Negative / Trade-offs**:
  - Compliance mode in production is completely irreversible; misconfiguring a retention period (e.g. setting 100 years instead of 10) permanently locks cloud storage with ongoing storage billing obligations.
  - Requires maintaining the archival batching engine and monitoring batch upload health.

