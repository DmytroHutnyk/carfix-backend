SET search_path TO carfix;

CREATE TABLE owners (
                        user_id uuid  PRIMARY KEY,
                        business_name varchar(100)  NOT NULL,
                        vat_in varchar(15)  NOT NULL,
                        regon varchar(9)  NOT NULL
);

