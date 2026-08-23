SET search_path TO carfix;

ALTER TABLE bookings
    DROP CONSTRAINT check_bookings_status;

ALTER TABLE bookings
    ADD CONSTRAINT check_bookings_status
        CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED', 'NO_SHOW'));
