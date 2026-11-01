# ADR-0017: Multi-Tenant Authorization Model & Policy Architecture (RBAC + ReBAC + ABAC)

## Status
**Accepted**

## Context & Problem Statement
In the early monolith, authorization was primarily implemented through coarse Spring Security roles (`ROLE_CLIENT`, `ROLE_EMPLOYEE`, `ROLE_ADMIN`) attached to endpoints. As MyBooking expands to manage multiple hotel properties, thousands of rooms, and distributed services, coarse role-based access control (RBAC) alone is dangerously inadequate.

A user possessing the `ROLE_EMPLOYEE` authority at Hotel Paris must never be permitted to inspect guest folios, modify room statuses, or review housekeeping tasks for Hotel London. Similarly, a guest possessing `ROLE_CLIENT` must only be permitted to access reservations that belong specifically to their own account. Relying solely on role checks creates critical Broken Object Level Authorization (BOLA / IDOR) and tenant cross-contamination risks.

How should MyBooking structure its authorization architecture across tenants, what model should govern permissions, where should decisions be enforced, and how should the platform prepare for externalized policy engines (OpenFGA, Cedar, OPA) without premature complexity?

## Decision Drivers
* **Strict Tenant Isolation**: Cross-hotel data access must be impossible without explicit multi-property administrative privileges.
* **Core Authorization Invariant**: **A valid authenticated token or an `EMPLOYEE` role alone is NEVER sufficient to access arbitrary hotel resources.**
* **Defense in Depth**: Authorization must be enforced at multiple layers (API gateway route, method security, domain service invariants, and database queries).
* **Evolutionary Approach**: Implement authorization natively in Spring Security and domain services initially (Phases 2–7), while defining clean Policy Enforcement Point (PEP) and Policy Decision Point (PDP) abstractions that allow future integration of modern policy engines (Phase 8+).
* **Testability & Auditability**: Authorization rules must be verifiable via automated positive and negative security tests. All authorization rejections must be audit-logged.
* **Traceability**: Fulfills `SEC-AUTHZ-001`, `SEC-AUTHZ-002`, and `SEC-AUTHZ-004` in `docs/security/security-engineering.md`.

## Considered Options
1. **Option 1: Coarse RBAC Only**: Continue using `@PreAuthorize("hasRole('EMPLOYEE')")` and rely on developers to manually write custom checks inside controller handlers.
2. **Option 2: Premature External Policy Engine (Immediate OpenFGA / OPA deployment)**: Introduce an external policy engine sidecar across all services during Phase 2.
3. **Option 3: Hybrid RBAC + ReBAC + ABAC with Domain-Owned PEPs & Staged PDP Evolution (Chosen)**: Establish a three-tier authorization model combining RBAC (eligibility), ReBAC (tenant and resource ownership), and ABAC (dynamic context) enforced natively via Spring Security and domain logic, with a defined migration path to externalized policy engines in Phase 8+.

## Decision Outcome
**Chosen Option**: **Option 3: Hybrid RBAC + ReBAC + ABAC with Domain-Owned PEPs & Staged PDP Evolution**.

### 1. Three-Tier Authorization Architecture

```
                       Incoming Request
                              │
                              ▼
        ┌───────────────────────────────────────────┐
        │ 1. Role-Based Access Control (RBAC)       │
        │    Is the caller functional eligible?     │
        │    e.g. @PreAuthorize("hasRole('STAFF')") │
        └─────────────────────┬─────────────────────┘
                              │ Passed
                              ▼
        ┌───────────────────────────────────────────┐
        │ 2. Relationship-Based AuthZ (ReBAC)       │
        │    Does caller own or belong to resource? │
        │    e.g. user.assignedHotels contains room.hotelId │
        │         reservation.guestId == caller.id  │
        └─────────────────────┬─────────────────────┘
                              │ Passed
                              ▼
        ┌───────────────────────────────────────────┐
        │ 3. Attribute-Based AuthZ (ABAC)           │
        │    Are dynamic contextual rules satisfied?│
        │    e.g. shift.isActive() == true          │
        │         room.status == CLEANING_REQUIRED  │
        └─────────────────────┬─────────────────────┘
                              │ Passed
                              ▼
                      Execute Domain Action
```

1. **Tier 1 — RBAC (Eligibility)**:
   - Coarse role boundaries (`ROLE_CLIENT`, `ROLE_EMPLOYEE`, `ROLE_ADMIN`).
   - Evaluated at the HTTP/controller entry point using Spring Security `@PreAuthorize`.
2. **Tier 2 — ReBAC (Relationship & Tenant Boundaries)**:
   - Models organizational and resource relationships.
   - Every physical hotel entity (`Room`, `Reservation`, `EmployeeTask`, `Installation`, `Shift`) belongs to an immutable `property_id`.
   - The user's assigned property IDs (`Set<Long> assignedPropertyIds`) are loaded into domain security context and compared against the target entity's `property_id`.
   - Guest ownership verifies `resource.clientId == principal.id`.
3. **Tier 3 — ABAC (Contextual & Business Rules)**:
   - Evaluates dynamic environmental attributes: employee active shift status, current time window, room occupancy status, geofencing coordinates.
   - For example: an employee may only update housekeeping status on a room if their scheduled shift is currently active.

### 2. Policy Enforcement Points (PEP) & Decision Points (PDP)
- **Policy Enforcement Point (PEP)**: Located directly within Spring Security method interceptors and domain application services. The PEP intercepts incoming invocations, collects contextual attributes (`UserPrincipal`, target entity, environmental state), queries the PDP, and throws `AccessDeniedException` if rejected.
- **Policy Decision Point (PDP)**:
  - *Interim (Phases 2–7)*: Implemented as strongly typed Spring components (`HotelSecurityEvaluator`, `ReservationSecurityService`) invoked via SpEL or explicit domain calls.
  - *Target (Phase 8+)*: Evaluated externalized PDP engines:
    - **OpenFGA (Google Zanzibar model)**: For ReBAC relationships (`relation: housekeeper of hotel_123`).
    - **Cedar / OPA**: For high-performance declarative ABAC policy evaluation via local sidecar cache.

### 3. Cross-Service Authorization Boundaries
- When service-to-service calls occur (e.g., `reservation-service` calling `payment-service`):
  - Service credentials alone (mTLS / Client Credentials) only grant network and transport access.
  - Business operations must carry the originating user's cryptographically signed context (or down-scoped OAuth2 Token Exchange token, RFC 8693) to verify that the originating user has authority over the requested target resource.

### 4. Testing & Verification Requirements
- **Mandatory Negative Testing**: Every controller and service method must include automated negative tests verifying:
  - Caller with valid token but wrong role is rejected with `403 Forbidden`.
  - Caller with valid `EMPLOYEE` role from Hotel A attempting to read/modify Hotel B resource is rejected with `403 Forbidden`.
  - Caller with valid `CLIENT` role attempting to view another client's reservation is rejected with `403 Forbidden` or `404 Not Found` (to prevent ID harvesting).
- **Tenant Isolation Tests**: Integration tests must execute automated cross-tenant data assertions verifying that repository queries never leak rows across property boundaries.

## Consequences & Operational Impact
* **Positive**:
  - Eliminates single points of failure where a valid token grants excessive lateral access.
  - Guarantees strict multi-property tenant isolation across hotel portfolios.
  - Keeps business authorization close to domain models without premature external toolchain overhead.
  - Clear architectural path for integrating OpenFGA or OPA when microservices scale.
* **Negative / Trade-offs**:
  - Requires developers to explicitly inject tenant context and execute ReBAC/ABAC checks on all mutating operations.
  - Entity models must consistently include and index `property_id`.

