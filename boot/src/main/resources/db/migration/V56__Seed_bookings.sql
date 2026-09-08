SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- Seed part 5 of 6 — bookings, their service segments and the occupancy rows
-- that hold the concrete resources.
--
-- Dates are anchored to weekdays, never to a raw "+N days" offset: availability
-- only exists on open days, so a booking that lands on a Sunday would sit on a
-- date where its resources are not available and every later slot check would
-- read nonsense. The two CURRENT_DATE bookings are the exception — they exist
-- for the 24h cancellation deadline in My Bookings and never feed slot math.
--
-- Occupancy rows are the only record of who and what was assigned (spec 3, #3):
-- one bay row per booking (the car stays in one bay for the whole visit), one
-- employee row per segment per filled slot, one equipment row per segment per
-- filled slot. They carry booking_id, which is what makes cancellation able to
-- free them.
--
-- Two bookings are deliberately shaped for edge cases:
--   ...0003 is CANCELLED and therefore has no occupancy rows at all — its time
--           was given back, exactly as cancellation must do;
--   ...0007 has no services, which exercises the empty-services / null-total
--           read path. V57 excludes it from the "every booking has segments"
--           assertion by id.
-- ---------------------------------------------------------------------------

-- Anchors: the next Tuesday/Wednesday/Thursday on or after tomorrow. Starting
-- at tomorrow keeps every anchored booking clear of the two CURRENT_DATE ones,
-- so no resource is double-booked whatever weekday the migration runs on.
WITH anchor AS (
    SELECT (CURRENT_DATE + 1 + ((2 - EXTRACT(ISODOW FROM CURRENT_DATE + 1)::int + 7) % 7)) AS tue,
           (CURRENT_DATE + 1 + ((3 - EXTRACT(ISODOW FROM CURRENT_DATE + 1)::int + 7) % 7)) AS wed,
           (CURRENT_DATE + 1 + ((4 - EXTRACT(ISODOW FROM CURRENT_DATE + 1)::int + 7) % 7)) AS thu
)
INSERT INTO bookings (booking_id, date, status, start_time, end_time, branch_id, car_profile_id)
SELECT v.booking_id,
       CASE v.day_key
           WHEN 'TUE' THEN a.tue
           WHEN 'WED' THEN a.wed
           WHEN 'THU' THEN a.thu
           ELSE CURRENT_DATE
       END + v.day_shift,
       v.status, v.start_time, v.end_time, v.branch_id, v.car_profile_id
FROM anchor a
CROSS JOIN (VALUES
    -- past visits
    ('30000000-0000-4000-8000-000000000001'::uuid, 'TUE', -42, 'COMPLETED',   TIME '09:00', TIME '11:30',
     '10000000-0000-4000-8000-000000000001'::uuid, '20000000-0000-4000-8000-000000000001'::uuid),
    ('30000000-0000-4000-8000-000000000002'::uuid, 'WED', -14, 'COMPLETED',   TIME '14:00', TIME '14:45',
     '10000000-0000-4000-8000-000000000002'::uuid, '20000000-0000-4000-8000-000000000002'::uuid),
    ('30000000-0000-4000-8000-000000000003'::uuid, 'THU',  -7, 'CANCELLED',   TIME '11:00', TIME '11:40',
     '10000000-0000-4000-8000-000000000004'::uuid, '20000000-0000-4000-8000-000000000003'::uuid),
    -- today: in progress, and one whose free-cancellation deadline has passed.
    -- Times sit inside 09:00-14:00 so they stay within opening hours whichever
    -- day the migration runs (Saturdays are the short day). A Sunday run puts
    -- them on a closed day — accepted, they never feed slot math.
    ('30000000-0000-4000-8000-000000000004'::uuid, 'TODAY', 0, 'IN_PROGRESS', TIME '09:00', TIME '10:45',
     '10000000-0000-4000-8000-000000000001'::uuid, '20000000-0000-4000-8000-000000000002'::uuid),
    ('30000000-0000-4000-8000-000000000005'::uuid, 'TODAY', 0, 'SCHEDULED',   TIME '12:00', TIME '13:00',
     '10000000-0000-4000-8000-000000000001'::uuid, '20000000-0000-4000-8000-000000000001'::uuid),
    -- upcoming: a two-service chain and a booking with no services
    ('30000000-0000-4000-8000-000000000006'::uuid, 'TUE',   7, 'SCHEDULED',   TIME '12:00', TIME '14:00',
     '10000000-0000-4000-8000-000000000002'::uuid, '20000000-0000-4000-8000-000000000003'::uuid),
    ('30000000-0000-4000-8000-000000000007'::uuid, 'THU',   7, 'SCHEDULED',   TIME '15:00', TIME '16:00',
     '10000000-0000-4000-8000-000000000001'::uuid, '20000000-0000-4000-8000-000000000001'::uuid),
    -- the two-mechanic job: holds Mokotow's only senior mechanic all day
    ('30000000-0000-4000-8000-000000000008'::uuid, 'WED',   0, 'SCHEDULED',   TIME '08:00', TIME '16:00',
     '10000000-0000-4000-8000-000000000001'::uuid, '20000000-0000-4000-8000-000000000004'::uuid),
    -- Serwis Ursus, booked solid 08:00-18:00 on its single bay: the branch that
    -- the availability filter must exclude for that date
    ('30000000-0000-4000-8000-000000000009'::uuid, 'THU',   0, 'SCHEDULED',   TIME '08:00', TIME '09:00',
     '10000000-0000-4000-8000-000000000007'::uuid, '20000000-0000-4000-8000-000000000001'::uuid),
    ('30000000-0000-4000-8000-000000000010'::uuid, 'THU',   0, 'SCHEDULED',   TIME '09:00', TIME '10:30',
     '10000000-0000-4000-8000-000000000007'::uuid, '20000000-0000-4000-8000-000000000002'::uuid),
    ('30000000-0000-4000-8000-000000000011'::uuid, 'THU',   0, 'SCHEDULED',   TIME '10:30', TIME '13:00',
     '10000000-0000-4000-8000-000000000007'::uuid, '20000000-0000-4000-8000-000000000003'::uuid),
    ('30000000-0000-4000-8000-000000000012'::uuid, 'THU',   0, 'SCHEDULED',   TIME '13:00', TIME '14:00',
     '10000000-0000-4000-8000-000000000007'::uuid, '20000000-0000-4000-8000-000000000004'::uuid),
    ('30000000-0000-4000-8000-000000000013'::uuid, 'THU',   0, 'SCHEDULED',   TIME '14:00', TIME '15:30',
     '10000000-0000-4000-8000-000000000007'::uuid, '20000000-0000-4000-8000-000000000001'::uuid),
    ('30000000-0000-4000-8000-000000000014'::uuid, 'THU',   0, 'SCHEDULED',   TIME '15:30', TIME '18:00',
     '10000000-0000-4000-8000-000000000007'::uuid, '20000000-0000-4000-8000-000000000002'::uuid)
) AS v(booking_id, day_key, day_shift, status, start_time, end_time, branch_id, car_profile_id);

-- Segments: per-service times inside the booking span, plus the price snapshot
-- taken at booking time (a later price edit must not rewrite history).
-- Segment starts sit on the 15-minute grid; ends do not (spec section 6), which
-- is why booking ...0006 runs 12:00-12:40 and then 12:45-14:00.
INSERT INTO bookings_services (booking_id, service_id, start_time, end_time, price)
SELECT v.booking_id, s.service_id, v.start_time, v.end_time, s.price
FROM (VALUES
    ('30000000-0000-4000-8000-000000000001'::uuid, 'Oil and filter change',      TIME '09:00', TIME '10:00'),
    ('30000000-0000-4000-8000-000000000001'::uuid, 'Brake pads replacement',     TIME '10:00', TIME '11:30'),
    ('30000000-0000-4000-8000-000000000002'::uuid, 'Computer diagnostics',       TIME '14:00', TIME '14:45'),
    ('30000000-0000-4000-8000-000000000003'::uuid, 'Seasonal tyre swap',         TIME '11:00', TIME '11:40'),
    ('30000000-0000-4000-8000-000000000004'::uuid, 'Air conditioning service',   TIME '09:00', TIME '10:00'),
    ('30000000-0000-4000-8000-000000000004'::uuid, 'Computer diagnostics',       TIME '10:00', TIME '10:45'),
    ('30000000-0000-4000-8000-000000000005'::uuid, 'Oil and filter change',      TIME '12:00', TIME '13:00'),
    ('30000000-0000-4000-8000-000000000006'::uuid, 'Seasonal tyre swap',         TIME '12:00', TIME '12:40'),
    ('30000000-0000-4000-8000-000000000006'::uuid, 'Wheel alignment',            TIME '12:45', TIME '14:00'),
    ('30000000-0000-4000-8000-000000000008'::uuid, 'Engine replacement',         TIME '08:00', TIME '16:00'),
    ('30000000-0000-4000-8000-000000000009'::uuid, 'Oil and filter change',      TIME '08:00', TIME '09:00'),
    ('30000000-0000-4000-8000-000000000010'::uuid, 'Brake pads replacement',     TIME '09:00', TIME '10:30'),
    ('30000000-0000-4000-8000-000000000011'::uuid, 'Shock absorber replacement', TIME '10:30', TIME '13:00'),
    ('30000000-0000-4000-8000-000000000012'::uuid, 'Oil and filter change',      TIME '13:00', TIME '14:00'),
    ('30000000-0000-4000-8000-000000000013'::uuid, 'Brake pads replacement',     TIME '14:00', TIME '15:30'),
    ('30000000-0000-4000-8000-000000000014'::uuid, 'Shock absorber replacement', TIME '15:30', TIME '18:00')
) AS v(booking_id, service_name, start_time, end_time)
JOIN bookings b ON b.booking_id = v.booking_id
JOIN services s ON s.name = v.service_name AND s.branch_id = b.branch_id;

-- Bay occupancy: one row per booking, spanning the whole visit.
INSERT INTO service_bays_bookings (booked_time, date, service_bay_id, booking_id)
SELECT tsrange(b.date + b.start_time, b.date + b.end_time, '[)'), b.date, sb.service_bay_id, b.booking_id
FROM (VALUES
    ('30000000-0000-4000-8000-000000000001'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000002'::uuid, 'Bay 3'),
    ('30000000-0000-4000-8000-000000000004'::uuid, 'Bay 3'),
    ('30000000-0000-4000-8000-000000000005'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000006'::uuid, 'Bay 4'),
    ('30000000-0000-4000-8000-000000000008'::uuid, 'Bay 2'),
    ('30000000-0000-4000-8000-000000000009'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000010'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000011'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000012'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000013'::uuid, 'Bay 1'),
    ('30000000-0000-4000-8000-000000000014'::uuid, 'Bay 1')
) AS v(booking_id, bay_name)
JOIN bookings b ON b.booking_id = v.booking_id
JOIN service_bays sb ON sb.branch_id = b.branch_id AND sb.name = v.bay_name;

-- Employee occupancy: one row per segment per filled slot. Booking ...0008 has
-- two rows for the same span because 'Engine replacement' declares two slots
-- and they must be filled by distinct people.
INSERT INTO employees_bookings (booked_time, date, employee_id, booking_id)
SELECT tsrange(b.date + v.start_time, b.date + v.end_time, '[)'), b.date, v.employee_id, b.booking_id
FROM (VALUES
    ('30000000-0000-4000-8000-000000000001'::uuid, '40000000-0000-4000-8000-000000000002'::uuid, TIME '09:00', TIME '10:00'),
    ('30000000-0000-4000-8000-000000000001'::uuid, '40000000-0000-4000-8000-000000000002'::uuid, TIME '10:00', TIME '11:30'),
    ('30000000-0000-4000-8000-000000000002'::uuid, '40000000-0000-4000-8000-000000000017'::uuid, TIME '14:00', TIME '14:45'),
    ('30000000-0000-4000-8000-000000000004'::uuid, '40000000-0000-4000-8000-000000000002'::uuid, TIME '09:00', TIME '10:00'),
    ('30000000-0000-4000-8000-000000000004'::uuid, '40000000-0000-4000-8000-000000000004'::uuid, TIME '10:00', TIME '10:45'),
    ('30000000-0000-4000-8000-000000000005'::uuid, '40000000-0000-4000-8000-000000000003'::uuid, TIME '12:00', TIME '13:00'),
    ('30000000-0000-4000-8000-000000000006'::uuid, '40000000-0000-4000-8000-000000000018'::uuid, TIME '12:00', TIME '12:40'),
    ('30000000-0000-4000-8000-000000000006'::uuid, '40000000-0000-4000-8000-000000000018'::uuid, TIME '12:45', TIME '14:00'),
    ('30000000-0000-4000-8000-000000000008'::uuid, '40000000-0000-4000-8000-000000000001'::uuid, TIME '08:00', TIME '16:00'),
    ('30000000-0000-4000-8000-000000000008'::uuid, '40000000-0000-4000-8000-000000000002'::uuid, TIME '08:00', TIME '16:00'),
    ('30000000-0000-4000-8000-000000000009'::uuid, '40000000-0000-4000-8000-000000000031'::uuid, TIME '08:00', TIME '09:00'),
    ('30000000-0000-4000-8000-000000000010'::uuid, '40000000-0000-4000-8000-000000000031'::uuid, TIME '09:00', TIME '10:30'),
    ('30000000-0000-4000-8000-000000000011'::uuid, '40000000-0000-4000-8000-000000000031'::uuid, TIME '10:30', TIME '13:00'),
    ('30000000-0000-4000-8000-000000000012'::uuid, '40000000-0000-4000-8000-000000000031'::uuid, TIME '13:00', TIME '14:00'),
    ('30000000-0000-4000-8000-000000000013'::uuid, '40000000-0000-4000-8000-000000000031'::uuid, TIME '14:00', TIME '15:30'),
    ('30000000-0000-4000-8000-000000000014'::uuid, '40000000-0000-4000-8000-000000000031'::uuid, TIME '15:30', TIME '18:00')
) AS v(booking_id, employee_id, start_time, end_time)
JOIN bookings b ON b.booking_id = v.booking_id;

-- Equipment occupancy: units are looked up per branch, since unit names repeat.
INSERT INTO equipment_bookings (booked_time, date, equipment_id, booking_id)
SELECT tsrange(b.date + v.start_time, b.date + v.end_time, '[)'), b.date, eq.equipment_id, b.booking_id
FROM (VALUES
    ('30000000-0000-4000-8000-000000000002'::uuid, 'OBD scanner 1',     TIME '14:00', TIME '14:45'),
    ('30000000-0000-4000-8000-000000000004'::uuid, 'AC service unit 1', TIME '09:00', TIME '10:00'),
    ('30000000-0000-4000-8000-000000000004'::uuid, 'OBD scanner 1',     TIME '10:00', TIME '10:45'),
    ('30000000-0000-4000-8000-000000000006'::uuid, 'Tyre changer 1',    TIME '12:00', TIME '12:40'),
    ('30000000-0000-4000-8000-000000000006'::uuid, 'Wheel balancer 1',  TIME '12:00', TIME '12:40'),
    ('30000000-0000-4000-8000-000000000006'::uuid, 'Alignment rig 1',   TIME '12:45', TIME '14:00'),
    ('30000000-0000-4000-8000-000000000008'::uuid, 'Engine hoist 1',    TIME '08:00', TIME '16:00'),
    ('30000000-0000-4000-8000-000000000008'::uuid, 'Trolley jack 1',    TIME '08:00', TIME '16:00'),
    ('30000000-0000-4000-8000-000000000011'::uuid, 'Trolley jack 1',    TIME '10:30', TIME '13:00'),
    ('30000000-0000-4000-8000-000000000014'::uuid, 'Trolley jack 1',    TIME '15:30', TIME '18:00')
) AS v(booking_id, unit_name, start_time, end_time)
JOIN bookings b ON b.booking_id = v.booking_id
JOIN equipment eq ON eq.branch_id = b.branch_id AND eq.name = v.unit_name;
