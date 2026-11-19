-- =============================================================================
-- V12__backfill_and_enforce_public_uuidv7_constraints.sql (IDP-T04 / ADR-0021)
-- =============================================================================
-- 1. Runs non-mutating preflight checking created_at on all 16 timestamped
--    Tier A tables (verifying zero NULL or pre-Unix-epoch timestamps).
-- 2. Defines session-scoped pure-SQL PL/pgSQL helper pg_temp.gen_uuid_v7(ts)
--    using only built-in PostgreSQL 16 pg_catalog primitives (zero extensions).
-- 3. Executes idempotent backfill (WHERE public_id IS NULL) across all 19
--    Tier A tables (16 from created_at; 3 without created_at from clock_timestamp()).
-- 4. Enforces ALTER COLUMN public_id SET NOT NULL and CREATE UNIQUE INDEX
--    uq_<tbl>_public_id ON <tbl>(public_id) across all 19 Tier A tables.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Non-Mutating Preflight on 16 Timestamped Tier A Tables
-- -----------------------------------------------------------------------------
DO $$
DECLARE
    v_tbl TEXT;
    v_col_exists BOOLEAN;
    v_invalid_ts_count BIGINT;
    v_timestamped_tables TEXT[] := ARRAY[
        'app_user',
        'employee',
        'room',
        'room_photo',
        'equipment',
        'installation',
        'reservation',
        'feedback',
        'feedback_reply',
        'event',
        'event_booking',
        'loyalty_account',
        'loyalty_transaction',
        'announcement',
        'announcement_reply',
        'employee_task'
    ];
BEGIN
    FOREACH v_tbl IN ARRAY v_timestamped_tables LOOP
        SELECT EXISTS (
            SELECT 1 FROM information_schema.columns
            WHERE table_schema = 'public'
              AND table_name = v_tbl
              AND column_name = 'created_at'
        ) INTO v_col_exists;

        IF NOT v_col_exists THEN
            RAISE EXCEPTION 'V12 Preflight Failed: table % is missing required created_at column', v_tbl;
        END IF;

        EXECUTE format(
            'SELECT COUNT(*) FROM public.%I WHERE public_id IS NULL AND (created_at IS NULL OR created_at < ''1970-01-01 00:00:00+00''::timestamptz)',
            v_tbl
        ) INTO v_invalid_ts_count;

        IF v_invalid_ts_count > 0 THEN
            RAISE EXCEPTION 'V12 Preflight Failed: table % contains % row(s) with NULL or pre-epoch (< 1970-01-01) created_at', v_tbl, v_invalid_ts_count;
        END IF;
    END LOOP;
END $$;

-- -----------------------------------------------------------------------------
-- 2. Pure-SQL PostgreSQL 16 RFC 9562 Section 5.7 UUIDv7 Generator (pg_temp)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION pg_temp.gen_uuid_v7(ts TIMESTAMPTZ)
RETURNS uuid LANGUAGE plpgsql VOLATILE AS $$
DECLARE
    v_ts_ms BIGINT;
    v_bytes BYTEA;
BEGIN
    v_ts_ms := FLOOR(EXTRACT(EPOCH FROM ts) * 1000.0)::BIGINT;
    v_bytes := uuid_send(gen_random_uuid());                        -- 16 CSPRNG bytes
    v_bytes := set_byte(v_bytes, 0, ((v_ts_ms >> 40) & 255)::INT);  -- bits 0..7   unix_ts_ms
    v_bytes := set_byte(v_bytes, 1, ((v_ts_ms >> 32) & 255)::INT);  -- bits 8..15  unix_ts_ms
    v_bytes := set_byte(v_bytes, 2, ((v_ts_ms >> 24) & 255)::INT);  -- bits 16..23 unix_ts_ms
    v_bytes := set_byte(v_bytes, 3, ((v_ts_ms >> 16) & 255)::INT);  -- bits 24..31 unix_ts_ms
    v_bytes := set_byte(v_bytes, 4, ((v_ts_ms >>  8) & 255)::INT);  -- bits 32..39 unix_ts_ms
    v_bytes := set_byte(v_bytes, 5, ( v_ts_ms        & 255)::INT);  -- bits 40..47 unix_ts_ms
    v_bytes := set_byte(v_bytes, 6, ((get_byte(v_bytes, 6) & 15) | 112)); -- version 0111 (7) + 4 rand bits
    -- byte 7 keeps 8 CSPRNG bits from gen_random_uuid() (completing 12-bit rand_a)
    v_bytes := set_byte(v_bytes, 8, ((get_byte(v_bytes, 8) & 63) | 128)); -- variant 10 (2)   + 6 rand bits
    -- bytes 9..15 keep 56 CSPRNG bits from gen_random_uuid() (completing 62-bit rand_b)
    RETURN encode(v_bytes, 'hex')::UUID;
END;
$$;

-- -----------------------------------------------------------------------------
-- 3. Idempotent UUIDv7 Backfill Across All 19 Tier A Tables
-- -----------------------------------------------------------------------------
-- 3a. 16 Tier A tables with created_at TIMESTAMPTZ
UPDATE app_user            SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE employee            SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE room                SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE room_photo          SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE equipment           SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE installation        SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE reservation         SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE feedback            SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE feedback_reply      SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE event               SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE event_booking       SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE loyalty_account     SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE loyalty_transaction SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE announcement        SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE announcement_reply  SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;
UPDATE employee_task       SET public_id = pg_temp.gen_uuid_v7(created_at) WHERE public_id IS NULL;

-- 3b. 3 Tier A tables without created_at (leave_request, shift, training)
UPDATE leave_request       SET public_id = pg_temp.gen_uuid_v7(clock_timestamp()) WHERE public_id IS NULL;
UPDATE shift               SET public_id = pg_temp.gen_uuid_v7(clock_timestamp()) WHERE public_id IS NULL;
UPDATE training            SET public_id = pg_temp.gen_uuid_v7(clock_timestamp()) WHERE public_id IS NULL;

-- -----------------------------------------------------------------------------
-- 4. Enforce NOT NULL and Authoritative UNIQUE Indexes Across All 19 Tier A Tables
-- -----------------------------------------------------------------------------
ALTER TABLE app_user            ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE employee            ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE room                ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE room_photo          ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE equipment           ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE installation        ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE reservation         ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE feedback            ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE feedback_reply      ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE event               ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE event_booking       ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE loyalty_account     ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE loyalty_transaction ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE announcement        ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE announcement_reply  ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE employee_task       ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE leave_request       ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE shift               ALTER COLUMN public_id SET NOT NULL;
ALTER TABLE training            ALTER COLUMN public_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_public_id            ON app_user (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_employee_public_id            ON employee (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_room_public_id                ON room (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_room_photo_public_id          ON room_photo (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_equipment_public_id           ON equipment (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_installation_public_id        ON installation (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_reservation_public_id         ON reservation (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_feedback_public_id            ON feedback (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_feedback_reply_public_id      ON feedback_reply (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_public_id               ON event (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_event_booking_public_id       ON event_booking (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_loyalty_account_public_id     ON loyalty_account (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_loyalty_transaction_public_id ON loyalty_transaction (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_announcement_public_id        ON announcement (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_announcement_reply_public_id  ON announcement_reply (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_employee_task_public_id       ON employee_task (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_leave_request_public_id       ON leave_request (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_shift_public_id               ON shift (public_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_training_public_id            ON training (public_id);
