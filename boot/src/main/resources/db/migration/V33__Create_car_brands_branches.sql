SET search_path TO carfix;

CREATE TABLE car_brands_branches (
                                     car_brand_id int,
                                     branch_id uuid,

                                     CONSTRAINT pk_car_brands_branches PRIMARY KEY (car_brand_id, branch_id)
);

