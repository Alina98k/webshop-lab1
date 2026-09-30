package com.example.webshop.service;

import com.example.webshop.dao.UserDao;
import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
public class UserService {
    private final UserDao userDao = new UserDaoJdbc();

    public User findByUsername(String username) throws SQLException {
        return userDao.findByUsername(username);
    }

}
