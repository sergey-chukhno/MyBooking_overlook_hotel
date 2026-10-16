# Data Modeling & Merise Architecture Register

## 1. Context & Purpose
This directory contains the foundational modeling artifacts for the **MyBooking Platform** (*Overlook Hotel*). It originated as the initial conceptual, logical, and physical modeling suite for the v1 monolithic prototype using the **Merise** information systems methodology and **UML** class diagrams.

---

## 2. Baseline Status: v1.0 Monolith Historical Baseline
The files currently in this directory represent the **v1.0 Monolithic Prototype**:
- `merise/MCD.mmd` & `MCD.svg`: Monolithic Conceptual Data Model (MCD - Modèle Conceptuel de Données).
- `merise/MPD.sql` & `MPD.svg`: Monolithic Physical Data Model (MPD - Modèle Physique de Données) targeting a single PostgreSQL schema.
- `uml/classes.mmd` & `UML.svg`: Java class hierarchy of the initial Spring Boot monolith.
- `functionalities/functional_requirements.md`: Initial functional scope and project kickoff specifications.
- `consistency/consistency_matrix.md`: Traceability matrix between initial features, APIs, and database tables.

> [!NOTE]
> This v1 baseline is preserved intact as a historical reference of our point of departure. As we modernize the platform, we avoid ad-hoc or destructive rewrites of these historical records.

---

## 3. Incremental Merise Modernization Strategy

As established in our architectural roadmap and governance rules, the data architecture transitions from a **single monolithic database** to **modular bounded context schemas** and ultimately to **Database-per-Service polyglot persistence**.

The Merise documentation is updated **incrementally at key milestones**:

```
v1 Monolithic Baseline (Current)
   │
   ▼
Phase 04: Modular Monolith Schema Isolation (Flyway V9__*)
   │   • Create per-context MCD & MPD for:
   │     - auth (Identity & RBAC)
   │     - inventory (Rooms, Facilities, Installations)
   │     - booking (Reservations, Folios, Guests)
   │     - loyalty (Reward Accounts & Ledgers)
   │     - operations (Housekeeping, Shifts, Staff)
   │
   ▼
Phases 06–13: Polyglot & Microservice Extractions
       • Service-specific data models:
         - C++ Pricing Matrix & Rule Tables (Phase 06)
         - Go Availability Redis Key Schema & Read Model (Phase 07)
         - Go Analytics ClickHouse Columnar Schema (Phase 10)
         - Rust IoT TimescaleDB Time-Series Telemetry (Phase 12)
         - ROS 2 Fleet Mission & Geofence Data Models (Phase 13)
```

---

## 4. Binding Governance & Update Obligation

> [!IMPORTANT]
> **MANDATORY GOVERNANCE RULE (from RULES.md)**:
> Developers and AI assistants must **never forget** to update the Merise and data model documentation whenever database schemas, entities, or bounded contexts are altered.
> 
> Specifically:
> 1. In **Phase 04**, when logical database schemas are separated via Flyway, updated Merise MCD and MPD diagrams must be created for each bounded context.
> 2. In **Phases 06–13**, as microservices are extracted, their dedicated data models (relational, time-series, columnar, in-memory) must be committed to documentation alongside code.
> 3. No pull request modifying database entities or Flyway migrations shall be merged without corresponding data model documentation updates.
