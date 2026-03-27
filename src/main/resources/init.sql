CREATE DATABASE IF NOT EXISTS vijayasreepos;
USE vijayasreepos;

-- Admin role
INSERT IGNORE INTO custom_roles (id, name, description, is_system, created_at, updated_at)
VALUES (1, 'Admin', 'Full access', true, NOW(), NOW());

-- All permissions for Admin
INSERT IGNORE INTO role_permissions (role_id, permission) VALUES
(1, 'PRODUCT_VIEW'), (1, 'PRODUCT_CREATE'), (1, 'PRODUCT_EDIT'),
(1, 'PRODUCT_DELETE'), (1, 'PRODUCT_IMPORT'), (1, 'PRODUCT_IMAGE_UPLOAD'),
(1, 'STOCK_VIEW'), (1, 'STOCK_ADJUST'), (1, 'STOCK_HISTORY'),
(1, 'STOCK_PURCHASE'), (1, 'SALES_CHECKOUT'), (1, 'SALES_APPLY_DISCOUNT'),
(1, 'REPORTS_DAILY'), (1, 'REPORTS_TRANSACTIONS'), (1, 'USER_VIEW'),
(1, 'USER_CREATE'), (1, 'USER_EDIT'), (1, 'USER_DEACTIVATE'),
(1, 'USER_GRANT_PERMISSION');

-- Ramamohan admin user (password: vst@2026)
INSERT IGNORE INTO users (name, username, password_hash, role_id, active, created_at)
VALUES ('Ramamohan', 'ramamohan',
'$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LPVNugpQXIS',
1, true, NOW());