CREATE DATABASE IF NOT EXISTS vijayasreepos;
USE vijayasreepos;

-- Admin role
INSERT IGNORE INTO custom_roles (id, name, description, is_system, created_at, updated_at)
VALUES (100, 'Admin', 'Full access', true, NOW(), NOW());

-- All permissions for Admin
INSERT IGNORE INTO role_permissions (role_id, permission) VALUES
(100, 'PRODUCT_VIEW'), (100, 'PRODUCT_CREATE'), (100, 'PRODUCT_EDIT'),
(100, 'PRODUCT_DELETE'), (100, 'PRODUCT_IMPORT'), (100, 'PRODUCT_IMAGE_UPLOAD'),
(100, 'STOCK_VIEW'), (100, 'STOCK_ADJUST'), (100, 'STOCK_HISTORY'),
(100, 'STOCK_PURCHASE'), (100, 'SALES_CHECKOUT'), (100, 'SALES_APPLY_DISCOUNT'),
(100, 'REPORTS_DAILY'), (100, 'REPORTS_TRANSACTIONS'), (100, 'USER_VIEW'),
(100, 'USER_CREATE'), (100, 'USER_EDIT'), (100, 'USER_DEACTIVATE'),
(100, 'USER_GRANT_PERMISSION'), (100, 'PRINT_STATION');

-- Ramamohan admin user (password: vst@2026)
INSERT IGNORE INTO users (name, username, password_hash, role_id, active, created_at)
VALUES ('Ramamohan', 'ramamohan',
'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVNugpQXIS',
1, true, NOW());