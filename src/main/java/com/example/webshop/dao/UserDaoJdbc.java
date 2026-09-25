package com.example.webshop.dao;

import com.example.webshop.model.User;
import com.example.webshop.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * {@code UserDaoJdbc} är den JDBC-baserade konkreta implementationen av {@link UserDao}.
 * Läser användarens grundläggande profilinformation och tillhörande roll(er) från databasen
 * samt utför CRUD-operationer och rolltilldelning.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li><em>Användaridentifiering</em> — Ger data för att hitta användaren och verifiera hash.
 *           I den tredelade arkitekturen Controller → Service → <strong>DAO</strong> → DB uppfylls kravet.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li><em>Utbyggd användarhantering och behörighetsklasser</em> (CUSTOMER/ADMIN/WAREHOUSE) —
 *           {@link #getRoles(Long)} och {@link #setRoles(Long, List)} tillhandahåller data för rollbaserad åtkomst.</li>
 *       <li>Ger stöd för listning, skapande, uppdatering och borttagning i admin-gränssnittet.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong>:
 *     <ul>
 *       <li>För att de rollbaserade administrationsvyerna (kategori-/produkt-/lagerhantering)
 *           ska fungera korrekt hanteras användar- och rollinformation konsekvent (inom transaktioner).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Förväntat databasschema (översikt)</h2>
 * <pre>
 * Tabell: users
 *   id (PK), username (UNIQUE), password_hash, full_name, email, active (BOOLEAN)
 *
 * Tabell: roles
 *   id (PK), name (t.ex. 'CUSTOMER', 'ADMIN', 'WAREHOUSE')
 *
 * Tabell: user_roles
 *   user_id (FK users.id), role_id (FK roles.id)
 * </pre>
 *
 * <h2>Prestanda / Indexrekommendationer</h2>
 * <ul>
 *   <li><code>users(username)</code> bör ha <strong>UNIQUE INDEX</strong> (för inloggning)</li>
 *   <li><code>user_roles(user_id)</code> och <code>user_roles(role_id)</code> bör indexeras</li>
 *   <li><code>roles(name)</code> bör ha <strong>UNIQUE INDEX</strong> (för matchning via rollnamn)</li>
 * </ul>
 *
 * <h2>Säkerhetsanmärkningar</h2>
 * <ul>
 *   <li>Lösenord lagras inte i klartext; fältet {@code password_hash} verifieras i service-lagret
 *       med algoritmer som BCrypt/Argon2 (se AuthService).</li>
 *   <li>{@code active=false}-användare ska inte kunna logga in (kontrolleras i service-lagret).</li>
 *   <li>RBAC – den returnerade <code>roles</code>-listan används i Controller-/Filter-lagret (t.ex. RoleFilter).</li>
 * </ul>
 *
 * <h2>Anslutnings- och felhantering</h2>
 * <ul>
 *   <li>Varje metod öppnar sin egen {@link Connection} via {@link Db#get()} och stänger den med try-with-resources.</li>
 *   <li>Batch- och konsistenskrävande operationer (t.ex. borttagning, rolluppdatering) körs inom en <strong>transaktion</strong>.</li>
 *   <li>JDBC-fel skickas vidare till anroparen som {@link SQLException} (hanteras i service-lagret).</li>
 * </ul>
 *
 * <h2>Synkronisering / Konsistens</h2>
 * <ul>
 *   <li>{@link #delete(Long)} och {@link #setRoles(Long, List)} körs atomärt inom en transaktion.</li>
 *   <li>Batch-införing ({@link #setRoles(Long, List)}) minskar round-trip vid flera rolltilldelningar.</li>
 * </ul>
 *
 * @see com.example.webshop.service.AuthService
 * @see com.example.webshop.controller.AuthController
 * @see com.example.webshop.filter.RoleFilter
 * @author Your Name
 * @since 1.0
 */
public class UserDaoJdbc implements UserDao {

    /**
     * Returnerar en användare utifrån användarnamn; {@code null} om ingen hittas.
     * <p>
     * Det returnerade objektet innehåller id, användarnamn, lösenordshash, fullständigt namn,
     * e-post och <em>aktivstatus</em>. Dessutom laddas användarens rollista via
     * {@link #getRoles(Long)}.
     * </p>
     *
     * @param username unikt användarnamn
     * @return {@link User} eller {@code null}
     * @throws SQLException JDBC-fel
     */
    @Override
    public User findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email, active FROM users WHERE username=?";
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
                u.setActive(rs.getBoolean("active"));
                u.setRoles(getRoles(u.getId()));
                return u;
            }
        }
    }

    /**
     * Returnerar rollnamnen för en användare (t.ex. {@code "CUSTOMER"}, {@code "ADMIN"}, {@code "WAREHOUSE"}).
     *
     * @param userId användar-ID
     * @return lista över rollnamn (kan vara tom)
     * @throws SQLException JDBC-fel
     */
    @Override
    public List<String> getRoles(Long userId) throws SQLException {
        String sql = "SELECT r.name FROM roles r JOIN user_roles ur ON r.id=ur.role_id WHERE ur.user_id=?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<String> roles = new ArrayList<>();
                while (rs.next()) roles.add(rs.getString(1));
                return roles;
            }
        }
    }

    /**
     * Hämtar alla användare. Roller laddas separat för varje användare.
     *
     * @return lista över användare
     * @throws SQLException JDBC-fel
     */
    @Override
    public List<User> findAll() throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email, active FROM users ORDER BY id";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<User> list = new ArrayList<>();
            while (rs.next()) list.add(map(rs).withRoles(getRoles(rs.getLong("id"))));
            return list;
        }
    }

    /**
     * Hämtar en användare baserat på ID (inklusive roller).
     *
     * @param id användar-ID
     * @return användare eller {@code null}
     * @throws SQLException JDBC-fel
     */
    @Override
    public User findById(Long id) throws SQLException {
        String sql = "SELECT id, username, password_hash, full_name, email, active FROM users WHERE id=?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs).withRoles(getRoles(id));
            }
        }
    }

    /**
     * Skapar en ny användare.
     *
     * @param u användare
     * @return genererat ID
     * @throws SQLException JDBC-fel eller om ID inte genereras
     */
    @Override
    public Long create(User u) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, full_name, email, active) VALUES(?,?,?,?,?)";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getUsername());
            ps.setString(2, u.getPasswordHash());
            ps.setString(3, u.getFullName());
            ps.setString(4, u.getEmail());
            ps.setBoolean(5, u.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("User ID genererades inte");
    }

    /**
     * Uppdaterar en användare. Om lösenordshash är tomt uppdateras inte lösenordet.
     *
     * @param u användare
     * @throws SQLException JDBC-fel
     */
    @Override
    public void update(User u) throws SQLException {
        // Om lösenordet inte ska ändras, uppdatera inte password_hash
        boolean hasPass = u.getPasswordHash()!=null && !u.getPasswordHash().isBlank();
        String sql = hasPass
                ? "UPDATE users SET username=?, password_hash=?, full_name=?, email=?, active=? WHERE id=?"
                : "UPDATE users SET username=?, full_name=?, email=?, active=? WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i=1;
            ps.setString(i++, u.getUsername());
            if (hasPass) ps.setString(i++, u.getPasswordHash());
            ps.setString(i++, u.getFullName());
            ps.setString(i++, u.getEmail());
            ps.setBoolean(i++, u.isActive());
            ps.setLong(i, u.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Tar bort en användare (först user_roles, sedan users). Körs atomärt inom en transaktion.
     *
     * @param id användar-ID
     * @throws SQLException JDBC-fel
     */
    @Override
    public void delete(Long id) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM user_roles WHERE user_id=?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement("DELETE FROM users WHERE id=?")) {
                    ps.setLong(1, id);
                    ps.executeUpdate();
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }

    /**
     * Returnerar alla roller som finns i systemet (t.ex. ADMIN, CUSTOMER, WAREHOUSE).
     *
     * @return lista över rollnamn
     * @throws SQLException JDBC-fel
     */
    @Override
    public List<String> listAllRoles() throws SQLException {
        String sql = "SELECT name FROM roles ORDER BY name";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<String> list = new ArrayList<>();
            while (rs.next()) list.add(rs.getString(1));
            return list;
        }
    }

    /**
     * Skriver om användarens rolluppsättning <strong>helt</strong>.
     * (Rensar först befintliga kopplingar och lägger sedan till nya roller via batch.)
     * Körs atomärt i en enda transaktion.
     *
     * @param userId användar-ID
     * @param roles  roller som ska tilldelas (tom = rensar alla)
     * @throws SQLException JDBC-fel
     */
    @Override
    public void setRoles(Long userId, List<String> roles) throws SQLException {
        try (Connection c = Db.get()) {
            c.setAutoCommit(false);
            try {
                // rensa
                try (PreparedStatement del = c.prepareStatement("DELETE FROM user_roles WHERE user_id=?")) {
                    del.setLong(1, userId);
                    del.executeUpdate();
                }
                // lägg till
                if (roles!=null && !roles.isEmpty()) {
                    String ins = """
                        INSERT INTO user_roles(user_id, role_id)
                        SELECT ?, r.id FROM roles r WHERE r.name=?
                        """;
                    try (PreparedStatement ps = c.prepareStatement(ins)) {
                        for (String r : roles) {
                            ps.setLong(1, userId);
                            ps.setString(2, r);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally { c.setAutoCommit(true); }
        }
    }

    /** Hjälpmetod som mappar en ResultSet till ett User-objekt. */
    private User map(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getLong("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setActive(rs.getBoolean("active"));
        return u;
    }
}
