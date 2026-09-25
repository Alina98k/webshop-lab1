package com.example.webshop.dao;

import com.example.webshop.model.Order;
import com.example.webshop.model.OrderItem;
import com.example.webshop.util.Db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * {@code OrderDaoJdbc} är en konkret JDBC-baserad implementering av {@link OrderDao}.
 * Hanterar skapande, uppdatering och hämtning av orderhuvuden (orders)
 * och orderrader (order_items) baserat på status eller filter.
 *
 * <h2>Uppgiftskoppling (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> — Trelagers-arkitektur: Service → DAO → DB-separation uppfylls här.</li>
 *   <li><strong>Betyg 4</strong> —
 *     <ul>
 *       <li><em>“Man ska kunna skicka ordrar som ska vara inom en transaktion.”</em>
 *           → {@link #createOrder(Connection, Order)},
 *           {@link #addItem(Connection, Long, OrderItem)} och {@link #updateStatus(Connection, Long, String)}
 *           är utformade för att köras på samma {@link Connection}.
 *           Transaktionshanteringen sker i <em>service-lagret</em> (auto-commit av, commit/rollback).</li>
 *       <li><em>Varulager/lagerhantering</em> → Minskning av lager sker oftast i Product-DAO;
 *           denna DAO ansvarar för säker skapning av orderposter.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong> —
 *     <ul>
 *       <li>För lagerpersonalens “packa”-flöde används statusbaserad listning
 *           ({@link #listByStatus(String)}) och statusuppdatering
 *           ({@link #updateStatus(Connection, Long, String)}).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Arkitektur och transaktionsprinciper</h2>
 * <ul>
 *   <li><b>TX-metoder</b> ({@code createOrder}, {@code addItem}, {@code updateStatus})
 *       öppnar inte egna {@code Connection} utan använder det som ges som parameter.
 *       På så sätt kan orderhuvud, orderrader och lageruppdateringar göras atomärt
 *       i <strong>en enda transaktion</strong>.</li>
 *   <li><b>Fristående läsmetoder</b> ({@code listByUser}, {@code listByStatus})
 *       öppnar egna {@link Connection} via {@link Db#get()} och stänger dem automatiskt
 *       med try-with-resources.</li>
 * </ul>
 *
 * <h2>Förväntad databasstruktur (översikt)</h2>
 * <pre>
 * Tabell: orders
 *   id (PK, BIGINT), user_id (BIGINT), status (VARCHAR), total (DECIMAL), created_at (TIMESTAMP DEFAULT NOW)
 *
 * Tabell: order_items
 *   id (PK), order_id (FK orders.id), product_id (FK products.id),
 *   quantity (INT), unit_price (DECIMAL)
 * </pre>
 *
 * <h2>Status-exempel</h2>
 * <p><code>CREATED</code>, <code>PACKED</code>, <code>SHIPPED</code>, <code>CANCELLED</code></p>
 *
 * <h2>Felkänslighet och hantering</h2>
 * <ul>
 *   <li>Alla JDBC-fel kastas uppåt som {@link SQLException}.</li>
 *   <li>Rollback vid misslyckad transaktion hanteras i <b>service-lagret</b>.</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
public class OrderDaoJdbc implements OrderDao {

    /**
     * Skapar ett nytt orderhuvud (orders) och returnerar det genererade ID:t.
     * <p><b>Transaktionsnot:</b> Denna metod arbetar på den {@link Connection} som ges;
     * bör anropas inom en pågående transaktion (auto-commit avstängt).</p>
     *
     * @param tx    Öppen {@link Connection} inom samma transaktion
     * @param order Order-huvud som ska skapas (userId, status, total måste vara satta)
     * @return Det genererade order-ID:t
     * @throws SQLException Om nyckel inte genereras eller vid JDBC-fel
     */
    @Override
    public Long createOrder(Connection tx, Order order) throws SQLException {
        String sql = "INSERT INTO orders(user_id, status, total) VALUES(?, ?, ?)";
        try (PreparedStatement ps = tx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, order.getUserId());
            ps.setString(2, order.getStatus());
            ps.setBigDecimal(3, order.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getLong(1);
            }
        }
        throw new SQLException("Order-ID kunde inte genereras");
    }

    /**
     * Lägger till en orderrad (order_items) till en order.
     * <p><b>Transaktionsnot:</b> Flera rader kan läggas till inom samma {@code tx};
     * vid fel gör service-lagret rollback.</p>
     *
     * @param tx      Transaktionsanslutning
     * @param orderId Order-ID
     * @param item    Orderrad (productId, quantity, unitPrice måste vara satta)
     * @throws SQLException Vid JDBC-fel
     */
    @Override
    public void addItem(Connection tx, Long orderId, OrderItem item) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity, unit_price) VALUES(?, ?, ?, ?)";
        try (PreparedStatement ps = tx.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            ps.setLong(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getUnitPrice());
            ps.executeUpdate();
        }
    }

    /**
     * Uppdaterar en orders status.
     * <p><b>Transaktionsnot:</b> I lager/leveransflöden (PACKED/SHIPPED)
     * kan detta ofta kombineras med andra tabelluppdateringar i samma transaktion.</p>
     *
     * @param tx      Transaktionsanslutning
     * @param orderId Order-ID
     * @param status  Ny status
     * @throws SQLException Vid JDBC-fel
     */
    @Override
    public void updateStatus(Connection tx, Long orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET status=? WHERE id=?";
        try (PreparedStatement ps = tx.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, orderId);
            ps.executeUpdate();
        }
    }

    /**
     * Hämtar en användares ordrar (senaste ID först).
     * <p>Denna metod öppnar en <b>fristående anslutning</b> och gör endast läsning.</p>
     *
     * @param userId Användar-ID
     * @return Lista med ordrar
     * @throws SQLException Vid JDBC-fel
     */
    @Override
    public List<Order> listByUser(Long userId) throws SQLException {
        String sql = "SELECT id, user_id, status, total, created_at FROM orders WHERE user_id=? ORDER BY id DESC";
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setUserId(rs.getLong("user_id"));
                    o.setStatus(rs.getString("status"));
                    o.setTotal(rs.getBigDecimal("total"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) o.setCreatedAt(ts.toInstant());
                    list.add(o);
                }
                return list;
            }
        }
    }

    /**
     * Hämtar ordrar baserat på status (i stigande ID-ordning).
     * <p>Används i lagerpersonalens vy med filter.</p>
     *
     * @param status Status-filter (t.ex. CREATED, PACKED)
     * @return Lista med ordrar
     * @throws SQLException Vid JDBC-fel
     */
    @Override
    public List<Order> listByStatus(String status) throws SQLException {
        String sql = "SELECT id, user_id, status, total, created_at FROM orders WHERE status=? ORDER BY id";
        try (Connection c = Db.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> list = new ArrayList<>();
                while (rs.next()) {
                    Order o = new Order();
                    o.setId(rs.getLong("id"));
                    o.setUserId(rs.getLong("user_id"));
                    o.setStatus(rs.getString("status"));
                    o.setTotal(rs.getBigDecimal("total"));
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) o.setCreatedAt(ts.toInstant());
                    list.add(o);
                }
                return list;
            }
        }
    }
}
