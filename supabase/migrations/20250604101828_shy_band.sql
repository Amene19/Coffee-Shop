-- Default roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_BARMAN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_SERVER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_CASHIER') ON CONFLICT (name) DO NOTHING;

-- Default admin user (password: admin123)
INSERT INTO users (username, password, first_name, last_name, email, active)
VALUES ('admin', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Admin', 'User', 'admin@coffeeshop.com', true);

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Sample users
INSERT INTO users (username, password, first_name, last_name, email, active)
VALUES 
  ('barman1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'John', 'Barista', 'barman@coffeeshop.com', true),
  ('server1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Sarah', 'Server', 'server@coffeeshop.com', true),
  ('cashier1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Mike', 'Cashier', 'cashier@coffeeshop.com', true);

-- Assign roles to sample users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'barman1' AND r.name = 'ROLE_BARMAN';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'server1' AND r.name = 'ROLE_SERVER';

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'cashier1' AND r.name = 'ROLE_CASHIER';

-- Categories
INSERT INTO categories (name, description)
VALUES 
  ('Coffee', 'Hot and cold coffee drinks'),
  ('Tea', 'Variety of teas'),
  ('Pastries', 'Fresh baked goods'),
  ('Sandwiches', 'Fresh sandwiches'),
  ('Desserts', 'Sweet treats');

-- Products
INSERT INTO products (name, description, price, category_id, available, preparation_time_in_minutes)
SELECT 'Espresso', 'Strong Italian coffee', 2.99, id, true, 3 FROM categories WHERE name = 'Coffee'
UNION ALL
SELECT 'Cappuccino', 'Espresso with steamed milk and foam', 3.99, id, true, 5 FROM categories WHERE name = 'Coffee'
UNION ALL
SELECT 'Green Tea', 'Traditional green tea', 2.99, id, true, 3 FROM categories WHERE name = 'Tea'
UNION ALL
SELECT 'Croissant', 'Buttery, flaky pastry', 2.99, id, true, 1 FROM categories WHERE name = 'Pastries';

-- Tables
INSERT INTO coffee_tables (table_number, capacity, status)
VALUES 
  (1, 2, 'AVAILABLE'),
  (2, 2, 'AVAILABLE'),
  (3, 4, 'AVAILABLE'),
  (4, 4, 'AVAILABLE');

-- Assign some tables to server
UPDATE coffee_tables 
SET assigned_server_id = (SELECT id FROM users WHERE username = 'server1')
WHERE table_number IN (1, 2);