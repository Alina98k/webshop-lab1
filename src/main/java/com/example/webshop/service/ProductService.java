package com.example.webshop.service;

import com.example.webshop.dao.*;
import com.example.webshop.model.*;

import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDao productDao = new ProductDaoJdbc();
    public List<Product> listActive() throws SQLException {
        return productDao.findAllActive();
    }

    public Product get(Long id) throws SQLException {
        return productDao.findById(id);
    }
}