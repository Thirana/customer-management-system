-- Development-only seed data for quickly testing GET endpoints in Postman.
-- Safe to rerun: this script removes only customers with the DEV-NIC-* prefix first.

START TRANSACTION;

DELETE FROM customers
WHERE nic_number LIKE 'DEV-NIC-%';

-- Resolve city IDs by name so the script remains stable even if auto-increment values differ.
SET @colombo_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Colombo' AND co.name = 'Sri Lanka'
    LIMIT 1
);
SET @kandy_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Kandy' AND co.name = 'Sri Lanka'
    LIMIT 1
);
SET @galle_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Galle' AND co.name = 'Sri Lanka'
    LIMIT 1
);
SET @chennai_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Chennai' AND co.name = 'India'
    LIMIT 1
);
SET @london_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'London' AND co.name = 'United Kingdom'
    LIMIT 1
);
SET @sydney_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Sydney' AND co.name = 'Australia'
    LIMIT 1
);
SET @toronto_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Toronto' AND co.name = 'Canada'
    LIMIT 1
);
SET @dubai_city_id := (
    SELECT c.id
    FROM cities c
    JOIN countries co ON co.id = c.country_id
    WHERE c.name = 'Dubai' AND co.name = 'United Arab Emirates'
    LIMIT 1
);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Nimal Perera', '1988-03-14', 'DEV-NIC-001');
SET @customer_nimal_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES
    (@customer_nimal_id, '0771111001'),
    (@customer_nimal_id, '0711111001');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_nimal_id, '12 Lake Road', 'Colombo 03', @colombo_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Anjali Fernando', '1992-07-22', 'DEV-NIC-002');
SET @customer_anjali_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES (@customer_anjali_id, '0771111002');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_anjali_id, '41 Hill Street', 'Kandy', @kandy_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Kasun Silva', '1985-11-08', 'DEV-NIC-003');
SET @customer_kasun_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES
    (@customer_kasun_id, '0771111003'),
    (@customer_kasun_id, '0751111003');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_kasun_id, '8 Fort Lane', 'Galle', @galle_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Meera Raman', '1995-01-30', 'DEV-NIC-004');
SET @customer_meera_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES (@customer_meera_id, '0771111004');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_meera_id, '22 Marina Road', 'Chennai', @chennai_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Oliver Smith', '1981-09-18', 'DEV-NIC-005');
SET @customer_oliver_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES (@customer_oliver_id, '0771111005');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_oliver_id, '15 Baker Street', 'London', @london_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Grace Wilson', '1990-12-05', 'DEV-NIC-006');
SET @customer_grace_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES
    (@customer_grace_id, '0771111006'),
    (@customer_grace_id, '0721111006');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_grace_id, '90 Harbour View', 'Sydney', @sydney_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Ethan Brown', '1979-05-26', 'DEV-NIC-007');
SET @customer_ethan_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES (@customer_ethan_id, '0771111007');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_ethan_id, '17 Maple Avenue', 'Toronto', @toronto_city_id);

INSERT INTO customers (name, date_of_birth, nic_number)
VALUES ('Dev Ayesha Khan', '1993-10-11', 'DEV-NIC-008');
SET @customer_ayesha_id := LAST_INSERT_ID();

INSERT INTO mobile_numbers (customer_id, mobile_number)
VALUES (@customer_ayesha_id, '0771111008');
INSERT INTO addresses (customer_id, address_line1, address_line2, city_id)
VALUES
    (@customer_ayesha_id, '33 Creek Tower', 'Dubai', @dubai_city_id);

-- Lightweight family links make the detail endpoint more useful during manual checks.
INSERT INTO customer_family (customer_id, family_member_id)
VALUES
    (@customer_nimal_id, @customer_anjali_id),
    (@customer_anjali_id, @customer_nimal_id),
    (@customer_kasun_id, @customer_meera_id),
    (@customer_grace_id, @customer_ethan_id),
    (@customer_ayesha_id, @customer_oliver_id);

COMMIT;

SELECT
    id,
    name,
    date_of_birth,
    nic_number
FROM customers
WHERE nic_number LIKE 'DEV-NIC-%'
ORDER BY name;
