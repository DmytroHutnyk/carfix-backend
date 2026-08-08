SET search_path TO carfix;

CREATE TABLE service_employee_requirement_roles (
                                                    service_employee_requirement_id int  NOT NULL,
                                                    role_id int  NOT NULL,

                                                    CONSTRAINT service_employee_requirement_roles_pk
                                                        PRIMARY KEY (service_employee_requirement_id, role_id)
);

ALTER TABLE service_employee_requirement_roles ADD CONSTRAINT fk_service_employee_requirement_roles_requirements FOREIGN KEY (service_employee_requirement_id) REFERENCES service_employee_requirements (service_employee_requirement_id);
ALTER TABLE service_employee_requirement_roles ADD CONSTRAINT fk_service_employee_requirement_roles_roles FOREIGN KEY (role_id) REFERENCES roles (role_id);
