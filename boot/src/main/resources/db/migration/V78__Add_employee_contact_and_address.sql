SET search_path TO carfix;

ALTER TABLE employees
    ADD COLUMN phone varchar(20),
    ADD COLUMN email varchar(255),
    ADD COLUMN street varchar(100),
    ADD COLUMN apartment varchar(20),
    ADD COLUMN region varchar(100),
    ADD COLUMN country varchar(100),
    ADD COLUMN postal_code varchar(20);
