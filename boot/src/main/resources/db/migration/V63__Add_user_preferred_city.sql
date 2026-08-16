SET search_path TO carfix;

-- City centre from the Google geocode of a preferred-location pick; nullable because seeded and
-- address-created cities have none until someone picks them.
ALTER TABLE cities
    ADD COLUMN latitude decimal(9,6) NULL,
    ADD COLUMN longitude decimal(9,6) NULL,
    ADD CONSTRAINT check_cities_coordinates CHECK ((latitude IS NULL) = (longitude IS NULL));

-- Preferred location = a pointer into the location hierarchy, not a copy of it.
ALTER TABLE users
    ADD COLUMN preferred_city_id int NULL,
    ADD CONSTRAINT fk_users_cities FOREIGN KEY (preferred_city_id) REFERENCES cities (city_id);
