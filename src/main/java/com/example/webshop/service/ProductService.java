package com.example.webshop.service;

import com.example.webshop.dao.*;
import com.example.webshop.entities.*;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDaoJdbc productDao = new ProductDaoJdbc();

    public List<Product> listProducts() throws SQLException {
        return productDao.findAll();
    }

    public Product get(Long id) throws SQLException {
        return productDao.findById(id);
    }
}