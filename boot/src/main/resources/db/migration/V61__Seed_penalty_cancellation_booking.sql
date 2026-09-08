SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- One extra booking for test@gmail.com whose free-cancellation deadline has
-- already passed, so cancelling it from My Bookings shows the penalty dialog.
--
-- V56 already seeds a same-day SCHEDULED booking for that deadline, but it
-- starts at 12:00 today: by the time anyone opens the page it is usually a
-- booking whose slot has come and gone. This one starts tomorrow morning, so
-- it is genuinely upcoming while still inside the 24h notice window
-- (Booking.SAFE_CANCELLATION_NOTICE): the deadline is 08:00 *today*.
--
-- Deliberate deviation, same one V56's CURRENT_DATE bookings take: the date is
-- CURRENT_DATE + 1 rather than the next open weekday, because "within 24h of
-- now" is the whole point of the row. A Saturday run therefore lands it on a
-- closed Sunday, and a run before 08:00 leaves the deadline in the future. It
-- never feeds slot math, so neither hurts anything but the fixture's realism.
-- ---------------------------------------------------------------------------

INSERT INTO bookings (booking_id, date, status, start_time, end_time, branch_id, car_profile_id)
VALUES ('30000000-0000-4000-8000-000000000015'::uuid, CURRENT_DATE + 1, 'SCHEDULED',
        TIME '08:00', TIME '09:00',
        '10000000-0000-4000-8000-000000000001'::uuid,  -- AutoSerwis Kowalski Mokotow
        '20000000-0000-4000-8000-000000000003'::uuid); -- Family Camry (test@gmail.com)

INSERT INTO bookings_services (booking_id, service_id, start_time, end_time, price)
SELECT b.booking_id, s.service_id, b.start_time, b.end_time, s.price
FROM bookings b
JOIN services s ON s.branch_id = b.branch_id AND s.name = 'Oil and filter change'
WHERE b.booking_id = '30000000-0000-4000-8000-000000000015'::uuid;

-- Bay occupancy: the whole visit, in Mokotow's Bay 1.
INSERT INTO service_bays_bookings (booked_time, date, service_bay_id, booking_id)
SELECT tsrange(b.date + b.start_time, b.date + b.end_time, '[)'), b.date, sb.service_bay_id, b.booking_id
FROM bookings b
JOIN service_bays sb ON sb.branch_id = b.branch_id AND sb.name = 'Bay 1'
WHERE b.booking_id = '30000000-0000-4000-8000-000000000015'::uuid;

-- Employee occupancy: one slot, the same mechanic V56 puts on Mokotow oil changes.
INSERT INTO employees_bookings (booked_time, date, employee_id, booking_id)
SELECT tsrange(b.date + b.start_time, b.date + b.end_time, '[)'), b.date,
       '40000000-0000-4000-8000-000000000003'::uuid, b.booking_id
FROM bookings b
WHERE b.booking_id = '30000000-0000-4000-8000-000000000015'::uuid;

-- 'Oil and filter change' declares no equipment requirement, so no equipment row.
