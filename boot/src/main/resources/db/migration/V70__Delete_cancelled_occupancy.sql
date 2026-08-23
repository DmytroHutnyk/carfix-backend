SET search_path TO carfix;

DELETE FROM service_bays_bookings WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'CANCELLED');
DELETE FROM employees_bookings    WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'CANCELLED');
DELETE FROM equipment_bookings    WHERE booking_id IN (SELECT booking_id FROM bookings WHERE status = 'CANCELLED');
