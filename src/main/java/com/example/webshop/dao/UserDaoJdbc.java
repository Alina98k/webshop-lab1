package com.example.webshop.dao;

import com.example.webshop.model.User;
import com.example.webshop.util.Db;

import java.sql.*;

public class UserDaoJdbc implements UserDao {
    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email, active " +
                "FROM users WHERE username=?";

        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                User u = new User();
                u.setId(rs.getLong("id"));
                u.setUsername(rs.getString("username"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setFullName(rs.getString("full_name"));
                u.setEmail(rs.getString("email"));

                return u;
            }
        }
    }
}