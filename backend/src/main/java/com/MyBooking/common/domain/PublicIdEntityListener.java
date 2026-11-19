package com.MyBooking.common.domain;

import com.MyBooking.common.util.UuidV7Generator;
import jakarta.persistence.PrePersist;

/**
 * JPA entity listener that automatically assigns an RFC 9562 UUIDv7 {@code publicId}
 * via {@link UuidV7Generator#next()} on {@link PrePersist} whenever {@code publicId}
 * has not already been pre-populated (IDP-T05 / ADR-0021).
 */
public class PublicIdEntityListener {

    @PrePersist
    public void assignPublicIdIfAbsent(Object entity) {
        if (entity instanceof PublicIdentifiable identifiable && identifiable.getPublicId() == null) {
            identifiable.setPublicId(UuidV7Generator.next());
        }
    }
}
