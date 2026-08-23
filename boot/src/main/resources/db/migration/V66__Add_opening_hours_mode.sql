SET search_path TO carfix;

-- OPEN vs BY_APPOINTMENT (owner wizard, opening-hours step). Closed days have no row. Existing readers
-- keep working: default OPEN, and the slot engine treats both modes as bookable.
ALTER TABLE opening_hours
    ADD COLUMN mode text NOT NULL DEFAULT 'OPEN',
    ADD CONSTRAINT check_opening_hours_mode CHECK (mode IN ('OPEN', 'BY_APPOINTMENT'));
