CREATE UNIQUE INDEX uk_customers_nic_number ON customers (nic_number);
CREATE INDEX idx_customers_name ON customers (name);
CREATE INDEX idx_customers_created_at ON customers (created_at);

CREATE INDEX idx_mobile_numbers_customer_id ON mobile_numbers (customer_id);

CREATE INDEX idx_addresses_customer_id ON addresses (customer_id);
CREATE INDEX idx_addresses_city_id ON addresses (city_id);

CREATE INDEX idx_cities_country_id ON cities (country_id);
CREATE INDEX idx_cities_name ON cities (name);

CREATE INDEX idx_customer_family_member_id ON customer_family (family_member_id);
