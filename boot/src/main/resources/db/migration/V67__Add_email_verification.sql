SET search_path TO carfix;

ALTER TABLE users
    ADD COLUMN email_verified_at timestamptz NULL;

CREATE TABLE email_verification_codes (
    user_id    uuid        PRIMARY KEY,
    code_hash  text        NOT NULL,
    issued_at  timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    attempts   int         NOT NULL DEFAULT 0,

    CONSTRAINT fk_email_verification_codes_users
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE,
    CONSTRAINT check_email_verification_codes_attempts
        CHECK (attempts >= 0),
    CONSTRAINT check_email_verification_codes_expiry
        CHECK (expires_at > issued_at)
);
