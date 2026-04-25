-- Seed enough master data for dropdowns and assignment demo flows.
INSERT INTO countries (name) VALUES
    ('Sri Lanka'),
    ('India'),
    ('United States'),
    ('United Kingdom'),
    ('Australia'),
    ('Canada'),
    ('Singapore'),
    ('Germany'),
    ('Japan'),
    ('United Arab Emirates');

INSERT INTO cities (name, country_id)
-- City inserts resolve country IDs by name so seed data stays readable.
SELECT 'Colombo', id FROM countries WHERE name = 'Sri Lanka';
INSERT INTO cities (name, country_id)
SELECT 'Kandy', id FROM countries WHERE name = 'Sri Lanka';
INSERT INTO cities (name, country_id)
SELECT 'Galle', id FROM countries WHERE name = 'Sri Lanka';
INSERT INTO cities (name, country_id)
SELECT 'Jaffna', id FROM countries WHERE name = 'Sri Lanka';
INSERT INTO cities (name, country_id)
SELECT 'Negombo', id FROM countries WHERE name = 'Sri Lanka';
INSERT INTO cities (name, country_id)
SELECT 'Matara', id FROM countries WHERE name = 'Sri Lanka';

INSERT INTO cities (name, country_id)
SELECT 'Chennai', id FROM countries WHERE name = 'India';
INSERT INTO cities (name, country_id)
SELECT 'Mumbai', id FROM countries WHERE name = 'India';
INSERT INTO cities (name, country_id)
SELECT 'Bangalore', id FROM countries WHERE name = 'India';
INSERT INTO cities (name, country_id)
SELECT 'Delhi', id FROM countries WHERE name = 'India';

INSERT INTO cities (name, country_id)
SELECT 'New York', id FROM countries WHERE name = 'United States';
INSERT INTO cities (name, country_id)
SELECT 'Los Angeles', id FROM countries WHERE name = 'United States';
INSERT INTO cities (name, country_id)
SELECT 'Chicago', id FROM countries WHERE name = 'United States';

INSERT INTO cities (name, country_id)
SELECT 'London', id FROM countries WHERE name = 'United Kingdom';
INSERT INTO cities (name, country_id)
SELECT 'Manchester', id FROM countries WHERE name = 'United Kingdom';

INSERT INTO cities (name, country_id)
SELECT 'Sydney', id FROM countries WHERE name = 'Australia';
INSERT INTO cities (name, country_id)
SELECT 'Melbourne', id FROM countries WHERE name = 'Australia';

INSERT INTO cities (name, country_id)
SELECT 'Toronto', id FROM countries WHERE name = 'Canada';
INSERT INTO cities (name, country_id)
SELECT 'Vancouver', id FROM countries WHERE name = 'Canada';

INSERT INTO cities (name, country_id)
SELECT 'Singapore', id FROM countries WHERE name = 'Singapore';

INSERT INTO cities (name, country_id)
SELECT 'Berlin', id FROM countries WHERE name = 'Germany';
INSERT INTO cities (name, country_id)
SELECT 'Munich', id FROM countries WHERE name = 'Germany';

INSERT INTO cities (name, country_id)
SELECT 'Tokyo', id FROM countries WHERE name = 'Japan';
INSERT INTO cities (name, country_id)
SELECT 'Osaka', id FROM countries WHERE name = 'Japan';

INSERT INTO cities (name, country_id)
SELECT 'Dubai', id FROM countries WHERE name = 'United Arab Emirates';
INSERT INTO cities (name, country_id)
SELECT 'Abu Dhabi', id FROM countries WHERE name = 'United Arab Emirates';
