SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- Seed part 4 of 6 — availability for every bookable resource, for the next
-- eight weeks.
--
-- This is what the owner's calendar entry (roadmap M7) will write: per-date
-- rows expanded from a recurrence rule, all rows of one rule sharing a
-- series_id (decision 2.5, option D). Each resource here gets exactly one
-- series, so 'delete this series' is one statement per resource.
--
-- series_id is taken from the shared sequence once per resource, in a
-- MATERIALIZED CTE — nextval is volatile, so the CTE is not inlined and each
-- resource is drawn exactly once. It is never a column DEFAULT: a default
-- fires per row and would give every row of a batch a different value, which
-- is the opposite of what a series means (spec section 5).
--
-- Rows follow branch opening hours, so nothing is available on Sunday and the
-- two tyre shops (07:00-20:00) run longer days than the rest.
-- ---------------------------------------------------------------------------

-- Bays: available whenever the branch is open.
WITH horizon AS (
    SELECT d::date AS date
    FROM generate_series(CURRENT_DATE, CURRENT_DATE + 56, INTERVAL '1 day') AS d
),
open_day AS (
    SELECT h.date, oh.branch_id, oh.start_time, oh.close_time
    FROM horizon h
    JOIN opening_hours oh
      ON oh.day_of_week = CASE EXTRACT(ISODOW FROM h.date)::int
                              WHEN 1 THEN 'MONDAY'
                              WHEN 2 THEN 'TUESDAY'
                              WHEN 3 THEN 'WEDNESDAY'
                              WHEN 4 THEN 'THURSDAY'
                              WHEN 5 THEN 'FRIDAY'
                              WHEN 6 THEN 'SATURDAY'
                              ELSE 'SUNDAY'
                          END
),
series AS MATERIALIZED (
    SELECT service_bay_id, nextval('availability_series_seq') AS series_id
    FROM service_bays
)
INSERT INTO service_bays_availability (available_time, date, series_id, service_bay_id)
SELECT tsrange(od.date + od.start_time, od.date + od.close_time, '[)'),
       od.date, se.series_id, sb.service_bay_id
FROM service_bays sb
JOIN open_day od ON od.branch_id = sb.branch_id
JOIN series se ON se.service_bay_id = sb.service_bay_id;

-- Equipment: same hours as the branch.
WITH horizon AS (
    SELECT d::date AS date
    FROM generate_series(CURRENT_DATE, CURRENT_DATE + 56, INTERVAL '1 day') AS d
),
open_day AS (
    SELECT h.date, oh.branch_id, oh.start_time, oh.close_time
    FROM horizon h
    JOIN opening_hours oh
      ON oh.day_of_week = CASE EXTRACT(ISODOW FROM h.date)::int
                              WHEN 1 THEN 'MONDAY'
                              WHEN 2 THEN 'TUESDAY'
                              WHEN 3 THEN 'WEDNESDAY'
                              WHEN 4 THEN 'THURSDAY'
                              WHEN 5 THEN 'FRIDAY'
                              WHEN 6 THEN 'SATURDAY'
                              ELSE 'SUNDAY'
                          END
),
series AS MATERIALIZED (
    SELECT equipment_id, nextval('availability_series_seq') AS series_id
    FROM equipment
)
INSERT INTO equipment_availability (available_time, date, series_id, equipment_id)
SELECT tsrange(od.date + od.start_time, od.date + od.close_time, '[)'),
       od.date, se.series_id, eq.equipment_id
FROM equipment eq
JOIN open_day od ON od.branch_id = eq.branch_id
JOIN series se ON se.equipment_id = eq.equipment_id;

-- Employees: full opening hours, with two deliberate exceptions.
--   * three apprentices work mornings only — afternoon bookings must fall back
--     on the alternatives listed in the requirement slot;
--   * two senior mechanics never work Saturdays — 'Engine replacement' is
--     therefore unbookable at Mokotow and Podgorze on Saturdays, which is a
--     correct answer, not a bug.
-- Suspended staff (Zofia Ostrowska) get availability like everyone else: only
-- the status filter may keep them out of a booking.
WITH horizon AS (
    SELECT d::date AS date
    FROM generate_series(CURRENT_DATE, CURRENT_DATE + 56, INTERVAL '1 day') AS d
),
open_day AS (
    SELECT h.date, oh.branch_id, oh.start_time, oh.close_time
    FROM horizon h
    JOIN opening_hours oh
      ON oh.day_of_week = CASE EXTRACT(ISODOW FROM h.date)::int
                              WHEN 1 THEN 'MONDAY'
                              WHEN 2 THEN 'TUESDAY'
                              WHEN 3 THEN 'WEDNESDAY'
                              WHEN 4 THEN 'THURSDAY'
                              WHEN 5 THEN 'FRIDAY'
                              WHEN 6 THEN 'SATURDAY'
                              ELSE 'SUNDAY'
                          END
),
mornings_only(user_id) AS (VALUES
    ('40000000-0000-4000-8000-000000000006'::uuid),   -- Kamil Duda, Mokotow
    ('40000000-0000-4000-8000-000000000013'::uuid),   -- Oskar Pawlak, Wola
    ('40000000-0000-4000-8000-000000000023'::uuid)    -- Julia Adamska, Praga
),
no_saturday(user_id) AS (VALUES
    ('40000000-0000-4000-8000-000000000001'::uuid),   -- Adam Nowak, Mokotow
    ('40000000-0000-4000-8000-000000000014'::uuid)    -- Grzegorz Nowicki, Podgorze
),
series AS MATERIALIZED (
    SELECT user_id, nextval('availability_series_seq') AS series_id
    FROM employees
)
INSERT INTO employees_availability (available_time, date, series_id, employee_id)
SELECT tsrange(od.date + od.start_time,
               od.date + CASE WHEN mo.user_id IS NOT NULL
                              THEN LEAST(od.close_time, TIME '13:00')
                              ELSE od.close_time
                         END,
               '[)'),
       od.date, se.series_id, e.user_id
FROM employees e
JOIN open_day od ON od.branch_id = e.branch_id
JOIN series se ON se.user_id = e.user_id
LEFT JOIN mornings_only mo ON mo.user_id = e.user_id
WHERE NOT (EXTRACT(ISODOW FROM od.date)::int = 6
           AND e.user_id IN (SELECT user_id FROM no_saturday));
