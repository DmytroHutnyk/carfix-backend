SET search_path TO carfix;

CREATE INDEX idx_service_employee_requirements_service_id
    ON service_employee_requirements (service_id);
CREATE INDEX idx_service_equipment_requirements_service_id
    ON service_equipment_requirements (service_id);

CREATE INDEX idx_services_service_bay_types_service_bay_type_id
    ON services_service_bay_types (service_bay_type_id);
CREATE INDEX idx_service_employee_requirement_roles_role_id
    ON service_employee_requirement_roles (role_id);
CREATE INDEX idx_service_equipment_requirement_types_equipment_type_id
    ON service_equipment_requirement_types (equipment_type_id);
CREATE INDEX idx_employees_roles_role_id ON employees_roles (role_id);

CREATE INDEX idx_car_brands_branches_branch_id ON car_brands_branches (branch_id);
CREATE INDEX idx_branches_files_branch_id ON branches_files (branch_id);

CREATE INDEX idx_car_models_car_brand_id ON car_models (car_brand_id);
CREATE INDEX idx_model_versions_car_model_id ON model_versions (car_model_id);

CREATE INDEX idx_users_preferred_city_id ON users (preferred_city_id);

CREATE INDEX idx_service_bay_types_branch_id ON service_bay_types (branch_id);
CREATE INDEX idx_equipment_types_branch_id ON equipment_types (branch_id);
CREATE INDEX idx_roles_branch_id ON roles (branch_id);
