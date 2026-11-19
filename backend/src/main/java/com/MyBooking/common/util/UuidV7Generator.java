package com.MyBooking.common.util;

import com.github.f4b6a3.uuid.UuidCreator;

import java.util.UUID;

/**
 * Approved RFC 9562 Section 5.7 Time-Ordered UUIDv7 Generator (IDP-T04 / ADR-0021).
 *
 * <p>Wraps {@link UuidCreator#getTimeOrderedEpoch()} from {@code com.github.f4b6a3:uuid-creator:6.0.0}
 * emitting a 128-bit UUID with a 48-bit Unix epoch millisecond prefix, 4-bit version {@code 7},
 * 2-bit IETF variant {@code 2}, and 74 random/monotonic bits seeded via {@code SecureRandom}.
 *
 * <p><b>Architectural Invariants (ADR-0021 / T-07):</b>
 * <ul>
 *   <li><b>Uniqueness Authority:</b> Strict monotonicity is not guaranteed across JVM restarts,
 *       clock rollbacks, or multiple JVM instances. PostgreSQL {@code UNIQUE} constraints
 *       ({@code uq_<table>_public_id}) are the final authoritative uniqueness guarantee.</li>
 *   <li><b>Timestamp Semantics:</b> The embedded 48-bit timestamp reflects UUID generation instant
 *       only; entity {@code created_at} remains the sole authoritative business creation timestamp.</li>
 *   <li><b>Authorization Pairing:</b> UUIDv7 eliminates sequential integer enumeration across the
 *       19 Tier A tables, but possession of a {@code public_id} never replaces authentication,
 *       RBAC, ownership checks, or tenant isolation.</li>
 * </ul>
 */
public final class UuidV7Generator {

    private UuidV7Generator() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Generates a new RFC 9562 Section 5.7 time-ordered UUIDv7.
     *
     * @return non-null version-7, variant-2 {@link UUID}
     */
    public static UUID next() {
        return UuidCreator.getTimeOrderedEpoch();
    }
}
