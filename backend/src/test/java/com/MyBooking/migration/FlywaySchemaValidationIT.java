package com.MyBooking.migration;

import com.MyBooking.auth.domain.Role;
import com.MyBooking.auth.domain.User;
import com.MyBooking.auth.repository.UserRepository;
import com.MyBooking.employee.domain.Department;
import com.MyBooking.employee.domain.Employee;
import com.MyBooking.employee.domain.EmployeeStatus;
import com.MyBooking.employee.domain.LeaveRequest;
import com.MyBooking.employee.domain.LeaveRequestStatus;
import com.MyBooking.employee.domain.Shift;
import com.MyBooking.employee.domain.Training;
import com.MyBooking.employee.repository.EmployeeRepository;
import com.MyBooking.employee.repository.LeaveRequestRepository;
import com.MyBooking.employee.repository.ShiftRepository;
import com.MyBooking.employee.repository.TrainingRepository;
import com.MyBooking.hotel_management.HotelManagementApplication;
import com.MyBooking.installation.domain.Installation;
import com.MyBooking.installation.repository.InstallationRepository;
import com.MyBooking.reservation.domain.Reservation;
import com.MyBooking.reservation.domain.ReservationStatus;
import com.MyBooking.reservation.repository.ReservationRepository;
import com.MyBooking.room.domain.Room;
import com.MyBooking.room.domain.RoomStatus;
import com.MyBooking.room.domain.RoomType;
import com.MyBooking.room.repository.RoomRepository;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.MigrationState;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * CICD-006 + IDP-T03 + IDP-T04 + IDP-T05:
 * Fail-closed PostgreSQL 16.6-alpine Flyway V1..V12 migration, Hibernate ddl-auto=validate
 * across all 23 JPA entities (19 Tier A + 4 Tier B), external IdP pair constraints (V10),
 * and RFC 9562 UUIDv7 backfill + Dual-Key repository verification.
 */
@SpringBootTest(classes = HotelManagementApplication.class)
@ActiveProfiles("test")
@Testcontainers
class FlywaySchemaValidationIT {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.6-alpine")
            .withDatabaseName("mybooking_flyway_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configurePostgresAndFlywayValidate(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    private static final List<String> TIER_A_TABLES = List.of(
            "app_user",
            "employee",
            "room",
            "room_photo",
            "equipment",
            "installation",
            "reservation",
            "feedback",
            "feedback_reply",
            "event",
            "event_booking",
            "loyalty_account",
            "loyalty_transaction",
            "announcement",
            "announcement_reply",
            "employee_task",
            "leave_request",
            "shift",
            "training"
    );

    private static final List<String> TIER_B_TABLES = List.of(
            "notification_preference",
            "room_status_update",
            "employee_training",
            "event_notification"
    );

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private InstallationRepository installationRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LeaveRequestRepository leaveRequestRepository;

    @Autowired
    private ShiftRepository shiftRepository;

    @Autowired
    private TrainingRepository trainingRepository;

    @Test
    @DisplayName("IDP-T03..T05 / CICD-006: Flyway V1..V12 apply cleanly and Hibernate ddl-auto=validate passes for all 23 entities")
    void flywayV1ThroughV12_AndHibernateDdlAutoValidate_ShouldPassOnPostgres16() {
        MigrationInfo[] applied = flyway.info().applied();
        assertThat(applied).hasSizeGreaterThanOrEqualTo(12);
        assertThat(Arrays.stream(applied).map(MigrationInfo::getState))
                .allMatch(state -> state == MigrationState.SUCCESS);

        // Verify all 19 Tier A tables have non-null public_id UUID column and uq_<tbl>_public_id unique index
        for (String table : TIER_A_TABLES) {
            Integer colCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = 'public' AND table_name = ? AND column_name = 'public_id' " +
                    "AND data_type = 'uuid' AND is_nullable = 'NO'",
                    Integer.class, table);
            assertThat(colCount).as("Tier A table %s must have NOT NULL UUID public_id", table).isEqualTo(1);

            Integer nullCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM public." + table + " WHERE public_id IS NULL",
                    Integer.class);
            assertThat(nullCount).as("Tier A table %s must have 0 NULL public_id rows", table).isZero();

            Integer idxCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public' AND tablename = ? AND indexname = ?",
                    Integer.class, table, "uq_" + table + "_public_id");
            assertThat(idxCount).as("Tier A table %s must have unique index uq_%s_public_id", table, table).isEqualTo(1);
        }

        // Verify all 4 Tier B tables do NOT have a surrogate public_id column
        for (String table : TIER_B_TABLES) {
            Integer colCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.columns " +
                    "WHERE table_schema = 'public' AND table_name = ? AND column_name = 'public_id'",
                    Integer.class, table);
            assertThat(colCount).as("Tier B table %s must NOT have surrogate public_id", table).isZero();
        }

        // Verify V6/V7/V8 seed rows were backfilled by V12 pg_temp.gen_uuid_v7 with valid RFC 9562 version 7, variant 2 UUIDs
        User seededAdmin = userRepository.findByEmail("admin@example.com").orElseThrow();
        assertThat(seededAdmin.getPublicId()).isNotNull();
        assertThat(seededAdmin.getPublicId().version()).isEqualTo(7);
        assertThat(seededAdmin.getPublicId().variant()).isEqualTo(2);

        User seededEmployeeUser = userRepository.findByEmail("employee@example.com").orElseThrow();
        Employee seededEmployee = employeeRepository.findByUserId(seededEmployeeUser.getId()).orElseThrow();
        assertThat(seededEmployee.getPublicId()).isNotNull();
        assertThat(seededEmployee.getPublicId().version()).isEqualTo(7);
        assertThat(seededEmployee.getPublicId().variant()).isEqualTo(2);
        // Employee publicId must be distinct from User publicId (Phase 4 work schema decoupling)
        assertThat(seededEmployee.getPublicId()).isNotEqualTo(seededEmployeeUser.getPublicId());

        List<Installation> installations = installationRepository.findAll();
        assertThat(installations).hasSizeGreaterThanOrEqualTo(6);
        for (Installation inst : installations) {
            assertThat(inst.getPublicId()).isNotNull();
            assertThat(inst.getPublicId().version()).isEqualTo(7);
            assertThat(inst.getPublicId().variant()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("IDP-T03 (V10): app_user enforces nullable password, chk_app_user_external_identity_pair, and uq_app_user_external_identity")
    void v10ExternalIdentityConstraints_ShouldEnforcePairSymmetryAndUniqueness() {
        String issuer = "http://127.0.0.1:8180/realms/mybooking";
        String subject = "00000000-0000-4000-a000-000000000099";

        // 1. Valid JIT-style user with null password and paired (external_issuer, external_subject)
        User jitClient = new User(
                "Jit", "Client", "jit.client@mybooking.local", null,
                "+33612345678", "10 Rue de Rivoli, Paris", LocalDate.of(1992, 5, 15), Role.CLIENT
        );
        jitClient.setExternalIssuer(issuer);
        jitClient.setExternalSubject(subject);
        jitClient.setEmailVerified(true);
        jitClient.setEnabled(true);

        User saved = userRepository.saveAndFlush(jitClient);
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getPassword()).isNull();
        assertThat(saved.getPublicId()).isNotNull();
        assertThat(saved.getPublicId().version()).isEqualTo(7);

        // Verify lookup by (externalIssuer, externalSubject) and by publicId
        assertThat(userRepository.findByExternalIssuerAndExternalSubject(issuer, subject))
                .isPresent()
                .get()
                .extracting(User::getPublicId)
                .isEqualTo(saved.getPublicId());
        assertThat(userRepository.findByPublicId(saved.getPublicId())).isPresent();

        // 2. Duplicate (external_issuer, external_subject) MUST fail uq_app_user_external_identity
        User duplicateIdpUser = new User(
                "Dup", "User", "dup.client@mybooking.local", null,
                "+33612345679", "12 Rue de Rivoli, Paris", LocalDate.of(1993, 6, 20), Role.CLIENT
        );
        duplicateIdpUser.setExternalIssuer(issuer);
        duplicateIdpUser.setExternalSubject(subject);

        assertThatThrownBy(() -> userRepository.saveAndFlush(duplicateIdpUser))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 3. Asymmetric pair (external_issuer set, external_subject NULL) MUST fail chk_app_user_external_identity_pair
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO app_user (public_id, first_name, last_name, email, phone, address, birth_date, role, external_issuer, external_subject) " +
                "VALUES (gen_random_uuid(), 'Half', 'Linked', 'half1@mybooking.local', '+33600000001', 'Addr', '1990-01-01', 'CLIENT', ?, NULL)",
                issuer
        )).isInstanceOf(DataIntegrityViolationException.class);

        // 4. Blank whitespace external_subject MUST fail chk_app_user_external_identity_pair
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO app_user (public_id, first_name, last_name, email, phone, address, birth_date, role, external_issuer, external_subject) " +
                "VALUES (gen_random_uuid(), 'Blank', 'Sub', 'half2@mybooking.local', '+33600000002', 'Addr', '1990-01-01', 'CLIENT', ?, '   ')",
                issuer
        )).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("IDP-T04..T05: Dual-key entities persist UUIDv7 publicId, enforce uq_<table>_public_id, and enforce ownership-scoped lookups")
    void dualKeyEntities_ShouldAssignUuidV7AndEnforceOwnershipScopedLookups() {
        User client1 = userRepository.saveAndFlush(new User(
                "Alice", "Guest", "alice.dualkey@mybooking.local", null,
                "+33611111111", "1 Avenue Opera", LocalDate.of(1991, 3, 10), Role.CLIENT
        ));
        User client2 = userRepository.saveAndFlush(new User(
                "Bob", "Peer", "bob.dualkey@mybooking.local", null,
                "+33622222222", "2 Avenue Opera", LocalDate.of(1990, 7, 22), Role.CLIENT
        ));

        Room room = roomRepository.saveAndFlush(new Room(
                "909-UUID", RoomType.DELUXE, 2, new BigDecimal("250.00"), "EUR", RoomStatus.AVAILABLE
        ));
        assertThat(room.getPublicId()).isNotNull();
        assertThat(room.getPublicId().version()).isEqualTo(7);

        Reservation res = reservationRepository.saveAndFlush(new Reservation(
                LocalDate.now().plusDays(10),
                LocalDate.now().plusDays(12),
                2,
                new BigDecimal("500.00"),
                "EUR",
                ReservationStatus.CONFIRMED,
                client1,
                room
        ));
        UUID resPublicId = res.getPublicId();
        assertThat(resPublicId).isNotNull();
        assertThat(resPublicId.version()).isEqualTo(7);

        // Owner lookup succeeds; peer lookup with wrong clientId returns Optional.empty() (T-06 / T-07 BOLA prevention)
        assertThat(reservationRepository.findByPublicIdAndClientId(resPublicId, client1.getId())).isPresent();
        assertThat(reservationRepository.findByPublicIdAndClientId(resPublicId, client2.getId())).isEmpty();

        // Verify the 3 Tier A tables without created_at (LeaveRequest, Shift, Training)
        User empUser = userRepository.findByEmail("employee@example.com").orElseThrow();
        LeaveRequest lr = leaveRequestRepository.saveAndFlush(new LeaveRequest(
                empUser, LocalDate.now().plusDays(20), LocalDate.now().plusDays(22), LeaveRequestStatus.PENDING, "Annual leave"
        ));
        Shift shift = shiftRepository.saveAndFlush(new Shift(
                empUser, LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(8)
        ));
        Training training = trainingRepository.saveAndFlush(new Training(
                "Fire Safety UUIDv7", LocalDate.now().plusDays(5), LocalDate.now().plusDays(6)
        ));

        assertThat(leaveRequestRepository.findByPublicIdAndEmployeeId(lr.getPublicId(), empUser.getId())).isPresent();
        assertThat(shiftRepository.findByPublicIdAndEmployeeId(shift.getPublicId(), empUser.getId())).isPresent();
        assertThat(trainingRepository.findByPublicId(training.getPublicId())).isPresent();

        // Duplicate public_id on any Tier A table MUST fail uq_<table>_public_id
        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO training (public_id, title, start_date, end_date) VALUES (?, 'Dup Training', CURRENT_DATE, CURRENT_DATE + 1)",
                training.getPublicId()
        )).isInstanceOf(DataIntegrityViolationException.class);
    }
}
