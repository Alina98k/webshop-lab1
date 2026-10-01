package com.example.webshop.dao;
import com.example.webshop.entities.User;
import com.example.webshop.util.Db;

import java.sql.*;

/**
 * DAO-klass som hanterar hämtning av användare från databasen.
 */
public class UserDaoJdbc {

    /**
     * Hämtar en användare utifrån användarnamn.
     *
     * @param username användarens användarnamn
     * @return användaren om den finns, annars null
     */
    public User findByUsername(String username) {
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

        } catch (SQLException e) {
            throw new RuntimeException("Kunde inte hämta användaren", e);
        }
    }
}
