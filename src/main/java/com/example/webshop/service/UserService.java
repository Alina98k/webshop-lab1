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
 * @see com.example.webshop.controller.AdminUserController
 * @see com.example.webshop.service.AuthService
 */
public class UserService {
    private final UserDao userDao = new UserDaoJdbc();

    /**
     * Returnerar alla användare (inklusive roller).
     *
     * @return lista över användare
     * @throws SQLException vid JDBC-fel från DAO-lagret
     */
    public List<User> listAll() throws SQLException { return userDao.findAll(); }

    /**
     * Hämtar användaren med angivet ID (inklusive roller).
     *
     * @param id användar-ID
     * @return {@link User} eller {@code null}
     * @throws SQLException vid DAO/JDBC-fel
     */
    public User get(Long id) throws SQLException { return userDao.findById(id); }

    /**
     * Returnerar alla definierade rollnamn i systemet.
     *
     * @return rollnamn (t.ex. ADMIN, CUSTOMER, WAREHOUSE)
     * @throws SQLException vid DAO/JDBC-fel
     */
    public List<String> listAllRoles() throws SQLException { return userDao.listAllRoles(); }

    /**
     * Returnerar användarens tilldelade roller.
     *
     * @param userId användar-ID
     * @return lista över rollnamn (kan vara tom)
     * @throws SQLException vid DAO/JDBC-fel
     */
    public List<String> getRoles(Long userId) throws SQLException { return userDao.getRoles(userId); }

    /**
     * Skapar en ny användare: hashar det angivna lösenordet med BCrypt,
     * sparar användaren och tilldelar roller.
     *
     * <p><strong>Regler:</strong></p>
     * <ul>
     *   <li>Lösenord är obligatoriskt; om tomt kastas {@link SQLException}.</li>
     *   <li>Hash genereras med {@code BCrypt.hashpw(plainPassword, BCrypt.gensalt())}.</li>
     *   <li>Roller sätts atomiskt via {@link UserDao#setRoles(Long, List)}.</li>
     * </ul>
     *
     * @param u             användare som ska skapas (username, fullName, email, active etc. måste vara ifyllda)
     * @param plainPassword klartextlösenord (obligatoriskt)
     * @param roles         roller som ska tilldelas (kan vara tom)
     * @return genererat användar-ID
     * @throws SQLException vid validerings-, DAO- eller JDBC-fel
     */
    public Long create(User u, String plainPassword, List<String> roles) throws SQLException {
        if (plainPassword == null || plainPassword.isBlank())
            throw new SQLException("Lösenord krävs");
        String hash = BCrypt.hashpw(plainPassword, BCrypt.gensalt());
        u.setPasswordHash(hash);
        Long id = userDao.create(u);
        userDao.setRoles(id, roles);
        return id;
    }

    /**
     * Uppdaterar en användare: om nytt lösenord anges hash’​as det och uppdateras;
     * annars behålls befintligt lösenord. Rollerna skrivs alltid om helt.
     *
     * <p><strong>Regler:</strong></p>
     * <ul>
     *   <li>Om {@code newPlainPassword} är tomt sätts inte {@code passwordHash} → DAO ändrar inte lösenordet.</li>
     *   <li>{@code setRoles} tar först bort befintliga kopplingar och lägger sedan till nya i batch (inom transaction).</li>
     * </ul>
     *
     * @param u                användaren som ska uppdateras
     * @param newPlainPassword nytt lösenord (valfritt)
     * @param roles            roller som ska tilldelas (tom lista rensar alla roller)
     * @throws SQLException vid DAO/JDBC-fel
     */
    public void update(User u, String newPlainPassword, List<String> roles) throws SQLException {
        if (newPlainPassword != null && !newPlainPassword.isBlank()) {
            String hash = BCrypt.hashpw(newPlainPassword, BCrypt.gensalt());
            u.setPasswordHash(hash);
        }
        userDao.update(u);          // om passwordHash är null uppdateras inte lösenordet
        userDao.setRoles(u.getId(), roles);
    }

    /**
     * Tar bort en användare. I DAO sker borttagning av både
     * användar-rollkopplingar och själva användaren inom en enda transaction.
     *
     * @param id användar-ID
     * @throws SQLException vid DAO/JDBC-fel
     */
    public void delete(Long id) throws SQLException { userDao.delete(id); }
}
