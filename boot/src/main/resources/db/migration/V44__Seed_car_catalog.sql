SET search_path TO carfix;

INSERT INTO car_brands (name) VALUES ('Toyota'), ('BMW'), ('Volkswagen'), ('Audi');

INSERT INTO car_models (name, car_brand_id) VALUES
    ('Camry',    (SELECT car_brand_id FROM car_brands WHERE name = 'Toyota')),
    ('Corolla',  (SELECT car_brand_id FROM car_brands WHERE name = 'Toyota'));

INSERT INTO car_models (name, car_brand_id) VALUES
    ('X5',       (SELECT car_brand_id FROM car_brands WHERE name = 'BMW')),
    ('3 Series', (SELECT car_brand_id FROM car_brands WHERE name = 'BMW'));

INSERT INTO car_models (name, car_brand_id) VALUES
    ('Golf',     (SELECT car_brand_id FROM car_brands WHERE name = 'Volkswagen')),
    ('Passat',   (SELECT car_brand_id FROM car_brands WHERE name = 'Volkswagen'));

INSERT INTO car_models (name, car_brand_id) VALUES
    ('A4',       (SELECT car_brand_id FROM car_brands WHERE name = 'Audi')),
    ('A6',       (SELECT car_brand_id FROM car_brands WHERE name = 'Audi'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('XV80 2.5 Hybrid', 2024, NULL, (SELECT car_model_id FROM car_models WHERE name = 'Camry')),
    ('XV70 2.5 Hybrid', 2018, 2024, (SELECT car_model_id FROM car_models WHERE name = 'Camry')),
    ('XV50 3.5 V6',     2012, 2017, (SELECT car_model_id FROM car_models WHERE name = 'Camry')),
    ('XV40 2.4',        2006, 2011, (SELECT car_model_id FROM car_models WHERE name = 'Camry'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('E210 1.8 Hybrid', 2019, 2024, (SELECT car_model_id FROM car_models WHERE name = 'Corolla')),
    ('E210 2.0 Hybrid', 2019, 2024, (SELECT car_model_id FROM car_models WHERE name = 'Corolla')),
    ('E170 2.0i',       2013, 2018, (SELECT car_model_id FROM car_models WHERE name = 'Corolla')),
    ('E150 1.6 VVT-i',  2007, 2012, (SELECT car_model_id FROM car_models WHERE name = 'Corolla'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('G05 xDrive30d M Sport', 2018, 2024, (SELECT car_model_id FROM car_models WHERE name = 'X5')),
    ('G05 xDrive40i',         2018, 2024, (SELECT car_model_id FROM car_models WHERE name = 'X5')),
    ('F15 xDrive25d',         2013, 2017, (SELECT car_model_id FROM car_models WHERE name = 'X5')),
    ('E70 3.0d',              2007, 2013, (SELECT car_model_id FROM car_models WHERE name = 'X5'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('G20 330i', 2019, 2024, (SELECT car_model_id FROM car_models WHERE name = '3 Series')),
    ('G20 320d', 2019, 2024, (SELECT car_model_id FROM car_models WHERE name = '3 Series')),
    ('F30 320i', 2012, 2018, (SELECT car_model_id FROM car_models WHERE name = '3 Series')),
    ('E90 325i', 2005, 2011, (SELECT car_model_id FROM car_models WHERE name = '3 Series'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('Mk8 1.5 TSI',  2019, NULL, (SELECT car_model_id FROM car_models WHERE name = 'Golf')),
    ('Mk7 2.0 TDI',  2012, 2019, (SELECT car_model_id FROM car_models WHERE name = 'Golf')),
    ('Mk6 1.4 TSI',  2008, 2012, (SELECT car_model_id FROM car_models WHERE name = 'Golf'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('B8 2.0 TDI',  2014, 2023, (SELECT car_model_id FROM car_models WHERE name = 'Passat')),
    ('B8 1.4 TSI',  2014, 2023, (SELECT car_model_id FROM car_models WHERE name = 'Passat')),
    ('B7 1.8 TSI',  2010, 2014, (SELECT car_model_id FROM car_models WHERE name = 'Passat'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('B9 2.0 TFSI', 2015, NULL, (SELECT car_model_id FROM car_models WHERE name = 'A4')),
    ('B9 2.0 TDI',  2015, NULL, (SELECT car_model_id FROM car_models WHERE name = 'A4')),
    ('B8 2.0 TDI',  2007, 2015, (SELECT car_model_id FROM car_models WHERE name = 'A4'));

INSERT INTO model_versions (name, start_production, end_production, car_model_id) VALUES
    ('C8 3.0 TDI quattro', 2018, NULL, (SELECT car_model_id FROM car_models WHERE name = 'A6')),
    ('C8 2.0 TFSI',        2018, NULL, (SELECT car_model_id FROM car_models WHERE name = 'A6')),
    ('C7 2.0 TDI',         2011, 2018, (SELECT car_model_id FROM car_models WHERE name = 'A6'));
