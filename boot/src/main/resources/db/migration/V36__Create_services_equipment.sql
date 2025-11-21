SET search_path TO carfix;

CREATE TABLE services_equipment (
                                    equipment_id int,
                                    service_id int,

                                    CONSTRAINT services_equipment_pk PRIMARY KEY (service_id,equipment_id)
);

