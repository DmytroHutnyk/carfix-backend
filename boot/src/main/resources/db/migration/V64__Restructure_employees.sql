SET search_path TO carfix;

-- An owner registers staff as "first name, last name, role" — no login account exists at that point.
-- The employee gets its own identity; user_id becomes the optional link to an account. Seeded employees
-- were users with the same id, so they are back-filled from their user rows and linked 1:1.
ALTER TABLE employees DROP CONSTRAINT fk_employees_users;
ALTER TABLE employees RENAME COLUMN user_id TO employee_id;
ALTER TABLE employees ALTER COLUMN employee_id SET DEFAULT gen_random_uuid();

ALTER TABLE employees
    ADD COLUMN first_name varchar(50),
    ADD COLUMN last_name varchar(50),
    ADD COLUMN user_id uuid NULL,
    ALTER COLUMN salary DROP NOT NULL;

UPDATE employees e
SET first_name = u.name,
    last_name  = u.surname,
    user_id    = u.user_id
FROM users u
WHERE u.user_id = e.employee_id;

ALTER TABLE employees
    ALTER COLUMN first_name SET NOT NULL,
    ALTER COLUMN last_name SET NOT NULL,
    ADD CONSTRAINT fk_employees_users FOREIGN KEY (user_id) REFERENCES users (user_id);

ALTER TABLE employees_roles RENAME COLUMN user_id TO employee_id;
