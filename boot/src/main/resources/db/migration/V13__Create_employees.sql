SET search_path TO carfix;

CREATE TABLE employees (
                           user_id uuid  PRIMARY KEY,
                           status text  NOT NULL,
                           notes text,
                           salary decimal(10,2)  NOT NULL,
                           branch_id uuid  NOT NULL,

                           CONSTRAINT check_employees_status
                               CHECK (status IN ('ACTIVE', 'SUSPENDED'))
);

