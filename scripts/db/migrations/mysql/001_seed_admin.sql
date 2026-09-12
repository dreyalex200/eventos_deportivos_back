-- ============================================================
-- Seed Initial Administrator User
-- ============================================================

USE sports_events;

-- BCrypt hash for password 'Prueba123+'
SET @admin_email = 'admin@sportsevents.com';
SET @admin_username = 'admin';
SET @admin_password_hash = '$2a$10$w09k2tWfZ0pW7iF2w6sSGeOeV9KzJgJ1zF3v4o2V7a0oY.R3jK6k6';

INSERT INTO users (username, password_hash, email, first_name, last_name, phone, status)
SELECT @admin_username, @admin_password_hash, @admin_email, 'Admin', 'SportsEvents', '+573001234567', 1
WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE email = @admin_email
);

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
