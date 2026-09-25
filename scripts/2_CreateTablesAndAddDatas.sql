--- =========================================
-- Webshop – Ren installation SQL (MySQL/InnoDB)
-- =========================================

-- 1) Skapa databasen och välj den
CREATE DATABASE IF NOT EXISTS `webshop`
  CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE `webshop`;

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";
/*!40101 SET NAMES utf8mb4 */;

-- 2) Stäng av främmande nyckelkontroller tillfälligt och ta bort tabeller i ordning
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS order_items;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS categories;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS roles;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- KATEGORIER
-- =========================
CREATE TABLE categories (
                            id BIGINT NOT NULL AUTO_INCREMENT,
                            name VARCHAR(80) NOT NULL,
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_categories_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO categories (id, name) VALUES
                                      (3, 'Insekter'),
                                      (1, 'Test');

-- =========================
-- ROLLER
-- =========================
CREATE TABLE roles (
                       id INT NOT NULL AUTO_INCREMENT,
                       name VARCHAR(32) NOT NULL,
                       PRIMARY KEY (id),
                       UNIQUE KEY uk_roles_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO roles (id, name) VALUES
                                 (2, 'ADMIN'),
                                 (1, 'CUSTOMER'),
                                 (3, 'WAREHOUSE');

-- =========================
-- ANVÄNDARE
-- =========================
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO users (id, username, password_hash, full_name, email, created_at, active) VALUES
                                                                                          (1, 'admin', '$2a$10$h5ZRQJ0/Xu/y.Q08yIEOJedM1d31GNEg/DIQrreu6vS7iNvsSjc1a', 'Admin', 'admin@example.com', '2025-10-05 00:38:06', 1),
                                                                                          (2, 'warehouse', '$2a$10$h5ZRQJ0/Xu/y.Q08yIEOJedM1d31GNEg/DIQrreu6vS7iNvsSjc1a', 'Lagerpersonal', 'wh@webshop.com', '2025-10-05 01:14:42', 1);

-- =========================
-- PRODUKTER
-- =========================
CREATE TABLE products (
                          id BIGINT NOT NULL AUTO_INCREMENT,
                          category_id BIGINT DEFAULT NULL,
                          name VARCHAR(120) NOT NULL,
                          description TEXT DEFAULT NULL,
                          price DECIMAL(10,2) NOT NULL,
                          stock INT NOT NULL DEFAULT 0,
                          active TINYINT(1) NOT NULL DEFAULT 1,
                          PRIMARY KEY (id),
                          KEY idx_products_category (category_id),
                          CONSTRAINT fk_products_category
                              FOREIGN KEY (category_id) REFERENCES categories(id)
                                  ON UPDATE RESTRICT ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO products (id, category_id, name, description, price, stock, active) VALUES
                                                                                    (1, NULL, 'Kaffekopp', 'Keramisk kopp', 129.90, 10, 1),
                                                                                    (2, NULL, 'Anteckningsbok', 'A5 linjerad anteckningsbok', 39.90, 7, 1),
                                                                                    (3, NULL, 'Mus', 'Trådbunden optisk mus', 199.00, 0, 1),
                                                                                    (4, 3, 'Testprodukt', '', 100.00, 200, 1);

-- =========================
-- BESTÄLLNINGAR
-- =========================
CREATE TABLE orders (
                        id BIGINT NOT NULL AUTO_INCREMENT,
                        user_id BIGINT NOT NULL,
                        status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
                        total DECIMAL(10,2) NOT NULL,
                        created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        PRIMARY KEY (id),
                        KEY idx_orders_user (user_id),
                        CONSTRAINT fk_orders_user
                            FOREIGN KEY (user_id) REFERENCES users(id)
                                ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO orders (id, user_id, status, total, created_at) VALUES
                                                                (3, 1, 'SHIPPED', 39.90, '2025-10-05 01:40:46'),
                                                                (4, 1, 'PACKED', 129.90, '2025-10-05 01:41:04'),
                                                                (5, 1, 'PACKED', 39.90, '2025-10-05 01:41:10'),
                                                                (6, 1, 'PACKED', 1596.00, '2025-10-05 01:41:17'),
                                                                (7, 1, 'PACKED', 169.80, '2025-10-05 02:56:15');

-- =========================
-- BESTÄLLNINGSRADER
-- =========================
CREATE TABLE order_items (
                             id BIGINT NOT NULL AUTO_INCREMENT,
                             order_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             unit_price DECIMAL(10,2) NOT NULL,
                             PRIMARY KEY (id),
                             KEY idx_oi_order (order_id),
                             KEY idx_oi_product (product_id),
                             CONSTRAINT fk_oi_order
                                 FOREIGN KEY (order_id) REFERENCES orders(id)
                                     ON UPDATE RESTRICT ON DELETE CASCADE,
                             CONSTRAINT fk_oi_product
                                 FOREIGN KEY (product_id) REFERENCES products(id)
                                     ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO order_items (id, order_id, product_id, quantity, unit_price) VALUES
                                                                             (1, 3, 2, 1, 39.90),
                                                                             (2, 4, 1, 1, 129.90),
                                                                             (3, 5, 2, 1, 39.90),
                                                                             (4, 6, 2, 40, 39.90),
                                                                             (5, 7, 2, 1, 39.90),
                                                                             (6, 7, 1, 1, 129.90);

-- =========================
-- ANVÄNDARROLLER (sammansatt primärnyckel)
-- =========================
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id INT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            KEY idx_ur_role (role_id),
                            CONSTRAINT fk_ur_user
                                FOREIGN KEY (user_id) REFERENCES users(id)
                                    ON UPDATE RESTRICT ON DELETE CASCADE,
                            CONSTRAINT fk_ur_role
                                FOREIGN KEY (role_id) REFERENCES roles(id)
                                    ON UPDATE RESTRICT ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

INSERT INTO user_roles (user_id, role_id) VALUES
                                              (1, 2),
                                              (2, 3);
