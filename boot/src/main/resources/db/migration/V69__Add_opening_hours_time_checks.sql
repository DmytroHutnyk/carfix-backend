SET search_path TO carfix;

ALTER TABLE opening_hours
    ADD CONSTRAINT check_opening_hours_time CHECK (close_time > start_time);

ALTER TABLE opening_hours_exceptions
    ADD CONSTRAINT check_opening_hours_exceptions_time
        CHECK (NOT is_open OR (start_time IS NOT NULL AND close_time IS NOT NULL AND close_time > start_time));
