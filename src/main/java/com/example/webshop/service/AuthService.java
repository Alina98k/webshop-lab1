package com.example.webshop.service;

import com.example.webshop.dao.UserDao;
import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
public class AuthService {

    private final UserDao userDao = new UserDaoJdbc();
    public User login(String username, String plainPassword) throws SQLException {
        User u = userDao.findByUsername(username);
        if (!BCrypt.checkpw(plainPassword, u.getPasswordHash())) return null;
        return u;
    }
}
