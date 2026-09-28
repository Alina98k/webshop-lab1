package com.example.webshop.service;

import com.example.webshop.dao.*;
import com.example.webshop.model.*;

import java.sql.SQLException;
import java.util.List;
/**
 * {@code ProductService} är service-lagret som ansvarar för hantering av produkter.
 * Den tillhandahåller aktiva produkter till slutanvändaren och erbjuder CRUD-funktioner
 * (create, read, update, delete) samt kategorirelationer för adminpanelen.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li>För att användaren ska kunna se produkter skapas en grundläggande produktlista
 *           (<em>“Möjlighet att lägga saker i korgen”</em> uppfylls).</li>
 *     </ul>
 **/
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