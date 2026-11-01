# Authoritative 13-Microservice Catalogue

This document defines the official **13-Microservice Target Topology** for the MyBooking platform.

---

## The 13 Microservices

```
                     +---------------------------------------+
                     |        edge-api-gateway (Go)          |  [1]
                     +-------------------+-------------------+
                                         |
         +-------------------------------+-------------------------------+
         |                               |                               |
         v                               v                               v
+------------------+           +-------------------+           +-------------------+
|  CORE BUSINESS   |           | HIGH-PERFORMANCE  |           | AUTONOMOUS & EDGE |
|   (Java/Spring)  |           |     ENGINES       |           |      (Rust)       |
+------------------+           +-------------------+           +-------------------+
| identity-svc  [2]|           | pricing-eng  [9]  | (C++20)   | iot-gateway  [12] |
| inventory-svc [3]|           | avail-search [10] | (Go)      | robot-fleet  [13] |
| reservat-svc  [4]|           | analytics-svc[11] | (Go)      +-------------------+
| loyalty-svc   [5]|           +-------------------+
| housekeep-svc [6]|
| payment-svc   [7]|
| notificat-svc [8]|
+------------------+
```

---

### Detailed Service Inventory

| # | Service Name | Bounded Context | Primary Responsibility | Data Store | Technology | Rationale | Extraction Phase |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **1** | `edge-api-gateway` | Ingress & Security | TLS termination, token verification, routing, rate limiting, request correlation. | Redis 7 | **Go (Golang)** | High-throughput non-blocking networking, low CPU/RAM overhead. | Phase 7 |
| **2** | `identity-service` | Identity & Access Domain | User profiles, hotel tenant assignments, staff metadata, domain authorization attributes (IdP authentication delegated to Keycloak). | PostgreSQL 14 | **Java / Spring Boot 3** | Domain user aggregate, Spring Security 6 resource server integration. | Milestone IdP / Phase 8 |
| **3** | `inventory-service` | Inventory & Facility | Room metadata, facility installations, equipment catalog, maintenance locks. | PostgreSQL 14 | **Java / Spring Boot 3** | Complex entity relationships, transactional catalog management. | Phase 4 |
| **4** | `reservation-service` | Booking & Stay | Booking lifecycle (Draft, Confirmed, Cancelled), stay records, check-in/out. | PostgreSQL 14 | **Java / Spring Boot 3** | Core ACID transactional domain logic, row-level booking locks. | Phase 4 |
| **5** | `loyalty-service` | Loyalty & Rewards | Member point accounts, earn/redeem ledger transactions, tier progression. | PostgreSQL 14 | **Java / Spring Boot 3** | Strict transactional consistency for balance debits and credits. | Phase 6 |
| **6** | `housekeeping-service` | Operations | Room cleanliness state machine (`DIRTY`, `CLEAN`), staff tasks, inspection. | PostgreSQL 14 | **Java / Spring Boot 3** | Rich domain state transitions, front-desk event synchronization. | Phase 7 |
| **7** | `payment-service` | Billing & Payments | Payment authorization, refunds, external gateway tokenization (Stripe/Mock). | PostgreSQL 14 | **Java / Spring Boot 3** | Strict PCI-DSS boundaries, idempotency, refund accounting. | Phase 9 |
| **8** | `notification-service`| Communications | Multi-channel communication dispatch (Email, SMS, Web Push, WebSockets). | PostgreSQL 14 | **Java / Spring Boot 3** | Reliable template rendering, retry queues, external API adapters. | Phase 5 / Phase 9 |
| **9** | `pricing-engine` | Dynamic Pricing | Dynamic yield optimization, seasonal demand matrix calculations (p99 < 15ms). | In-Memory / Redis | **C++20 (gRPC)** | High-performance numerical optimization, zero GC latency. | Phase 6 |
| **10**| `availability-search` | Search & Discovery | High-concurrency room search across date ranges and amenity filters. | Redis 7 | **Go (Golang)** | Extreme concurrent read throughput via goroutines over Redis. | Phase 7 |
| **11**| `analytics-service` | Business Intelligence | Historical reporting, RevPAR, ADR, occupancy OLAP event stream ingestion. | ClickHouse | **Go (Golang)** | High-throughput Kafka stream consumer batching into ClickHouse. | Phase 10 |
| **12**| `iot-gateway-service` | Smart Room & IoT | MQTT broker integration, lock BLE command dispatch, sensor telemetry. | TimescaleDB | **Rust (gRPC/MQTT)** | Memory safety, deterministic low-level hardware I/O, zero runtime GC. | Phase 12 |
| **13**| `robot-fleet-service` | Fleet Operations | ROS 2 mission dispatch, cleaning/delivery task routing, safety geofencing. | PostgreSQL / Redis | **Rust (gRPC)** | Safe concurrency, native ROS 2 bridge, real-time safety guarantees. | Phase 13 |

---

## Explicit Technology Justifications

1. **Why Go for `analytics-service` instead of C++?**
   * ClickHouse itself performs the heavy vectorized C++ numerical aggregations internally.
   * `analytics-service` functions as a high-throughput event pipe (consuming from Kafka and batch-writing to ClickHouse) and an HTTP/gRPC query gateway for dashboards.
   * Go provides superior concurrency (goroutines), robust Kafka libraries, native ClickHouse drivers (`clickhouse-go`), and high developer productivity with minimal memory usage.
2. **Why C++20 for `pricing-engine`?**
   * Unlike analytics, dynamic room yield pricing executes linear programming algorithms, combinatorial room assignment models, and continuous pricing recalculations directly inside the service's memory space.
   * C++20 delivers deterministic execution times without JVM or Go garbage collection pauses.
3. **Why Rust for IoT and Robotics?**
   * Memory safety without garbage collection is critical for edge gateways and physical robot actuators where unexpected GC pauses can cause packet drops or latency spikes in safety-critical loops.
