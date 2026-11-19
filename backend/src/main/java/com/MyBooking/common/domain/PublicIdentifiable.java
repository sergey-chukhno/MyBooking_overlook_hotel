package com.MyBooking.common.domain;

import java.util.UUID;

/**
 * Contract implemented by all 19 Tier A external-facing JPA entities carrying a
 * Dual-Key RFC 9562 UUIDv7 {@code public_id} alongside their internal {@code BIGINT}
 * primary key (IDP-T05 / ADR-0021).
 */
public interface PublicIdentifiable {

    UUID getPublicId();

    void setPublicId(UUID publicId);
}
