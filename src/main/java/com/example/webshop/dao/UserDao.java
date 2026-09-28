package com.example.webshop.dao;

import com.example.webshop.model.User;
import java.sql.SQLException;
import java.util.List;
public interface UserDao {
    User findByUsername(String username) throws SQLException;

}
