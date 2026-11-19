-- =============================================================================
-- V9__reconcile_phase3_jpa_schema_drift.sql (IDP-T03)
-- =============================================================================
-- Reconciles all PostgreSQL 16 DDL differences between Flyway V1..V8 and the
-- 23 Phase 3 JPA @Entity classes so Hibernate ddl-auto=validate passes cleanly.
-- Runs as a single atomic transaction with non-mutating safety preflights.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Non-Mutating Safety Preflight Checks (Fail Fast, Never Mutate Business Data)
-- -----------------------------------------------------------------------------
DO $$
DECLARE
    v_dup_np_count BIGINT;
    v_bad_loyalty_type_count BIGINT;
    v_overflow_eb_count BIGINT;
BEGIN
    -- Check 1: Duplicate (user_id, notification_type) in notification_preference
    SELECT COUNT(*) INTO v_dup_np_count
    FROM (
        SELECT user_id, notification_type
        FROM notification_preference
        GROUP BY user_id, notification_type
        HAVING COUNT(*) > 1
    ) dup;
    IF v_dup_np_count > 0 THEN
        RAISE EXCEPTION 'V9 Preflight Failed: found % duplicate (user_id, notification_type) pair(s) in notification_preference', v_dup_np_count;
    END IF;

    -- Check 2: Invalid loyalty_transaction.type values outside ('EARN','REDEEM','REFUND','CLAWBACK')
    SELECT COUNT(*) INTO v_bad_loyalty_type_count
    FROM loyalty_transaction
    WHERE type NOT IN ('EARN', 'REDEEM', 'REFUND', 'CLAWBACK');
    IF v_bad_loyalty_type_count > 0 THEN
        RAISE EXCEPTION 'V9 Preflight Failed: found % row(s) in loyalty_transaction with unsupported type', v_bad_loyalty_type_count;
    END IF;

    -- Check 3: Numeric overflow in event_booking.total_price before narrowing NUMERIC(12,2) -> NUMERIC(10,2)
    SELECT COUNT(*) INTO v_overflow_eb_count
    FROM event_booking
    WHERE ABS(total_price) >= 100000000.00;
    IF v_overflow_eb_count > 0 THEN
        RAISE EXCEPTION 'V9 Preflight Failed: found % row(s) in event_booking where total_price exceeds NUMERIC(10,2) bounds', v_overflow_eb_count;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 2. Add Optimistic Locking @Version Columns (BIGINT NOT NULL DEFAULT 0)
-- -----------------------------------------------------------------------------
ALTER TABLE announcement    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE employee        ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE leave_request   ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE installation    ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE loyalty_account ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE room            ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- -----------------------------------------------------------------------------
-- 3. Add Phase 3 Department & Targeting Columns on employee and announcement
-- -----------------------------------------------------------------------------
ALTER TABLE employee
    ADD COLUMN IF NOT EXISTS department VARCHAR(32) NOT NULL DEFAULT 'ALL';

ALTER TABLE announcement
    ADD COLUMN IF NOT EXISTS target_department VARCHAR(32) NOT NULL DEFAULT 'ALL',
    ADD COLUMN IF NOT EXISTS target_role VARCHAR(16),
    ALTER COLUMN content TYPE VARCHAR(2000);

ALTER TABLE announcement_reply
    ALTER COLUMN message TYPE VARCHAR(1000);

-- -----------------------------------------------------------------------------
-- 4. Reconcile loyalty_account Audit Columns & loyalty_transaction Type Check
-- -----------------------------------------------------------------------------
ALTER TABLE loyalty_account
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

DO $$
DECLARE
    v_con_name TEXT;
BEGIN
    FOR v_con_name IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON c.conrelid = t.oid
        JOIN pg_namespace n ON t.relnamespace = n.oid
        WHERE n.nspname = 'public'
          AND t.relname = 'loyalty_transaction'
          AND c.contype = 'c'
          AND pg_get_constraintdef(c.oid) ILIKE '%type%'
    LOOP
        EXECUTE format('ALTER TABLE public.loyalty_transaction DROP CONSTRAINT %I', v_con_name);
    END LOOP;
END $$;

ALTER TABLE loyalty_transaction
    ADD CONSTRAINT chk_loyalty_transaction_type
    CHECK (type IN ('EARN', 'REDEEM', 'REFUND', 'CLAWBACK'));

-- -----------------------------------------------------------------------------
-- 5. Reconcile notification_preference Unique Constraint, room_status_update, & event_booking
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_notification_preference_user_type'
    ) THEN
        ALTER TABLE notification_preference
            ADD CONSTRAINT uq_notification_preference_user_type UNIQUE (user_id, notification_type);
    END IF;
END $$;

ALTER TABLE room_status_update
    ALTER COLUMN previous_status TYPE VARCHAR(32),
    ALTER COLUMN new_status TYPE VARCHAR(32);

ALTER TABLE event_booking
    ALTER COLUMN total_price TYPE NUMERIC(10,2);

-- -----------------------------------------------------------------------------
-- 6. Reconcile Remaining Phase 1-3 Column Types, Nullability & Missing Columns
--    for Strict Hibernate 6 ddl-auto=validate Compliance across All 23 Entities
-- -----------------------------------------------------------------------------
-- room & installation & reservation: CHAR(3) -> VARCHAR(3), TEXT -> VARCHAR(500), UNIQUE constraints
ALTER TABLE room
    ALTER COLUMN currency TYPE VARCHAR(3),
    ALTER COLUMN description TYPE VARCHAR(500);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_class t ON c.conrelid = t.oid
        WHERE t.relname = 'room' AND c.contype = 'u' AND pg_get_constraintdef(c.oid) ILIKE '%(number)%'
    ) THEN
        ALTER TABLE room ADD CONSTRAINT uq_room_number UNIQUE (number);
    END IF;
END $$;

ALTER TABLE installation
    ALTER COLUMN currency TYPE VARCHAR(3),
    ALTER COLUMN description TYPE VARCHAR(500);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint c
        JOIN pg_class t ON c.conrelid = t.oid
        WHERE t.relname = 'installation' AND c.contype = 'u' AND pg_get_constraintdef(c.oid) ILIKE '%(name)%'
    ) THEN
        ALTER TABLE installation ADD CONSTRAINT uq_installation_name UNIQUE (name);
    END IF;
END $$;

ALTER TABLE event
    ALTER COLUMN currency TYPE VARCHAR(3),
    ALTER COLUMN description TYPE VARCHAR(500);

ALTER TABLE reservation
    ALTER COLUMN currency TYPE VARCHAR(3);

-- equipment: SERIAL (INT) -> BIGINT, add Equipment entity columns
ALTER TABLE room_equipment DROP CONSTRAINT IF EXISTS room_equipment_equipment_id_fkey;
ALTER TABLE equipment
    ALTER COLUMN id TYPE BIGINT,
    ADD COLUMN IF NOT EXISTS description VARCHAR(500),
    ADD COLUMN IF NOT EXISTS equipment_type VARCHAR(32) NOT NULL DEFAULT 'FURNITURE',
    ADD COLUMN IF NOT EXISTS quantity INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS available_quantity INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS brand VARCHAR(50),
    ADD COLUMN IF NOT EXISTS model VARCHAR(50),
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE room_equipment
    ALTER COLUMN equipment_id TYPE BIGINT,
    ADD CONSTRAINT room_equipment_equipment_id_fkey FOREIGN KEY (equipment_id) REFERENCES equipment(id) ON DELETE RESTRICT;

-- room_photo: align with RoomPhoto entity columns
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'room_photo' AND column_name = 'url'
    ) AND NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'room_photo' AND column_name = 'photo_url'
    ) THEN
        ALTER TABLE room_photo RENAME COLUMN url TO photo_url;
    END IF;
END $$;

ALTER TABLE room_photo
    ALTER COLUMN photo_url TYPE VARCHAR(500),
    ADD COLUMN IF NOT EXISTS caption VARCHAR(200),
    ADD COLUMN IF NOT EXISTS display_order INTEGER NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS is_active BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS photo_type VARCHAR(50),
    ADD COLUMN IF NOT EXISTS file_size BIGINT,
    ADD COLUMN IF NOT EXISTS file_name VARCHAR(100),
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

-- feedback & feedback_reply: add updated_at and unique constraint (user_id, reservation_id)
ALTER TABLE feedback
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uq_feedback_user_reservation'
    ) THEN
        ALTER TABLE feedback
            ADD CONSTRAINT uq_feedback_user_reservation UNIQUE (user_id, reservation_id);
    END IF;
END $$;

ALTER TABLE feedback_reply
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ;

-- employee_task & leave_request: align TEXT columns to VARCHAR bounds in entities
ALTER TABLE employee_task
    ALTER COLUMN description TYPE VARCHAR(1000),
    ALTER COLUMN note TYPE VARCHAR(500),
    ALTER COLUMN photo_url TYPE VARCHAR(500);

ALTER TABLE leave_request
    ALTER COLUMN reason TYPE VARCHAR(500);

-- training & employee_training: SERIAL (INT) -> BIGINT
ALTER TABLE employee_training DROP CONSTRAINT IF EXISTS employee_training_training_id_fkey;
ALTER TABLE training
    ALTER COLUMN id TYPE BIGINT;
ALTER TABLE employee_training
    ALTER COLUMN training_id TYPE BIGINT,
    ADD CONSTRAINT employee_training_training_id_fkey FOREIGN KEY (training_id) REFERENCES training(id) ON DELETE RESTRICT;

-- event_notification: reconcile with EventNotification entity
DROP TABLE IF EXISTS event_notification CASCADE;
CREATE TABLE event_notification (
    id BIGSERIAL PRIMARY KEY,
    event_booking_id BIGINT NOT NULL REFERENCES event_booking(id) ON DELETE CASCADE,
    user_id BIGINT NOT NULL REFERENCES app_user(id) ON DELETE RESTRICT,
    message VARCHAR(500) NOT NULL,
    type VARCHAR(20) NOT NULL,
    sent_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_event_notification_booking ON event_notification(event_booking_id);
CREATE INDEX idx_event_notification_user ON event_notification(user_id);
