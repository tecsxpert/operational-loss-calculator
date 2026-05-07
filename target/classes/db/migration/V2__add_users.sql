-- Passwords are 'password123' encrypted with BCrypt
INSERT INTO users (username, email, password, role, created_at, updated_at)
VALUES 
('admin', 'admin@example.com', '$2a$10$8.UnVuG9HLpUsXVq/Q7gH.o5U8D.D9f3PXZe3i.D.K52yA2Xv.H1q', 'ROLE_ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('user1', 'user1@example.com', '$2a$10$8.UnVuG9HLpUsXVq/Q7gH.o5U8D.D9f3PXZe3i.D.K52yA2Xv.H1q', 'ROLE_USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
