package com.example.webshop.service;

import com.example.webshop.dao.OrderDao;
import com.example.webshop.dao.OrderDaoJdbc;
import com.example.webshop.dao.ProductDao;
import com.example.webshop.dao.ProductDaoJdbc;
import com.example.webshop.model.Order;
import com.example.webshop.model.OrderItem;
import com.example.webshop.model.Product;
import com.example.webshop.util.Db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
/**
 * {@code OrderService} är service-lagret som hanterar flödet för att skapa en order från kundvagnen.
 * Skapandet av orderhuvud (orders), orderrader (order_items) och lagerminskning
 * utförs inom <b>en enda databastransaktion</b>.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> – Infrastruktur:
 *     <ul>
 *       <li>Trelagersarkitekturen (Controller → Service → DAO) konkretiseras här.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong> – Utökade krav:
 *     <ul>
 *       <li><em>“Man ska kunna skicka ordrar som ska vara inom en transaktion.”</em>
 *           Ordern, dess rader och lagerminskning sker på samma {@link Connection}
 *           inom en <strong>transaktion med auto-commit avstängd</strong>.</li>
 *       <li><em>Varulager</em> (lagerkontroll): Produkterna laddas omedelbart innan beställningen
 *           för att säkerställa att de är aktiva och har tillräckligt lager.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong> – Integration med administrativa flöden:
 *     <ul>
 *       <li>Beställningar som skapas här visas i lagrets panel (warehouse),
 *           där statusändringar (PACKED/SHIPPED) hanteras av administratören.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Transaktion och datakonsistens</h2>
 * <ol>
 *   <li><b>Förkontroll:</b> Om kundvagnen är tom kastas ett fel; produkterna laddas om från databasen
 *       och kontrolleras för aktiv status och tillräckligt lager.</li>
 *   <li><b>Orderhuvud:</b> Totalsumman beräknas och sparas i {@code orders}-tabellen.</li>
 *   <li><b>Orderrader + Lager:</b> För varje kundvagnsartikel skapas en {@code order_items}-rad och
 *       motsvarande produktlager minskas <em>inom samma transaktion</em>.</li>
 *   <li><b>Atomacitet:</b> Vid fel görs <code>rollback</code>; annars <code>commit</code>.</li>
 * </ol>
 *
 * <h2>Valuta / Beräkning</h2>
 * <ul>
 *   <li>Totalsumman beräknas med {@link BigDecimal} (för att undvika avrundningsfel).</li>
 *   <li>Avrundning och valutaformat hanteras i controller/vy-lagret
 *       eller via en separat valutahanteringstjänst.</li>
 * </ul>
 *
 * <h2>Undantag och felhantering</h2>
 * <ul>
 *   <li>Fel i transaktionen kastas som {@link SQLException} till högre lager.</li>
 *   <li>Otillräckligt lager, inaktiva produkter eller saknade produkter rapporteras med tydliga felmeddelanden.</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
public class OrderService {

    /** DAO för orderposter. */
    private final OrderDao orderDao = new OrderDaoJdbc();
    /** DAO för produkter (läsning och lageruppdatering). */
    private final ProductDao productDao = new ProductDaoJdbc();
    /** Hjälpservice för att beräkna totalsumma. */
    private final CartService cartService = new CartService();

    /**
     * Skapar en ny order för angiven användare och kundvagn.
     * <p>
     * <b>Flöde:</b>
     * <ol>
     *   <li>Kasta fel om kundvagnen är tom.</li>
     *   <li>Starta transaktion (auto-commit=false).</li>
     *   <li>Kontrollera lager och aktiv status för varje produkt.</li>
     *   <li>Skapa orderhuvud och hämta genererat ID.</li>
     *   <li>Lägg till orderrader och minska lager.</li>
     *   <li>Commit; rollback vid fel.</li>
     * </ol>
     * </p>
     *
     * @param userId användarens ID (hämtas från session)
     * @param cart   kundvagnens artiklar (produkt + antal)
     * @return det skapade order-ID:t
     * @throws SQLException vid validerings- eller databasfel
     */
    public Long placeOrder(Long userId, List<CartService.CartItem> cart) throws SQLException {
        if (cart == null || cart.isEmpty()) throw new SQLException("Kundvagnen är tom");

        try (Connection tx = Db.get()) {
            tx.setAutoCommit(false);
            try {
                // 1) kontrollera lager och aktiv status (senaste data)
                for (CartService.CartItem ci : cart) {
                    Product p = productDao.findById(ci.getProduct().getId());
                    if (p == null || !p.isActive())
                        throw new SQLException("Produkten hittades inte: " + ci.getProduct().getId());
                    if (p.getStock() < ci.getQty())
                        throw new SQLException("Otillräckligt lager: " + p.getName());
                }

                // 2) skapa orderhuvud
                BigDecimal total = cartService.calcTotal(cart);
                Order order = new Order();
                order.setUserId(userId);
                order.setStatus("CREATED");
                order.setTotal(total);
                Long orderId = orderDao.createOrder(tx, order);

                // 3) orderrader + lagerminskning
                for (CartService.CartItem ci : cart) {
                    OrderItem it = new OrderItem();
                    it.setOrderId(orderId);
                    it.setProductId(ci.getProduct().getId());
                    it.setQuantity(ci.getQty());
                    it.setUnitPrice(ci.getProduct().getPrice());
                    orderDao.addItem(tx, orderId, it);

                    // lager: negativ delta → minska
                    productDao.updateStock(tx, ci.getProduct().getId(), -ci.getQty());
                }

                tx.commit();
                return orderId;

            } catch (Exception e) {
                tx.rollback();
                if (e instanceof SQLException) throw (SQLException) e;
                throw new SQLException(e);
            } finally {
                tx.setAutoCommit(true);
            }
        }
    }
}
