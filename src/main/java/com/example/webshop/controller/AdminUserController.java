package com.example.webshop.controller;

import com.example.webshop.model.User;
import com.example.webshop.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

/**
 * {@code AdminUserController} är en {@link HttpServlet} som hanterar
 * CRUD-operationer och <em>rollhantering</em> för användare i <strong>administrationspanelen</strong>.
 *
 * <h2>Koppling till uppgiften (vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 4</strong> — Avancerad användarhantering och <em>behörighetsklasser</em>:
 *       listar, lägger till, redigerar och tar bort användare samt tilldelar flera roller
 *       (t.ex. ADMIN, WAREHOUSE, CUSTOMER).</li>
 *   <li><strong>Betyg 5</strong> — Utvecklar administrationsgränssnittet och möjliggör
 *       rollbaserad åtkomst till sidor som kategori-, produkt- och lagerhantering.</li>
 * </ul>
 *
 * <h2>Slutpunkter</h2>
 * <pre>
 *  GET  /admin/users              → lista användare
 *  GET  /admin/users/new          → nytt användarformulär
 *  GET  /admin/users/edit?id=...  → redigeringsformulär
 *  POST /admin/users/save         → skapa/uppdatera (+roller)
 *  POST /admin/users/delete       → ta bort
 * </pre>
 *
 * <h2>Vyfiler (JSP)</h2>
 * <ul>
 *   <li>Lista: <code>/WEB-INF/views/admin/users.jsp</code> (request-attribut: {@code list})</li>
 *   <li>Formulär: <code>/WEB-INF/views/admin/user_form.jsp</code>
 *       (request-attribut: {@code u} = User, {@code userRoles}, {@code allRoles})</li>
 * </ul>
 *
 * <h2>Begäransparametrar</h2>
 * <ul>
 *   <li>{@code id} — användarens ID (Long, valfritt)</li>
 *   <li>{@code username}, {@code fullName}, {@code email}</li>
 *   <li>{@code password} — lämnas tomt vid uppdatering om lösenordet inte ska ändras</li>
 *   <li>{@code active} — “on”/“true” → {@code true}</li>
 *   <li>{@code roles} — flervalsparameter (String[])</li>
 * </ul>
 *
 * <h2>Arkitektur och ansvarsfördelning</h2>
 * <p>
 * Controllern anropar affärslogiken via {@link UserService} och ansvarar för datakoppling,
 * omdirigering och val av JSP. Affärslogik och datalagring hanteras i Service- och DAO-lagren,
 * vilket upprätthåller den tredelade arkitekturen (Controller → Service → DAO).
 * </p>
 *
 * <h2>Säkerhet</h2>
 * <ul>
 *   <li>Dessa slutpunkter ska endast vara tillgängliga för användare med rollen <strong>ADMIN</strong> (t.ex. via {@code RoleFilter}).</li>
 *   <li>CSRF-skydd bör användas i POST-formulär via säkerhetstoken.</li>
 *   <li>Lösenord uppdateras endast om {@code password}-fältet är ifyllt
 *       (hanteras i {@link UserService}).</li>
 * </ul>
 *
 * <h2>Felkedjor</h2>
 * <ul>
 *   <li>Alla JDBC-fel kapslas i {@link ServletException}.</li>
 *   <li>Okända undersökvägar returnerar <strong>404</strong>.</li>
 * </ul>
 */
@WebServlet(name="AdminUserController", urlPatterns={"/admin/users","/admin/users/*"})
public class AdminUserController extends HttpServlet {

    /** Servicelager som hanterar användar- och rollfunktionalitet. */
    private final UserService service = new UserService();

    /**
     * Hanterar {@code GET}-förfrågningar.
     * <ul>
     *   <li>{@code null} → listar alla användare</li>
     *   <li>{@code /new} → öppnar ett tomt formulär + hämtar alla roller</li>
     *   <li>{@code /edit} → laddar användare enligt {@code id}, användarens roller och alla tillgängliga roller</li>
     *   <li>Övriga → 404</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // null, /new, /edit
        try {
            if (path == null) {
                req.setAttribute("list", service.listAll());
                req.getRequestDispatcher("/WEB-INF/views/admin/users.jsp").forward(req, resp);
            } else if ("/new".equals(path)) {
                req.setAttribute("allRoles", service.listAllRoles()); // ["ADMIN","WAREHOUSE","CUSTOMER",...]
                req.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp").forward(req, resp);
            } else if ("/edit".equals(path)) {
                Long id = Long.valueOf(req.getParameter("id"));
                req.setAttribute("u", service.get(id));
                req.setAttribute("userRoles", service.getRoles(id));   // användarens nuvarande roller
                req.setAttribute("allRoles", service.listAllRoles());  // alla tillgängliga roller
                req.getRequestDispatcher("/WEB-INF/views/admin/user_form.jsp").forward(req, resp);
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
     *   <li>{@code /save} → skapa eller uppdatera användare (+roller)</li>
     *   <li>{@code /delete} → ta bort användare</li>
     *   <li>Övrigt → 404</li>
     * </ul>
     *
     * <p><strong>Not:</strong> Om {@code password} är tomt vid uppdatering, bevaras det befintliga lösenordet.
     * Roller hanteras som en flervalsparameter och skickas vidare till {@link UserService}.</p>
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // /save, /delete
        try {
            if ("/save".equals(path)) {
                String idStr = req.getParameter("id");
                String username = req.getParameter("username");
                String fullName = req.getParameter("fullName");
                String email = req.getParameter("email");
                String password = req.getParameter("password"); // kan vara tomt vid uppdatering
                boolean active = "on".equalsIgnoreCase(req.getParameter("active")) ||
                        "true".equalsIgnoreCase(req.getParameter("active"));
                String[] rolesArr = req.getParameterValues("roles"); // flervalsfält
                List<String> roles = rolesArr == null ? List.of() : Arrays.asList(rolesArr);

                User u = new User();
                if (idStr != null && !idStr.isBlank()) u.setId(Long.valueOf(idStr));
                u.setUsername(username);
                u.setFullName(fullName);
                u.setEmail(email);
                u.setActive(active);

                if (u.getId() == null) service.create(u, password, roles);
                else service.update(u, password, roles); // lösenord uppdateras bara om det är ifyllt

                resp.sendRedirect(req.getContextPath() + "/admin/users");

            } else if ("/delete".equals(path)) {
                Long id = Long.valueOf(req.getParameter("id"));
                service.delete(id);
                resp.sendRedirect(req.getContextPath() + "/admin/users");
            } else {
                resp.sendError(404);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
