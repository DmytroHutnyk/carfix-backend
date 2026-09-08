BEGIN;
SET search_path TO carfix;

DELETE FROM reviews WHERE review_id::text LIKE 'feed5ee0%';
DELETE FROM service_bays_bookings WHERE booking_id::text LIKE 'feed0b00%';
DELETE FROM employees_bookings WHERE booking_id::text LIKE 'feed0b00%';
DELETE FROM equipment_bookings WHERE booking_id::text LIKE 'feed0b00%';
DELETE FROM bookings_services WHERE booking_id::text LIKE 'feed0b00%';
DELETE FROM bookings WHERE booking_id::text LIKE 'feed0b00%';

DELETE FROM service_bays_availability WHERE service_bay_id BETWEEN 10000 AND 18999;
DELETE FROM equipment_availability WHERE equipment_id BETWEEN 10000 AND 18999;
DELETE FROM employees_availability WHERE employee_id::text LIKE 'feede3ee%';

DELETE FROM service_employee_requirement_roles WHERE service_employee_requirement_id BETWEEN 10000 AND 18999;
DELETE FROM service_equipment_requirement_types WHERE service_equipment_requirement_id BETWEEN 10000 AND 18999;
DELETE FROM service_employee_requirements WHERE service_employee_requirement_id BETWEEN 10000 AND 18999;
DELETE FROM service_equipment_requirements WHERE service_equipment_requirement_id BETWEEN 10000 AND 18999;
DELETE FROM services_service_bay_types WHERE service_id BETWEEN 10000 AND 18999;

DELETE FROM employees_roles WHERE employee_id::text LIKE 'feede3ee%';
DELETE FROM employees WHERE employee_id::text LIKE 'feede3ee%';
DELETE FROM equipment WHERE equipment_id BETWEEN 10000 AND 18999;
DELETE FROM service_bays WHERE service_bay_id BETWEEN 10000 AND 18999;
DELETE FROM services WHERE service_id BETWEEN 10000 AND 18999;

DELETE FROM car_brands_branches WHERE branch_id::text LIKE 'feedb4a0%';
DELETE FROM opening_hours WHERE branch_id::text LIKE 'feedb4a0%';
DELETE FROM branches WHERE branch_id::text LIKE 'feedb4a0%';
DELETE FROM addresses WHERE address_id BETWEEN 10000 AND 18999;

DELETE FROM car_profiles WHERE car_profile_id::text LIKE 'feedca40%';
DELETE FROM customers WHERE user_id::text LIKE 'feedc0de%';
DELETE FROM owners WHERE user_id::text LIKE 'feed0001%';
DELETE FROM users WHERE user_id::text LIKE 'feedc0de%' OR user_id::text LIKE 'feed0001%';

DELETE FROM model_versions WHERE model_version_id BETWEEN 1001 AND 1120;
DELETE FROM car_models WHERE car_model_id BETWEEN 501 AND 564;
DELETE FROM car_brands WHERE car_brand_id BETWEEN 101 AND 130;
DELETE FROM cities WHERE city_id BETWEEN 101 AND 120;
DELETE FROM regions WHERE region_id BETWEEN 101 AND 110;

COMMIT;
