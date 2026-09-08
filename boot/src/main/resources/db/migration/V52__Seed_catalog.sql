SET search_path TO carfix;

-- Seed part 1 of 6 — geography and the curated catalogs.
--
-- The fixture as a whole (V52-V56) stands in for owner resource management
-- (roadmap M7) until that exists: search, slot computation and booking are all
-- exercised against these rows. V57 asserts the fixture is internally
-- consistent, so a typo in any join below fails the migration instead of
-- quietly producing an empty table.
--
-- Catalog tables are curated platform data, not owner-created (spec 2.4,
-- "Why the taxonomies are tables, not enums"), which is why they are seeded
-- here once and referenced by name everywhere else.

INSERT INTO countries (iso, name) VALUES ('PL', 'Poland');

INSERT INTO regions (name, countries_iso) VALUES
    ('Masovian Voivodeship',      'PL'),
    ('Lesser Poland Voivodeship', 'PL');

INSERT INTO cities (name, region_id) VALUES
    ('Warsaw', (SELECT region_id FROM regions WHERE name = 'Masovian Voivodeship')),
    ('Kraków', (SELECT region_id FROM regions WHERE name = 'Lesser Poland Voivodeship'));

-- Service categories — the search dimension of decision 2.8.

INSERT INTO service_categories (name) VALUES
    ('Engine'),
    ('Maintenance'),
    ('Suspension'),
    ('Brakes'),
    ('Tyres'),
    ('Electrical'),
    ('Bodywork'),
    ('Diagnostics');

-- Bay types — a service names the types it accepts (OR); the car occupies one
-- bay for the whole visit.

INSERT INTO service_bay_types (name) VALUES
    ('Two-post lift'),
    ('Four-post lift'),
    ('Diagnostics station'),
    ('Tyre station'),
    ('Bodywork booth');

-- Equipment types — requirement slots list acceptable types, never units.
-- 'Brake lathe' and 'Welding station' are seeded without any unit or
-- requirement referencing them: an unused catalog row must not break anything.

INSERT INTO equipment_types (name) VALUES
    ('OBD scanner'),
    ('Engine hoist'),
    ('Trolley jack'),
    ('Tyre changer'),
    ('Wheel balancer'),
    ('Wheel alignment rig'),
    ('AC service unit'),
    ('Brake lathe'),
    ('Welding station');

-- Roles — the seniority ladder is enumerated per requirement slot, so no
-- role-inheritance table exists (spec 2.4).

INSERT INTO roles (name) VALUES
    ('Senior engine mechanic'),
    ('Engine mechanic'),
    ('Electrician'),
    ('Diagnostician'),
    ('Tyre technician'),
    ('Bodywork technician'),
    ('Apprentice'),
    ('Master technician');
