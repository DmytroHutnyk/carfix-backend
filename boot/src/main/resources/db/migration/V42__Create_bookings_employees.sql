SET search_path TO carfix;

CREATE TABLE bookings_employees(
                                   booking_id uuid NOT NULL,
                                   employee_id uuid NOT NULL,

                                   CONSTRAINT pk_bookings_employees
                                       PRIMARY KEY (booking_id, employee_id)
);

