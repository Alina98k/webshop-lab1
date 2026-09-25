package com.example.webshop.dao;

import com.example.webshop.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ProductDao {
    List<Product> findAllActive() throws SQLException;
    Product findById(Long id) throws SQLException;

    // För admin:
    List<Product> findAll() throws SQLException;
    Long create(Product p) throws SQLException;
    void update(Product p) throws SQLException;
    void delete(Long id) throws SQLException;

    void updateStock(Connection tx, Long productId, int delta) throws SQLException;
}
