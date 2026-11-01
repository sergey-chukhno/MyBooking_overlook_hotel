# Reconciled 15-Phase Engineering Roadmap (Trello Backlog Authority)

This document serves as the authoritative architectural baseline and Trello backlog specification for the 15 engineering phases of the MyBooking Platform.

---

## The 15 Phases

```text
PHASE 01: Repository Consolidation & Test Harness Repair
PHASE 02: Zero-Trust Security & Authentication Hardening
PHASE 03: Domain Refactoring & Reservation Core Fixes
MILESTONE IdP: Keycloak Identity Provider Adoption & Next.js BFF Architecture
PHASE 04: Modular Monolith Bounded Contexts & PostgreSQL Schema Isolation
PHASE 05: Transactional Outbox & In-Memory Domain Events
PHASE 06: C++20 Dynamic Pricing & Yield Engine Integration (gRPC)
PHASE 07: Go Availability Search & Edge API Gateway (Go + Redis)
PHASE 08: Identity Domain Service Extraction & SPIFFE/SPIRE Workload Identity (Java)
PHASE 09: Distributed Event Streaming & Saga Workflows (Kafka, Payment & Notification Svcs)
PHASE 10: Database-per-Service Migration & Analytics Service in Go (ClickHouse + Kafka)
PHASE 11: AI Agent Platform & Deterministic Policy Guard Layer
PHASE 12: Rust IoT Edge Gateway & Smart Room Telemetry (Rust + TimescaleDB)
PHASE 13: ROS 2 Autonomous Robot & Drone Fleet Management (Rust + ROS 2)
PHASE 14: Rust + Tauri Desktop Workstation Application
PHASE 15: Kubernetes DevSecOps Deployment & Production Hardening
```

---

## Detailed Phase Backlog

### PHASE 01: Repository Consolidation & Test Harness Repair
* **Objective**: Remove obsolete root `/src` directory, configure test profiles to resolve Spring Boot 3.5 `BeanDefinitionOverrideException`, and establish 100% test suite pass rate.
* **Scope**: Clean workspace structure, update test YAML profiles, align Maven wrapper.
* **Deliverables**: Consolidated repository root, passing `./mvnw test` suite, ADR-0001, ADR-0002.
* **Dependencies**: None.

### PHASE 02: Zero-Trust Security & Authentication Hardening
* **Objective**: Eliminate the critical `X-User-Id` IDOR vulnerability, enforce cryptographically verified `UserPrincipal` injection, and strengthen JWT secrets.
* **Scope**: Refactor `EmployeeController` and other endpoints to use `@AuthenticationPrincipal`, configure RS256/HMAC-256 keys, remove manual token substring parsing.
* **Deliverables**: Secure endpoints, zero IDOR vulnerabilities, ADR-0003.
* **Dependencies**: Phase 01.

### PHASE 03: Domain Refactoring & Reservation Core Fixes
* **Objective**: Correct the date overlap JPQL calculation, fix self-conflict on reservation updates, and decouple static room status mutations from dynamic bookings.
* **Scope**: `ReservationRepository`, `ReservationService`, date validation invariants.
* **Deliverables**: Interval overlap tests passing, back-to-back booking support.
* **Dependencies**: Phase 02.

### MILESTONE IdP: Keycloak Identity Provider Adoption & Next.js BFF Architecture
* **Objective**: Adopt Keycloak as centralized OAuth2/OIDC authority, eliminate browser token storage via Next.js BFF, migrate user credentials with forced password reset, and enable asymmetric JWKS token verification.
* **Scope**: Docker Compose Keycloak deployment, realm-as-code configuration (`mybooking-platform`), user account migration preserving `app_user.id`, Next.js BFF server routes with `HttpOnly` session cookies, Spring Security Resource Server JWKS integration, positive/negative integration tests.
* **Deliverables**: Self-hosted Keycloak service, asymmetric RS256/ES256 verification, zero `localStorage` tokens, ADR-0016.
* **Dependencies**: Phase 03.

### PHASE 04: Modular Monolith Bounded Contexts & PostgreSQL Schema Isolation
* **Objective**: Separate the monolithic PostgreSQL schema into logical schemas (`auth`, `inventory`, `booking`, `loyalty`, `operations`) via Flyway migrations, reflecting the new externalized credential model.
* **Scope**: Flyway DDL scripts (`V9__*`), package-private encapsulation, eliminate cross-domain SQL joins, update Merise documentation (MCD, MLD, MPD in `modelisation/`).
* **Deliverables**: Isolated schemas, domain aggregate models, ADR-0005, ADR-0017.
* **Dependencies**: Milestone IdP.

### PHASE 05: Transactional Outbox & In-Memory Domain Events
* **Objective**: Decouple state mutations from side-effects using the Transactional Outbox pattern and Spring Application Events.
* **Scope**: `outbox` table, Outbox Relay worker, `notification-service` initial module.
* **Deliverables**: Outbox event infrastructure, domain event catalog, ADR-0006.
* **Dependencies**: Phase 04.

### PHASE 06: C++20 Dynamic Pricing & Yield Engine Integration
* **Objective**: Implement the high-performance C++20 matrix solver and linear programming yield engine, integrating with Spring Boot via gRPC.
* **Scope**: C++20 project, Protobuf contracts, Spring gRPC client, Resilience4j circuit breaker.
* **Deliverables**: Microservice #9 (`pricing-engine`), p99 < 15ms benchmark, ADR-0007.
* **Dependencies**: Phase 05.

### PHASE 07: Go Availability Search & Edge API Gateway
* **Objective**: Implement high-concurrency room search in Go backed by Redis bitset indexes and deploy the Go Edge API Gateway.
* **Scope**: Microservice #1 (`edge-api-gateway`), Microservice #10 (`availability-search`), Redis cache syncer.
* **Deliverables**: Gateway rate-limiter, 5,000 req/sec search throughput, ADR-0008.
* **Dependencies**: Phase 06.

### PHASE 08: Identity Domain Service Extraction & SPIFFE/SPIRE Workload Identity
* **Objective**: Extract `identity-service` as an autonomous domain service managing user profiles, hotel assignments, and authorization attributes, deploying SPIFFE/SPIRE service identities for inter-service mTLS.
* **Scope**: Microservice #2 (`identity-service`), profile management, tenant authorization attribute resolver, SPIFFE/SPIRE mTLS configuration.
* **Deliverables**: Autonomous identity domain service, SPIFFE workload attestation, inter-service mTLS contracts.
* **Dependencies**: Phase 07.

### PHASE 09: Distributed Event Streaming & Saga Workflows
* **Objective**: Deploy Apache Kafka cluster and orchestrate distributed transactions using Saga choreography across reservations, payments, and loyalty.
* **Scope**: Microservice #7 (`payment-service`), Microservice #8 (`notification-service`), Kafka topic topology, DLQ retry handler.
* **Deliverables**: Zero double-booking Saga workflow, compensating transactions, ADR-0009, ADR-0015.
* **Dependencies**: Phase 08.

### PHASE 10: Database-per-Service Migration & Analytics Service in Go
* **Objective**: Isolate database instances completely per service and deploy `analytics-service` in Go streaming Kafka events into ClickHouse.
* **Scope**: Microservice #11 (`analytics-service`), ClickHouse OLAP schema, database network segmentation.
* **Deliverables**: Sub-second RevPAR/ADR aggregations across 10M rows, ADR-0014.
* **Dependencies**: Phase 09.

### PHASE 11: AI Agent Platform & Deterministic Policy Guard Layer
* **Objective**: Build sandboxed AI Agent Gateway with prompt-injection defense and RBAC authorization tool layers.
* **Scope**: AI Model Gateway, RAG vector store (pgvector/Redis), deterministic policy checker, human-in-the-loop approval.
* **Deliverables**: AI guest concierge, AI operational copilot, ADR-0010.
* **Dependencies**: Phase 10.

### PHASE 12: Rust IoT Edge Gateway & Smart Room Telemetry
* **Objective**: Deploy Rust IoT Edge Gateway handling MQTT telemetry and smart door lock BLE command dispatch into TimescaleDB.
* **Scope**: Microservice #12 (`iot-gateway-service`), TimescaleDB time-series schema, device registration.
* **Deliverables**: 20,000 events/sec telemetry ingestion, ADR-0011.
* **Dependencies**: Phase 11.

### PHASE 13: ROS 2 Autonomous Robot & Drone Fleet Management
* **Objective**: Implement autonomous robot cleaning and inspection mission dispatch in Rust interfacing with ROS 2 Nav2.
* **Scope**: Microservice #13 (`robot-fleet-service`), ROS 2 bridge, human proximity safety overrides.
* **Deliverables**: Automated housekeeping mission dispatcher, safety kill-switch, ADR-0012.
* **Dependencies**: Phase 12.

### PHASE 14: Rust + Tauri Desktop Workstation Application
* **Objective**: Build native desktop workstation application for hotel front desk and housekeeping administration with native keycard hardware support.
* **Scope**: Tauri desktop shell (macOS/Windows/Linux), encrypted SQLite offline cache, hardware keycard encoder plugin.
* **Deliverables**: Standalone workstation installer, ADR-0004.
* **Dependencies**: Phase 13.

### PHASE 15: Kubernetes DevSecOps Deployment & Production Hardening
* **Objective**: Deploy full 13-microservice topology to Kubernetes using Helm charts, SPIFFE/SPIRE mTLS, and end-to-end DevSecOps pipelines.
* **Scope**: Helm charts, Kyverno policies, OpenTelemetry Prometheus/Grafana dashboards, k6 distributed load testing.
* **Deliverables**: Production Kubernetes cluster deployment, automated CI/CD pipeline, ADR-0013.
* **Dependencies**: Phase 14.
