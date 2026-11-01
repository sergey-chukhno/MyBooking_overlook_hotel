# Architecture Decision Records (ADR) Register

This register documents all Architecture Decision Records (ADRs) for the MyBooking platform. We do **not** write ADRs on the fly as an afterthought; foundational architectural decisions are formally indexed, evaluated, and accepted prior to code execution.

## ADR Status Legend
* **Proposed**: Under review by the engineering team / architect.
* **Accepted**: Formally approved; guides subsequent ticket implementation.
* **Superseded**: Replaced by a newer architectural decision.
* **Rejected**: Evaluated and discarded with recorded rationale.

---

## Authoritative ADR Index

| ADR ID | Title | Scope / Context | Status | Target Phase |
| :--- | :--- | :--- | :--- | :--- |
| **ADR-0001** | [Modular Monolith Foundation to Staged Evolutionary Microservices](ADR-0001-modular-monolith-to-evolutionary-microservices.md) | Architecture Style | **Accepted** | Phase 01 / Phase 04 |
| **ADR-0002** | [Single Authoritative Backend Root (`backend/src`) & Test Harness Standards](ADR-0002-backend-root-consolidation-and-test-harness.md) | Build & Project Structure | **Accepted** | Phase 01 |
| **ADR-0003** | [Zero-Trust Principal Identity Resolution & Deprecation of Client-Supplied Identity](ADR-0003-zero-trust-principal-identity-resolution.md) | Security & Auth | **Accepted** | Phase 02 |
| **ADR-0004** | [Client Tier Coexistence & Next.js BFF Session Encryption Key Management](ADR-0004-client-tier-coexistence-and-bff-session-encryption.md) | Frontend & Client Tier | **Accepted** | Phase 01 / Phase 14 / Milestone IdP |
| **ADR-0005** | Database-per-Service & Polyglot Persistence Strategy | Data Architecture | **Accepted** | Phase 04 / Phase 10 |
| **ADR-0006** | Transactional Outbox Pattern & Kafka Event Streaming for Async Side-Effects | Distributed Systems | **Proposed** | Phase 05 / Phase 09 |
| **ADR-0007** | C++20 gRPC Dynamic Yield Pricing Engine for Numerical Matrix Optimization | Performance / Pricing | **Proposed** | Phase 06 |
| **ADR-0008** | Go (Golang) for High-Concurrency Availability Search & Edge API Gateway | Ingress / Search Engine | **Proposed** | Phase 07 |
| **ADR-0009** | Saga Choreography & Compensating Transactions for Cross-Service Bookings | Distributed Transactions | **Proposed** | Phase 09 |
| **ADR-0010** | Sandboxed AI Agent Gateway with Deterministic Policy Guard & Human Override | AI Architecture | **Proposed** | Phase 11 |
| **ADR-0011** | Rust for IoT Edge Gateway, Smart Room Telemetry & MQTT Protocol Bridge | IoT & Hardware | **Proposed** | Phase 12 |
| **ADR-0012** | ROS 2 Robot & Drone Fleet Task Dispatch with Deterministic Safety Geofence | Robotics & Autonomous | **Proposed** | Phase 13 |
| **ADR-0013** | Kubernetes Topology, Helm Packaging & Zero-Trust DevSecOps CI/CD Pipelines | Infrastructure & DevOps | **Proposed** | Phase 15 |
| **ADR-0014** | [Technology Selection of Go for Analytics Service](ADR-0014-go-for-analytics-service.md) | Business Intelligence / OLAP | **Accepted** | Phase 10 |
| **ADR-0015** | [Distributed Systems Resilience Patterns (Circuit Breakers, Retries, Bulkheads, Backpressure)](ADR-0015-distributed-resilience-patterns.md) | Fault Tolerance & Resilience | **Accepted** | Phase 06 / Phase 09 |
| **ADR-0016** | [Keycloak as Centralized Identity Provider & OAuth2/OIDC Authority](ADR-0016-keycloak-as-identity-provider.md) | Identity & Access (IAM) | **Accepted** | Milestone IdP |
| **ADR-0017** | [Multi-Tenant Authorization Model & Policy Architecture (RBAC + ReBAC + ABAC)](ADR-0017-authorization-model-and-multitenant-permission-architecture.md) | Authorization & Policy | **Accepted** | Phase 02 / Phase 04 / Phase 08 |
| **ADR-0019** | [Gateway Internal Delegation Token Architecture (Downstream Service Identity Propagation)](ADR-0019-gateway-internal-delegation-token-architecture.md) | Ingress & Delegation | **Accepted** | Phase 07 |
| **ADR-0020** | [Two-Layer Audit Architecture (ClickHouse Operational Analytics & S3 Object Lock WORM Archival)](ADR-0020-s3-object-lock-and-audit-retention.md) | Audit & Compliance | **Accepted** | Phase 10 |

---

## ADR Template Standard
Every ADR must adhere to the standard format:
1. **Context & Problem Statement**: The engineering challenge and drivers.
2. **Decision Drivers**: Factors considered (performance, security, maintainability).
3. **Considered Options**: The technical alternatives evaluated.
4. **Decision Outcome**: The selected option and justification.
5. **Pros and Cons of the Options**: Explicit trade-offs analyzed.
6. **Consequences & Operational Impact**: How this decision impacts development, deployment, and testing.
