package com.MyBooking.common.util;

import com.MyBooking.common.domain.PublicIdEntityListener;
import com.MyBooking.room.domain.Room;
import com.MyBooking.room.domain.RoomStatus;
import com.MyBooking.room.domain.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UuidV7Generator} and {@link PublicIdEntityListener} (IDP-T04, IDP-T05).
 */
class UuidV7GeneratorTest {

    @Test
    @DisplayName("IDP-T04: UuidV7Generator emits RFC 9562 version 7 and IETF variant 2 UUIDs with valid epoch ms prefix")
    void next_ShouldEmitRfc9562Version7Variant2Uuid() {
        long beforeMs = System.currentTimeMillis();
        UUID uuid = UuidV7Generator.next();
        long afterMs = System.currentTimeMillis();

        assertThat(uuid).isNotNull();
        assertThat(uuid.version()).isEqualTo(7);
        assertThat(uuid.variant()).isEqualTo(2);

        long extractedEpochMs = uuid.getMostSignificantBits() >>> 16;
        assertThat(extractedEpochMs).isBetween(beforeMs - 5L, afterMs + 5L);
    }

    @Test
    @DisplayName("IDP-T04: UuidV7Generator achieves 100% uniqueness across concurrent multi-threaded generation")
    void next_ShouldGenerateUniqueUuidsConcurrently() throws InterruptedException {
        int threads = 8;
        int perThread = 2_000;
        int total = threads * perThread;
        Set<UUID> generated = ConcurrentHashMap.newKeySet(total);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    startLatch.await();
                    for (int i = 0; i < perThread; i++) {
                        UUID id = UuidV7Generator.next();
                        assertThat(id.version()).isEqualTo(7);
                        assertThat(id.variant()).isEqualTo(2);
                        generated.add(id);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertThat(doneLatch.await(15, TimeUnit.SECONDS)).isTrue();
        pool.shutdownNow();

        assertThat(generated).hasSize(total);
    }

    @Test
    @DisplayName("IDP-T05: PublicIdEntityListener assigns UUIDv7 when null and preserves pre-populated publicId")
    void publicIdEntityListener_ShouldAssignWhenNullAndPreserveExisting() {
        PublicIdEntityListener listener = new PublicIdEntityListener();
        Room room = new Room("101", RoomType.SINGLE, 1, new BigDecimal("120.00"), "EUR", RoomStatus.AVAILABLE);

        assertThat(room.getPublicId()).isNull();
        listener.assignPublicIdIfAbsent(room);

        UUID assigned = room.getPublicId();
        assertThat(assigned).isNotNull();
        assertThat(assigned.version()).isEqualTo(7);
        assertThat(assigned.variant()).isEqualTo(2);

        // Calling listener again when publicId is already set must preserve the existing UUID
        UUID customUuid = UuidV7Generator.next();
        room.setPublicId(customUuid);
        listener.assignPublicIdIfAbsent(room);
        assertThat(room.getPublicId()).isEqualTo(customUuid);
    }
}
