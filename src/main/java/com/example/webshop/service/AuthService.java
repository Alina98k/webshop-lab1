package com.example.webshop.service;

import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.entities.User;

/**
 * Service-klass som hanterar inloggning och kontroll av användarens lösenord.
 */
public class AuthService {

    private final UserDaoJdbc userDao = new UserDaoJdbc();

    /**
     * Kontrollerar användarens inloggningsuppgifter.
     *
     * @param username användarens användarnamn
     * @param plainPassword lösenordet som användaren anger
     * @return användaren om inloggningen lyckas, annars null
     */
    public User login(String username, String plainPassword) {
        User u = userDao.findByUsername(username);

        if (u == null) return null;
        if (!plainPassword.equals(u.getPasswordHash())) {
            return null;
        }

        return u;
    }
}
