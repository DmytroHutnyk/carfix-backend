SET search_path TO carfix;

CREATE INDEX idx_branches_owner_id ON branches (owner_id);
CREATE INDEX idx_branches_address_id ON branches (address_id);

CREATE INDEX idx_services_branch_id_status ON services (branch_id, status);
CREATE INDEX idx_services_service_category_id ON services (service_category_id);

CREATE INDEX idx_service_bays_branch_id_status ON service_bays (branch_id, status);
CREATE INDEX idx_service_bays_service_bay_type_id ON service_bays (service_bay_type_id);

CREATE INDEX idx_equipment_branch_id_status ON equipment (branch_id, status);
CREATE INDEX idx_equipment_equipment_type_id ON equipment (equipment_type_id);

CREATE INDEX idx_employees_branch_id_status ON employees (branch_id, status);

CREATE INDEX idx_opening_hours_branch_id ON opening_hours (branch_id);
CREATE INDEX idx_opening_hours_exceptions_branch_id_date ON opening_hours_exceptions (branch_id, date);
