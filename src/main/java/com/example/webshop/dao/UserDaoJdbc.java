package com.example.webshop.dao;

import com.example.webshop.model.User;
import com.example.webshop.util.Db;

import java.sql.*;

/**
 * JDBC-baserad implementation av UserDao.
 * Används för att hämta en användare vid inloggning.
 */
public class UserDaoJdbc implements UserDao {

    /**
     * Returnerar en användare utifrån användarnamn.
     * Returnerar null om ingen användare hittas.
     */
    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email " +
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