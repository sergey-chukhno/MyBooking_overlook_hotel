-- =============================================================================
-- V11__add_nullable_public_uuidv7_columns.sql (IDP-T04 / ADR-0021)
-- =============================================================================
-- Adds nullable public_id UUID columns across all 19 Tier A external-facing
-- tables (explicitly including employee for Phase 4 work schema decoupling)
-- while keeping internal BIGINT primary/foreign keys intact.
-- The 4 Tier B tables (notification_preference, room_status_update,
-- employee_training, event_notification) are excluded from surrogate public_id.
-- =============================================================================

ALTER TABLE app_user            ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE employee            ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE room                ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE room_photo          ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE equipment           ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE installation        ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE reservation         ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE feedback            ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE feedback_reply      ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE event               ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE event_booking       ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE loyalty_account     ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE loyalty_transaction ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE announcement        ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE announcement_reply  ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE employee_task       ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE leave_request       ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE shift               ADD COLUMN IF NOT EXISTS public_id UUID;
ALTER TABLE training            ADD COLUMN IF NOT EXISTS public_id UUID;
