-- ============================================================
-- Seed Initial Administrator User
-- ============================================================

USE sports_events;

-- BCrypt hash for password 'Prueba123+'
SET @admin_email = 'admin@sportsevents.com';
SET @admin_username = 'admin';
SET @admin_password_hash = '$2a$10$vW/3HAb0AfAGATzWlptP1e8oifwxR3gVd1FWO1/YhQkKxLCf0oi1q';

INSERT INTO users (username, password_hash, email, first_name, last_name, phone, status)
SELECT @admin_username, @admin_password_hash, @admin_email, 'Admin', 'SportsEvents', '+573001234567', 1
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = @admin_email
);

UPDATE users
SET password_hash = @admin_password_hash
WHERE email = @admin_email;

INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE u.email = @admin_email AND r.code = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    JOIN users u2 ON ur.user_id = u2.id
    JOIN roles r2 ON ur.role_id = r2.id
    WHERE u2.email = @admin_email AND r2.code = 'ADMIN'
);
