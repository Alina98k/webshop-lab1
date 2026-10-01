package com.example.webshop.dao;

import com.example.webshop.entities.Product;
import com.example.webshop.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO-klass som hanterar hämtning av produkter från databasen.
 */
public class ProductDaoJdbc {

    /**
     * Hämtar alla produkter från databasen.
     *
     * @return en lista med produkter
     */
    public List<Product> findAll() {
        String sql = "SELECT id, name, description, price " +
                "FROM products ORDER BY id";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Product> list = new ArrayList<>();

            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getBigDecimal("price"));
                list.add(p);
            }

            return list;

        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte hämta produkter", e);
        }
    }

    /**
     * Hämtar en specifik produkt utifrån dess id.
     *
     * @param id produktens id
     * @return produkten om den finns, annars null
     */
    public Product findById(Long id) {
        String sql = "SELECT id, name, description, price " +
                "FROM products WHERE id=?";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setLong(1, id);


            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getBigDecimal("price"));
                return p;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte hämta produkten", e);
        }
    }
}