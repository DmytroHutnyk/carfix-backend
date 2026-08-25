SET search_path TO carfix;

CREATE INDEX idx_services_name_trgm ON services USING gin (name gin_trgm_ops);
CREATE INDEX idx_service_categories_name_trgm ON service_categories USING gin (name gin_trgm_ops);
CREATE INDEX idx_branches_name_trgm ON branches USING gin (name gin_trgm_ops);
CREATE INDEX idx_cities_name_trgm ON cities USING gin (name gin_trgm_ops);
CREATE INDEX idx_regions_name_trgm ON regions USING gin (name gin_trgm_ops);

CREATE INDEX idx_addresses_latitude_longitude ON addresses (latitude, longitude);
CREATE INDEX idx_addresses_city_id ON addresses (city_id);

CREATE INDEX idx_cities_region_id ON cities (region_id);
CREATE INDEX idx_regions_countries_iso ON regions (countries_iso);
