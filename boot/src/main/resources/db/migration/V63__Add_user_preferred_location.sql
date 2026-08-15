SET search_path TO carfix;

-- Preferred location = the saved search location (city / region / country + geocoded centre), not a postal
-- address; it may be region- or country-only, hence plain columns instead of a city_id FK.
ALTER TABLE users
    ADD COLUMN preferred_city varchar(100) NULL,
    ADD COLUMN preferred_region varchar(100) NULL,
    ADD COLUMN preferred_country_iso varchar(2) NULL,
    ADD COLUMN preferred_latitude decimal(9,6) NULL,
    ADD COLUMN preferred_longitude decimal(9,6) NULL,
    ADD CONSTRAINT fk_users_preferred_country
        FOREIGN KEY (preferred_country_iso) REFERENCES countries (iso),
    ADD CONSTRAINT check_users_preferred_location_country
        CHECK (preferred_country_iso IS NOT NULL
               OR (preferred_city IS NULL AND preferred_region IS NULL
                   AND preferred_latitude IS NULL AND preferred_longitude IS NULL)),
    ADD CONSTRAINT check_users_preferred_coordinates
        CHECK ((preferred_latitude IS NULL) = (preferred_longitude IS NULL));
