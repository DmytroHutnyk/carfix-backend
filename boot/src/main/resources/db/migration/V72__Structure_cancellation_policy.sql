SET search_path TO carfix;

UPDATE branches SET cancellation_policy = 'MODERATE';

ALTER TABLE branches
    ALTER COLUMN cancellation_policy SET NOT NULL,
    ALTER COLUMN cancellation_policy SET DEFAULT 'MODERATE',
    ADD CONSTRAINT check_branches_cancellation_policy
        CHECK (cancellation_policy IN ('STRICT', 'MODERATE', 'FLEXIBLE'));
