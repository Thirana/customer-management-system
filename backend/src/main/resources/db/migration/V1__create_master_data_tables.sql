-- Master data is created before customer tables so addresses can reference cities.
CREATE TABLE countries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_countries_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE cities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    country_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    -- The same city name can exist in different countries, but not twice in one country.
    CONSTRAINT uk_cities_name_country UNIQUE (name, country_id),
    CONSTRAINT fk_cities_country
        FOREIGN KEY (country_id)
        REFERENCES countries (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
