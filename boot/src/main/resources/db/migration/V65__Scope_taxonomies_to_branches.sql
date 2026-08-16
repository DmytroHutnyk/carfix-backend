SET search_path TO carfix;

-- Owner-defined taxonomies: a type belongs to the branch that declared it. NULL = platform row from the
-- V52 seed, shared by the seeded branches. No unique constraint on purpose (deferred-indexes rule):
-- uniqueness inside one registration is enforced by the application.
ALTER TABLE service_bay_types ADD COLUMN branch_id uuid NULL;
ALTER TABLE equipment_types   ADD COLUMN branch_id uuid NULL;
ALTER TABLE roles             ADD COLUMN branch_id uuid NULL;

ALTER TABLE service_bay_types ADD CONSTRAINT fk_service_bay_types_branches FOREIGN KEY (branch_id) REFERENCES branches (branch_id);
ALTER TABLE equipment_types   ADD CONSTRAINT fk_equipment_types_branches   FOREIGN KEY (branch_id) REFERENCES branches (branch_id);
ALTER TABLE roles             ADD CONSTRAINT fk_roles_branches             FOREIGN KEY (branch_id) REFERENCES branches (branch_id);
