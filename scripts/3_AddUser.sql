USE `webshop`;

-- Lägg till en ny kund i users-tabellen
INSERT INTO `users` (id, username, password_hash, full_name, email, created_at, active)
VALUES
    (3, 'user',
     '$2a$10$h5ZRQJ0/Xu/y.Q08yIEOJedM1d31GNEg/DIQrreu6vS7iNvsSjc1a', -- bcrypt(123456)
     'Kund', 'user@webshop.com', NOW(), 1);

-- Tilldela roll: CUSTOMER -> id=1
INSERT INTO `user_roles` (user_id, role_id) VALUES (3, 1);
