package com.example.webshop.controller;

import com.example.webshop.model.Product;
import com.example.webshop.service.ProductService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
/**
 * {@code AdminProductController} är en {@link HttpServlet}-klass som hanterar
 * CRUD-operationer (Create, Read, Update, Delete) för produktentiteter i adminpanelen.
 *
 * <h2>Koppling till uppgiften (vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> — Den trelagersarkitektur (Controller → Service → DAO → DB) implementeras här.</li>
 *   <li><strong>Betyg 4</strong> — Hantering av <em>varulager</em> (lagerstatus och synlighet):
 *       fält för lager, produktaktivitet och grundläggande data för orderflödet.</li>
 *   <li><strong>Betyg 5</strong> — Kravet <em>”Lägga till och redigera varor och varukategorier”</em>
 *       uppfylls direkt av denna controller och dess relaterade JSP-sidor
 *       (skapa, redigera, ta bort produkter).</li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Visa produktlista (<code>GET /admin/products</code>)</li>
 *   <li>Öppna formulär för ny produkt (<code>GET /admin/products/new</code>)</li>
 *   <li>Ladda befintlig produkt i redigeringsformuläret (<code>GET /admin/products/edit?id={id}</code>)</li>
 *   <li>Skapa eller uppdatera produkt (<code>POST /admin/products/save</code>)</li>
 *   <li>Ta bort produkt (<code>POST /admin/products/delete</code>)</li>
 * </ul>
 *
 * <h2>Använda JSP-sidor</h2>
 * <ul>
 *   <li><strong>Lista:</strong> <code>/WEB-INF/views/admin/products.jsp</code>
 *       (request-attribut: {@code list})</li>
 *   <li><strong>Formulär:</strong> <code>/WEB-INF/views/admin/product_form.jsp</code>
 *       (request-attribut: {@code p} = Produkt, {@code cats} = Kategorilista)</li>
 * </ul>
 *
 * <h2>Begäransparametrar</h2>
 * <ul>
 *   <li>{@code id} – Produktens ID (vid redigering, borttagning, uppdatering)</li>
 *   <li>{@code name} – Produktnamn</li>
 *   <li>{@code description} – Produktbeskrivning</li>
 *   <li>{@code price} – Produktpris ({@link BigDecimal})</li>
 *   <li>{@code stock} – Lagerantal (int)</li>
 *   <li>{@code active} – Om produkten är aktiv (“on”/“true” → {@code true})</li>
 *   <li>{@code categoryId} – Produktens kategori-ID (kan vara tomt)</li>
 * </ul>
 *
 * <h2>Omdirigeringar och fel</h2>
 * <ul>
 *   <li>Vid lyckade POST-operationer sker omdirigering till <code>{contextPath}/admin/products</code>.</li>
 *   <li>Ogiltiga undersökvägar returnerar <strong>HTTP 404</strong>.</li>
 *   <li>Alla {@link SQLException}-fel kapslas och kastas som {@link ServletException}.</li>
 * </ul>
 *
 * <h2>Trelagersarkitektur</h2>
 * <p>
 * Servleten tar emot förfrågningar, väljer korrekt JSP och delegerar databehandling
 * till {@link ProductService}. Affärslogik och databasåtkomst hanteras i Service- och DAO-lagren,
 * vilket säkerställer tydlig separation mellan Controller – Service – DAO.
 * </p>
 *
 * <h2>Säkerhet</h2>
 * <p>
 * Denna servlet är endast avsedd för adminpanelen. Inloggning och behörighetskontroller
 * (t.ex. endast ADMIN-roll) implementeras i yttre lager, exempelvis {@code RoleFilter}.
 * </p>
 *
 * <h2>Exempel på förfrågningar</h2>
 * <pre>
 * GET  /admin/products
 * GET  /admin/products/new
 * GET  /admin/products/edit?id=7
 * POST /admin/products/save       name=Telefon&amp;price=5999.99&amp;stock=10&amp;active=on&amp;categoryId=2
 * POST /admin/products/delete     id=7
 * </pre>
 *
 * @author Your Name
 * @since 1.0
 */
@WebServlet(name = "AdminProductController",
        urlPatterns = {"/admin/products", "/admin/products/*"})
public class AdminProductController extends HttpServlet {

    /**
     * Servicelagret som hanterar produktoperationer (lista, skapa, uppdatera, ta bort)
     * samt hämtar tillhörande kategorier.
     * <p>Designad som stateless och trådsäker för flerkörning i servletmiljö.</p>
     */
    private final ProductService service = new ProductService();

    /**
     * Hanterar {@code GET}-förfrågningar.
     * <ul>
     *   <li><strong>{@code null}</strong> → hämtar produktlista och vidarebefordrar till <code>products.jsp</code></li>
     *   <li><strong>{@code "/new"}</strong> → öppnar formulär för ny produkt och sätter {@code cats}</li>
     *   <li><strong>{@code "/edit"}</strong> → laddar produkt enligt {@code id} samt kategorier</li>
     *   <li>Övriga fall → <strong>404</strong></li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // null, /new, /edit
        try {
            if (path == null) {
                req.setAttribute("list", service.listAll());
                req.getRequestDispatcher("/WEB-INF/views/admin/products.jsp").forward(req, resp);
            } else if ("/new".equals(path)) {
                req.setAttribute("cats", service.categories());
                req.getRequestDispatcher("/WEB-INF/views/admin/product_form.jsp").forward(req, resp);
            } else if ("/edit".equals(path)) {
                Long id = Long.valueOf(req.getParameter("id"));
                req.setAttribute("p", service.get(id));
                req.setAttribute("cats", service.categories());
                req.getRequestDispatcher("/WEB-INF/views/admin/product_form.jsp").forward(req, resp);
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Hanterar {@code POST}-förfrågningar.
     * <ul>
     *   <li><strong>/save</strong> → skapa eller uppdatera produkt</li>
     *   <li><strong>/delete</strong> → ta bort produkt</li>
     *   <li>Övrigt → <strong>404</strong></li>
     * </ul>
     * <p><strong>Konverteringar:</strong> price→{@link BigDecimal}, stock→int, active→boolean,
     * categoryId→{@code Long}/{@code null}. Vid framgång sker omdirigering till listvyn.</p>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // /save, /delete
        try {
            if ("/save".equals(path)) {
                String idStr = req.getParameter("id");
                String name = req.getParameter("name");
                String desc = req.getParameter("description");
                String priceStr = req.getParameter("price");
                String stockStr = req.getParameter("stock");
                String activeStr = req.getParameter("active");
                String catStr = req.getParameter("categoryId");

                Product p = new Product();
                if (idStr != null && !idStr.isBlank()) p.setId(Long.valueOf(idStr));
                p.setName(name);
                p.setDescription(desc);
                p.setPrice(new BigDecimal(priceStr));
                p.setStock(Integer.parseInt(stockStr));
                p.setActive("on".equalsIgnoreCase(activeStr) || "true".equalsIgnoreCase(activeStr));
                if (catStr == null || catStr.isBlank()) p.setCategoryId(null);
                else p.setCategoryId(Long.valueOf(catStr));

                if (p.getId() == null) service.create(p); else service.update(p);
                resp.sendRedirect(req.getContextPath() + "/admin/products");

            } else if ("/delete".equals(path)) {
                Long id = Long.valueOf(req.getParameter("id"));
                service.delete(id);
                resp.sendRedirect(req.getContextPath() + "/admin/products");
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
