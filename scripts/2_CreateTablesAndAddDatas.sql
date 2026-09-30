CREATE DATABASE IF NOT EXISTS webshop
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE webshop;

SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';
SET time_zone = '+00:00';

DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS users;

-- Användare
CREATE TABLE users (
                       id BIGINT NOT NULL AUTO_INCREMENT,
                       username VARCHAR(64) NOT NULL,
                       password_hash VARCHAR(200) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(120) NOT NULL,
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       active TINYINT(1) NOT NULL DEFAULT 1,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_users_username (username),
                       UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users
(id, username, password_hash, full_name, email, created_at, active)
VALUES
    (1, 'admin',
     '$2a$10$h5ZRQJ0/Xu/y.Q08yIEOJedM1d31GNEg/DIQrreu6vS7iNvsSjc1a',
     'Admin', 'admin@example.com', '2025-10-05 00:38:06', 1),

    (2, 'warehouse',
     '$2a$10$h5ZRQJ0/Xu/y.Q08yIEOJedM1d31GNEg/DIQrreu6vS7iNvsSjc1a',
     'Lagerpersonal', 'wh@webshop.com', '2025-10-05 01:14:42', 1);

-- Produkter
CREATE TABLE products (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          name VARCHAR(120) NOT NULL,
                          description TEXT DEFAULT NULL,
                          price DECIMAL(10,2) NOT NULL,
                          stock INT NOT NULL DEFAULT 0,
                          active TINYINT(1) NOT NULL DEFAULT 1,
                          PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO products (id, name, description, price)
VALUES
    (1, 'Matchapulver',
     'Japanskt grönt tepulver, 30 g',
     149.90),

    (2, 'Matchavisp',
     'Bambuvisp för att blanda matcha',
     99.00),

    (3, 'Matchaskål',
     'Matchagrön keramikskål med vågig kant',
     179.00),

    (4, 'Matchakopp',
     'Keramisk kopp med vågig kant för din matchalatte',
     129.00);

-- Kontrollera produkterna
SELECT id, name, description, price
FROM products
ORDER BY id;