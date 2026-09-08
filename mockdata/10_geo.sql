BEGIN;
SET search_path TO carfix;
INSERT INTO regions (region_id, name, countries_iso) VALUES
(101, 'Silesian Voivodeship', 'PL'),
(102, 'Greater Poland Voivodeship', 'PL'),
(103, 'Lower Silesian Voivodeship', 'PL'),
(104, 'Pomeranian Voivodeship', 'PL'),
(105, 'Łódź Voivodeship', 'PL'),
(106, 'West Pomeranian Voivodeship', 'PL'),
(107, 'Lublin Voivodeship', 'PL'),
(108, 'Kuyavian-Pomeranian Voivodeship', 'PL'),
(109, 'Subcarpathian Voivodeship', 'PL'),
(110, 'Warmian-Masurian Voivodeship', 'PL');
INSERT INTO cities (city_id, name, region_id, latitude, longitude) VALUES
(101, 'Łódź', 105, 51.7592, 19.4560),
(102, 'Wrocław', 103, 51.1079, 17.0385),
(103, 'Poznań', 102, 52.4064, 16.9252),
(104, 'Gdańsk', 104, 54.3520, 18.6466),
(105, 'Szczecin', 106, 53.4285, 14.5528),
(106, 'Bydgoszcz', 108, 53.1235, 18.0084),
(107, 'Lublin', 107, 51.2465, 22.5684),
(108, 'Katowice', 101, 50.2649, 19.0238),
(109, 'Gdynia', 104, 54.5189, 18.5305),
(110, 'Częstochowa', 101, 50.8118, 19.1203),
(111, 'Radom', 1, 51.4027, 21.1471),
(112, 'Rzeszów', 109, 50.0413, 21.9990),
(113, 'Toruń', 108, 53.0138, 18.5984),
(114, 'Gliwice', 101, 50.2945, 18.6714),
(115, 'Olsztyn', 110, 53.7784, 20.4801),
(116, 'Sopot', 104, 54.4418, 18.5601),
(117, 'Płock', 1, 52.5468, 19.7064),
(118, 'Tarnów', 2, 50.0121, 20.9858),
(119, 'Zabrze', 101, 50.3249, 18.7857),
(120, 'Elbląg', 110, 54.1522, 19.4088);
COMMIT;
