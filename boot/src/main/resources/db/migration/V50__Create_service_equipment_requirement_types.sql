SET search_path TO carfix;

CREATE TABLE service_equipment_requirement_types (
                                                     service_equipment_requirement_id int  NOT NULL,
                                                     equipment_type_id int  NOT NULL,

                                                     CONSTRAINT service_equipment_requirement_types_pk
                                                         PRIMARY KEY (service_equipment_requirement_id, equipment_type_id)
);

ALTER TABLE service_equipment_requirement_types ADD CONSTRAINT fk_service_equipment_requirement_types_requirements FOREIGN KEY (service_equipment_requirement_id) REFERENCES service_equipment_requirements (service_equipment_requirement_id);
ALTER TABLE service_equipment_requirement_types ADD CONSTRAINT fk_service_equipment_requirement_types_equipment_types FOREIGN KEY (equipment_type_id) REFERENCES equipment_types (equipment_type_id);
