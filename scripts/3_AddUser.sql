USE `webshop`;

 INSERT INTO users
    (id, username, password_hash, full_name, email)
    VALUES
        (1, 'admin',
         '123456',
         'Admin', 'admin@example.com');
