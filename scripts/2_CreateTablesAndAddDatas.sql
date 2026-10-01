CREATE DATABASE IF NOT EXISTS webshop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE webshop;

SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';
SET time_zone = '+00:00';

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(64) NOT NULL,
                       password_hash VARCHAR(200) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(120) NOT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users
(id, username, password_hash, full_name, email)
VALUES
    (1, 'admin',
     '123456',
     'Admin', 'admin@example.com');

CREATE TABLE products (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          name VARCHAR(120) NOT NULL,
                          description TEXT DEFAULT NULL,
                          price DECIMAL(10,2) NOT NULL,
                          PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO products (id, name, description, price)
VALUES
    (1, 'Matchapulver',
     'Japanskt grönt tepulver, 30 g',
     199.00),

    (2, 'Matchavisp',
     'Bambuvisp för att blanda matcha',
     99.00),

    (3, 'Matchaskål',
     'Matchagrön keramikskål med vågig kant',
     179.00),

    (4, 'Matchakopp',
     'Keramisk kopp med vågig kant för din matchalatte',
     129.00);

SELECT id, name, description, price
FROM products
ORDER BY id;