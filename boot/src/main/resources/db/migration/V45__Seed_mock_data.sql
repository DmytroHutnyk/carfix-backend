SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- Geography
-- ---------------------------------------------------------------------------

INSERT INTO countries (iso, name) VALUES ('PL', 'Poland');

INSERT INTO regions (name, countries_iso) VALUES
    ('Mazowieckie',  'PL'),
    ('Malopolskie',  'PL');

INSERT INTO cities (name, region_id) VALUES
    ('Warszawa', (SELECT region_id FROM regions WHERE name = 'Mazowieckie')),
    ('Krakow',   (SELECT region_id FROM regions WHERE name = 'Malopolskie'));


INSERT INTO addresses (street_name, building_number, flat_number, postal_code, city_id) VALUES
    ('Pulawska', '145', NULL, '02-715', (SELECT city_id FROM cities WHERE name = 'Warszawa')),
    ('Wielicka', '28',  NULL, '30-552', (SELECT city_id FROM cities WHERE name = 'Krakow'));

-- ---------------------------------------------------------------------------
-- Users
-- ---------------------------------------------------------------------------

INSERT INTO users (user_id, name, surname, phone_number, ph_country_code, email, password, role, date_of_birth, address_id) VALUES
    ('00000000-0000-4000-8000-000000000001', 'Marek', 'Kowalski', '600100200', '+48', 'owner@carfix.dev',
     '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C', 'OWNER', '1979-02-08', NULL),
    ('00000000-0000-4000-8000-000000000002', 'Test', 'User', '600123456', '+48', 'test@gmail.com',
     '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C', 'CUSTOMER', '1995-04-12', NULL);

INSERT INTO owners (user_id, business_name, vat_in, regon) VALUES
    ('00000000-0000-4000-8000-000000000001', 'AutoSerwis Kowalski', '5252445567', '146892132');

INSERT INTO customers (user_id, status) VALUES
    ('00000000-0000-4000-8000-000000000002', 'ACTIVE');

-- ---------------------------------------------------------------------------
-- Branches
-- ---------------------------------------------------------------------------

INSERT INTO branches (branch_id, name, phone_number, email, status, tz, address_id, owner_id) VALUES
    ('10000000-0000-4000-8000-000000000001', 'AutoSerwis Kowalski Mokotow', '+48221234567',
     'mokotow@autoserwis-kowalski.pl', 'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Pulawska'),
     '00000000-0000-4000-8000-000000000001'),
    ('10000000-0000-4000-8000-000000000002', 'AutoSerwis Kowalski Podgorze', '+48123456789',
     'podgorze@autoserwis-kowalski.pl', 'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Wielicka'),
     '00000000-0000-4000-8000-000000000001');

INSERT INTO opening_hours (day_of_week, start_time, close_time, branch_id)
SELECT d.day, d.opens, d.closes, b.branch_id
FROM (VALUES
    ('MONDAY',    TIME '08:00', TIME '18:00'),
    ('TUESDAY',   TIME '08:00', TIME '18:00'),
    ('WEDNESDAY', TIME '08:00', TIME '18:00'),
    ('THURSDAY',  TIME '08:00', TIME '18:00'),
    ('FRIDAY',    TIME '08:00', TIME '18:00'),
    ('SATURDAY',  TIME '09:00', TIME '14:00')
) AS d(day, opens, closes)
CROSS JOIN (VALUES
    ('10000000-0000-4000-8000-000000000001'::uuid),
    ('10000000-0000-4000-8000-000000000002'::uuid)
) AS b(branch_id);

-- ---------------------------------------------------------------------------
-- Service bays and services
-- ---------------------------------------------------------------------------

INSERT INTO service_bay_types (name) VALUES
    ('Two-post lift'),
    ('Diagnostics station'),
    ('Tyre station');

-- Bay names repeat across branches, so every later lookup filters on branch_id too.
INSERT INTO service_bays (name, status, notes, service_bay_type_id, branch_id)
SELECT t.bay_name, 'ACTIVE', NULL,
       (SELECT service_bay_type_id FROM service_bay_types WHERE name = t.type_name),
       b.branch_id
FROM (VALUES
    ('Bay 1', 'Two-post lift'),
    ('Bay 2', 'Diagnostics station'),
    ('Bay 3', 'Tyre station')
) AS t(bay_name, type_name)
CROSS JOIN (VALUES
    ('10000000-0000-4000-8000-000000000001'::uuid),
    ('10000000-0000-4000-8000-000000000002'::uuid)
) AS b(branch_id);

INSERT INTO service_categories (name) VALUES
    ('Maintenance'),
    ('Diagnostics'),
    ('Tyres'),
    ('Brakes');

INSERT INTO services (name, description, duration_minutes, price, status, service_bay_id, service_category_id)
SELECT s.name, s.description, s.duration_minutes, s.price, 'ACTIVE',
       (SELECT sb.service_bay_id FROM service_bays sb
        WHERE sb.name = s.bay_name AND sb.branch_id = b.branch_id),
       (SELECT service_category_id FROM service_categories WHERE name = s.category_name)
FROM (VALUES
    ('Oil and filter change',    'Engine oil drain, filter swap and level check.',      60::smallint, 249.00, 'Bay 1', 'Maintenance'),
    ('Brake pads replacement',   'Front or rear pad replacement including disc check.', 90::smallint, 459.00, 'Bay 1', 'Brakes'),
    ('Computer diagnostics',     'Full OBD-II scan with fault-code report.',            45::smallint, 199.00, 'Bay 2', 'Diagnostics'),
    ('Air conditioning service', 'Refrigerant refill, leak test and filter change.',    60::smallint, 299.00, 'Bay 2', 'Maintenance'),
    ('Seasonal tyre swap',       'Four-wheel swap with balancing and pressure check.',  40::smallint, 149.00, 'Bay 3', 'Tyres')
) AS s(name, description, duration_minutes, price, bay_name, category_name)
CROSS JOIN (VALUES
    ('10000000-0000-4000-8000-000000000001'::uuid),
    ('10000000-0000-4000-8000-000000000002'::uuid)
) AS b(branch_id);

-- ---------------------------------------------------------------------------
-- Car profiles
-- ---------------------------------------------------------------------------

-- model_versions.name is not unique across models ('B8 2.0 TDI' exists under both
-- Passat and A4), so lookups join through car_models and car_brands.
INSERT INTO car_profiles (car_profile_id, name, vin, plates, service_certificate_date, insurance_date, customer_id, file_id, model_version_id)
SELECT c.car_profile_id, c.name, c.vin, c.plates, c.service_certificate_date, c.insurance_date,
       '00000000-0000-4000-8000-000000000002', NULL,
       (SELECT mv.model_version_id
        FROM model_versions mv
        JOIN car_models cm ON cm.car_model_id = mv.car_model_id
        JOIN car_brands cb ON cb.car_brand_id = cm.car_brand_id
        WHERE mv.name = c.version_name AND cm.name = c.model_name AND cb.name = c.brand_name)
FROM (VALUES
    ('20000000-0000-4000-8000-000000000001'::uuid, 'Daily Golf',  'WVWZZZAUZLW123456', 'WA 12345',
     DATE '2026-11-20', DATE '2027-03-15', 'Volkswagen', 'Golf',     'Mk8 1.5 TSI'),
    ('20000000-0000-4000-8000-000000000002'::uuid, 'Weekend BMW', 'WBA5R71090FH12345', 'KR 8891A',
     DATE '2027-01-10', DATE '2026-09-01', 'BMW',        '3 Series', 'G20 330i'),
    ('20000000-0000-4000-8000-000000000003'::uuid, 'Family Camry', NULL,               'WE 4477K',
     NULL,             NULL,               'Toyota',     'Camry',    'XV70 2.5 Hybrid')
) AS c(car_profile_id, name, vin, plates, service_certificate_date, insurance_date, brand_name, model_name, version_name);

-- ---------------------------------------------------------------------------
-- Bookings
-- ---------------------------------------------------------------------------


INSERT INTO bookings (booking_id, date, status, start_time, end_time, branch_id, car_profile_id) VALUES
    ('30000000-0000-4000-8000-000000000001', CURRENT_DATE - 45, 'COMPLETED',   '09:00', '10:30',
     '10000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001'),
    ('30000000-0000-4000-8000-000000000002', CURRENT_DATE - 12, 'COMPLETED',   '14:00', '15:00',
     '10000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000002'),
    ('30000000-0000-4000-8000-000000000003', CURRENT_DATE - 5,  'CANCELLED',   '11:00', '12:00',
     '10000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000003'),
    ('30000000-0000-4000-8000-000000000004', CURRENT_DATE,      'IN_PROGRESS', '08:00', '12:00',
     '10000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000002'),
    -- Starts today, so the 24h safe-cancellation deadline has already passed.
    ('30000000-0000-4000-8000-000000000005', CURRENT_DATE,      'SCHEDULED',   '17:00', '18:00',
     '10000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001'),
    ('30000000-0000-4000-8000-000000000006', CURRENT_DATE + 9,  'SCHEDULED',   '12:00', '13:30',
     '10000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000003'),
    -- Left without services on purpose: exercises the empty-services / null-total path.
    ('30000000-0000-4000-8000-000000000007', CURRENT_DATE + 21, 'SCHEDULED',   '15:00', '16:00',
     '10000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000001');

INSERT INTO bookings_services (booking_id, service_id)
SELECT l.booking_id,
       (SELECT s.service_id
        FROM services s
        JOIN service_bays sb ON sb.service_bay_id = s.service_bay_id
        WHERE s.name = l.service_name AND sb.branch_id = l.branch_id)
FROM (VALUES
    ('30000000-0000-4000-8000-000000000001'::uuid, '10000000-0000-4000-8000-000000000001'::uuid, 'Oil and filter change'),
    ('30000000-0000-4000-8000-000000000001'::uuid, '10000000-0000-4000-8000-000000000001'::uuid, 'Brake pads replacement'),
    ('30000000-0000-4000-8000-000000000002'::uuid, '10000000-0000-4000-8000-000000000002'::uuid, 'Computer diagnostics'),
    ('30000000-0000-4000-8000-000000000003'::uuid, '10000000-0000-4000-8000-000000000001'::uuid, 'Seasonal tyre swap'),
    ('30000000-0000-4000-8000-000000000004'::uuid, '10000000-0000-4000-8000-000000000001'::uuid, 'Air conditioning service'),
    ('30000000-0000-4000-8000-000000000004'::uuid, '10000000-0000-4000-8000-000000000001'::uuid, 'Computer diagnostics'),
    ('30000000-0000-4000-8000-000000000005'::uuid, '10000000-0000-4000-8000-000000000001'::uuid, 'Oil and filter change'),
    ('30000000-0000-4000-8000-000000000006'::uuid, '10000000-0000-4000-8000-000000000002'::uuid, 'Seasonal tyre swap'),
    ('30000000-0000-4000-8000-000000000006'::uuid, '10000000-0000-4000-8000-000000000002'::uuid, 'Oil and filter change')
) AS l(booking_id, branch_id, service_name);
