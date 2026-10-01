package com.example.webshop.service;

import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.model.User;

import java.sql.SQLException;

public class UserService {
    private final UserDaoJdbc userDao = new UserDaoJdbc();

    public User findByUsername(String username) throws SQLException {
        return userDao.findByUsername(username);
    }

}
