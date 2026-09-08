SET search_path TO carfix;

-- ---------------------------------------------------------------------------
-- Seed part 2 of 6 — owners, branches and everything a branch owns:
-- bays, equipment units, employees, services and the requirement slots that
-- say what each service needs (spec 2.2/2.3/2.4).
--
-- Seven branches, five in Warsaw and two in Kraków, deliberately unequal:
--
--   MOK  AutoSerwis Kowalski Mokotow   full workshop + bodywork booth
--   WOL  AutoSerwis Kowalski Wola      full workshop
--   POD  AutoSerwis Kowalski Podgorze  full workshop (Kraków)
--   PRA  Opony Express Praga           tyre shop
--   NHU  Opony Express Nowa Huta       tyre shop (Kraków)
--   URY  Diagnostyka Ursynow           diagnostics only
--   URS  Serwis Ursus                  one bay, one mechanic
--
-- The inequality is the point: search must return different branches for
-- different services/categories, and a one-bay branch is the cheapest way to
-- get a fully-booked day (V56) for the availability filter.
--
-- Every account shares one bcrypt hash so any seeded user can be logged in
-- with the same dev password.
-- ---------------------------------------------------------------------------

INSERT INTO users (user_id, name, surname, phone_number, ph_country_code, email, password, role, date_of_birth, address_id) VALUES
    ('00000000-0000-4000-8000-000000000001', 'Marek', 'Kowalski', '600100200', '+48', 'owner@carfix.dev',
     '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C', 'OWNER', '1979-02-08', NULL),
    ('00000000-0000-4000-8000-000000000003', 'Anna', 'Zielinska', '600100201', '+48', 'owner2@carfix.dev',
     '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C', 'OWNER', '1984-06-30', NULL);

INSERT INTO owners (user_id, business_name, vat_in, regon) VALUES
    ('00000000-0000-4000-8000-000000000001', 'AutoSerwis Kowalski', '5252445567', '146892132'),
    ('00000000-0000-4000-8000-000000000003', 'Opony Express',       '6772389012', '351234567');

-- ---------------------------------------------------------------------------
-- Branch addresses. Coordinates are real district-level positions, so distance
-- sorting and an optional radius cut behave sensibly. City/region names are the
-- English strings Google Places returns, which is what search matches on.
-- google_place_id stays NULL: nothing here came from the Places API.
-- ---------------------------------------------------------------------------

INSERT INTO addresses (street_name, building_number, flat_number, postal_code, latitude, longitude, google_place_id, city_id) VALUES
    ('Pulawska',           '145', NULL, '02-715', 52.179300, 21.024600, NULL, (SELECT city_id FROM cities WHERE name = 'Warsaw')),
    ('Wielicka',           '28',  NULL, '30-552', 50.042100, 19.962800, NULL, (SELECT city_id FROM cities WHERE name = 'Kraków')),
    ('Kasprzaka',          '25',  NULL, '01-234', 52.230800, 20.958700, NULL, (SELECT city_id FROM cities WHERE name = 'Warsaw')),
    ('Grochowska',         '210', NULL, '04-077', 52.245100, 21.088900, NULL, (SELECT city_id FROM cities WHERE name = 'Warsaw')),
    ('Bulwarowa',          '15',  NULL, '31-751', 50.075600, 20.036800, NULL, (SELECT city_id FROM cities WHERE name = 'Kraków')),
    ('Pileckiego',         '63',  NULL, '02-781', 52.146900, 21.031400, NULL, (SELECT city_id FROM cities WHERE name = 'Warsaw')),
    ('Gierdziejewskiego',  '7',   NULL, '02-495', 52.196600, 20.877300, NULL, (SELECT city_id FROM cities WHERE name = 'Warsaw'));

INSERT INTO branches (branch_id, name, phone_number, email, status, tz, address_id, owner_id) VALUES
    ('10000000-0000-4000-8000-000000000001', 'AutoSerwis Kowalski Mokotow',  '+48221234567', 'mokotow@autoserwis-kowalski.pl',  'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Pulawska'),          '00000000-0000-4000-8000-000000000001'),
    ('10000000-0000-4000-8000-000000000002', 'AutoSerwis Kowalski Podgorze', '+48123456789', 'podgorze@autoserwis-kowalski.pl', 'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Wielicka'),          '00000000-0000-4000-8000-000000000001'),
    ('10000000-0000-4000-8000-000000000003', 'AutoSerwis Kowalski Wola',     '+48221234568', 'wola@autoserwis-kowalski.pl',     'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Kasprzaka'),         '00000000-0000-4000-8000-000000000001'),
    ('10000000-0000-4000-8000-000000000004', 'Opony Express Praga',          '+48225551020', 'praga@opony-express.pl',          'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Grochowska'),        '00000000-0000-4000-8000-000000000003'),
    ('10000000-0000-4000-8000-000000000005', 'Opony Express Nowa Huta',      '+48125551030', 'nowahuta@opony-express.pl',       'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Bulwarowa'),         '00000000-0000-4000-8000-000000000003'),
    ('10000000-0000-4000-8000-000000000006', 'Diagnostyka Ursynow',          '+48227778090', 'kontakt@diagnostyka-ursynow.pl',  'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Pileckiego'),        '00000000-0000-4000-8000-000000000003'),
    ('10000000-0000-4000-8000-000000000007', 'Serwis Ursus',                 '+48224443311', 'warsztat@serwis-ursus.pl',        'ACTIVE', 'Europe/Warsaw',
     (SELECT address_id FROM addresses WHERE street_name = 'Gierdziejewskiego'), '00000000-0000-4000-8000-000000000003');

-- Tyre shops open longer than the rest; availability rows (V54) are generated
-- from these hours, so the difference propagates to every slot query.
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
    ('10000000-0000-4000-8000-000000000002'::uuid),
    ('10000000-0000-4000-8000-000000000003'::uuid),
    ('10000000-0000-4000-8000-000000000006'::uuid),
    ('10000000-0000-4000-8000-000000000007'::uuid)
) AS b(branch_id);

INSERT INTO opening_hours (day_of_week, start_time, close_time, branch_id)
SELECT d.day, d.opens, d.closes, b.branch_id
FROM (VALUES
    ('MONDAY',    TIME '07:00', TIME '20:00'),
    ('TUESDAY',   TIME '07:00', TIME '20:00'),
    ('WEDNESDAY', TIME '07:00', TIME '20:00'),
    ('THURSDAY',  TIME '07:00', TIME '20:00'),
    ('FRIDAY',    TIME '07:00', TIME '20:00'),
    ('SATURDAY',  TIME '08:00', TIME '16:00')
) AS d(day, opens, closes)
CROSS JOIN (VALUES
    ('10000000-0000-4000-8000-000000000004'::uuid),
    ('10000000-0000-4000-8000-000000000005'::uuid)
) AS b(branch_id);

-- Serviced brands — the car-profile filter in search narrows on this.
-- Ursynow and Ursus deliberately cover only part of the catalog.
WITH branch_key(key, branch_id) AS (VALUES
    ('MOK', '10000000-0000-4000-8000-000000000001'::uuid),
    ('POD', '10000000-0000-4000-8000-000000000002'::uuid),
    ('WOL', '10000000-0000-4000-8000-000000000003'::uuid),
    ('PRA', '10000000-0000-4000-8000-000000000004'::uuid),
    ('NHU', '10000000-0000-4000-8000-000000000005'::uuid),
    ('URY', '10000000-0000-4000-8000-000000000006'::uuid),
    ('URS', '10000000-0000-4000-8000-000000000007'::uuid)
)
INSERT INTO car_brands_branches (car_brand_id, branch_id)
SELECT cb.car_brand_id, bk.branch_id
FROM (VALUES
    ('MOK', 'Toyota'), ('MOK', 'BMW'), ('MOK', 'Volkswagen'), ('MOK', 'Audi'),
    ('WOL', 'Toyota'), ('WOL', 'BMW'), ('WOL', 'Volkswagen'), ('WOL', 'Audi'),
    ('POD', 'Toyota'), ('POD', 'BMW'), ('POD', 'Volkswagen'), ('POD', 'Audi'),
    ('PRA', 'Toyota'), ('PRA', 'BMW'), ('PRA', 'Volkswagen'), ('PRA', 'Audi'),
    ('NHU', 'Toyota'), ('NHU', 'BMW'), ('NHU', 'Volkswagen'), ('NHU', 'Audi'),
    ('URY', 'Volkswagen'), ('URY', 'Audi'),
    ('URS', 'Toyota'), ('URS', 'Volkswagen')
) AS v(branch_key, brand_name)
JOIN branch_key bk ON bk.key = v.branch_key
JOIN car_brands cb ON cb.name = v.brand_name;

-- ---------------------------------------------------------------------------
-- Service bays. Bay names repeat across branches, so every later lookup joins
-- on branch_id as well as name.
-- Podgorze's four-post lift is SUSPENDED: slot computation must skip it while
-- the branch stays bookable through its two-post lifts.
-- ---------------------------------------------------------------------------

WITH branch_key(key, branch_id) AS (VALUES
    ('MOK', '10000000-0000-4000-8000-000000000001'::uuid),
    ('POD', '10000000-0000-4000-8000-000000000002'::uuid),
    ('WOL', '10000000-0000-4000-8000-000000000003'::uuid),
    ('PRA', '10000000-0000-4000-8000-000000000004'::uuid),
    ('NHU', '10000000-0000-4000-8000-000000000005'::uuid),
    ('URY', '10000000-0000-4000-8000-000000000006'::uuid),
    ('URS', '10000000-0000-4000-8000-000000000007'::uuid)
)
INSERT INTO service_bays (name, status, notes, service_bay_type_id, branch_id)
SELECT v.bay_name, v.status, NULL, sbt.service_bay_type_id, bk.branch_id
FROM (VALUES
    ('MOK', 'Bay 1', 'Two-post lift',       'ACTIVE'),
    ('MOK', 'Bay 2', 'Two-post lift',       'ACTIVE'),
    ('MOK', 'Bay 3', 'Diagnostics station', 'ACTIVE'),
    ('MOK', 'Bay 4', 'Tyre station',        'ACTIVE'),
    ('MOK', 'Bay 5', 'Four-post lift',      'ACTIVE'),
    ('MOK', 'Bay 6', 'Bodywork booth',      'ACTIVE'),
    ('WOL', 'Bay 1', 'Two-post lift',       'ACTIVE'),
    ('WOL', 'Bay 2', 'Two-post lift',       'ACTIVE'),
    ('WOL', 'Bay 3', 'Diagnostics station', 'ACTIVE'),
    ('WOL', 'Bay 4', 'Tyre station',        'ACTIVE'),
    ('WOL', 'Bay 5', 'Four-post lift',      'ACTIVE'),
    ('POD', 'Bay 1', 'Two-post lift',       'ACTIVE'),
    ('POD', 'Bay 2', 'Two-post lift',       'ACTIVE'),
    ('POD', 'Bay 3', 'Diagnostics station', 'ACTIVE'),
    ('POD', 'Bay 4', 'Tyre station',        'ACTIVE'),
    ('POD', 'Bay 5', 'Four-post lift',      'SUSPENDED'),
    ('PRA', 'Bay 1', 'Tyre station',        'ACTIVE'),
    ('PRA', 'Bay 2', 'Tyre station',        'ACTIVE'),
    ('PRA', 'Bay 3', 'Two-post lift',       'ACTIVE'),
    ('NHU', 'Bay 1', 'Tyre station',        'ACTIVE'),
    ('NHU', 'Bay 2', 'Tyre station',        'ACTIVE'),
    ('NHU', 'Bay 3', 'Two-post lift',       'ACTIVE'),
    ('URY', 'Bay 1', 'Diagnostics station', 'ACTIVE'),
    ('URY', 'Bay 2', 'Diagnostics station', 'ACTIVE'),
    ('URY', 'Bay 3', 'Two-post lift',       'ACTIVE'),
    ('URS', 'Bay 1', 'Two-post lift',       'ACTIVE')
) AS v(branch_key, bay_name, type_name, status)
JOIN branch_key bk ON bk.key = v.branch_key
JOIN service_bay_types sbt ON sbt.name = v.type_name;

-- ---------------------------------------------------------------------------
-- Equipment units. Unit names are unique per branch only.
-- Two trolley jacks per full workshop on purpose: clutch replacement declares
-- two slots that both accept 'Trolley jack', which is only satisfiable with
-- two distinct units (spec 2.4).
-- Wola's second scanner is SUSPENDED — the branch keeps working on the first.
-- ---------------------------------------------------------------------------

WITH branch_key(key, branch_id) AS (VALUES
    ('MOK', '10000000-0000-4000-8000-000000000001'::uuid),
    ('POD', '10000000-0000-4000-8000-000000000002'::uuid),
    ('WOL', '10000000-0000-4000-8000-000000000003'::uuid),
    ('PRA', '10000000-0000-4000-8000-000000000004'::uuid),
    ('NHU', '10000000-0000-4000-8000-000000000005'::uuid),
    ('URY', '10000000-0000-4000-8000-000000000006'::uuid),
    ('URS', '10000000-0000-4000-8000-000000000007'::uuid)
)
INSERT INTO equipment (name, notes, status, equipment_type_id, branch_id)
SELECT v.unit_name, NULL, v.status, et.equipment_type_id, bk.branch_id
FROM (VALUES
    ('MOK', 'OBD scanner 1',     'OBD scanner',         'ACTIVE'),
    ('MOK', 'OBD scanner 2',     'OBD scanner',         'ACTIVE'),
    ('MOK', 'Engine hoist 1',    'Engine hoist',        'ACTIVE'),
    ('MOK', 'Trolley jack 1',    'Trolley jack',        'ACTIVE'),
    ('MOK', 'Trolley jack 2',    'Trolley jack',        'ACTIVE'),
    ('MOK', 'Tyre changer 1',    'Tyre changer',        'ACTIVE'),
    ('MOK', 'Wheel balancer 1',  'Wheel balancer',      'ACTIVE'),
    ('MOK', 'Alignment rig 1',   'Wheel alignment rig', 'ACTIVE'),
    ('MOK', 'AC service unit 1', 'AC service unit',     'ACTIVE'),
    ('WOL', 'OBD scanner 1',     'OBD scanner',         'ACTIVE'),
    ('WOL', 'OBD scanner 2',     'OBD scanner',         'SUSPENDED'),
    ('WOL', 'Engine hoist 1',    'Engine hoist',        'ACTIVE'),
    ('WOL', 'Trolley jack 1',    'Trolley jack',        'ACTIVE'),
    ('WOL', 'Trolley jack 2',    'Trolley jack',        'ACTIVE'),
    ('WOL', 'Tyre changer 1',    'Tyre changer',        'ACTIVE'),
    ('WOL', 'Wheel balancer 1',  'Wheel balancer',      'ACTIVE'),
    ('WOL', 'Alignment rig 1',   'Wheel alignment rig', 'ACTIVE'),
    ('WOL', 'AC service unit 1', 'AC service unit',     'ACTIVE'),
    ('POD', 'OBD scanner 1',     'OBD scanner',         'ACTIVE'),
    ('POD', 'OBD scanner 2',     'OBD scanner',         'ACTIVE'),
    ('POD', 'Engine hoist 1',    'Engine hoist',        'ACTIVE'),
    ('POD', 'Trolley jack 1',    'Trolley jack',        'ACTIVE'),
    ('POD', 'Trolley jack 2',    'Trolley jack',        'ACTIVE'),
    ('POD', 'Tyre changer 1',    'Tyre changer',        'ACTIVE'),
    ('POD', 'Wheel balancer 1',  'Wheel balancer',      'ACTIVE'),
    ('POD', 'Alignment rig 1',   'Wheel alignment rig', 'ACTIVE'),
    ('POD', 'AC service unit 1', 'AC service unit',     'ACTIVE'),
    ('PRA', 'Tyre changer 1',    'Tyre changer',        'ACTIVE'),
    ('PRA', 'Tyre changer 2',    'Tyre changer',        'ACTIVE'),
    ('PRA', 'Wheel balancer 1',  'Wheel balancer',      'ACTIVE'),
    ('PRA', 'Alignment rig 1',   'Wheel alignment rig', 'ACTIVE'),
    ('NHU', 'Tyre changer 1',    'Tyre changer',        'ACTIVE'),
    ('NHU', 'Tyre changer 2',    'Tyre changer',        'ACTIVE'),
    ('NHU', 'Wheel balancer 1',  'Wheel balancer',      'ACTIVE'),
    ('NHU', 'Alignment rig 1',   'Wheel alignment rig', 'ACTIVE'),
    ('URY', 'OBD scanner 1',     'OBD scanner',         'ACTIVE'),
    ('URY', 'OBD scanner 2',     'OBD scanner',         'ACTIVE'),
    ('URY', 'AC service unit 1', 'AC service unit',     'ACTIVE'),
    ('URS', 'Trolley jack 1',    'Trolley jack',        'ACTIVE')
) AS v(branch_key, unit_name, type_name, status)
JOIN branch_key bk ON bk.key = v.branch_key
JOIN equipment_types et ON et.name = v.type_name;

-- ---------------------------------------------------------------------------
-- Employees. An employee is a user (role EMPLOYEE) plus an employees row.
-- Emails are derived from the name, so they stay unique and short enough for
-- users.email varchar(30).
-- Zofia Ostrowska is SUSPENDED but does get availability rows in V54: the only
-- thing that must keep her out of a booking is the status filter.
-- ---------------------------------------------------------------------------

WITH staff(user_id, first_name, surname, phone, branch_key, role_name, status) AS (VALUES
    ('40000000-0000-4000-8000-000000000001'::uuid, 'Adam',      'Nowak',       '500000001', 'MOK', 'Senior engine mechanic', 'ACTIVE'),
    ('40000000-0000-4000-8000-000000000002'::uuid, 'Jakub',     'Wisniewski',  '500000002', 'MOK', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000003'::uuid, 'Michal',    'Wojcik',      '500000003', 'MOK', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000004'::uuid, 'Ewa',       'Kaminska',    '500000004', 'MOK', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000005'::uuid, 'Tomasz',    'Zajac',       '500000005', 'MOK', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000006'::uuid, 'Kamil',     'Duda',        '500000006', 'MOK', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000007'::uuid, 'Robert',    'Sikora',      '500000007', 'MOK', 'Bodywork technician',    'ACTIVE'),
    ('40000000-0000-4000-8000-000000000008'::uuid, 'Pawel',     'Krawczyk',    '500000008', 'WOL', 'Senior engine mechanic', 'ACTIVE'),
    ('40000000-0000-4000-8000-000000000009'::uuid, 'Marcin',    'Sobczak',     '500000009', 'WOL', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000010'::uuid, 'Lukasz',    'Baran',       '500000010', 'WOL', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000011'::uuid, 'Natalia',   'Wrobel',      '500000011', 'WOL', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000012'::uuid, 'Damian',    'Rutkowski',   '500000012', 'WOL', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000013'::uuid, 'Oskar',     'Pawlak',      '500000013', 'WOL', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000014'::uuid, 'Grzegorz',  'Nowicki',     '500000014', 'POD', 'Senior engine mechanic', 'ACTIVE'),
    ('40000000-0000-4000-8000-000000000015'::uuid, 'Rafal',     'Zawadzki',    '500000015', 'POD', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000016'::uuid, 'Bartosz',   'Mazur',       '500000016', 'POD', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000017'::uuid, 'Karolina',  'Sadowska',    '500000017', 'POD', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000018'::uuid, 'Sebastian', 'Lis',         '500000018', 'POD', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000019'::uuid, 'Igor',      'Cieslak',     '500000019', 'POD', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000020'::uuid, 'Wojciech',  'Kubiak',      '500000020', 'PRA', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000021'::uuid, 'Dawid',     'Gorski',      '500000021', 'PRA', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000022'::uuid, 'Patryk',    'Michalak',    '500000022', 'PRA', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000023'::uuid, 'Julia',     'Adamska',     '500000023', 'PRA', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000024'::uuid, 'Mateusz',   'Szewczyk',    '500000024', 'NHU', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000025'::uuid, 'Konrad',    'Bak',         '500000025', 'NHU', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000026'::uuid, 'Filip',     'Kowalczyk',   '500000026', 'NHU', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000027'::uuid, 'Weronika',  'Sokolowska',  '500000027', 'NHU', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000028'::uuid, 'Artur',     'Jaworski',    '500000028', 'URY', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000029'::uuid, 'Milosz',    'Kaczmarek',   '500000029', 'URY', 'Electrician',            'ACTIVE'),
    ('40000000-0000-4000-8000-000000000030'::uuid, 'Dominik',   'Urban',       '500000030', 'URY', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000031'::uuid, 'Henryk',    'Malinowski',  '500000031', 'URS', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000032'::uuid, 'Zofia',     'Ostrowska',   '500000032', 'MOK', 'Engine mechanic',        'SUSPENDED')
)
INSERT INTO users (user_id, name, surname, phone_number, ph_country_code, email, password, role, date_of_birth, address_id)
SELECT s.user_id, s.first_name, s.surname, s.phone, '+48',
       lower(left(s.first_name, 1) || s.surname) || '@carfix.dev',
       '$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C',
       'EMPLOYEE', NULL, NULL
FROM staff s;

WITH branch_key(key, branch_id) AS (VALUES
    ('MOK', '10000000-0000-4000-8000-000000000001'::uuid),
    ('POD', '10000000-0000-4000-8000-000000000002'::uuid),
    ('WOL', '10000000-0000-4000-8000-000000000003'::uuid),
    ('PRA', '10000000-0000-4000-8000-000000000004'::uuid),
    ('NHU', '10000000-0000-4000-8000-000000000005'::uuid),
    ('URY', '10000000-0000-4000-8000-000000000006'::uuid),
    ('URS', '10000000-0000-4000-8000-000000000007'::uuid)
),
staff(user_id, branch_key, role_name, status) AS (VALUES
    ('40000000-0000-4000-8000-000000000001'::uuid, 'MOK', 'Senior engine mechanic', 'ACTIVE'),
    ('40000000-0000-4000-8000-000000000002'::uuid, 'MOK', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000003'::uuid, 'MOK', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000004'::uuid, 'MOK', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000005'::uuid, 'MOK', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000006'::uuid, 'MOK', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000007'::uuid, 'MOK', 'Bodywork technician',    'ACTIVE'),
    ('40000000-0000-4000-8000-000000000008'::uuid, 'WOL', 'Senior engine mechanic', 'ACTIVE'),
    ('40000000-0000-4000-8000-000000000009'::uuid, 'WOL', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000010'::uuid, 'WOL', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000011'::uuid, 'WOL', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000012'::uuid, 'WOL', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000013'::uuid, 'WOL', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000014'::uuid, 'POD', 'Senior engine mechanic', 'ACTIVE'),
    ('40000000-0000-4000-8000-000000000015'::uuid, 'POD', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000016'::uuid, 'POD', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000017'::uuid, 'POD', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000018'::uuid, 'POD', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000019'::uuid, 'POD', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000020'::uuid, 'PRA', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000021'::uuid, 'PRA', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000022'::uuid, 'PRA', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000023'::uuid, 'PRA', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000024'::uuid, 'NHU', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000025'::uuid, 'NHU', 'Tyre technician',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000026'::uuid, 'NHU', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000027'::uuid, 'NHU', 'Apprentice',             'ACTIVE'),
    ('40000000-0000-4000-8000-000000000028'::uuid, 'URY', 'Diagnostician',          'ACTIVE'),
    ('40000000-0000-4000-8000-000000000029'::uuid, 'URY', 'Electrician',            'ACTIVE'),
    ('40000000-0000-4000-8000-000000000030'::uuid, 'URY', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000031'::uuid, 'URS', 'Engine mechanic',        'ACTIVE'),
    ('40000000-0000-4000-8000-000000000032'::uuid, 'MOK', 'Engine mechanic',        'SUSPENDED')
)
INSERT INTO employees (user_id, status, notes, salary, branch_id)
SELECT s.user_id, s.status, NULL,
       CASE s.role_name
           WHEN 'Senior engine mechanic' THEN 9500.00
           WHEN 'Engine mechanic'        THEN 7200.00
           WHEN 'Electrician'            THEN 7000.00
           WHEN 'Diagnostician'          THEN 7500.00
           WHEN 'Tyre technician'        THEN 6000.00
           WHEN 'Bodywork technician'    THEN 7000.00
           WHEN 'Apprentice'             THEN 4300.00
       END,
       bk.branch_id
FROM staff s
JOIN branch_key bk ON bk.key = s.branch_key;

-- Primary role of every employee, then the second role of the few who hold one.
-- Employees with two roles matter: they can fill slots of either kind, which is
-- exactly what makes the matching non-trivial.
WITH staff(user_id, role_name) AS (VALUES
    ('40000000-0000-4000-8000-000000000001'::uuid, 'Senior engine mechanic'),
    ('40000000-0000-4000-8000-000000000002'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000003'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000004'::uuid, 'Diagnostician'),
    ('40000000-0000-4000-8000-000000000005'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000006'::uuid, 'Apprentice'),
    ('40000000-0000-4000-8000-000000000007'::uuid, 'Bodywork technician'),
    ('40000000-0000-4000-8000-000000000008'::uuid, 'Senior engine mechanic'),
    ('40000000-0000-4000-8000-000000000009'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000010'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000011'::uuid, 'Diagnostician'),
    ('40000000-0000-4000-8000-000000000012'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000013'::uuid, 'Apprentice'),
    ('40000000-0000-4000-8000-000000000014'::uuid, 'Senior engine mechanic'),
    ('40000000-0000-4000-8000-000000000015'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000016'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000017'::uuid, 'Diagnostician'),
    ('40000000-0000-4000-8000-000000000018'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000019'::uuid, 'Apprentice'),
    ('40000000-0000-4000-8000-000000000020'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000021'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000022'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000023'::uuid, 'Apprentice'),
    ('40000000-0000-4000-8000-000000000024'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000025'::uuid, 'Tyre technician'),
    ('40000000-0000-4000-8000-000000000026'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000027'::uuid, 'Apprentice'),
    ('40000000-0000-4000-8000-000000000028'::uuid, 'Diagnostician'),
    ('40000000-0000-4000-8000-000000000029'::uuid, 'Electrician'),
    ('40000000-0000-4000-8000-000000000030'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000031'::uuid, 'Engine mechanic'),
    ('40000000-0000-4000-8000-000000000032'::uuid, 'Engine mechanic'),
    -- second roles
    ('40000000-0000-4000-8000-000000000004'::uuid, 'Electrician'),
    ('40000000-0000-4000-8000-000000000011'::uuid, 'Electrician'),
    ('40000000-0000-4000-8000-000000000017'::uuid, 'Electrician'),
    ('40000000-0000-4000-8000-000000000031'::uuid, 'Master technician')
)
INSERT INTO employees_roles (user_id, role_id)
SELECT s.user_id, r.role_id
FROM staff s
JOIN roles r ON r.name = s.role_name;

-- ---------------------------------------------------------------------------
-- Services. Duration and price are per branch copy; every branch that offers a
-- service gets its own row (services belong to a branch, decision 2.2).
-- Nowa Huta's wheel alignment is SUSPENDED so search has something to exclude.
-- ---------------------------------------------------------------------------

WITH branch_key(key, branch_id) AS (VALUES
    ('MOK', '10000000-0000-4000-8000-000000000001'::uuid),
    ('POD', '10000000-0000-4000-8000-000000000002'::uuid),
    ('WOL', '10000000-0000-4000-8000-000000000003'::uuid),
    ('PRA', '10000000-0000-4000-8000-000000000004'::uuid),
    ('NHU', '10000000-0000-4000-8000-000000000005'::uuid),
    ('URY', '10000000-0000-4000-8000-000000000006'::uuid),
    ('URS', '10000000-0000-4000-8000-000000000007'::uuid)
),
service_catalog(name, description, duration_minutes, price, category_name) AS (VALUES
    ('Oil and filter change',        'Engine oil drain, filter swap and level check.',              60::smallint,  249.00, 'Maintenance'),
    ('Brake pads replacement',       'Front or rear pad replacement including disc check.',         90::smallint,  459.00, 'Brakes'),
    ('Computer diagnostics',         'Full OBD-II scan with a written fault-code report.',          45::smallint,  199.00, 'Diagnostics'),
    ('Air conditioning service',     'Refrigerant refill, leak test and cabin filter change.',      60::smallint,  299.00, 'Maintenance'),
    ('Seasonal tyre swap',           'Four-wheel swap with balancing and pressure check.',          40::smallint,  149.00, 'Tyres'),
    ('Wheel alignment',              'Four-wheel geometry measurement and adjustment.',             75::smallint,  249.00, 'Suspension'),
    ('Shock absorber replacement',   'Axle shock absorber replacement with a road test.',          150::smallint,  890.00, 'Suspension'),
    ('Clutch replacement',           'Clutch kit replacement including gearbox removal.',          300::smallint, 2200.00, 'Engine'),
    ('Engine replacement',           'Full engine swap with fluid refill and test run.',           480::smallint, 6500.00, 'Engine'),
    ('Electrical fault diagnosis',   'Wiring and electrical component fault tracing.',              90::smallint,  259.00, 'Electrical'),
    ('Paint scratch repair',         'Local scratch repair with colour matching and polishing.',   240::smallint, 1200.00, 'Bodywork')
),
offering(branch_key, service_name, status) AS (VALUES
    ('MOK', 'Oil and filter change',      'ACTIVE'),
    ('MOK', 'Brake pads replacement',     'ACTIVE'),
    ('MOK', 'Computer diagnostics',       'ACTIVE'),
    ('MOK', 'Air conditioning service',   'ACTIVE'),
    ('MOK', 'Seasonal tyre swap',         'ACTIVE'),
    ('MOK', 'Wheel alignment',            'ACTIVE'),
    ('MOK', 'Shock absorber replacement', 'ACTIVE'),
    ('MOK', 'Clutch replacement',         'ACTIVE'),
    ('MOK', 'Engine replacement',         'ACTIVE'),
    ('MOK', 'Electrical fault diagnosis', 'ACTIVE'),
    ('MOK', 'Paint scratch repair',       'ACTIVE'),
    ('WOL', 'Oil and filter change',      'ACTIVE'),
    ('WOL', 'Brake pads replacement',     'ACTIVE'),
    ('WOL', 'Computer diagnostics',       'ACTIVE'),
    ('WOL', 'Air conditioning service',   'ACTIVE'),
    ('WOL', 'Seasonal tyre swap',         'ACTIVE'),
    ('WOL', 'Wheel alignment',            'ACTIVE'),
    ('WOL', 'Shock absorber replacement', 'ACTIVE'),
    ('WOL', 'Clutch replacement',         'ACTIVE'),
    ('WOL', 'Engine replacement',         'ACTIVE'),
    ('WOL', 'Electrical fault diagnosis', 'ACTIVE'),
    ('POD', 'Oil and filter change',      'ACTIVE'),
    ('POD', 'Brake pads replacement',     'ACTIVE'),
    ('POD', 'Computer diagnostics',       'ACTIVE'),
    ('POD', 'Air conditioning service',   'ACTIVE'),
    ('POD', 'Seasonal tyre swap',         'ACTIVE'),
    ('POD', 'Wheel alignment',            'ACTIVE'),
    ('POD', 'Shock absorber replacement', 'ACTIVE'),
    ('POD', 'Clutch replacement',         'ACTIVE'),
    ('POD', 'Engine replacement',         'ACTIVE'),
    ('POD', 'Electrical fault diagnosis', 'ACTIVE'),
    ('PRA', 'Oil and filter change',      'ACTIVE'),
    ('PRA', 'Brake pads replacement',     'ACTIVE'),
    ('PRA', 'Seasonal tyre swap',         'ACTIVE'),
    ('PRA', 'Wheel alignment',            'ACTIVE'),
    ('NHU', 'Oil and filter change',      'ACTIVE'),
    ('NHU', 'Brake pads replacement',     'ACTIVE'),
    ('NHU', 'Seasonal tyre swap',         'ACTIVE'),
    ('NHU', 'Wheel alignment',            'SUSPENDED'),
    ('URY', 'Oil and filter change',      'ACTIVE'),
    ('URY', 'Computer diagnostics',       'ACTIVE'),
    ('URY', 'Air conditioning service',   'ACTIVE'),
    ('URY', 'Electrical fault diagnosis', 'ACTIVE'),
    ('URS', 'Oil and filter change',      'ACTIVE'),
    ('URS', 'Brake pads replacement',     'ACTIVE'),
    ('URS', 'Shock absorber replacement', 'ACTIVE')
)
INSERT INTO services (name, description, duration_minutes, price, status, branch_id, service_category_id)
SELECT c.name, c.description, c.duration_minutes, c.price, o.status, bk.branch_id, sc.service_category_id
FROM offering o
JOIN service_catalog c ON c.name = o.service_name
JOIN branch_key bk ON bk.key = o.branch_key
JOIN service_categories sc ON sc.name = c.category_name;

-- Acceptable bay types (OR semantics: any free bay of a listed type will do).
-- Fans out to every branch copy of the service on purpose.
INSERT INTO services_service_bay_types (service_id, service_bay_type_id)
SELECT s.service_id, sbt.service_bay_type_id
FROM (VALUES
    ('Oil and filter change',      'Two-post lift'),
    ('Oil and filter change',      'Four-post lift'),
    ('Brake pads replacement',     'Two-post lift'),
    ('Brake pads replacement',     'Four-post lift'),
    ('Computer diagnostics',       'Diagnostics station'),
    ('Air conditioning service',   'Diagnostics station'),
    ('Air conditioning service',   'Two-post lift'),
    ('Seasonal tyre swap',         'Tyre station'),
    ('Wheel alignment',            'Tyre station'),
    ('Wheel alignment',            'Four-post lift'),
    ('Shock absorber replacement', 'Two-post lift'),
    ('Shock absorber replacement', 'Four-post lift'),
    ('Clutch replacement',         'Two-post lift'),
    ('Engine replacement',         'Two-post lift'),
    ('Electrical fault diagnosis', 'Diagnostics station'),
    ('Paint scratch repair',       'Bodywork booth')
) AS v(service_name, type_name)
JOIN services s ON s.name = v.service_name
JOIN service_bay_types sbt ON sbt.name = v.type_name;

-- ---------------------------------------------------------------------------
-- Employee requirement slots — AND across slots, OR inside a slot (spec 2.4).
--
-- 'Engine replacement' is the greedy trap, present at every full workshop and
-- independent of any date: slot 'Engine specialist' accepts only the branch's
-- single senior mechanic, while 'Assisting mechanic' accepts that same senior
-- OR an engine mechanic. A greedy matcher that fills the assisting slot first
-- takes the senior and then declares the service unfillable; Kuhn's matching
-- finds (specialist = senior, assistant = engine mechanic) every time.
-- ---------------------------------------------------------------------------

INSERT INTO service_employee_requirements (name, service_id)
SELECT v.requirement_name, s.service_id
FROM (VALUES
    ('Oil and filter change',      'Mechanic'),
    ('Brake pads replacement',     'Mechanic'),
    ('Computer diagnostics',       'Diagnostician'),
    ('Air conditioning service',   'Mechanic'),
    ('Seasonal tyre swap',         'Tyre technician'),
    ('Wheel alignment',            'Tyre technician'),
    ('Shock absorber replacement', 'Mechanic'),
    ('Clutch replacement',         'Lead mechanic'),
    ('Clutch replacement',         'Assisting mechanic'),
    ('Engine replacement',         'Engine specialist'),
    ('Engine replacement',         'Assisting mechanic'),
    ('Electrical fault diagnosis', 'Electrician'),
    ('Paint scratch repair',       'Bodywork technician')
) AS v(service_name, requirement_name)
JOIN services s ON s.name = v.service_name;

INSERT INTO service_employee_requirement_roles (service_employee_requirement_id, role_id)
SELECT req.service_employee_requirement_id, r.role_id
FROM (VALUES
    ('Oil and filter change',      'Mechanic',            'Engine mechanic'),
    ('Oil and filter change',      'Mechanic',            'Senior engine mechanic'),
    ('Oil and filter change',      'Mechanic',            'Master technician'),
    ('Brake pads replacement',     'Mechanic',            'Engine mechanic'),
    ('Brake pads replacement',     'Mechanic',            'Senior engine mechanic'),
    ('Brake pads replacement',     'Mechanic',            'Master technician'),
    ('Computer diagnostics',       'Diagnostician',       'Diagnostician'),
    ('Computer diagnostics',       'Diagnostician',       'Electrician'),
    ('Air conditioning service',   'Mechanic',            'Engine mechanic'),
    ('Air conditioning service',   'Mechanic',            'Master technician'),
    ('Seasonal tyre swap',         'Tyre technician',     'Tyre technician'),
    ('Seasonal tyre swap',         'Tyre technician',     'Apprentice'),
    ('Wheel alignment',            'Tyre technician',     'Tyre technician'),
    ('Wheel alignment',            'Tyre technician',     'Master technician'),
    ('Shock absorber replacement', 'Mechanic',            'Engine mechanic'),
    ('Shock absorber replacement', 'Mechanic',            'Senior engine mechanic'),
    ('Shock absorber replacement', 'Mechanic',            'Master technician'),
    ('Clutch replacement',         'Lead mechanic',       'Senior engine mechanic'),
    ('Clutch replacement',         'Lead mechanic',       'Engine mechanic'),
    ('Clutch replacement',         'Assisting mechanic',  'Engine mechanic'),
    ('Clutch replacement',         'Assisting mechanic',  'Apprentice'),
    ('Engine replacement',         'Engine specialist',   'Senior engine mechanic'),
    ('Engine replacement',         'Assisting mechanic',  'Senior engine mechanic'),
    ('Engine replacement',         'Assisting mechanic',  'Engine mechanic'),
    ('Electrical fault diagnosis', 'Electrician',         'Electrician'),
    ('Paint scratch repair',       'Bodywork technician', 'Bodywork technician')
) AS v(service_name, requirement_name, role_name)
JOIN services s ON s.name = v.service_name
JOIN service_employee_requirements req
     ON req.service_id = s.service_id AND req.name = v.requirement_name
JOIN roles r ON r.name = v.role_name;

-- ---------------------------------------------------------------------------
-- Equipment requirement slots. Oil change, brake pads and paint repair declare
-- none — an empty equipment requirement set is legal (spec 2.4).
-- Clutch replacement declares two slots that both accept 'Trolley jack':
-- distinctness is what forces two units.
-- ---------------------------------------------------------------------------

INSERT INTO service_equipment_requirements (name, service_id)
SELECT v.requirement_name, s.service_id
FROM (VALUES
    ('Computer diagnostics',       'Scanner'),
    ('Air conditioning service',   'AC unit'),
    ('Seasonal tyre swap',         'Tyre changer'),
    ('Seasonal tyre swap',         'Balancer'),
    ('Wheel alignment',            'Alignment rig'),
    ('Shock absorber replacement', 'Jack'),
    ('Clutch replacement',         'Front jack'),
    ('Clutch replacement',         'Rear jack'),
    ('Engine replacement',         'Hoist'),
    ('Engine replacement',         'Jack'),
    ('Electrical fault diagnosis', 'Scanner')
) AS v(service_name, requirement_name)
JOIN services s ON s.name = v.service_name;

INSERT INTO service_equipment_requirement_types (service_equipment_requirement_id, equipment_type_id)
SELECT req.service_equipment_requirement_id, et.equipment_type_id
FROM (VALUES
    ('Computer diagnostics',       'Scanner',       'OBD scanner'),
    ('Air conditioning service',   'AC unit',       'AC service unit'),
    ('Seasonal tyre swap',         'Tyre changer',  'Tyre changer'),
    ('Seasonal tyre swap',         'Balancer',      'Wheel balancer'),
    ('Wheel alignment',            'Alignment rig', 'Wheel alignment rig'),
    ('Shock absorber replacement', 'Jack',          'Trolley jack'),
    ('Clutch replacement',         'Front jack',    'Trolley jack'),
    ('Clutch replacement',         'Rear jack',     'Trolley jack'),
    ('Engine replacement',         'Hoist',         'Engine hoist'),
    ('Engine replacement',         'Jack',          'Trolley jack'),
    ('Electrical fault diagnosis', 'Scanner',       'OBD scanner')
) AS v(service_name, requirement_name, type_name)
JOIN services s ON s.name = v.service_name
JOIN service_equipment_requirements req
     ON req.service_id = s.service_id AND req.name = v.requirement_name
JOIN equipment_types et ON et.name = v.type_name;
