CREATE TABLE customers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(150) NOT NULL,
    date_of_birth DATE NOT NULL,
    nic_number VARCHAR(50) NOT NULL,
    -- Audit timestamps are database-managed to keep inserts and updates simple.
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE mobile_numbers (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    mobile_number VARCHAR(30) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_mobile_numbers_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id)
        -- Child contact rows should be removed with the owning customer.
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE addresses (
    id BIGINT NOT NULL AUTO_INCREMENT,
    customer_id BIGINT NOT NULL,
    address_line1 VARCHAR(255),
    address_line2 VARCHAR(255),
    city_id BIGINT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_addresses_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_addresses_city
        -- Country is derived through city_id, so addresses do not duplicate country_id.
        FOREIGN KEY (city_id)
        REFERENCES cities (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Family members are modeled as customer-to-customer links.
CREATE TABLE customer_family (
    customer_id BIGINT NOT NULL,
    family_member_id BIGINT NOT NULL,
    PRIMARY KEY (customer_id, family_member_id),
    CONSTRAINT fk_customer_family_customer
        FOREIGN KEY (customer_id)
        REFERENCES customers (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_customer_family_member
        FOREIGN KEY (family_member_id)
        REFERENCES customers (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_customer_family_not_self
        -- The API should also prevent this, but the database keeps the invariant safe.
        CHECK (customer_id <> family_member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
