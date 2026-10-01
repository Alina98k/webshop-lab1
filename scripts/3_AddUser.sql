USE webshop;

-- Lägger till testanvändaren om användarnamnet inte redan finns
INSERT INTO users
(username, password_hash, full_name, email)
SELECT 'admin', '123456', 'Admin', 'admin@example.com'
    WHERE NOT EXISTS (
    SELECT 1 FROM users WHERE username = 'admin'
);

SELECT id, username, password_hash
FROM users;