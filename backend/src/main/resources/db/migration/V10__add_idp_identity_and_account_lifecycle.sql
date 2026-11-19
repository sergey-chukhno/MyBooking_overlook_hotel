-- =============================================================================
-- V10__add_idp_identity_and_account_lifecycle.sql (IDP-T03)
-- =============================================================================
-- Adds external IdP identity mapping (external_issuer, external_subject) and
-- account lifecycle columns to app_user, drops NOT NULL on app_user.password,
-- and enforces atomic pair integrity (chk_app_user_external_identity_pair) +
-- composite uniqueness (uq_app_user_external_identity) per SEC-STD-001 §2.3.
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. Non-Mutating Safety Preflight (Verify Pre-Existing Identity Pair Symmetry)
-- -----------------------------------------------------------------------------
DO $$
DECLARE
    v_asymmetric_count BIGINT;
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'app_user' AND column_name = 'external_issuer'
    ) AND EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_schema = 'public' AND table_name = 'app_user' AND column_name = 'external_subject'
    ) THEN
        EXECUTE $q$
            SELECT COUNT(*) FROM app_user
            WHERE NOT (
                (external_issuer IS NULL AND external_subject IS NULL)
                OR (external_issuer IS NOT NULL AND btrim(external_issuer) <> ''
                    AND external_subject IS NOT NULL AND btrim(external_subject) <> '')
            )
        $q$ INTO v_asymmetric_count;

        IF v_asymmetric_count > 0 THEN
            RAISE EXCEPTION 'V10 Preflight Failed: found % app_user row(s) with asymmetric or blank external_issuer/external_subject', v_asymmetric_count;
        END IF;
    END IF;
END $$;

-- -----------------------------------------------------------------------------
-- 2. Add External Identity & Account Lifecycle Columns to app_user
-- -----------------------------------------------------------------------------
ALTER TABLE app_user
    ADD COLUMN IF NOT EXISTS external_issuer  VARCHAR(255),
    ADD COLUMN IF NOT EXISTS external_subject VARCHAR(128),
    ADD COLUMN IF NOT EXISTS email_verified   BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS enabled          BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS auth_migrated_at TIMESTAMPTZ;

-- -----------------------------------------------------------------------------
-- 3. Drop NOT NULL from app_user.password (Keycloak Owns Credentials)
-- -----------------------------------------------------------------------------
ALTER TABLE app_user
    ALTER COLUMN password DROP NOT NULL;

-- -----------------------------------------------------------------------------
-- 4. Enforce Atomic Pair Integrity & Multi-Issuer Uniqueness
-- -----------------------------------------------------------------------------
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'chk_app_user_external_identity_pair'
    ) THEN
        ALTER TABLE app_user
            ADD CONSTRAINT chk_app_user_external_identity_pair
            CHECK (
                (external_issuer IS NULL AND external_subject IS NULL)
                OR (
                    external_issuer IS NOT NULL
                    AND btrim(external_issuer) <> ''
                    AND external_subject IS NOT NULL
                    AND btrim(external_subject) <> ''
                )
            );
    END IF;
END $$;

CREATE UNIQUE INDEX IF NOT EXISTS uq_app_user_external_identity
    ON app_user (external_issuer, external_subject)
    WHERE external_issuer IS NOT NULL AND external_subject IS NOT NULL;
