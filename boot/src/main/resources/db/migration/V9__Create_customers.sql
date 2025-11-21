SET search_path TO carfix;

-- check_customers_status option list can be changed
CREATE TABLE customers (user_id uuid  PRIMARY KEY,
                        status text  NOT NULL

                            CONSTRAINT check_customers_status
                                CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

