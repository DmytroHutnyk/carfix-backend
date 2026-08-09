SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- V57 put hand-typed rating/review_count numbers on branches as a placeholder
-- for a reviews feature that had no mapping yet. From here on those columns are
-- a cache over the `reviews` table: this migration does the one-time backfill,
-- and ReviewService keeps it current from then on.
--
-- The seed is deliberately small — five reviews. A review needs a booking
-- (fk_reviews_bookings) and a booking can be reviewed only once
-- (uq_reviews_booking_id), so realistic counts would mean hundreds of booking
-- rows. Five is enough to exercise every path the UI has: a branch with several
-- reviews (rounded mean), branches with exactly one, and branches with none
-- (NULL/NULL -> the "New" badge).
--
-- Two of the five hang off the COMPLETED bookings V56 already seeded
-- (...0001 at Mokotow, ...0002 at Podgorze). The other three need new bookings,
-- which use Piotr's car profile (20000000-…-0004), never test@gmail.com's, so
-- the demo account's My Bookings list is unchanged. They carry no
-- bookings_services and no occupancy rows on purpose: they are historical
-- records that exist to hold a review, and V56 already establishes that a
-- booking with no service segments is legal (booking …0007). They sit 70+ days
-- in the past, so they never feed slot math.
-- ---------------------------------------------------------------------------

INSERT INTO bookings (booking_id, date, status, start_time, end_time, branch_id, car_profile_id)
SELECT v.booking_id,
       CURRENT_DATE - v.days_ago,
       'COMPLETED',
       TIME '09:00',
       TIME '10:00',
       b.branch_id,
       '20000000-0000-4000-8000-000000000004'::uuid
FROM (VALUES
    ('31000000-0000-4000-8000-000000000001'::uuid, 'AutoSerwis Kowalski Mokotow', 90),
    ('31000000-0000-4000-8000-000000000002'::uuid, 'AutoSerwis Kowalski Mokotow', 83),
    ('31000000-0000-4000-8000-000000000003'::uuid, 'AutoSerwis Kowalski Wola',    76)
) AS v(booking_id, branch_name, days_ago)
JOIN branches b ON b.name = v.branch_name;

INSERT INTO reviews (review_id, stars_number, contents, booking_id)
VALUES
    -- on bookings V56 already seeded
    (gen_random_uuid(), 5, 'Quick, fair price, car was ready on time.',      '30000000-0000-4000-8000-000000000001'),
    (gen_random_uuid(), 4, 'Solid diagnostics, ran a little late.',          '30000000-0000-4000-8000-000000000002'),
    -- on the three bookings above
    (gen_random_uuid(), 4, 'Good work, waiting area could be nicer.',        '31000000-0000-4000-8000-000000000001'),
    (gen_random_uuid(), 5, 'Explained everything before touching anything.', '31000000-0000-4000-8000-000000000002'),
    (gen_random_uuid(), 5, 'Best workshop I have used in Warsaw.',           '31000000-0000-4000-8000-000000000003');

-- One-time backfill. Same rule the domain states: rating = mean of the stars,
-- review_count = how many. Branches with no reviews go back to NULL/NULL, which
-- check_branches_rating_pair requires and the card renders as "New".
UPDATE branches b
SET rating = agg.rating,
    review_count = agg.review_count
FROM (
    SELECT bk.branch_id,
           ROUND(AVG(r.stars_number)::numeric, 1) AS rating,
           COUNT(*)::int                          AS review_count
    FROM reviews r
    JOIN bookings bk ON bk.booking_id = r.booking_id
    GROUP BY bk.branch_id
) agg
WHERE agg.branch_id = b.branch_id;

UPDATE branches
SET rating = NULL,
    review_count = NULL
WHERE branch_id NOT IN (
    SELECT bk.branch_id
    FROM reviews r
    JOIN bookings bk ON bk.booking_id = r.booking_id
);
