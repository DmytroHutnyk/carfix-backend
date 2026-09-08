BEGIN;
SET search_path TO carfix;

INSERT INTO addresses(address_id,street_name,building_number,postal_code,latitude,longitude,city_id) VALUES (16000,'ul. Wielicka','12A','30-552',50.0397,19.9591,2);
INSERT INTO branches(branch_id,name,phone_number,email,status,tz,address_id,owner_id,description,cancellation_policy) VALUES ('feedb4a0-0000-4000-8000-000000000007','Premium Motors Kraków','+48121000007','krakow@carfix.dev','ACTIVE','Europe/Warsaw',16000,'feed0001-0000-4000-8000-000000000004','Warsztat premium w centrum Krakowa obsługujący wszystkie marki europejskie.','STRICT');
INSERT INTO opening_hours(day_of_week,start_time,close_time,branch_id,mode) VALUES
  ('MONDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000007','OPEN'),
  ('TUESDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000007','OPEN'),
  ('WEDNESDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000007','OPEN'),
  ('THURSDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000007','OPEN'),
  ('FRIDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000007','OPEN'),
  ('SATURDAY',time '09:00',time '14:00','feedb4a0-0000-4000-8000-000000000007','OPEN');
INSERT INTO car_brands_branches(car_brand_id,branch_id) VALUES
  (1, 'feedb4a0-0000-4000-8000-000000000007'),
  (2, 'feedb4a0-0000-4000-8000-000000000007'),
  (3, 'feedb4a0-0000-4000-8000-000000000007'),
  (4, 'feedb4a0-0000-4000-8000-000000000007'),
  (101, 'feedb4a0-0000-4000-8000-000000000007'),
  (105, 'feedb4a0-0000-4000-8000-000000000007'),
  (110, 'feedb4a0-0000-4000-8000-000000000007'),
  (115, 'feedb4a0-0000-4000-8000-000000000007');
INSERT INTO services(service_id,name,description,duration_minutes,price,status,branch_id,service_category_id) VALUES
  (16100,'Oil & filter change','Standardowa wymiana oleju silnikowego wraz z filtrem.',45,199,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',2),
  (16101,'Full service inspection','Kompleksowy przegląd techniczny pojazdu.',120,499,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',2),
  (16102,'Front brake pads replacement','Wymiana klocków hamulcowych na osi przedniej.',90,449,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',4),
  (16103,'Brake fluid flush','Wymiana i odpowietrzenie płynu hamulcowego.',60,249,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',4),
  (16104,'Shock absorber replacement','Wymiana amortyzatorów przód lub tył.',120,899,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',3),
  (16105,'Seasonal tyre change','Sezonowa wymiana opon na felgach.',45,149,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',5),
  (16106,'Wheel alignment','Ustawienie geometrii kół.',90,349,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',5),
  (16107,'Timing belt replacement','Wymiana paska rozrządu wraz z osprzętem.',180,1499,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',1),
  (16108,'Battery replacement','Wymiana akumulatora i test instalacji.',30,159,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',6),
  (16109,'Alternator diagnostics & repair','Diagnostyka i naprawa alternatora.',120,699,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',6),
  (16110,'Computer diagnostics','Kompleksowa diagnostyka komputerowa pojazdu.',60,249,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',8),
  (16111,'Dent & paint repair','Naprawa wgnieceń i lakierowanie elementu nadwozia.',180,1299,'ACTIVE','feedb4a0-0000-4000-8000-000000000007',7);
INSERT INTO services_service_bay_types(service_id,service_bay_type_id) VALUES
  (16100,1),
  (16101,1),
  (16102,1),
  (16103,1),
  (16104,1),
  (16105,4),
  (16106,4),
  (16107,1),
  (16108,3),
  (16109,3),
  (16110,3),
  (16111,5);
INSERT INTO service_bays(service_bay_id,name,status,service_bay_type_id,branch_id) VALUES
  (16200,'Lift bay 1','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000007'),
  (16201,'Lift bay 2','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000007'),
  (16202,'Diagnostics bay','ACTIVE',3,'feedb4a0-0000-4000-8000-000000000007'),
  (16203,'Tyre bay','ACTIVE',4,'feedb4a0-0000-4000-8000-000000000007'),
  (16204,'Bodywork bay','ACTIVE',5,'feedb4a0-0000-4000-8000-000000000007');
INSERT INTO equipment(equipment_id,name,status,equipment_type_id,branch_id) VALUES
  (16300,'OBD scanner','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000007'),
  (16301,'Engine hoist','ACTIVE',2,'feedb4a0-0000-4000-8000-000000000007'),
  (16302,'Tyre changer','ACTIVE',4,'feedb4a0-0000-4000-8000-000000000007'),
  (16303,'Wheel alignment rig','ACTIVE',6,'feedb4a0-0000-4000-8000-000000000007'),
  (16304,'Brake lathe','ACTIVE',8,'feedb4a0-0000-4000-8000-000000000007'),
  (16305,'Welding station','ACTIVE',9,'feedb4a0-0000-4000-8000-000000000007');
INSERT INTO employees(employee_id,status,salary,branch_id,first_name,last_name,user_id) VALUES
  ('feede3ee-0000-4000-8000-000000000701'::uuid,'ACTIVE',8200,'feedb4a0-0000-4000-8000-000000000007','Tomasz','Nowak',NULL),
  ('feede3ee-0000-4000-8000-000000000702'::uuid,'ACTIVE',6300,'feedb4a0-0000-4000-8000-000000000007','Piotr','Kowalski',NULL),
  ('feede3ee-0000-4000-8000-000000000703'::uuid,'ACTIVE',6100,'feedb4a0-0000-4000-8000-000000000007','Adam','Wiśniewski',NULL),
  ('feede3ee-0000-4000-8000-000000000704'::uuid,'ACTIVE',5200,'feedb4a0-0000-4000-8000-000000000007','Marek','Zieliński',NULL),
  ('feede3ee-0000-4000-8000-000000000705'::uuid,'ACTIVE',5800,'feedb4a0-0000-4000-8000-000000000007','Karol','Lewandowski',NULL),
  ('feede3ee-0000-4000-8000-000000000706'::uuid,'ACTIVE',4700,'feedb4a0-0000-4000-8000-000000000007','Bartosz','Kamiński',NULL);
INSERT INTO employees_roles(employee_id,role_id) VALUES
  ('feede3ee-0000-4000-8000-000000000701'::uuid,1),
  ('feede3ee-0000-4000-8000-000000000701'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000702'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000702'::uuid,4),
  ('feede3ee-0000-4000-8000-000000000703'::uuid,3),
  ('feede3ee-0000-4000-8000-000000000703'::uuid,4),
  ('feede3ee-0000-4000-8000-000000000704'::uuid,5),
  ('feede3ee-0000-4000-8000-000000000705'::uuid,6),
  ('feede3ee-0000-4000-8000-000000000706'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000706'::uuid,8);
INSERT INTO service_employee_requirements(service_employee_requirement_id,name,service_id) VALUES
  (16400,'Technician',16100),
  (16401,'Technician',16101),
  (16402,'Technician',16102),
  (16403,'Technician',16103),
  (16404,'Technician',16104),
  (16405,'Technician',16105),
  (16406,'Technician',16106),
  (16407,'Technician',16107),
  (16408,'Technician',16108),
  (16409,'Technician',16109),
  (16410,'Technician',16110),
  (16411,'Technician',16111);
INSERT INTO service_employee_requirement_roles(service_employee_requirement_id,role_id) VALUES
  (16400,2),
  (16401,2),
  (16402,2),
  (16403,2),
  (16404,2),
  (16405,5),
  (16406,5),
  (16407,1),
  (16408,3),
  (16409,3),
  (16410,4),
  (16411,6);
INSERT INTO service_equipment_requirements(service_equipment_requirement_id,name,service_id) VALUES
  (16500,'Equipment',16102),
  (16501,'Equipment',16105),
  (16502,'Equipment',16106),
  (16503,'Equipment',16107),
  (16504,'Equipment',16109),
  (16505,'Equipment',16110),
  (16506,'Equipment',16111);
INSERT INTO service_equipment_requirement_types(service_equipment_requirement_id,equipment_type_id) VALUES
  (16500,8),
  (16501,4),
  (16502,6),
  (16503,2),
  (16504,1),
  (16505,1),
  (16506,9);
INSERT INTO service_bays_availability(available_time,date,service_bay_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (16200),(16201),(16202),(16203),(16204)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO service_bays_availability(available_time,date,service_bay_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (16200),(16201),(16202),(16203),(16204)) v(id) WHERE extract(dow from d)=6;
INSERT INTO employees_availability(available_time,date,employee_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES ('feede3ee-0000-4000-8000-000000000701'::uuid),('feede3ee-0000-4000-8000-000000000702'::uuid),('feede3ee-0000-4000-8000-000000000703'::uuid),('feede3ee-0000-4000-8000-000000000704'::uuid),('feede3ee-0000-4000-8000-000000000705'::uuid),('feede3ee-0000-4000-8000-000000000706'::uuid)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO employees_availability(available_time,date,employee_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES ('feede3ee-0000-4000-8000-000000000701'::uuid),('feede3ee-0000-4000-8000-000000000702'::uuid),('feede3ee-0000-4000-8000-000000000703'::uuid),('feede3ee-0000-4000-8000-000000000704'::uuid),('feede3ee-0000-4000-8000-000000000705'::uuid),('feede3ee-0000-4000-8000-000000000706'::uuid)) v(id) WHERE extract(dow from d)=6;
INSERT INTO equipment_availability(available_time,date,equipment_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (16300),(16301),(16302),(16303),(16304),(16305)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO equipment_availability(available_time,date,equipment_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (16300),(16301),(16302),(16303),(16304),(16305)) v(id) WHERE extract(dow from d)=6;
INSERT INTO bookings(booking_id,date,status,start_time,end_time,branch_id,car_profile_id,created_at) VALUES
  ('feed0b00-0000-4000-8000-000000007001'::uuid,DATE '2026-08-12','COMPLETED',time '08:00',time '08:45','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000040',(DATE '2026-08-12' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007002'::uuid,DATE '2026-08-14','COMPLETED',time '10:00',time '12:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000041',(DATE '2026-08-14' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007003'::uuid,DATE '2026-08-18','COMPLETED',time '12:00',time '13:30','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000042',(DATE '2026-08-18' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007004'::uuid,DATE '2026-08-20','COMPLETED',time '14:00',time '15:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000043',(DATE '2026-08-20' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007005'::uuid,DATE '2026-08-24','COMPLETED',time '08:00',time '10:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000044',(DATE '2026-08-24' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007006'::uuid,DATE '2026-08-27','CANCELLED',time '10:00',time '10:45','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000045',(DATE '2026-08-27' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007007'::uuid,DATE '2026-08-31','COMPLETED',time '12:00',time '13:30','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000031',(DATE '2026-08-31' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007008'::uuid,DATE '2026-09-02','NO_SHOW',time '14:00',time '17:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000032',(DATE '2026-09-02' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007009'::uuid,DATE '2026-09-04','COMPLETED',time '08:00',time '08:30','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000033',(DATE '2026-09-04' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007010'::uuid,DATE '2026-09-11','SCHEDULED',time '10:00',time '12:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000034',(DATE '2026-09-11' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007011'::uuid,DATE '2026-09-18','SCHEDULED',time '12:00',time '13:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000035',(DATE '2026-09-18' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000007012'::uuid,DATE '2026-09-25','SCHEDULED',time '14:00',time '17:00','feedb4a0-0000-4000-8000-000000000007','feedca40-0000-4000-8000-000000000036',(DATE '2026-09-25' - interval '5 days'));
INSERT INTO bookings_services(booking_id,service_id,start_time,end_time,price) VALUES
  ('feed0b00-0000-4000-8000-000000007001'::uuid,16100,time '08:00',time '08:45',199),
  ('feed0b00-0000-4000-8000-000000007002'::uuid,16101,time '10:00',time '12:00',499),
  ('feed0b00-0000-4000-8000-000000007003'::uuid,16102,time '12:00',time '13:30',449),
  ('feed0b00-0000-4000-8000-000000007004'::uuid,16103,time '14:00',time '15:00',249),
  ('feed0b00-0000-4000-8000-000000007005'::uuid,16104,time '08:00',time '10:00',899),
  ('feed0b00-0000-4000-8000-000000007006'::uuid,16105,time '10:00',time '10:45',149),
  ('feed0b00-0000-4000-8000-000000007007'::uuid,16106,time '12:00',time '13:30',349),
  ('feed0b00-0000-4000-8000-000000007008'::uuid,16107,time '14:00',time '17:00',1499),
  ('feed0b00-0000-4000-8000-000000007009'::uuid,16108,time '08:00',time '08:30',159),
  ('feed0b00-0000-4000-8000-000000007010'::uuid,16109,time '10:00',time '12:00',699),
  ('feed0b00-0000-4000-8000-000000007011'::uuid,16110,time '12:00',time '13:00',249),
  ('feed0b00-0000-4000-8000-000000007012'::uuid,16111,time '14:00',time '17:00',1299);
INSERT INTO service_bays_bookings(booked_time,date,service_bay_id,booking_id) VALUES
  (tsrange((DATE '2026-08-12'+time '08:00')::timestamp,(DATE '2026-08-12'+time '08:45')::timestamp,'[)'),DATE '2026-08-12',16200,'feed0b00-0000-4000-8000-000000007001'::uuid),
  (tsrange((DATE '2026-08-14'+time '10:00')::timestamp,(DATE '2026-08-14'+time '12:00')::timestamp,'[)'),DATE '2026-08-14',16200,'feed0b00-0000-4000-8000-000000007002'::uuid),
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18',16200,'feed0b00-0000-4000-8000-000000007003'::uuid),
  (tsrange((DATE '2026-08-20'+time '14:00')::timestamp,(DATE '2026-08-20'+time '15:00')::timestamp,'[)'),DATE '2026-08-20',16200,'feed0b00-0000-4000-8000-000000007004'::uuid),
  (tsrange((DATE '2026-08-24'+time '08:00')::timestamp,(DATE '2026-08-24'+time '10:00')::timestamp,'[)'),DATE '2026-08-24',16200,'feed0b00-0000-4000-8000-000000007005'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31',16203,'feed0b00-0000-4000-8000-000000007007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02',16200,'feed0b00-0000-4000-8000-000000007008'::uuid),
  (tsrange((DATE '2026-09-04'+time '08:00')::timestamp,(DATE '2026-09-04'+time '08:30')::timestamp,'[)'),DATE '2026-09-04',16202,'feed0b00-0000-4000-8000-000000007009'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11',16202,'feed0b00-0000-4000-8000-000000007010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18',16202,'feed0b00-0000-4000-8000-000000007011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25',16204,'feed0b00-0000-4000-8000-000000007012'::uuid);
INSERT INTO employees_bookings(booked_time,date,employee_id,booking_id) VALUES
  (tsrange((DATE '2026-08-12'+time '08:00')::timestamp,(DATE '2026-08-12'+time '08:45')::timestamp,'[)'),DATE '2026-08-12','feede3ee-0000-4000-8000-000000000701'::uuid,'feed0b00-0000-4000-8000-000000007001'::uuid),
  (tsrange((DATE '2026-08-14'+time '10:00')::timestamp,(DATE '2026-08-14'+time '12:00')::timestamp,'[)'),DATE '2026-08-14','feede3ee-0000-4000-8000-000000000701'::uuid,'feed0b00-0000-4000-8000-000000007002'::uuid),
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18','feede3ee-0000-4000-8000-000000000701'::uuid,'feed0b00-0000-4000-8000-000000007003'::uuid),
  (tsrange((DATE '2026-08-20'+time '14:00')::timestamp,(DATE '2026-08-20'+time '15:00')::timestamp,'[)'),DATE '2026-08-20','feede3ee-0000-4000-8000-000000000701'::uuid,'feed0b00-0000-4000-8000-000000007004'::uuid),
  (tsrange((DATE '2026-08-24'+time '08:00')::timestamp,(DATE '2026-08-24'+time '10:00')::timestamp,'[)'),DATE '2026-08-24','feede3ee-0000-4000-8000-000000000701'::uuid,'feed0b00-0000-4000-8000-000000007005'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31','feede3ee-0000-4000-8000-000000000704'::uuid,'feed0b00-0000-4000-8000-000000007007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02','feede3ee-0000-4000-8000-000000000701'::uuid,'feed0b00-0000-4000-8000-000000007008'::uuid),
  (tsrange((DATE '2026-09-04'+time '08:00')::timestamp,(DATE '2026-09-04'+time '08:30')::timestamp,'[)'),DATE '2026-09-04','feede3ee-0000-4000-8000-000000000703'::uuid,'feed0b00-0000-4000-8000-000000007009'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11','feede3ee-0000-4000-8000-000000000703'::uuid,'feed0b00-0000-4000-8000-000000007010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18','feede3ee-0000-4000-8000-000000000702'::uuid,'feed0b00-0000-4000-8000-000000007011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25','feede3ee-0000-4000-8000-000000000705'::uuid,'feed0b00-0000-4000-8000-000000007012'::uuid);
INSERT INTO equipment_bookings(booked_time,date,equipment_id,booking_id) VALUES
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18',16304,'feed0b00-0000-4000-8000-000000007003'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31',16303,'feed0b00-0000-4000-8000-000000007007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02',16301,'feed0b00-0000-4000-8000-000000007008'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11',16300,'feed0b00-0000-4000-8000-000000007010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18',16300,'feed0b00-0000-4000-8000-000000007011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25',16305,'feed0b00-0000-4000-8000-000000007012'::uuid);
INSERT INTO reviews(review_id,stars_number,contents,booking_id,created_at) VALUES
  ('feed5ee0-0000-4000-8000-000000007001'::uuid,5,'Bardzo profesjonalna obsługa, polecam!','feed0b00-0000-4000-8000-000000007001'::uuid,(DATE '2026-08-12' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000007002'::uuid,4,'Good service, slightly long wait.','feed0b00-0000-4000-8000-000000007002'::uuid,(DATE '2026-08-14' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000007003'::uuid,5,'Szybko i solidnie wykonane.','feed0b00-0000-4000-8000-000000007003'::uuid,(DATE '2026-08-18' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000007005'::uuid,3,NULL,'feed0b00-0000-4000-8000-000000007005'::uuid,(DATE '2026-08-24' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000007009'::uuid,4,'Fair price, friendly staff.','feed0b00-0000-4000-8000-000000007009'::uuid,(DATE '2026-09-04' + interval '2 days'));
UPDATE branches SET rating=sub.avg, review_count=sub.cnt FROM (SELECT round(avg(stars_number)::numeric,1) avg,count(*) cnt FROM reviews r JOIN bookings bk ON bk.booking_id=r.booking_id WHERE bk.branch_id='feedb4a0-0000-4000-8000-000000000007') sub WHERE branch_id='feedb4a0-0000-4000-8000-000000000007';

INSERT INTO addresses(address_id,street_name,building_number,postal_code,latitude,longitude,city_id) VALUES (17000,'ul. Podkarpacka','5','35-082',50.0413,21.9990,112);
INSERT INTO branches(branch_id,name,phone_number,email,status,tz,address_id,owner_id,description,cancellation_policy) VALUES ('feedb4a0-0000-4000-8000-000000000008','Premium Motors Rzeszów','+48171000008','rzeszow@carfix.dev','ACTIVE','Europe/Warsaw',17000,'feed0001-0000-4000-8000-000000000004','Nowoczesny serwis samochodowy w Rzeszowie z pełnym zapleczem diagnostycznym.','MODERATE');
INSERT INTO opening_hours(day_of_week,start_time,close_time,branch_id,mode) VALUES
  ('MONDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000008','OPEN'),
  ('TUESDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000008','OPEN'),
  ('WEDNESDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000008','OPEN'),
  ('THURSDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000008','OPEN'),
  ('FRIDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000008','OPEN'),
  ('SATURDAY',time '09:00',time '14:00','feedb4a0-0000-4000-8000-000000000008','OPEN');
INSERT INTO car_brands_branches(car_brand_id,branch_id) VALUES
  (1, 'feedb4a0-0000-4000-8000-000000000008'),
  (2, 'feedb4a0-0000-4000-8000-000000000008'),
  (3, 'feedb4a0-0000-4000-8000-000000000008'),
  (4, 'feedb4a0-0000-4000-8000-000000000008'),
  (108, 'feedb4a0-0000-4000-8000-000000000008'),
  (112, 'feedb4a0-0000-4000-8000-000000000008'),
  (120, 'feedb4a0-0000-4000-8000-000000000008'),
  (125, 'feedb4a0-0000-4000-8000-000000000008');
INSERT INTO services(service_id,name,description,duration_minutes,price,status,branch_id,service_category_id) VALUES
  (17100,'Oil & filter change','Standardowa wymiana oleju silnikowego wraz z filtrem.',45,199,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',2),
  (17101,'Full service inspection','Kompleksowy przegląd techniczny pojazdu.',120,499,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',2),
  (17102,'Front brake pads replacement','Wymiana klocków hamulcowych na osi przedniej.',90,449,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',4),
  (17103,'Brake fluid flush','Wymiana i odpowietrzenie płynu hamulcowego.',60,249,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',4),
  (17104,'Shock absorber replacement','Wymiana amortyzatorów przód lub tył.',120,899,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',3),
  (17105,'Seasonal tyre change','Sezonowa wymiana opon na felgach.',45,149,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',5),
  (17106,'Wheel alignment','Ustawienie geometrii kół.',90,349,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',5),
  (17107,'Timing belt replacement','Wymiana paska rozrządu wraz z osprzętem.',180,1499,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',1),
  (17108,'Battery replacement','Wymiana akumulatora i test instalacji.',30,159,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',6),
  (17109,'Alternator diagnostics & repair','Diagnostyka i naprawa alternatora.',120,699,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',6),
  (17110,'Computer diagnostics','Kompleksowa diagnostyka komputerowa pojazdu.',60,249,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',8),
  (17111,'Dent & paint repair','Naprawa wgnieceń i lakierowanie elementu nadwozia.',180,1299,'ACTIVE','feedb4a0-0000-4000-8000-000000000008',7);
INSERT INTO services_service_bay_types(service_id,service_bay_type_id) VALUES
  (17100,1),
  (17101,1),
  (17102,1),
  (17103,1),
  (17104,1),
  (17105,4),
  (17106,4),
  (17107,1),
  (17108,3),
  (17109,3),
  (17110,3),
  (17111,5);
INSERT INTO service_bays(service_bay_id,name,status,service_bay_type_id,branch_id) VALUES
  (17200,'Lift bay 1','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000008'),
  (17201,'Lift bay 2','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000008'),
  (17202,'Diagnostics bay','ACTIVE',3,'feedb4a0-0000-4000-8000-000000000008'),
  (17203,'Tyre bay','ACTIVE',4,'feedb4a0-0000-4000-8000-000000000008'),
  (17204,'Bodywork bay','ACTIVE',5,'feedb4a0-0000-4000-8000-000000000008');
INSERT INTO equipment(equipment_id,name,status,equipment_type_id,branch_id) VALUES
  (17300,'OBD scanner','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000008'),
  (17301,'Engine hoist','ACTIVE',2,'feedb4a0-0000-4000-8000-000000000008'),
  (17302,'Tyre changer','ACTIVE',4,'feedb4a0-0000-4000-8000-000000000008'),
  (17303,'Wheel alignment rig','ACTIVE',6,'feedb4a0-0000-4000-8000-000000000008'),
  (17304,'Brake lathe','ACTIVE',8,'feedb4a0-0000-4000-8000-000000000008'),
  (17305,'Welding station','ACTIVE',9,'feedb4a0-0000-4000-8000-000000000008');
INSERT INTO employees(employee_id,status,salary,branch_id,first_name,last_name,user_id) VALUES
  ('feede3ee-0000-4000-8000-000000000801'::uuid,'ACTIVE',8500,'feedb4a0-0000-4000-8000-000000000008','Grzegorz','Wójcik',NULL),
  ('feede3ee-0000-4000-8000-000000000802'::uuid,'ACTIVE',6400,'feedb4a0-0000-4000-8000-000000000008','Michał','Kaczmarek',NULL),
  ('feede3ee-0000-4000-8000-000000000803'::uuid,'ACTIVE',6000,'feedb4a0-0000-4000-8000-000000000008','Damian','Krawczyk',NULL),
  ('feede3ee-0000-4000-8000-000000000804'::uuid,'ACTIVE',5100,'feedb4a0-0000-4000-8000-000000000008','Rafał','Piotrowski',NULL),
  ('feede3ee-0000-4000-8000-000000000805'::uuid,'ACTIVE',5900,'feedb4a0-0000-4000-8000-000000000008','Sebastian','Grabowski',NULL),
  ('feede3ee-0000-4000-8000-000000000806'::uuid,'ACTIVE',4900,'feedb4a0-0000-4000-8000-000000000008','Kamil','Nowicki',NULL);
INSERT INTO employees_roles(employee_id,role_id) VALUES
  ('feede3ee-0000-4000-8000-000000000801'::uuid,1),
  ('feede3ee-0000-4000-8000-000000000801'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000802'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000802'::uuid,4),
  ('feede3ee-0000-4000-8000-000000000803'::uuid,3),
  ('feede3ee-0000-4000-8000-000000000803'::uuid,4),
  ('feede3ee-0000-4000-8000-000000000804'::uuid,5),
  ('feede3ee-0000-4000-8000-000000000805'::uuid,6),
  ('feede3ee-0000-4000-8000-000000000806'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000806'::uuid,8);
INSERT INTO service_employee_requirements(service_employee_requirement_id,name,service_id) VALUES
  (17400,'Technician',17100),
  (17401,'Technician',17101),
  (17402,'Technician',17102),
  (17403,'Technician',17103),
  (17404,'Technician',17104),
  (17405,'Technician',17105),
  (17406,'Technician',17106),
  (17407,'Technician',17107),
  (17408,'Technician',17108),
  (17409,'Technician',17109),
  (17410,'Technician',17110),
  (17411,'Technician',17111);
INSERT INTO service_employee_requirement_roles(service_employee_requirement_id,role_id) VALUES
  (17400,2),
  (17401,2),
  (17402,2),
  (17403,2),
  (17404,2),
  (17405,5),
  (17406,5),
  (17407,1),
  (17408,3),
  (17409,3),
  (17410,4),
  (17411,6);
INSERT INTO service_equipment_requirements(service_equipment_requirement_id,name,service_id) VALUES
  (17500,'Equipment',17102),
  (17501,'Equipment',17105),
  (17502,'Equipment',17106),
  (17503,'Equipment',17107),
  (17504,'Equipment',17109),
  (17505,'Equipment',17110),
  (17506,'Equipment',17111);
INSERT INTO service_equipment_requirement_types(service_equipment_requirement_id,equipment_type_id) VALUES
  (17500,8),
  (17501,4),
  (17502,6),
  (17503,2),
  (17504,1),
  (17505,1),
  (17506,9);
INSERT INTO service_bays_availability(available_time,date,service_bay_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (17200),(17201),(17202),(17203),(17204)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO service_bays_availability(available_time,date,service_bay_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (17200),(17201),(17202),(17203),(17204)) v(id) WHERE extract(dow from d)=6;
INSERT INTO employees_availability(available_time,date,employee_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES ('feede3ee-0000-4000-8000-000000000801'::uuid),('feede3ee-0000-4000-8000-000000000802'::uuid),('feede3ee-0000-4000-8000-000000000803'::uuid),('feede3ee-0000-4000-8000-000000000804'::uuid),('feede3ee-0000-4000-8000-000000000805'::uuid),('feede3ee-0000-4000-8000-000000000806'::uuid)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO employees_availability(available_time,date,employee_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES ('feede3ee-0000-4000-8000-000000000801'::uuid),('feede3ee-0000-4000-8000-000000000802'::uuid),('feede3ee-0000-4000-8000-000000000803'::uuid),('feede3ee-0000-4000-8000-000000000804'::uuid),('feede3ee-0000-4000-8000-000000000805'::uuid),('feede3ee-0000-4000-8000-000000000806'::uuid)) v(id) WHERE extract(dow from d)=6;
INSERT INTO equipment_availability(available_time,date,equipment_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (17300),(17301),(17302),(17303),(17304),(17305)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO equipment_availability(available_time,date,equipment_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (17300),(17301),(17302),(17303),(17304),(17305)) v(id) WHERE extract(dow from d)=6;
INSERT INTO bookings(booking_id,date,status,start_time,end_time,branch_id,car_profile_id,created_at) VALUES
  ('feed0b00-0000-4000-8000-000000008001'::uuid,DATE '2026-08-12','COMPLETED',time '08:00',time '08:45','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000037',(DATE '2026-08-12' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008002'::uuid,DATE '2026-08-14','COMPLETED',time '10:00',time '12:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000038',(DATE '2026-08-14' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008003'::uuid,DATE '2026-08-18','COMPLETED',time '12:00',time '13:30','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000039',(DATE '2026-08-18' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008004'::uuid,DATE '2026-08-20','COMPLETED',time '14:00',time '15:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000040',(DATE '2026-08-20' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008005'::uuid,DATE '2026-08-24','COMPLETED',time '08:00',time '10:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000041',(DATE '2026-08-24' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008006'::uuid,DATE '2026-08-27','CANCELLED',time '10:00',time '10:45','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000042',(DATE '2026-08-27' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008007'::uuid,DATE '2026-08-31','COMPLETED',time '12:00',time '13:30','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000043',(DATE '2026-08-31' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008008'::uuid,DATE '2026-09-02','NO_SHOW',time '14:00',time '17:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000044',(DATE '2026-09-02' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008009'::uuid,DATE '2026-09-04','COMPLETED',time '08:00',time '08:30','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000045',(DATE '2026-09-04' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008010'::uuid,DATE '2026-09-11','SCHEDULED',time '10:00',time '12:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000031',(DATE '2026-09-11' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008011'::uuid,DATE '2026-09-18','SCHEDULED',time '12:00',time '13:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000032',(DATE '2026-09-18' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000008012'::uuid,DATE '2026-09-25','SCHEDULED',time '14:00',time '17:00','feedb4a0-0000-4000-8000-000000000008','feedca40-0000-4000-8000-000000000033',(DATE '2026-09-25' - interval '5 days'));
INSERT INTO bookings_services(booking_id,service_id,start_time,end_time,price) VALUES
  ('feed0b00-0000-4000-8000-000000008001'::uuid,17100,time '08:00',time '08:45',199),
  ('feed0b00-0000-4000-8000-000000008002'::uuid,17101,time '10:00',time '12:00',499),
  ('feed0b00-0000-4000-8000-000000008003'::uuid,17102,time '12:00',time '13:30',449),
  ('feed0b00-0000-4000-8000-000000008004'::uuid,17103,time '14:00',time '15:00',249),
  ('feed0b00-0000-4000-8000-000000008005'::uuid,17104,time '08:00',time '10:00',899),
  ('feed0b00-0000-4000-8000-000000008006'::uuid,17105,time '10:00',time '10:45',149),
  ('feed0b00-0000-4000-8000-000000008007'::uuid,17106,time '12:00',time '13:30',349),
  ('feed0b00-0000-4000-8000-000000008008'::uuid,17107,time '14:00',time '17:00',1499),
  ('feed0b00-0000-4000-8000-000000008009'::uuid,17108,time '08:00',time '08:30',159),
  ('feed0b00-0000-4000-8000-000000008010'::uuid,17109,time '10:00',time '12:00',699),
  ('feed0b00-0000-4000-8000-000000008011'::uuid,17110,time '12:00',time '13:00',249),
  ('feed0b00-0000-4000-8000-000000008012'::uuid,17111,time '14:00',time '17:00',1299);
INSERT INTO service_bays_bookings(booked_time,date,service_bay_id,booking_id) VALUES
  (tsrange((DATE '2026-08-12'+time '08:00')::timestamp,(DATE '2026-08-12'+time '08:45')::timestamp,'[)'),DATE '2026-08-12',17200,'feed0b00-0000-4000-8000-000000008001'::uuid),
  (tsrange((DATE '2026-08-14'+time '10:00')::timestamp,(DATE '2026-08-14'+time '12:00')::timestamp,'[)'),DATE '2026-08-14',17200,'feed0b00-0000-4000-8000-000000008002'::uuid),
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18',17200,'feed0b00-0000-4000-8000-000000008003'::uuid),
  (tsrange((DATE '2026-08-20'+time '14:00')::timestamp,(DATE '2026-08-20'+time '15:00')::timestamp,'[)'),DATE '2026-08-20',17200,'feed0b00-0000-4000-8000-000000008004'::uuid),
  (tsrange((DATE '2026-08-24'+time '08:00')::timestamp,(DATE '2026-08-24'+time '10:00')::timestamp,'[)'),DATE '2026-08-24',17200,'feed0b00-0000-4000-8000-000000008005'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31',17203,'feed0b00-0000-4000-8000-000000008007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02',17200,'feed0b00-0000-4000-8000-000000008008'::uuid),
  (tsrange((DATE '2026-09-04'+time '08:00')::timestamp,(DATE '2026-09-04'+time '08:30')::timestamp,'[)'),DATE '2026-09-04',17202,'feed0b00-0000-4000-8000-000000008009'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11',17202,'feed0b00-0000-4000-8000-000000008010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18',17202,'feed0b00-0000-4000-8000-000000008011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25',17204,'feed0b00-0000-4000-8000-000000008012'::uuid);
INSERT INTO employees_bookings(booked_time,date,employee_id,booking_id) VALUES
  (tsrange((DATE '2026-08-12'+time '08:00')::timestamp,(DATE '2026-08-12'+time '08:45')::timestamp,'[)'),DATE '2026-08-12','feede3ee-0000-4000-8000-000000000801'::uuid,'feed0b00-0000-4000-8000-000000008001'::uuid),
  (tsrange((DATE '2026-08-14'+time '10:00')::timestamp,(DATE '2026-08-14'+time '12:00')::timestamp,'[)'),DATE '2026-08-14','feede3ee-0000-4000-8000-000000000801'::uuid,'feed0b00-0000-4000-8000-000000008002'::uuid),
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18','feede3ee-0000-4000-8000-000000000801'::uuid,'feed0b00-0000-4000-8000-000000008003'::uuid),
  (tsrange((DATE '2026-08-20'+time '14:00')::timestamp,(DATE '2026-08-20'+time '15:00')::timestamp,'[)'),DATE '2026-08-20','feede3ee-0000-4000-8000-000000000801'::uuid,'feed0b00-0000-4000-8000-000000008004'::uuid),
  (tsrange((DATE '2026-08-24'+time '08:00')::timestamp,(DATE '2026-08-24'+time '10:00')::timestamp,'[)'),DATE '2026-08-24','feede3ee-0000-4000-8000-000000000801'::uuid,'feed0b00-0000-4000-8000-000000008005'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31','feede3ee-0000-4000-8000-000000000804'::uuid,'feed0b00-0000-4000-8000-000000008007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02','feede3ee-0000-4000-8000-000000000801'::uuid,'feed0b00-0000-4000-8000-000000008008'::uuid),
  (tsrange((DATE '2026-09-04'+time '08:00')::timestamp,(DATE '2026-09-04'+time '08:30')::timestamp,'[)'),DATE '2026-09-04','feede3ee-0000-4000-8000-000000000803'::uuid,'feed0b00-0000-4000-8000-000000008009'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11','feede3ee-0000-4000-8000-000000000803'::uuid,'feed0b00-0000-4000-8000-000000008010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18','feede3ee-0000-4000-8000-000000000802'::uuid,'feed0b00-0000-4000-8000-000000008011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25','feede3ee-0000-4000-8000-000000000805'::uuid,'feed0b00-0000-4000-8000-000000008012'::uuid);
INSERT INTO equipment_bookings(booked_time,date,equipment_id,booking_id) VALUES
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18',17304,'feed0b00-0000-4000-8000-000000008003'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31',17303,'feed0b00-0000-4000-8000-000000008007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02',17301,'feed0b00-0000-4000-8000-000000008008'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11',17300,'feed0b00-0000-4000-8000-000000008010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18',17300,'feed0b00-0000-4000-8000-000000008011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25',17305,'feed0b00-0000-4000-8000-000000008012'::uuid);
INSERT INTO reviews(review_id,stars_number,contents,booking_id,created_at) VALUES
  ('feed5ee0-0000-4000-8000-000000008001'::uuid,4,'Sprawnie i profesjonalnie.','feed0b00-0000-4000-8000-000000008001'::uuid,(DATE '2026-08-12' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000008002'::uuid,5,'Excellent, very satisfied.','feed0b00-0000-4000-8000-000000008002'::uuid,(DATE '2026-08-14' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000008003'::uuid,5,NULL,'feed0b00-0000-4000-8000-000000008003'::uuid,(DATE '2026-08-18' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000008005'::uuid,4,'Miła obsługa, polecam.','feed0b00-0000-4000-8000-000000008005'::uuid,(DATE '2026-08-24' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000008009'::uuid,3,'Ok, could be faster.','feed0b00-0000-4000-8000-000000008009'::uuid,(DATE '2026-09-04' + interval '2 days'));
UPDATE branches SET rating=sub.avg, review_count=sub.cnt FROM (SELECT round(avg(stars_number)::numeric,1) avg,count(*) cnt FROM reviews r JOIN bookings bk ON bk.booking_id=r.booking_id WHERE bk.branch_id='feedb4a0-0000-4000-8000-000000000008') sub WHERE branch_id='feedb4a0-0000-4000-8000-000000000008';

INSERT INTO addresses(address_id,street_name,building_number,postal_code,latitude,longitude,city_id) VALUES (18000,'ul. Lubelska','20','20-400',51.2465,22.5684,107);
INSERT INTO branches(branch_id,name,phone_number,email,status,tz,address_id,owner_id,description,cancellation_policy) VALUES ('feedb4a0-0000-4000-8000-000000000009','CarFix Serwis Lublin','+48811000009','lublin@carfix.dev','ACTIVE','Europe/Warsaw',18000,'feed0001-0000-4000-8000-000000000001','Rodzinny warsztat samochodowy w Lublinie, obsługujący klientów od lat.','FLEXIBLE');
INSERT INTO opening_hours(day_of_week,start_time,close_time,branch_id,mode) VALUES
  ('MONDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000009','OPEN'),
  ('TUESDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000009','OPEN'),
  ('WEDNESDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000009','OPEN'),
  ('THURSDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000009','OPEN'),
  ('FRIDAY',time '08:00',time '18:00','feedb4a0-0000-4000-8000-000000000009','OPEN'),
  ('SATURDAY',time '09:00',time '14:00','feedb4a0-0000-4000-8000-000000000009','OPEN');
INSERT INTO car_brands_branches(car_brand_id,branch_id) VALUES
  (1, 'feedb4a0-0000-4000-8000-000000000009'),
  (2, 'feedb4a0-0000-4000-8000-000000000009'),
  (3, 'feedb4a0-0000-4000-8000-000000000009'),
  (4, 'feedb4a0-0000-4000-8000-000000000009'),
  (103, 'feedb4a0-0000-4000-8000-000000000009'),
  (118, 'feedb4a0-0000-4000-8000-000000000009'),
  (122, 'feedb4a0-0000-4000-8000-000000000009'),
  (130, 'feedb4a0-0000-4000-8000-000000000009');
INSERT INTO services(service_id,name,description,duration_minutes,price,status,branch_id,service_category_id) VALUES
  (18100,'Oil & filter change','Standardowa wymiana oleju silnikowego wraz z filtrem.',45,199,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',2),
  (18101,'Full service inspection','Kompleksowy przegląd techniczny pojazdu.',120,499,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',2),
  (18102,'Front brake pads replacement','Wymiana klocków hamulcowych na osi przedniej.',90,449,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',4),
  (18103,'Brake fluid flush','Wymiana i odpowietrzenie płynu hamulcowego.',60,249,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',4),
  (18104,'Shock absorber replacement','Wymiana amortyzatorów przód lub tył.',120,899,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',3),
  (18105,'Seasonal tyre change','Sezonowa wymiana opon na felgach.',45,149,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',5),
  (18106,'Wheel alignment','Ustawienie geometrii kół.',90,349,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',5),
  (18107,'Timing belt replacement','Wymiana paska rozrządu wraz z osprzętem.',180,1499,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',1),
  (18108,'Battery replacement','Wymiana akumulatora i test instalacji.',30,159,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',6),
  (18109,'Alternator diagnostics & repair','Diagnostyka i naprawa alternatora.',120,699,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',6),
  (18110,'Computer diagnostics','Kompleksowa diagnostyka komputerowa pojazdu.',60,249,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',8),
  (18111,'Dent & paint repair','Naprawa wgnieceń i lakierowanie elementu nadwozia.',180,1299,'ACTIVE','feedb4a0-0000-4000-8000-000000000009',7);
INSERT INTO services_service_bay_types(service_id,service_bay_type_id) VALUES
  (18100,1),
  (18101,1),
  (18102,1),
  (18103,1),
  (18104,1),
  (18105,4),
  (18106,4),
  (18107,1),
  (18108,3),
  (18109,3),
  (18110,3),
  (18111,5);
INSERT INTO service_bays(service_bay_id,name,status,service_bay_type_id,branch_id) VALUES
  (18200,'Lift bay 1','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000009'),
  (18201,'Lift bay 2','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000009'),
  (18202,'Diagnostics bay','ACTIVE',3,'feedb4a0-0000-4000-8000-000000000009'),
  (18203,'Tyre bay','ACTIVE',4,'feedb4a0-0000-4000-8000-000000000009'),
  (18204,'Bodywork bay','ACTIVE',5,'feedb4a0-0000-4000-8000-000000000009');
INSERT INTO equipment(equipment_id,name,status,equipment_type_id,branch_id) VALUES
  (18300,'OBD scanner','ACTIVE',1,'feedb4a0-0000-4000-8000-000000000009'),
  (18301,'Engine hoist','ACTIVE',2,'feedb4a0-0000-4000-8000-000000000009'),
  (18302,'Tyre changer','ACTIVE',4,'feedb4a0-0000-4000-8000-000000000009'),
  (18303,'Wheel alignment rig','ACTIVE',6,'feedb4a0-0000-4000-8000-000000000009'),
  (18304,'Brake lathe','ACTIVE',8,'feedb4a0-0000-4000-8000-000000000009'),
  (18305,'Welding station','ACTIVE',9,'feedb4a0-0000-4000-8000-000000000009');
INSERT INTO employees(employee_id,status,salary,branch_id,first_name,last_name,user_id) VALUES
  ('feede3ee-0000-4000-8000-000000000901'::uuid,'ACTIVE',9500,'feedb4a0-0000-4000-8000-000000000009','Wojciech','Pawłowski',NULL),
  ('feede3ee-0000-4000-8000-000000000902'::uuid,'ACTIVE',6600,'feedb4a0-0000-4000-8000-000000000009','Łukasz','Michalski',NULL),
  ('feede3ee-0000-4000-8000-000000000903'::uuid,'ACTIVE',6200,'feedb4a0-0000-4000-8000-000000000009','Paweł','Adamczyk',NULL),
  ('feede3ee-0000-4000-8000-000000000904'::uuid,'ACTIVE',5300,'feedb4a0-0000-4000-8000-000000000009','Dawid','Dudek',NULL),
  ('feede3ee-0000-4000-8000-000000000905'::uuid,'ACTIVE',6000,'feedb4a0-0000-4000-8000-000000000009','Mateusz','Zając',NULL),
  ('feede3ee-0000-4000-8000-000000000906'::uuid,'ACTIVE',4500,'feedb4a0-0000-4000-8000-000000000009','Krzysztof','Wróbel',NULL);
INSERT INTO employees_roles(employee_id,role_id) VALUES
  ('feede3ee-0000-4000-8000-000000000901'::uuid,1),
  ('feede3ee-0000-4000-8000-000000000901'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000902'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000902'::uuid,4),
  ('feede3ee-0000-4000-8000-000000000903'::uuid,3),
  ('feede3ee-0000-4000-8000-000000000903'::uuid,4),
  ('feede3ee-0000-4000-8000-000000000904'::uuid,5),
  ('feede3ee-0000-4000-8000-000000000905'::uuid,6),
  ('feede3ee-0000-4000-8000-000000000906'::uuid,2),
  ('feede3ee-0000-4000-8000-000000000906'::uuid,8);
INSERT INTO service_employee_requirements(service_employee_requirement_id,name,service_id) VALUES
  (18400,'Technician',18100),
  (18401,'Technician',18101),
  (18402,'Technician',18102),
  (18403,'Technician',18103),
  (18404,'Technician',18104),
  (18405,'Technician',18105),
  (18406,'Technician',18106),
  (18407,'Technician',18107),
  (18408,'Technician',18108),
  (18409,'Technician',18109),
  (18410,'Technician',18110),
  (18411,'Technician',18111);
INSERT INTO service_employee_requirement_roles(service_employee_requirement_id,role_id) VALUES
  (18400,2),
  (18401,2),
  (18402,2),
  (18403,2),
  (18404,2),
  (18405,5),
  (18406,5),
  (18407,1),
  (18408,3),
  (18409,3),
  (18410,4),
  (18411,6);
INSERT INTO service_equipment_requirements(service_equipment_requirement_id,name,service_id) VALUES
  (18500,'Equipment',18102),
  (18501,'Equipment',18105),
  (18502,'Equipment',18106),
  (18503,'Equipment',18107),
  (18504,'Equipment',18109),
  (18505,'Equipment',18110),
  (18506,'Equipment',18111);
INSERT INTO service_equipment_requirement_types(service_equipment_requirement_id,equipment_type_id) VALUES
  (18500,8),
  (18501,4),
  (18502,6),
  (18503,2),
  (18504,1),
  (18505,1),
  (18506,9);
INSERT INTO service_bays_availability(available_time,date,service_bay_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (18200),(18201),(18202),(18203),(18204)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO service_bays_availability(available_time,date,service_bay_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (18200),(18201),(18202),(18203),(18204)) v(id) WHERE extract(dow from d)=6;
INSERT INTO employees_availability(available_time,date,employee_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES ('feede3ee-0000-4000-8000-000000000901'::uuid),('feede3ee-0000-4000-8000-000000000902'::uuid),('feede3ee-0000-4000-8000-000000000903'::uuid),('feede3ee-0000-4000-8000-000000000904'::uuid),('feede3ee-0000-4000-8000-000000000905'::uuid),('feede3ee-0000-4000-8000-000000000906'::uuid)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO employees_availability(available_time,date,employee_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES ('feede3ee-0000-4000-8000-000000000901'::uuid),('feede3ee-0000-4000-8000-000000000902'::uuid),('feede3ee-0000-4000-8000-000000000903'::uuid),('feede3ee-0000-4000-8000-000000000904'::uuid),('feede3ee-0000-4000-8000-000000000905'::uuid),('feede3ee-0000-4000-8000-000000000906'::uuid)) v(id) WHERE extract(dow from d)=6;
INSERT INTO equipment_availability(available_time,date,equipment_id)
SELECT tsrange((d+time '08:00')::timestamp,(d+time '18:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (18300),(18301),(18302),(18303),(18304),(18305)) v(id) WHERE extract(dow from d) IN (1,2,3,4,5);
INSERT INTO equipment_availability(available_time,date,equipment_id)
SELECT tsrange((d+time '09:00')::timestamp,(d+time '14:00')::timestamp,'[)'),d,v.id
FROM generate_series(DATE '2026-08-11',DATE '2026-10-05',interval '1 day') g(d)
CROSS JOIN (VALUES (18300),(18301),(18302),(18303),(18304),(18305)) v(id) WHERE extract(dow from d)=6;
INSERT INTO bookings(booking_id,date,status,start_time,end_time,branch_id,car_profile_id,created_at) VALUES
  ('feed0b00-0000-4000-8000-000000009001'::uuid,DATE '2026-08-12','COMPLETED',time '08:00',time '08:45','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000034',(DATE '2026-08-12' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009002'::uuid,DATE '2026-08-14','COMPLETED',time '10:00',time '12:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000035',(DATE '2026-08-14' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009003'::uuid,DATE '2026-08-18','COMPLETED',time '12:00',time '13:30','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000036',(DATE '2026-08-18' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009004'::uuid,DATE '2026-08-20','COMPLETED',time '14:00',time '15:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000037',(DATE '2026-08-20' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009005'::uuid,DATE '2026-08-24','COMPLETED',time '08:00',time '10:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000038',(DATE '2026-08-24' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009006'::uuid,DATE '2026-08-27','CANCELLED',time '10:00',time '10:45','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000039',(DATE '2026-08-27' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009007'::uuid,DATE '2026-08-31','COMPLETED',time '12:00',time '13:30','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000040',(DATE '2026-08-31' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009008'::uuid,DATE '2026-09-02','NO_SHOW',time '14:00',time '17:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000041',(DATE '2026-09-02' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009009'::uuid,DATE '2026-09-04','COMPLETED',time '08:00',time '08:30','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000042',(DATE '2026-09-04' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009010'::uuid,DATE '2026-09-11','SCHEDULED',time '10:00',time '12:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000043',(DATE '2026-09-11' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009011'::uuid,DATE '2026-09-18','SCHEDULED',time '12:00',time '13:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000044',(DATE '2026-09-18' - interval '5 days')),
  ('feed0b00-0000-4000-8000-000000009012'::uuid,DATE '2026-09-25','SCHEDULED',time '14:00',time '17:00','feedb4a0-0000-4000-8000-000000000009','feedca40-0000-4000-8000-000000000045',(DATE '2026-09-25' - interval '5 days'));
INSERT INTO bookings_services(booking_id,service_id,start_time,end_time,price) VALUES
  ('feed0b00-0000-4000-8000-000000009001'::uuid,18100,time '08:00',time '08:45',199),
  ('feed0b00-0000-4000-8000-000000009002'::uuid,18101,time '10:00',time '12:00',499),
  ('feed0b00-0000-4000-8000-000000009003'::uuid,18102,time '12:00',time '13:30',449),
  ('feed0b00-0000-4000-8000-000000009004'::uuid,18103,time '14:00',time '15:00',249),
  ('feed0b00-0000-4000-8000-000000009005'::uuid,18104,time '08:00',time '10:00',899),
  ('feed0b00-0000-4000-8000-000000009006'::uuid,18105,time '10:00',time '10:45',149),
  ('feed0b00-0000-4000-8000-000000009007'::uuid,18106,time '12:00',time '13:30',349),
  ('feed0b00-0000-4000-8000-000000009008'::uuid,18107,time '14:00',time '17:00',1499),
  ('feed0b00-0000-4000-8000-000000009009'::uuid,18108,time '08:00',time '08:30',159),
  ('feed0b00-0000-4000-8000-000000009010'::uuid,18109,time '10:00',time '12:00',699),
  ('feed0b00-0000-4000-8000-000000009011'::uuid,18110,time '12:00',time '13:00',249),
  ('feed0b00-0000-4000-8000-000000009012'::uuid,18111,time '14:00',time '17:00',1299);
INSERT INTO service_bays_bookings(booked_time,date,service_bay_id,booking_id) VALUES
  (tsrange((DATE '2026-08-12'+time '08:00')::timestamp,(DATE '2026-08-12'+time '08:45')::timestamp,'[)'),DATE '2026-08-12',18200,'feed0b00-0000-4000-8000-000000009001'::uuid),
  (tsrange((DATE '2026-08-14'+time '10:00')::timestamp,(DATE '2026-08-14'+time '12:00')::timestamp,'[)'),DATE '2026-08-14',18200,'feed0b00-0000-4000-8000-000000009002'::uuid),
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18',18200,'feed0b00-0000-4000-8000-000000009003'::uuid),
  (tsrange((DATE '2026-08-20'+time '14:00')::timestamp,(DATE '2026-08-20'+time '15:00')::timestamp,'[)'),DATE '2026-08-20',18200,'feed0b00-0000-4000-8000-000000009004'::uuid),
  (tsrange((DATE '2026-08-24'+time '08:00')::timestamp,(DATE '2026-08-24'+time '10:00')::timestamp,'[)'),DATE '2026-08-24',18200,'feed0b00-0000-4000-8000-000000009005'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31',18203,'feed0b00-0000-4000-8000-000000009007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02',18200,'feed0b00-0000-4000-8000-000000009008'::uuid),
  (tsrange((DATE '2026-09-04'+time '08:00')::timestamp,(DATE '2026-09-04'+time '08:30')::timestamp,'[)'),DATE '2026-09-04',18202,'feed0b00-0000-4000-8000-000000009009'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11',18202,'feed0b00-0000-4000-8000-000000009010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18',18202,'feed0b00-0000-4000-8000-000000009011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25',18204,'feed0b00-0000-4000-8000-000000009012'::uuid);
INSERT INTO employees_bookings(booked_time,date,employee_id,booking_id) VALUES
  (tsrange((DATE '2026-08-12'+time '08:00')::timestamp,(DATE '2026-08-12'+time '08:45')::timestamp,'[)'),DATE '2026-08-12','feede3ee-0000-4000-8000-000000000901'::uuid,'feed0b00-0000-4000-8000-000000009001'::uuid),
  (tsrange((DATE '2026-08-14'+time '10:00')::timestamp,(DATE '2026-08-14'+time '12:00')::timestamp,'[)'),DATE '2026-08-14','feede3ee-0000-4000-8000-000000000901'::uuid,'feed0b00-0000-4000-8000-000000009002'::uuid),
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18','feede3ee-0000-4000-8000-000000000901'::uuid,'feed0b00-0000-4000-8000-000000009003'::uuid),
  (tsrange((DATE '2026-08-20'+time '14:00')::timestamp,(DATE '2026-08-20'+time '15:00')::timestamp,'[)'),DATE '2026-08-20','feede3ee-0000-4000-8000-000000000901'::uuid,'feed0b00-0000-4000-8000-000000009004'::uuid),
  (tsrange((DATE '2026-08-24'+time '08:00')::timestamp,(DATE '2026-08-24'+time '10:00')::timestamp,'[)'),DATE '2026-08-24','feede3ee-0000-4000-8000-000000000901'::uuid,'feed0b00-0000-4000-8000-000000009005'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31','feede3ee-0000-4000-8000-000000000904'::uuid,'feed0b00-0000-4000-8000-000000009007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02','feede3ee-0000-4000-8000-000000000901'::uuid,'feed0b00-0000-4000-8000-000000009008'::uuid),
  (tsrange((DATE '2026-09-04'+time '08:00')::timestamp,(DATE '2026-09-04'+time '08:30')::timestamp,'[)'),DATE '2026-09-04','feede3ee-0000-4000-8000-000000000903'::uuid,'feed0b00-0000-4000-8000-000000009009'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11','feede3ee-0000-4000-8000-000000000903'::uuid,'feed0b00-0000-4000-8000-000000009010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18','feede3ee-0000-4000-8000-000000000902'::uuid,'feed0b00-0000-4000-8000-000000009011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25','feede3ee-0000-4000-8000-000000000905'::uuid,'feed0b00-0000-4000-8000-000000009012'::uuid);
INSERT INTO equipment_bookings(booked_time,date,equipment_id,booking_id) VALUES
  (tsrange((DATE '2026-08-18'+time '12:00')::timestamp,(DATE '2026-08-18'+time '13:30')::timestamp,'[)'),DATE '2026-08-18',18304,'feed0b00-0000-4000-8000-000000009003'::uuid),
  (tsrange((DATE '2026-08-31'+time '12:00')::timestamp,(DATE '2026-08-31'+time '13:30')::timestamp,'[)'),DATE '2026-08-31',18303,'feed0b00-0000-4000-8000-000000009007'::uuid),
  (tsrange((DATE '2026-09-02'+time '14:00')::timestamp,(DATE '2026-09-02'+time '17:00')::timestamp,'[)'),DATE '2026-09-02',18301,'feed0b00-0000-4000-8000-000000009008'::uuid),
  (tsrange((DATE '2026-09-11'+time '10:00')::timestamp,(DATE '2026-09-11'+time '12:00')::timestamp,'[)'),DATE '2026-09-11',18300,'feed0b00-0000-4000-8000-000000009010'::uuid),
  (tsrange((DATE '2026-09-18'+time '12:00')::timestamp,(DATE '2026-09-18'+time '13:00')::timestamp,'[)'),DATE '2026-09-18',18300,'feed0b00-0000-4000-8000-000000009011'::uuid),
  (tsrange((DATE '2026-09-25'+time '14:00')::timestamp,(DATE '2026-09-25'+time '17:00')::timestamp,'[)'),DATE '2026-09-25',18305,'feed0b00-0000-4000-8000-000000009012'::uuid);
INSERT INTO reviews(review_id,stars_number,contents,booking_id,created_at) VALUES
  ('feed5ee0-0000-4000-8000-000000009001'::uuid,5,'Świetna robota, wrócę.','feed0b00-0000-4000-8000-000000009001'::uuid,(DATE '2026-08-12' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000009002'::uuid,5,'Very professional team.','feed0b00-0000-4000-8000-000000009002'::uuid,(DATE '2026-08-14' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000009003'::uuid,4,'Dobrze wykonana usługa.','feed0b00-0000-4000-8000-000000009003'::uuid,(DATE '2026-08-18' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000009005'::uuid,4,'Solid work, on time.','feed0b00-0000-4000-8000-000000009005'::uuid,(DATE '2026-08-24' + interval '2 days')),
  ('feed5ee0-0000-4000-8000-000000009009'::uuid,3,'Przeciętnie, długo czekałem.','feed0b00-0000-4000-8000-000000009009'::uuid,(DATE '2026-09-04' + interval '2 days'));
UPDATE branches SET rating=sub.avg, review_count=sub.cnt FROM (SELECT round(avg(stars_number)::numeric,1) avg,count(*) cnt FROM reviews r JOIN bookings bk ON bk.booking_id=r.booking_id WHERE bk.branch_id='feedb4a0-0000-4000-8000-000000000009') sub WHERE branch_id='feedb4a0-0000-4000-8000-000000000009';

COMMIT;
