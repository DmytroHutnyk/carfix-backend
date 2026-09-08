BEGIN;
SET search_path TO carfix;

INSERT INTO users (user_id, name, surname, phone_number, ph_country_code, email, password, role, date_of_birth, preferred_city_id, email_verified_at) VALUES
('feed0001-0000-4000-8000-000000000001','Krzysztof','Wiśniewski','700000001','+48','k.wisniewski1@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','OWNER',NULL,1,TIMESTAMPTZ '2026-05-01 10:00:00+02'),
('feed0001-0000-4000-8000-000000000002','Magdalena','Kowalska','700000002','+48','m.kowalska2@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','OWNER',NULL,2,TIMESTAMPTZ '2026-05-01 10:00:00+02'),
('feed0001-0000-4000-8000-000000000003','Tomasz','Zieliński','700000003','+48','t.zielinski3@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','OWNER',NULL,101,TIMESTAMPTZ '2026-05-01 10:00:00+02'),
('feed0001-0000-4000-8000-000000000004','Anna','Lewandowska','700000004','+48','a.lewandowska4@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','OWNER',NULL,102,TIMESTAMPTZ '2026-05-01 10:00:00+02');

INSERT INTO owners (user_id, business_name, vat_in, regon) VALUES
('feed0001-0000-4000-8000-000000000001','Auto Serwis Wiśniewski','1000000001','100000001'),
('feed0001-0000-4000-8000-000000000002','Kowalska Motors','1000000002','100000002'),
('feed0001-0000-4000-8000-000000000003','Zieliński Auto Serwis','1000000003','100000003'),
('feed0001-0000-4000-8000-000000000004','Lewandowska Car Care','1000000004','100000004');

INSERT INTO users (user_id, name, surname, phone_number, ph_country_code, email, password, role, date_of_birth, preferred_city_id, email_verified_at) VALUES
('feedc0de-0000-4000-8000-000000000001','Jan','Kowalski','800000001','+48','cust01@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1970-03-15',1,TIMESTAMPTZ '2026-01-05 08:00:00+01'),
('feedc0de-0000-4000-8000-000000000002','Piotr','Nowak','800000002','+48','cust02@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1985-07-22',2,TIMESTAMPTZ '2026-01-10 09:15:00+01'),
('feedc0de-0000-4000-8000-000000000003','Anna','Wójcik','800000003','+48','cust03@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1992-11-05',101,TIMESTAMPTZ '2026-01-15 11:30:00+01'),
('feedc0de-0000-4000-8000-000000000004','Katarzyna','Kowalczyk','800000004','+48','cust04@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1978-01-30',102,TIMESTAMPTZ '2026-02-01 07:45:00+01'),
('feedc0de-0000-4000-8000-000000000005','Michał','Kamiński','800000005','+48','cust05@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','2000-09-12',103,TIMESTAMPTZ '2026-02-05 12:00:00+01'),
('feedc0de-0000-4000-8000-000000000006','Agnieszka','Lewandowska','800000006','+48','cust06@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1965-04-18',104,TIMESTAMPTZ '2026-02-10 14:20:00+01'),
('feedc0de-0000-4000-8000-000000000007','Tomasz','Dąbrowski','800000007','+48','cust07@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1995-06-25',105,TIMESTAMPTZ '2026-02-15 16:00:00+01'),
('feedc0de-0000-4000-8000-000000000008','Ewa','Zielińska','800000008','+48','cust08@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1988-12-01',106,TIMESTAMPTZ '2026-03-01 08:30:00+01'),
('feedc0de-0000-4000-8000-000000000009','Paweł','Szymański','800000009','+48','cust09@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','2004-02-14',107,TIMESTAMPTZ '2026-03-05 09:00:00+01'),
('feedc0de-0000-4000-8000-000000000010','Joanna','Woźniak','800000010','+48','cust10@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1973-08-09',108,TIMESTAMPTZ '2026-03-10 10:10:00+01'),
('feedc0de-0000-4000-8000-000000000011','Marcin','Kozłowski','800000011','+48','cust11@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1990-10-20',109,TIMESTAMPTZ '2026-03-15 11:11:00+01'),
('feedc0de-0000-4000-8000-000000000012','Monika','Jankowska','800000012','+48','cust12@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1982-05-03',110,TIMESTAMPTZ '2026-04-01 08:00:00+02'),
('feedc0de-0000-4000-8000-000000000013','Grzegorz','Mazur','800000013','+48','cust13@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1967-07-07',111,TIMESTAMPTZ '2026-04-05 09:00:00+02'),
('feedc0de-0000-4000-8000-000000000014','Karolina','Krawczyk','800000014','+48','cust14@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1999-03-28',112,TIMESTAMPTZ '2026-04-10 10:00:00+02'),
('feedc0de-0000-4000-8000-000000000015','Adam','Piotrowski','800000015','+48','cust15@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1976-11-11',113,TIMESTAMPTZ '2026-04-15 11:00:00+02'),
('feedc0de-0000-4000-8000-000000000016','Natalia','Grabowska','800000016','+48','cust16@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','2001-01-19',114,TIMESTAMPTZ '2026-04-20 12:00:00+02'),
('feedc0de-0000-4000-8000-000000000017','Rafał','Nowakowski','800000017','+48','cust17@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1984-09-06',115,NULL),
('feedc0de-0000-4000-8000-000000000018','Aleksandra','Pawłowska','800000018','+48','cust18@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1993-04-23',116,NULL),
('feedc0de-0000-4000-8000-000000000019','Damian','Michalski','800000019','+48','cust19@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1969-12-30',117,NULL),
('feedc0de-0000-4000-8000-000000000020','Justyna','Adamczyk','800000020','+48','cust20@carfix.dev','$2a$10$gugdEEm0A42XC7YVHMyiduqXiiqGWUHXPvtrCjlaqlDnZJwVgER3C','CUSTOMER','1997-06-15',118,NULL);

INSERT INTO customers (user_id, status) VALUES
('feedc0de-0000-4000-8000-000000000001','ACTIVE'),
('feedc0de-0000-4000-8000-000000000002','ACTIVE'),
('feedc0de-0000-4000-8000-000000000003','ACTIVE'),
('feedc0de-0000-4000-8000-000000000004','ACTIVE'),
('feedc0de-0000-4000-8000-000000000005','ACTIVE'),
('feedc0de-0000-4000-8000-000000000006','ACTIVE'),
('feedc0de-0000-4000-8000-000000000007','ACTIVE'),
('feedc0de-0000-4000-8000-000000000008','ACTIVE'),
('feedc0de-0000-4000-8000-000000000009','ACTIVE'),
('feedc0de-0000-4000-8000-000000000010','ACTIVE'),
('feedc0de-0000-4000-8000-000000000011','ACTIVE'),
('feedc0de-0000-4000-8000-000000000012','ACTIVE'),
('feedc0de-0000-4000-8000-000000000013','ACTIVE'),
('feedc0de-0000-4000-8000-000000000014','ACTIVE'),
('feedc0de-0000-4000-8000-000000000015','ACTIVE'),
('feedc0de-0000-4000-8000-000000000016','ACTIVE'),
('feedc0de-0000-4000-8000-000000000017','ACTIVE'),
('feedc0de-0000-4000-8000-000000000018','ACTIVE'),
('feedc0de-0000-4000-8000-000000000019','ACTIVE'),
('feedc0de-0000-4000-8000-000000000020','SUSPENDED');

INSERT INTO car_profiles (car_profile_id, name, vin, plates, service_certificate_date, insurance_date, customer_id, model_version_id)
SELECT
  ('feedca40-0000-4000-8000-' || lpad(n::text,12,'0'))::uuid,
  CASE WHEN n % 3 = 0 THEN 'Family car'
       WHEN n % 3 = 1 THEN 'Daily driver ' || n
       ELSE 'Weekend car ' || n END,
  CASE WHEN n <= 38 THEN upper(substr(md5('carfixvin' || n::text),1,17)) ELSE NULL END,
  CASE WHEN n % 2 = 0 THEN 'WA' || lpad(n::text,4,'0') ELSE 'KR' || lpad((n*3)::text,4,'0') END,
  CASE WHEN n % 2 = 0 THEN (DATE '2026-01-01' + (n || ' days')::interval)::date ELSE NULL END,
  CASE WHEN n % 2 = 1 THEN (DATE '2027-01-01' + (n || ' days')::interval)::date ELSE NULL END,
  ('feedc0de-0000-4000-8000-' || lpad((((n-1) % 20) + 1)::text,12,'0'))::uuid,
  1001 + ((n*7) % 64)
FROM generate_series(1,45) AS n;

COMMIT;
