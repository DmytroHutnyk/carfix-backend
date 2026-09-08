SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- Seed part 3 of 6 — customers and their cars.
--
-- Two customers, because "not my booking" and "not my car" paths need a second
-- account that owns none of the first one's rows.
-- ---------------------------------------------------------------------------

INSERT INTO users (user_id, name, surname, phone_number, ph_country_code, email, password, role, date_of_birth, address_id) VALUES
    ('00000000-0000-4000-8000-000000000002', 'Test', 'User', '600123456', '+48', 'test@gmail.com',
     '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C', 'CUSTOMER', '1995-04-12', NULL),
    ('00000000-0000-4000-8000-000000000004', 'Piotr', 'Lewandowski', '600123457', '+48', 'piotr@carfix.dev',
     '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C', 'CUSTOMER', '1988-11-03', NULL);

INSERT INTO customers (user_id, status) VALUES
    ('00000000-0000-4000-8000-000000000002', 'ACTIVE'),
    ('00000000-0000-4000-8000-000000000004', 'ACTIVE');

-- model_versions.name is not unique across models ('B8 2.0 TDI' exists under both
-- Passat and A4), so lookups join through car_models and car_brands.
INSERT INTO car_profiles (car_profile_id, name, vin, plates, service_certificate_date, insurance_date, customer_id, file_id, model_version_id)
SELECT c.car_profile_id, c.name, c.vin, c.plates, c.service_certificate_date, c.insurance_date,
       c.customer_id, NULL,
       (SELECT mv.model_version_id
        FROM model_versions mv
        JOIN car_models cm ON cm.car_model_id = mv.car_model_id
        JOIN car_brands cb ON cb.car_brand_id = cm.car_brand_id
        WHERE mv.name = c.version_name AND cm.name = c.model_name AND cb.name = c.brand_name)
FROM (VALUES
    ('20000000-0000-4000-8000-000000000001'::uuid, 'Daily Golf',     'WVWZZZAUZLW123456', 'WA 12345',
     DATE '2026-11-20', DATE '2027-03-15', '00000000-0000-4000-8000-000000000002'::uuid, 'Volkswagen', 'Golf',     'Mk8 1.5 TSI'),
    ('20000000-0000-4000-8000-000000000002'::uuid, 'Weekend BMW',    'WBA5R71090FH12345', 'KR 8891A',
     DATE '2027-01-10', DATE '2026-09-01', '00000000-0000-4000-8000-000000000002'::uuid, 'BMW',        '3 Series', 'G20 330i'),
    ('20000000-0000-4000-8000-000000000003'::uuid, 'Family Camry',   NULL,                'WE 4477K',
     NULL,              NULL,              '00000000-0000-4000-8000-000000000002'::uuid, 'Toyota',     'Camry',    'XV70 2.5 Hybrid'),
    ('20000000-0000-4000-8000-000000000004'::uuid, 'Company Passat', 'WVWZZZ3CZKE654321', 'WX 5566B',
     DATE '2026-10-05', DATE '2027-02-28', '00000000-0000-4000-8000-000000000004'::uuid, 'Volkswagen', 'Passat',   'B8 2.0 TDI')
) AS c(car_profile_id, name, vin, plates, service_certificate_date, insurance_date, customer_id, brand_name, model_name, version_name);
