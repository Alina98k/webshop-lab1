package com.example.webshop.dao;

import com.example.webshop.model.User;
import java.sql.SQLException;
import java.util.List;
public interface UserDao {
    User findByUsername(String username) throws SQLException;
    List<String> getRoles(Long userId) throws SQLException;

    List<User> findAll() throws SQLException;
    User findById(Long id) throws SQLException;
    Long create(User u) throws SQLException;

    // om passwordHash är null, ändra inte lösenordet
    void update(User u) throws SQLException;

    void delete(Long id) throws SQLException;

    // roller som är definierade i systemet (roles-tabellen)
    List<String> listAllRoles() throws SQLException;

    // uppdatera user_roles (användarens roller)
    void setRoles(Long userId, List<String> roles) throws SQLException;
}
