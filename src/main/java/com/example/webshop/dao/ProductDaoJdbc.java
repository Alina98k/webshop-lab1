package com.example.webshop.dao;

import com.example.webshop.model.Product;
import com.example.webshop.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * {@code ProductDaoJdbc} är en konkret JDBC-baserad implementation av {@link ProductDao}.
 * Den hanterar listning/läsning, skapande, uppdatering, borttagning och
 * <b>lageruppdatering</b> (ökning/minskning) av produkter.
 *
 * <h2>Koppling till uppgiften (vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> — Kravet på <em>3-lagersarkitektur</em>:
 *     Controller → Service → <b>DAO</b> → DB uppfylls av denna klass.
 *   </li>
 *   <li><strong>Betyg 4</strong> —
 *     <ul>
 *       <li><em>Varulager ska finnas</em>: med {@link #updateStock(Connection, Long, int)}
 *           hanteras lagerförändringar på ett säkert sätt (kontroll av tillräckligt lager).</li>
 *       <li><em>Ordrar ska ske i transaktion</em>: lagerreduceringar anropas på samma {@link Connection}
 *           i orderflödet för att garantera <strong>atomaritet</strong>.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong> — Administrativa funktioner som att lägga till, redigera
 *       eller ta bort produkter sker via dessa DAO-metoder; uppfyller MVC + 3-lagerstrukturen.
 *   </li>
 * </ul>
 *
 * <h2>Arkitektur och transaktionskontrakt</h2>
 * <ul>
 *   <li><b>Fristående läs-/skrivmetoder</b> (t.ex. {@link #findAll()}, {@link #create(Product)}):
 *       öppnar sina egna anslutningar via {@link Db#get()} och stänger dem med try-with-resources.</li>
 *   <li><b>TX-aware metod</b> {@link #updateStock(Connection, Long, int)}:
 *       arbetar på den <em>givna</em> {@link Connection}; <b>auto-commit är avstängt</b> och
 *       commit/rollback hanteras i service-lagret (tillsammans med orderflödet).</li>
 * </ul>
 *
 * <h2>Förväntat databasschema (översikt)</h2>
 * <pre>
 * Tabell: products
 *   id (PK), category_id (FK, nullable), name, description, price DECIMAL, stock INT, active BOOLEAN
 * </pre>
 *
 * <h2>Fel- och dataintegritet</h2>
 * <ul>
 *   <li>Alla JDBC-fel skickas vidare som {@link SQLException} till överordnat lager.</li>
 *   <li>{@link #updateStock(Connection, Long, int)} uppdaterar 0 rader vid otillräckligt lager
 *       och kastar en beskrivande {@link SQLException} (optimistiskt skydd).</li>
 *   <li>Fältet <code>price</code> använder {@link java.math.BigDecimal}; avrundning ska ske
 *       enligt valuta/locale-strategin som definieras i service-lagret.</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
public class ProductDaoJdbc implements ProductDao {

    /**
     * Hämtar endast aktiva (säljbara) produkter i stigande ID-ordning.
     *
     * @return lista av aktiva {@link Product}
     * @throws SQLException vid JDBC-fel
     */
    @Override
    public List<Product> findAllActive() throws SQLException {
        String sql = "SELECT id, category_id, name, description, price, stock, active " +
                "FROM products WHERE active = TRUE ORDER BY id";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Product> list = new ArrayList<>();
            while (rs.next()) {
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setCategoryId((Long) rs.getObject("category_id")); // kan vara null
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setStock(rs.getInt("stock"));
                p.setActive(rs.getBoolean("active"));
                list.add(p);
            }
            return list;
        }
    }

    /**
     * Hämtar en produkt efter ID; returnerar {@code null} om den inte finns.
     *
     * @param id produkt-ID
     * @return {@link Product} eller {@code null}
     * @throws SQLException JDBC-fel
     */
    public Product findById(Long id) throws SQLException {
        String sql = "SELECT id, category_id, name, description, price, stock, active " +
                "FROM products WHERE id=?";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Product p = new Product();
                p.setId(rs.getLong("id"));
                p.setCategoryId((Long) rs.getObject("category_id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setPrice(rs.getBigDecimal("price"));
                p.setStock(rs.getInt("stock"));
                p.setActive(rs.getBoolean("active"));
                return p;
            }
        }
    }

    /**
     * Ökar/minskar lagret med det angivna <em>delta</em> (t.ex. -3 vid order).
     * <p>
     * <b>Transaktionsnot:</b> I samma orderflöde ska orderhuvud, orderrader och lagerminskning
     * ske inom <b>samma Connection</b>; commit/rollback hanteras av service-lagret.
     * </p>
     * <p>
     * <b>Dataintegritet:</b> UPDATE-satsen innehåller villkoret <code>(stock + ?) &gt;= 0</code>,
     * vilket förhindrar negativt lager. Uppdaterar 0 rader vid brist och kastar {@link SQLException}.
     * </p>
     *
     * @param tx        öppen {@link Connection} till samma transaktion
     * @param productId produkt-ID
     * @param delta     lagerförändring (negativ = minska, positiv = öka)
     * @throws SQLException vid otillräckligt lager eller JDBC-fel
     */
    @Override
    public void updateStock(Connection tx, Long productId, int delta) throws SQLException {
        String sql = "UPDATE products SET stock = stock + ? WHERE id = ? AND (stock + ?) >= 0";
        try (PreparedStatement ps = tx.prepareStatement(sql)) {
            ps.setInt(1, delta);
            ps.setLong(2, productId);
            ps.setInt(3, delta); // negativ = kontrollera gräns
            int updated = ps.executeUpdate();
            if (updated == 0)
                throw new SQLException("Lager kunde inte uppdateras (otillräckligt lager?) productId=" + productId);
        }
    }

    /**
     * Hämtar alla produkter (aktiva och inaktiva) i fallande ID-ordning.
     *
     * @return lista av {@link Product}
     * @throws SQLException JDBC-fel
     */
    @Override
    public List<Product> findAll() throws SQLException {
        String sql = "SELECT id, category_id, name, description, price, stock, active FROM products ORDER BY id DESC";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            List<Product> list = new ArrayList<>();
            while (rs.next()) {
                Product p = map(rs);
                list.add(p);
            }
            return list;
        }
    }

    /**
     * Skapar en ny produkt och returnerar det genererade ID:t.
     *
     * @param p produkt att lägga till
     * @return genererat produkt-ID
     * @throws SQLException JDBC-fel eller om ID inte genereras
     */
    @Override
    public Long create(Product p) throws SQLException {
        String sql = "INSERT INTO products(category_id,name,description,price,stock,active) VALUES(?,?,?,?,?,?)";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (p.getCategoryId()==null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setBoolean(6, p.isActive());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Produkt-ID genererades inte");
    }

    /**
     * Uppdaterar en befintlig produkt.
     *
     * @param p produkt att uppdatera (ID krävs)
     * @throws SQLException JDBC-fel
     */
    @Override
    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET category_id=?, name=?, description=?, price=?, stock=?, active=? WHERE id=?";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (p.getCategoryId()==null) ps.setNull(1, Types.BIGINT); else ps.setLong(1, p.getCategoryId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStock());
            ps.setBoolean(6, p.isActive());
            ps.setLong(7, p.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Tar bort en produkt enligt ID.
     *
     * @param id produkt-ID
     * @throws SQLException JDBC-fel
     */
    @Override
    public void delete(Long id) throws SQLException {
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * Hjälpmetod för att mappa en {@link ResultSet}-rad till {@link Product}.
     *
     * @param rs JDBC-resultat
     * @return mappad {@link Product}
     * @throws SQLException JDBC-fel
     */
    // hjälpfunktion:
    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getLong("id"));
        p.setCategoryId((Long) rs.getObject("category_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStock(rs.getInt("stock"));
        p.setActive(rs.getBoolean("active"));
        return p;
    }
}
