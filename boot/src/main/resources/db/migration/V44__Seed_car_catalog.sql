SET search_path TO carfix;

INSERT INTO car_brands (name) VALUES ('Toyota'), ('BMW');

INSERT INTO car_models (name, car_brand_id) VALUES
    ('Camry',    (SELECT car_brand_id FROM car_brands WHERE name = 'Toyota')),
    ('Corolla',  (SELECT car_brand_id FROM car_brands WHERE name = 'Toyota'));

INSERT INTO car_models (name, car_brand_id) VALUES
    ('X5',       (SELECT car_brand_id FROM car_brands WHERE name = 'BMW')),
    ('3 Series', (SELECT car_brand_id FROM car_brands WHERE name = 'BMW'));

INSERT INTO model_generations (name, start_production, end_production, car_model_id) VALUES
    ('2.5 Hybrid', 2018, 2024, (SELECT car_model_id FROM car_models WHERE name = 'Camry')),
    ('3.5 V6',     2012, 2017, (SELECT car_model_id FROM car_models WHERE name = 'Camry'));

INSERT INTO model_generations (name, start_production, end_production, car_model_id) VALUES
    ('1.8 Hybrid', 2019, 2024, (SELECT car_model_id FROM car_models WHERE name = 'Corolla')),
    ('2.0i',       2013, 2018, (SELECT car_model_id FROM car_models WHERE name = 'Corolla'));

INSERT INTO model_generations (name, start_production, end_production, car_model_id) VALUES
    ('xDrive30d M Sport', 2018, 2024, (SELECT car_model_id FROM car_models WHERE name = 'X5')),
    ('xDrive25d',         2013, 2017, (SELECT car_model_id FROM car_models WHERE name = 'X5'));

INSERT INTO model_generations (name, start_production, end_production, car_model_id) VALUES
    ('330i', 2019, 2024, (SELECT car_model_id FROM car_models WHERE name = '3 Series')),
    ('320i', 2012, 2018, (SELECT car_model_id FROM car_models WHERE name = '3 Series'));
