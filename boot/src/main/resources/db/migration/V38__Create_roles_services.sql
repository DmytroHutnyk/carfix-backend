SET search_path TO carfix;

CREATE TABLE roles_services (
                                service_id int,
                                role_id int,

                                CONSTRAINT roles_services_pk PRIMARY KEY (service_id,role_id)
);

