# MyBooking Platform Architecture Documentation

Welcome to the technical documentation repository for the **MyBooking Platform** (*Overlook Platform*).

## Documentation Structure

```text
docs/
├── README.md                                  # Documentation overview and guidelines
├── architecture/                              # High-level architecture specifications
│   ├── high-level-architecture-baseline-to-target.md # High-level architecture: v1 monolith to target enterprise platform
│   ├── system-context.md                      # C4 System Context & Container diagrams
│   ├── bounded-contexts.md                    # DDD context maps and domain boundaries
│   ├── service-catalog.md                     # The Authoritative 13-Microservice Catalogue
│   ├── security-concurrency-performance.md    # Zero-Trust, Concurrency Locks & Performance Budgets
│   ├── data-ownership.md                      # Database-per-service and polyglot persistence strategy
│   ├── event-architecture.md                  # Domain events, Kafka schema ownership & delivery rules
│   └── distributed-transactions.md            # Saga patterns, Outbox pattern, and idempotency
├── decisions/                                 # Architecture Decision Records (ADRs)
│   └── README.md                              # The Authoritative ADR Register Index
├── security/                                  # Threat models, zero-trust policies, and OWASP audits
├── ai/                                        # AI Agent Gateway, RAG, and policy guard layer
├── iot/                                       # Smart Room IoT edge gateway and telemetry specs
├── devops/                                    # CI/CD, DevSecOps pipelines, and K8s deployment manifests
└── roadmap/                                   # 15-Phase Trello backlog and roadmap references
    └── 15-phase-roadmap.md                    # Reconciled engineering roadmap
```

## Source of Truth Principles

1. **Architecture Specifications**: `docs/architecture/` defines target design intent.
2. **ADRs**: `docs/decisions/` records binding architectural choices and trade-offs.
3. **Roadmap**: `docs/roadmap/15-phase-roadmap.md` serves as the Trello backlog authority.
4. **Code & Tests**: The implementation in `backend/`, `frontend/`, and target microservices must strictly match these specifications.
