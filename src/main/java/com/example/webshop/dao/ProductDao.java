package com.example.webshop.dao;

import com.example.webshop.model.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface ProductDao {
    List<Product> findAllActive() throws SQLException;
    Product findById(Long id) throws SQLException;
    // För admin:
}
