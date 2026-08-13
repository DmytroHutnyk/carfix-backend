SET search_path TO carfix;

ALTER TABLE reviews
    ADD COLUMN created_at timestamptz NOT NULL DEFAULT now();

-- Backfill seeded reviews (V58): written the evening after their booking,
-- so "Newest" ordering visibly follows booking dates.
UPDATE reviews r
SET created_at = ((b.date + 1) + time '18:30')::timestamptz
FROM bookings b
WHERE b.booking_id = r.booking_id;
