-- Initialize countries with VAT rates
INSERT INTO countries (name, vat_percent) VALUES
('Sweden', 25.0),
('Germany', 19.0),
('France', 20.0)
ON CONFLICT (name) DO NOTHING;

-- Sample products for testing
INSERT INTO products (id, name, base_price, country) VALUES
('prod-1', 'Laptop', 1000.0, 'Sweden'),
('prod-2', 'Mouse', 50.0, 'Sweden'),
('prod-3', 'Keyboard', 100.0, 'Germany'),
('prod-4', 'Monitor', 300.0, 'Germany'),
('prod-5', 'Headphones', 150.0, 'France'),
('prod-6', 'Webcam', 80.0, 'France')
ON CONFLICT (id) DO NOTHING;
