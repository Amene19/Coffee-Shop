-- Default roles
INSERT IGNORE INTO roles (name) VALUES ('ROLE_ADMIN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_BARMAN');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_SERVER');
INSERT IGNORE INTO roles (name) VALUES ('ROLE_CASHIER');

-- Default admin user (password: admin123)
INSERT IGNORE INTO users (username, password, first_name, last_name, email, active)
VALUES ('admin', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Admin', 'User', 'admin@coffeeshop.com', true);

-- Assign admin role to admin user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'ROLE_ADMIN';

-- Default barman user (password: admin123)
INSERT IGNORE INTO users (username, password, first_name, last_name, email, active)
VALUES ('barman1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Barman', 'User', 'barman1@coffeeshop.com', true);

-- Assign barman role to barman user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'barman1' AND r.name = 'ROLE_BARMAN';

-- Default server user (password: admin123)
INSERT IGNORE INTO users (username, password, first_name, last_name, email, active)
VALUES ('server1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Server', 'User', 'server1@coffeeshop.com', true);

-- Assign server role to server user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'server1' AND r.name = 'ROLE_SERVER';

-- Default cashier user (password: admin123)
INSERT IGNORE INTO users (username, password, first_name, last_name, email, active)
VALUES ('cashier1', '$2a$10$5.bLsuE/p7NUZVnYrQUQCOTmIZK1iLcVj9Z7XlPXO.mRoEEyJRJkC', 'Cashier', 'User', 'cashier1@coffeeshop.com', true);

-- Assign cashier role to cashier user
INSERT IGNORE INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'cashier1' AND r.name = 'ROLE_CASHIER'; 