package com.example.webshop.controller;

import com.example.webshop.model.Category;
import com.example.webshop.service.CategoryService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * {@code AdminCategoryController} är en {@link HttpServlet}-implementation som hanterar
 * grundläggande CRUD-funktioner för <strong>kategori</strong>-entiteter i admin-gränssnittet.
 *
 * <h2>Koppling till uppgiften (vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> — Den trelagersarkitektur (Controller → Service → DAO → DB) konkretiseras här.</li>
 *   <li><strong>Betyg 4</strong> — Stöd för produkt-/lagersystem där kategorier, lager och produkter hanteras.</li>
 *   <li><strong>Betyg 5</strong> — Kravet <em>”Lägga till och redigera varor och varukategorier”</em>
 *       uppfylls direkt. Denna controller hanterar CRUD-flödet för kategorier i adminpanelen.</li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Visa kategorilistan (GET <code>/admin/categories</code>)</li>
 *   <li>Visa formulär för ny kategori (GET <code>/admin/categories/new</code>)</li>
 *   <li>Visa formulär för att redigera befintlig kategori (GET <code>/admin/categories/edit?id={id}</code>)</li>
 *   <li>Skapa/uppdatera kategori (POST <code>/admin/categories/save</code>)</li>
 *   <li>Ta bort kategori (POST <code>/admin/categories/delete</code>)</li>
 * </ul>
 *
 * <h2>Vyfiler (JSP)</h2>
 * <ul>
 *   <li>Lista: <code>/WEB-INF/views/admin/categories.jsp</code> (request-attribut: {@code list})</li>
 *   <li>Formulär:  <code>/WEB-INF/views/admin/category_form.jsp</code> (request-attribut: {@code cat}, valfritt)</li>
 * </ul>
 *
 * <h2>Begäransparametrar</h2>
 * <ul>
 *   <li>{@code id} – ID för kategori (vid redigering/borttagning/uppdatering) ({@link Long})</li>
 *   <li>{@code name} – Kategorinamnet vid skapande/uppdatering</li>
 * </ul>
 *
 * <h2>Omdirigering och statuskoder</h2>
 * <ul>
 *   <li>Vid lyckad POST-operation sker omdirigering till listvyn:
 *       <code>{contextPath}/admin/categories</code></li>
 *   <li>Okända sökvägar returnerar <strong>404</strong>.</li>
 *   <li>Tekniska fel (t.ex. JDBC) kapslas i {@link ServletException}.</li>
 * </ul>
 *
 * <h2>Säkerhet/behörighet</h2>
 * <p>
 * Denna servlet är avsedd för <em>adminpanelen</em> och bör endast kunna nås av användare med rollen
 * <code>ADMIN</code>. Själva autentisering och behörighetskontroll hanteras utanför denna klass
 * (t.ex. av {@code RoleFilter}).
 * </p>
 *
 * <h2>Affärslogik och integration</h2>
 * <p>
 * All affärslogik och dataåtkomst hanteras av {@link CategoryService}.
 * Servleten ansvarar endast för routing, datakoppling och vyval, vilket
 * säkerställer tydlig ansvarsfördelning enligt MVC och trelagersarkitekturen.
 * </p>
 *
 * <h2>Trådsäkerhet</h2>
 * <p>
 * Servlet-instanser körs i flera trådar av containern.
 * {@link CategoryService}-instansen förväntas vara <em>trådsäker</em> (stateless design,
 * kortlivade {@code Connection}-objekt, connection poolning etc.).
 * </p>
 *
 * <h2>Exempel på förfrågningar</h2>
 * <pre>
 * GET  /admin/categories
 * GET  /admin/categories/new
 * GET  /admin/categories/edit?id=42
 * POST /admin/categories/save       body: name=Elektronik
 * POST /admin/categories/save       body: id=42&amp;name=Uppdaterat+Namn
 * POST /admin/categories/delete     body: id=42
 * </pre>
 *
 * @author Your Name
 * @since 1.0
 */
@WebServlet(name = "AdminCategoryController",
        urlPatterns = {"/admin/categories", "/admin/categories/*"})
public class AdminCategoryController extends HttpServlet {

    /** Service som kapslar in affärslogik och dataåtkomst för kategorier. */
    private final CategoryService service = new CategoryService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // null, /new, /edit
        try {
            if (path == null) { // lista
                List<Category> list = service.list();
                req.setAttribute("list", list);
                req.getRequestDispatcher("/WEB-INF/views/admin/categories.jsp").forward(req, resp);
            } else if ("/new".equals(path)) {
                req.getRequestDispatcher("/WEB-INF/views/admin/category_form.jsp").forward(req, resp);
            } else if ("/edit".equals(path)) {
                Long id = Long.valueOf(req.getParameter("id"));
                Category c = service.get(id);
                req.setAttribute("cat", c);
                req.getRequestDispatcher("/WEB-INF/views/admin/category_form.jsp").forward(req, resp);
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // /save, /delete
        try {
            if ("/save".equals(path)) {
                String idStr = req.getParameter("id");
                String name = req.getParameter("name");
                if (idStr == null || idStr.isBlank()) {
                    service.create(name);
                } else {
                    service.update(Long.valueOf(idStr), name);
                }
                resp.sendRedirect(req.getContextPath() + "/admin/categories");
            } else if ("/delete".equals(path)) {
                Long id = Long.valueOf(req.getParameter("id"));
                service.delete(id);
                resp.sendRedirect(req.getContextPath() + "/admin/categories");
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
