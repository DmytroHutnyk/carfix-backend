SET search_path TO carfix;

CREATE INDEX idx_service_bays_availability_service_bay_id_date
    ON service_bays_availability (service_bay_id, date);
CREATE INDEX idx_service_bays_bookings_service_bay_id_date
    ON service_bays_bookings (service_bay_id, date);

CREATE INDEX idx_employees_availability_employee_id_date
    ON employees_availability (employee_id, date);
CREATE INDEX idx_employees_bookings_employee_id_date
    ON employees_bookings (employee_id, date);

CREATE INDEX idx_equipment_availability_equipment_id_date
    ON equipment_availability (equipment_id, date);
CREATE INDEX idx_equipment_bookings_equipment_id_date
    ON equipment_bookings (equipment_id, date);

CREATE INDEX idx_service_bays_bookings_booking_id ON service_bays_bookings (booking_id);
CREATE INDEX idx_employees_bookings_booking_id ON employees_bookings (booking_id);
CREATE INDEX idx_equipment_bookings_booking_id ON equipment_bookings (booking_id);

CREATE INDEX idx_service_bays_availability_series_id
    ON service_bays_availability (series_id) WHERE series_id IS NOT NULL;
CREATE INDEX idx_employees_availability_series_id
    ON employees_availability (series_id) WHERE series_id IS NOT NULL;
CREATE INDEX idx_equipment_availability_series_id
    ON equipment_availability (series_id) WHERE series_id IS NOT NULL;
