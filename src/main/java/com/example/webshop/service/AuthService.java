package com.example.webshop.service;

import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {
    private final UserDaoJdbc userDao = new UserDaoJdbc();
    public User login(String username, String plainPassword) {
        try {
            User u = userDao.findByUsername(username);

            if (u == null) return null;

            if (!BCrypt.checkpw(plainPassword, u.getPasswordHash())) {
                return null;
            }

            return u;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
