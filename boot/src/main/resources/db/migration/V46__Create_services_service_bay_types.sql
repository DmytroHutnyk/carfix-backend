SET search_path TO carfix;

CREATE TABLE services_service_bay_types (
                                            service_id int  NOT NULL,
                                            service_bay_type_id int  NOT NULL,

                                            CONSTRAINT services_service_bay_types_pk
                                                PRIMARY KEY (service_id, service_bay_type_id)
);

ALTER TABLE services_service_bay_types ADD CONSTRAINT fk_services_service_bay_types_services FOREIGN KEY (service_id) REFERENCES services (service_id);
ALTER TABLE services_service_bay_types ADD CONSTRAINT fk_services_service_bay_types_service_bay_types FOREIGN KEY (service_bay_type_id) REFERENCES service_bay_types (service_bay_type_id);
