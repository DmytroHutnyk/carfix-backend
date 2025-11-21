SET search_path TO carfix;

CREATE TABLE employees_roles (
                                 user_id uuid,
                                 role_id int,

                                 CONSTRAINT pk_employees_roles PRIMARY KEY (user_id, role_id)
);

