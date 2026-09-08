SET search_path TO carfix;

CREATE INDEX idx_bookings_car_profile_id_date ON bookings (car_profile_id, date);
CREATE INDEX idx_bookings_branch_id_date ON bookings (branch_id, date);

CREATE INDEX idx_car_profiles_customer_id ON car_profiles (customer_id);
CREATE INDEX idx_car_profiles_model_version_id ON car_profiles (model_version_id);
CREATE INDEX idx_car_profiles_file_id ON car_profiles (file_id);

CREATE INDEX idx_bookings_services_service_id ON bookings_services (service_id);
