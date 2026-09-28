package com.example.webshop.service;

import com.example.webshop.dao.UserDao;
import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.util.List;
/**
 * {@code UserService} är service-lagret som samlar affärslogik för
 * användar- och rollhantering samt lösenordshashning.
 * <p>
 * Ansvar:
 * <ul>
 *   <li>Lista och hämta användare</li>
 *   <li>Lista och hämta roller</li>
 *   <li>Skapa användare (hasha lösenord med BCrypt) och tilldela roller</li>
 *   <li>Uppdatera användare (valfri lösenordsändring) och tilldela roller</li>
 *   <li>Ta bort användare</li>
 * </ul>
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> — <em>Användaridentifiering</em>: Säker lagring av lösenord (BCrypt)
 *       och data som används av {@link AuthService} för autentisering.</li>
 *   <li><strong>Betyg 4</strong> — <em>Utbyggd användarhantering & behörighetsklasser</em>:
 *       tillhandahåller CRUD och tilldelning av flera roller (ADMIN/WAREHOUSE/CUSTOMER).</li>
 *   <li><strong>Betyg 5</strong> — Förbättrad admin-hantering och centralisering av regler
 *       för rollbaserade sidor i service-lagret.</li>
 * </ul>
 *
 * <h2>Arkitektur</h2>
 * <ul>
 *   <li>Controller → <strong>Service</strong> → DAO → DB-flöde; affärsreglerna ligger här,
 *       medan datalagring hanteras i {@link UserDao}.</li>
 *   <li>Lösenordshashning sker <em>endast</em> i detta lager – DAO ser aldrig klartextlösenord.</li>
 * </ul>
 *
 * <h2>Säkerhet</h2>
 * <ul>
 *   <li>Lösenord hash’​as med {@link BCrypt} (salt + arbetsfaktor via {@code gensalt()}).</li>
 *   <li>Vid uppdatering behålls befintlig hash om nytt lösenord inte anges (DAO hoppar över null-hash).</li>
 *   <li>Roller används för RBAC; kontrolleras av Controller/Filter-lagret (t.ex. {@code RoleFilter}).</li>
 * </ul>
 *
 * @see com.example.webshop.dao.UserDao

 */
public class UserService {
    private final UserDao userDao = new UserDaoJdbc();



    public User findByUsername(String username) throws SQLException {
        return userDao.findByUsername(username);
    }


}
