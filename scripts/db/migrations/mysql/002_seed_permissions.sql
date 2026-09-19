-- ============================================================
-- Seed Initial Permissions and Assign to Roles
-- ============================================================

USE sports_events;

-- Insert core permissions for User Management
INSERT INTO permissions (code, name, description, status)
SELECT 'USERS_CREATE', 'Create Users', 'Allows creating new users in the platform', 1
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'USERS_CREATE'
);

INSERT INTO permissions (code, name, description, status)
SELECT 'USERS_READ', 'Read Users', 'Allows reading user information and profiles', 1
WHERE NOT EXISTS (
    SELECT 1 FROM permissions WHERE code = 'USERS_READ'
);

-- Assign permissions to ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r, permissions p
WHERE r.code = 'ADMIN' AND p.code IN ('USERS_CREATE', 'USERS_READ')
AND NOT EXISTS (
    SELECT 1 FROM role_permissions rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id
);
