package com.example.webshop.service;

import com.example.webshop.dao.*;
import com.example.webshop.entities.*;

import java.util.List;

/**
 * Service-klass som hanterar produkter i webshopen.
 */
public class ProductService {

    private final ProductDaoJdbc productDao = new ProductDaoJdbc();

    /**
     * Hämtar alla aktiva produkter.
     * @return en lista med produkter
     */
    public List<Product> listAll() {
        return productDao.findAll();
    }

    /**
     * Hämtar en produkt utifrån dess id.
     *
     * @param id produktens id
     * @return produkten om den finns, annars null
     */
    public Product get(Long id) {
        return productDao.findById(id);
    }
}