package com.example.webshop.dao;

import com.example.webshop.model.Category;
import java.sql.SQLException;
import java.util.List;

public interface CategoryDao {
    List<Category> findAll() throws SQLException;
    Category findById(Long id) throws SQLException;
    Long create(Category c) throws SQLException;
    void update(Category c) throws SQLException;
    void delete(Long id) throws SQLException;
}
