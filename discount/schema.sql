-- Create countries table
CREATE TABLE IF NOT EXISTS countries (
    name VARCHAR(50) PRIMARY KEY,
    vat_percent DOUBLE PRECISION NOT NULL
);

-- Create products table
CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    base_price DOUBLE PRECISION NOT NULL,
    country VARCHAR(50) NOT NULL REFERENCES countries(name)
);

-- Create discounts table
CREATE TABLE IF NOT EXISTS discounts (
    product_id VARCHAR(255) NOT NULL REFERENCES products(id),
    discount_id VARCHAR(255) NOT NULL,
    percent DOUBLE PRECISION NOT NULL,
    PRIMARY KEY (product_id, discount_id)
);
