package com.example.webshop.service;

import com.example.webshop.dao.UserDao;
import com.example.webshop.dao.UserDaoJdbc;
import com.example.webshop.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

/**
 * {@code AuthService} hanterar autentisering (inloggning) av användare.
 * Den hittar användaren via {@link UserDao}, kontrollerar om kontot är aktivt
 * och verifierar lösenordet med BCrypt-algoritmen.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li><em>Användaridentifiering</em> – uppfylls direkt genom denna tjänst.</li>
 *       <li>Representerar “Service”-lagret i en 3-lagers arkitektur
 *           (Controller → Service → DAO → DB).</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li><em>Utbyggd användarhantering</em> – tillåter endast aktiva konton att logga in.</li>
 *       <li><em>Behörighetsklasser</em> – roller som laddas i {@link User}-objektet
 *           används senare i t.ex. {@code RoleFilter}.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Hämta användaren från databasen ({@link UserDao#findByUsername(String)}).</li>
 *   <li>Kontrollera om kontot är aktivt.</li>
 *   <li>Verifiera lösenordet med BCrypt.</li>
 *   <li>Returnera {@link User} om autentisering lyckas, annars {@code null}.</li>
 * </ul>
 *
 * <h2>Säkerhetsanmärkningar</h2>
 * <ul>
 *   <li><b>Hash-algoritm:</b> {@link BCrypt} används – den ger stark saltning
 *       och justerbar beräkningskostnad enligt moderna säkerhetsstandarder.</li>
 *   <li><b>Skydd mot timing-attacker:</b> {@link BCrypt#checkpw(String, String)} jämför i konstant tid
 *       för att undvika tidsbaserade attacker.</li>
 *   <li><b>Kontroll av kontots status:</b> <code>user.isActive()</code> säkerställer att inaktiva
 *       konton inte kan logga in.</li>
 *   <li>I produktion bör misslyckade inloggningsförsök räknas och eventuellt leda till tillfällig låsning.</li>
 * </ul>
 *
 * <h2>Exempel på användning (i Controller)</h2>
 * <pre>{@code
 * AuthService authService = new AuthService();
 * User user = authService.login(username, password);
 * if (user != null) {
 *     session.setAttribute("user", user);
 *     session.setAttribute("roles", user.getRoles());
 * } else {
 *     request.setAttribute("error", "Fel användarnamn eller lösenord");
 * }
 * }</pre>
 *
 * <h2>Undantagshantering</h2>
 * <ul>
 *   <li>Databasfel kastas vidare som {@link SQLException}.</li>
 *   <li>Om användarnamn eller lösenord är felaktigt returneras {@code null}.</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
public class AuthService {

    /** DAO-lager för åtkomst till användardata. */
    private final UserDao userDao = new UserDaoJdbc();

    /**
     * Utför inloggning.
     * <ul>
     *   <li>Hittar användaren via användarnamnet.</li>
     *   <li>Kontrollerar om kontot är aktivt.</li>
     *   <li>Verifierar lösenordet med BCrypt.</li>
     *   <li>Returnerar {@link User} om lyckad inloggning, annars {@code null}.</li>
     * </ul>
     *
     * @param username      användarnamn
     * @param plainPassword inskrivet lösenord i klartext
     * @return {@link User} om inloggning lyckas, annars {@code null}
     * @throws SQLException vid databasfel
     */
    public User login(String username, String plainPassword) throws SQLException {
        User u = userDao.findByUsername(username);
        if (u == null || !u.isActive()) return null;
        if (!BCrypt.checkpw(plainPassword, u.getPasswordHash())) return null;
        return u;
    }
}
