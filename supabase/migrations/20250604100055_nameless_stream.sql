-- Default roles
INSERT INTO roles (name) VALUES ('ROLE_ADMIN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_BARMAN') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_SERVER') ON CONFLICT (name) DO NOTHING;
INSERT INTO roles (name) VALUES ('ROLE_CASHIER') ON CONFLICT (name) DO NOTHING;

-- Default admin user (password: admin123)
INSERT INTO users (username, password, first_name, last_name, email, active)
VALUES ('admin', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Admin', 'User', 'admin@coffeeshop.com', true)
ON CONFLICT (username) DO NOTHING;

-- Assign admin role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN'
ON CONFLICT DO NOTHING;

-- Sample users
INSERT INTO users (username, password, first_name, last_name, email, active)
VALUES 
  ('barman1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'John', 'Barista', 'barman@coffeeshop.com', true),
  ('server1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Sarah', 'Server', 'server@coffeeshop.com', true),
  ('cashier1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Mike', 'Cashier', 'cashier@coffeeshop.com', true)
ON CONFLICT (username) DO NOTHING;

-- Assign roles to sample users
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'barman1' AND r.name = 'ROLE_BARMAN'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'server1' AND r.name = 'ROLE_SERVER'
ON CONFLICT DO NOTHING;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'cashier1' AND r.name = 'ROLE_CASHIER'
ON CONFLICT DO NOTHING;

-- Categories
INSERT INTO categories (name, description)
VALUES 
  ('Coffee', 'Hot and cold coffee drinks'),
  ('Tea', 'Variety of teas'),
  ('Pastries', 'Fresh baked goods'),
  ('Sandwiches', 'Fresh sandwiches'),
  ('Desserts', 'Sweet treats')
ON CONFLICT (name) DO NOTHING;

-- Products
INSERT INTO products (name, description, price, category_id, available, preparation_time_in_minutes)
VALUES 
  ('Espresso', 'Strong Italian coffee', 2.99, (SELECT id FROM categories WHERE name = 'Coffee'), true, 3),
  ('Cappuccino', 'Espresso with steamed milk and foam', 3.99, (SELECT id FROM categories WHERE name = 'Coffee'), true, 5),
  ('Latte', 'Espresso with steamed milk', 4.49, (SELECT id FROM categories WHERE name = 'Coffee'), true, 4),
  ('Americano', 'Espresso with hot water', 3.49, (SELECT id FROM categories WHERE name = 'Coffee'), true, 3),
  ('Green Tea', 'Traditional green tea', 2.99, (SELECT id FROM categories WHERE name = 'Tea'), true, 3),
  ('Croissant', 'Buttery, flaky pastry', 2.99, (SELECT id FROM categories WHERE name = 'Pastries'), true, 1),
  ('Chocolate Cake', 'Rich chocolate cake slice', 4.99, (SELECT id FROM categories WHERE name = 'Desserts'), true, 2),
  ('Ham & Cheese Sandwich', 'Classic ham and cheese', 6.99, (SELECT id FROM categories WHERE name = 'Sandwiches'), true, 8)
ON CONFLICT (name) DO NOTHING;

-- Product ingredients
INSERT INTO product_ingredients (product_id, ingredient)
VALUES 
  ((SELECT id FROM products WHERE name = 'Espresso'), 'Coffee beans'),
  ((SELECT id FROM products WHERE name = 'Cappuccino'), 'Coffee beans'),
  ((SELECT id FROM products WHERE name = 'Cappuccino'), 'Milk'),
  ((SELECT id FROM products WHERE name = 'Latte'), 'Coffee beans'),
  ((SELECT id FROM products WHERE name = 'Latte'), 'Milk'),
  ((SELECT id FROM products WHERE name = 'Americano'), 'Coffee beans'),
  ((SELECT id FROM products WHERE name = 'Americano'), 'Hot water'),
  ((SELECT id FROM products WHERE name = 'Green Tea'), 'Green tea leaves'),
  ((SELECT id FROM products WHERE name = 'Croissant'), 'Flour'),
  ((SELECT id FROM products WHERE name = 'Croissant'), 'Butter'),
  ((SELECT id FROM products WHERE name = 'Chocolate Cake'), 'Flour'),
  ((SELECT id FROM products WHERE name = 'Chocolate Cake'), 'Cocoa'),
  ((SELECT id FROM products WHERE name = 'Chocolate Cake'), 'Sugar'),
  ((SELECT id FROM products WHERE name = 'Ham & Cheese Sandwich'), 'Bread'),
  ((SELECT id FROM products WHERE name = 'Ham & Cheese Sandwich'), 'Ham'),
  ((SELECT id FROM products WHERE name = 'Ham & Cheese Sandwich'), 'Cheese')
ON CONFLICT DO NOTHING;

-- Tables
INSERT INTO coffee_tables (table_number, capacity, status)
VALUES 
  (1, 2, 'AVAILABLE'),
  (2, 2, 'AVAILABLE'),
  (3, 4, 'AVAILABLE'),
  (4, 4, 'AVAILABLE'),
  (5, 6, 'AVAILABLE'),
  (6, 6, 'AVAILABLE'),
  (7, 8, 'AVAILABLE'),
  (8, 2, 'AVAILABLE')
ON CONFLICT DO NOTHING;

-- Assign some tables to server
UPDATE coffee_tables 
SET assigned_server_id = (SELECT id FROM users WHERE username = 'server1')
WHERE table_number IN (1, 2, 3, 4)
AND assigned_server_id IS NULL;