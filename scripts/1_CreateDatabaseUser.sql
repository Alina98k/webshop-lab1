-- Skapar databasen och en användare med rättigheter till webshop
CREATE DATABASE IF NOT EXISTS webshop CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

CREATE USER 'webshop'@'%' IDENTIFIED BY 'webshop';

GRANT ALL PRIVILEGES ON webshop.* TO 'webshop'@'%';

FLUSH PRIVILEGES;