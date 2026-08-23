SET search_path TO carfix;

-- V64 turned user_id into an optional account link; one account may back at most one employee row.
ALTER TABLE employees ADD CONSTRAINT uq_employees_user_id UNIQUE (user_id);
