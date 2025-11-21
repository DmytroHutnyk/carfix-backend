SET search_path TO carfix;

CREATE TABLE car_profiles (
                              car_profile_id uuid  DEFAULT gen_random_uuid() PRIMARY KEY,
                              name varchar(100)  NOT NULL,
                              vin varchar(17),
                              plates varchar(10),
                              service_certificate_date date,
                              insurance_date date,
                              customer_id uuid  NOT NULL,
                              file_id int,
                              model_generation_id int  NOT NULL
);

