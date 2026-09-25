package com.example.webshop.controller;

import com.example.webshop.service.CartService;
import com.example.webshop.service.OrderService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
/**
 * {@code OrderController} är en {@link HttpServlet} som hanterar orderläggning
 * (”place order”) från kundvagnen.
 *
 * <h2>Uppgiftskoppling (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> – Grundläggande varukorgs- och produktfunktioner:
 *     <ul>
 *       <li>Denna klass är en fortsättning på varukorgsstrukturen från Betyg 3.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong> – Avancerade funktioner (lager- och orderhantering):
 *     <ul>
 *       <li><em>“Man ska kunna skicka ordrar som ska vara inom en transaktion.”</em>
 *           → {@link OrderService#placeOrder(Long, List)} uppfyller detta krav.</li>
 *       <li><em>“Varulager ska finnas så man vet om varan finns i lager.”</em>
 *           → {@link OrderService} kontrollerar och uppdaterar lagersaldot vid beställning.</li>
 *       <li><em>Utbyggd användarhantering</em> – Ordrar kopplas till användarens ID från sessionen.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong> – Administrativa funktioner (lagerpersonal, admin):
 *     <ul>
 *       <li>Denna klass hanterar kundens del av orderflödet.
 *           Lagerpersonalens funktioner för att visa eller “packa” ordrar
 *           hanteras i en separat klass, t.ex. <code>AdminOrderController</code>.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Omvandlar användarens aktuella kundvagn till en order.</li>
 *   <li>Ser till att ordern sparas i databasen inom en transaktion.</li>
 *   <li>Rensar kundvagnen och dirigerar till en bekräftelsesida om allt lyckas.</li>
 *   <li>Vid fel skickas ett felmeddelande till JSP-vyn.</li>
 * </ul>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li><code>POST /orders/place</code> – omvandlar den aktuella kundvagnen till en order.</li>
 * </ul>
 *
 * <h2>Flöde</h2>
 * <ol>
 *   <li>Hämtar {@code userId} från sessionen (autentisering är ännu inte obligatorisk).</li>
 *   <li>Hämtar aktiv kundvagn via {@link CartService#getOrCreateCart(HttpSession)}.</li>
 *   <li>Anropar {@link OrderService#placeOrder(Long, List)}:
 *       <ul>
 *         <li>Skapar ny post i order-tabellen.</li>
 *         <li>Minskar lagersaldot för berörda produkter.</li>
 *         <li>Allt sker inom en <strong>transaktion</strong>.</li>
 *       </ul>
 *   </li>
 *   <li>Om ordern lyckas rensas kundvagnen och användaren dirigeras till
 *       <code>order_success.jsp</code>.</li>
 * </ol>
 *
 * <h2>Arkitektur och MVC</h2>
 * <p>
 * Controllern ansvarar endast för routing, sessionsåtkomst och felhantering.
 * All affärslogik finns i {@link OrderService} och dataåtkomst i DAO-lagret.
 * På så sätt bevaras den 3-lagers arkitekturen.
 * </p>
 *
 * <h2>Sessionsanvändning</h2>
 * <ul>
 *   <li>{@code "userId"} – ID för inloggad användare (antas vara 1L i demoversionen).</li>
 *   <li>Kundvagnen lagras i sessionen och rensas efter genomförd order.</li>
 * </ul>
 *
 * <h2>Vyer (JSP)</h2>
 * <ul>
 *   <li><code>/WEB-INF/views/order_success.jsp</code> – bekräftelsesida efter beställning.</li>
 *   <li><code>/WEB-INF/views/cart.jsp</code> – återgångssida vid fel.</li>
 * </ul>
 *
 * <h2>Felhantering</h2>
 * <ul>
 *   <li>{@link SQLException} fångas och meddelandet lagras i attributet <code>"error"</code>.</li>
 *   <li>Vid databasfel dirigeras användaren tillbaka till <code>cart.jsp</code>.</li>
 * </ul>
 *
 * <h2>Framtida utbyggnad</h2>
 * <ul>
 *   <li>GET <code>/orders/my</code> – kan användas för att visa användarens orderhistorik.</li>
 *   <li>Separata orderhanteringssidor för admin eller lagerpersonal (Betyg 5).</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
@WebServlet(name = "OrderController", urlPatterns = {"/orders/*"})
public class OrderController extends HttpServlet {

    /** Hanterar användarens sessionbaserade kundvagn. */
    private final CartService cartService = new CartService();

    /** Hanterar orderläggning, lageruppdatering och transaktionshantering. */
    private final OrderService orderService = new OrderService();

    /**
     * Hanterar {@code POST}-förfrågningar.
     *
     * <p><strong>Stödd sökväg:</strong> <code>/place</code>
     * (omvandlar aktiv kundvagn till en order).</p>
     *
     * @param req  HTTP-förfrågan
     * @param resp HTTP-svar
     * @throws ServletException vid databasfel
     * @throws IOException      vid omdirigeringsfel
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // /place
        if ("/place".equals(path)) {
            handlePlace(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Skapar en ny order baserad på den aktuella kundvagnen.
     *
     * <p>Denna metod anropas vanligtvis från en “Beställ” (Place Order)-knapp.
     * <ul>
     *   <li>Hämtar användar-ID från sessionen; om det saknas tilldelas ett temporärt ID (för demo).</li>
     *   <li>Hämtar produktlistan från CartService.</li>
     *   <li>Anropar {@link OrderService#placeOrder(Long, List)} för att skapa order och orderrader i databasen
     *       inom en transaktion.</li>
     *   <li>Om allt lyckas rensas kundvagnen och bekräftelsesidan visas.</li>
     * </ul>
     * </p>
     *
     * @param req  HTTP-förfrågan (session innehåller userId och kundvagnsdata)
     * @param resp HTTP-svar (dirigering till JSP)
     * @throws ServletException vid databasfel
     * @throws IOException      vid omdirigeringsfel
     */
    private void handlePlace(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            // ⚠️ Om ingen inloggning finns ännu, använd temporärt användar-ID:
            Long userId = (Long) req.getSession(true).getAttribute("userId");
            if (userId == null) {
                userId = 1L; // standardanvändare för demo
                req.getSession().setAttribute("userId", userId);
            }

            List<CartService.CartItem> cart = cartService.getOrCreateCart(req.getSession(true));
            Long orderId = orderService.placeOrder(userId, cart);

            // Rensa kundvagn
            cart.clear();

            // Lyckad order: skicka orderId till JSP
            req.setAttribute("orderId", orderId);
            req.getRequestDispatcher("/WEB-INF/views/order_success.jsp").forward(req, resp);

        } catch (SQLException e) {
            // Vid fel: återgå till kundvagnssidan
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, resp);
        }
    }

    // I framtiden kan t.ex. GET /orders/my läggas till
}
