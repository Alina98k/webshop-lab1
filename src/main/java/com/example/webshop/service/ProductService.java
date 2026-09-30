package com.example.webshop.service;
import com.example.webshop.dao.ProductDao;
import com.example.webshop.dao.ProductDaoJdbc;
import com.example.webshop.model.Product;

import java.sql.SQLException;
import java.util.List;
public class ProductService {

    /** DAO för åtkomst till produkter. */
    private final ProductDao productDao = new ProductDaoJdbc();

    /**
     * Returnerar endast aktiva (till försäljning) produkter.
     *
     * @return lista över aktiva produkter
     * @throws SQLException vid databasfel
     */
    public List<Product> listActive() throws SQLException {
        return productDao.findAllActive();
    }

    /**
     * Returnerar produkten med angivet ID.
     *
     * @param id produktens ID
     * @return {@link Product} eller {@code null} om ej hittad
     * @throws SQLException vid databasfel
     */
    public Product get(Long id) throws SQLException {
        return productDao.findById(id);
    }
}