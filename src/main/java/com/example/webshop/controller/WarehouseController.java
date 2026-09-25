package com.example.webshop.controller;

import com.example.webshop.dao.OrderDao;
import com.example.webshop.dao.OrderDaoJdbc;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
/**
 * {@code WarehouseController} är en {@link HttpServlet} som hanterar
 * orderlistning och ”packning” (ändring till PACKED-status) för lagerpersonal
 * (WAREHOUSE-roll).
 *
 * <h2>Uppgiftskoppling (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li><em>Orderhantering inom transaktion</em> — vid POST (packning) uppdateras
 *           status i databasen inom en transaktion för att säkerställa konsistens.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 5</strong>:
 *     <ul>
 *       <li><em>Lagerpersonal ska kunna se och ”packa” ordrar</em> — GET visar ordrar
 *           baserat på status, POST ändrar status till “PACKED” — <strong>uppfyllt</strong>.</li>
 *       <li><em>Behörighet</em> — Denna klass är lagerpersonalens gränssnitt;
 *           rollkontroll (endast {@code WAREHOUSE}) bör hanteras i ett {@code Filter} eller säkerhetslager.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>GET <code>/warehouse/orders</code> — listar ordrar efter {@code status}-parametern
 *       (standard: {@code CREATED}).</li>
 *   <li>POST <code>/warehouse/pack</code> — uppdaterar orderns status till {@code PACKED}
 *       inom en transaktion.</li>
 * </ul>
 *
 * <h2>Arkitektur (MVC &amp; 3-lagersmodell)</h2>
 * <ul>
 *   <li><strong>Controller</strong>: Denna klass (routing, request/response, enkel validering).</li>
 *   <li><strong>DAO</strong>: {@link OrderDaoJdbc} (databasåtkomst och statusuppdatering).</li>
 *   <li><strong>View</strong>: <code>/WEB-INF/views/warehouse/orders.jsp</code> (lista ordrar).</li>
 * </ul>
 *
 * <h2>Vyer (JSP)</h2>
 * <ul>
 *   <li>Lista: <code>/WEB-INF/views/warehouse/orders.jsp</code>
 *       (request-attribut: {@code list} = ordrar, {@code status} = filter).</li>
 * </ul>
 *
 * <h2>Säkerhet</h2>
 * <ul>
 *   <li>Dessa endpoints är avsedda för lagerpersonal (WAREHOUSE).
 *       Roll- och sessionskontroll bör göras utanför denna klass (t.ex. i {@code Filter}).</li>
 *   <li>CSRF-skydd rekommenderas för POST-anrop.</li>
 * </ul>
 *
 * <h2>Felhantering</h2>
 * <ul>
 *   <li>Databasfel kapslas i {@link ServletException}.</li>
 *   <li>Vid ogiltiga eller saknade parametrar returneras lämpliga HTTP-statuskoder.</li>
 * </ul>
 *
 * <h2>Exempelanrop</h2>
 * <pre>
 * GET  /warehouse/orders?status=CREATED
 * POST /warehouse/pack      body: id=123
 * </pre>
 *
 * @author Your Name
 * @since 1.0
 */
@WebServlet(name="WarehouseController", urlPatterns={"/warehouse/orders","/warehouse/pack","/warehouse/ship"})
public class WarehouseController extends HttpServlet {

    /** DAO som listar ordrar efter status och uppdaterar orderstatus. */
    private final OrderDao orderDao = new OrderDaoJdbc();

    /**
     * Lagerpersonals vy: visar ordrar baserat på {@code status}-parametern.
     * <p>Om parametern saknas används {@code CREATED} som standard.</p>
     *
     * <ul>
     *   <li>Request-attribut:
     *     <ul>
     *       <li><code>list</code> – ordrar filtrerade efter status.</li>
     *       <li><code>status</code> – aktuellt filtervärde.</li>
     *     </ul>
     *   </li>
     *   <li>Vy: <code>/WEB-INF/views/warehouse/orders.jsp</code></li>
     * </ul>
     *
     * @param req  HTTP-förfrågan (valfri parameter: {@code status})
     * @param resp HTTP-svar
     * @throws ServletException vid JSP-forwarding eller databasfel
     * @throws IOException vid I/O-fel
     */
    @Override protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String status = req.getParameter("status");
        if (status == null || status.isBlank()) status = "CREATED";
        try {
            req.setAttribute("list", orderDao.listByStatus(status));
            req.setAttribute("status", status);
            req.getRequestDispatcher("/WEB-INF/views/warehouse/orders.jsp").forward(req, resp);
        } catch (SQLException e) { throw new ServletException(e); }
    }

    /**
     * Packningsåtgärd: ändrar statusen för den angivna {@code id}-ordern till {@code PACKED}.
     * <p>Operationen sker inom en <strong>transaktion</strong>. Vid framgång
     * omdirigeras användaren tillbaka till listan över ordrar med status CREATED.</p>
     *
     * @param req  HTTP-förfrågan (obligatorisk parameter: {@code id})
     * @param resp HTTP-svar
     * @throws ServletException vid DB/transaction-fel
     * @throws IOException vid omdirigeringsfel
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) { resp.sendError(400); return; }
        Long orderId = Long.valueOf(idStr);

        // Vilken endpoint anropades?
        String path = req.getServletPath(); // /warehouse/pack eller /warehouse/ship
        String newStatus;
        if (path.endsWith("/pack"))      newStatus = "PACKED";
        else if (path.endsWith("/ship")) newStatus = "SHIPPED";
        else { resp.sendError(404); return; }

        try (var c = com.example.webshop.util.Db.get()) {
            c.setAutoCommit(false);
            try {
                new OrderDaoJdbc().updateStatus(c, orderId, newStatus);
                c.commit();
                // Smidig återgång: gå tillbaka till listan med föregående status
                String back = "PACKED".equals(newStatus) ? "CREATED" : "PACKED";
                resp.sendRedirect(req.getContextPath()+"/warehouse/orders?status="+back);
            } catch (Exception e) {
                c.rollback(); throw e;
            } finally { c.setAutoCommit(true); }
        } catch (Exception e) { throw new ServletException(e); }
    }
}
