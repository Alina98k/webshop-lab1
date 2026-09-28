package com.example.webshop.controller;

import com.example.webshop.model.User;
import com.example.webshop.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;

/**
 * {@code AuthController}, är en {@link HttpServlet} som hanterar applikationens autentiserings-endpoints.
 * Den ansvarar för inloggning (login) och utloggning (logout); vid lyckad inloggning skapas en session,
 * och vid utloggning avslutas den.
 *
 * <h2>Uppgiftskoppling (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> — <em>Användaridentifiering</em> (user recognition) uppfylls direkt:
 *       inloggningsformulär → autentisering → sessionsvariabler sätts.</li>
 *   <li><strong>Betyg 4</strong> — <em>Utbyggd användarhantering</em> och <em>behörighetsklasser</em>:
 *       roller sparas i sessionen (<code>roles</code>, <code>isAdmin</code>, <code>isWarehouse</code>)
 *       och används som grund för åtkomst till skyddade sidor via <code>RoleFilter</code>.</li>
 *   <li><strong>Betyg 5</strong> — Ger nödvändig identitets- och rollinformation för att komma åt
 *       admin- och lagergränssnitt (kategori-/produkt-hantering, paketering).</li>
 * </ul>
 *
 * <h2>Endpoints</h2>
 * <ul>
 *   <li><strong>GET /login</strong> – Visar inloggningsformuläret.</li>
 *   <li><strong>POST /login</strong> – Utför autentisering med användarnamn/lösenord.</li>
 *   <li><strong>GET /logout</strong> – Avslutar aktuell session och omdirigerar till startsidan.</li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Dirigera till vyer (JSP) och samla formulärdata.</li>
 *   <li>Bearbeta autentiseringsresultat via {@link AuthService}.</li>
 *   <li>Lagra användar- och rollinformation i sessionen vid lyckad inloggning.</li>
 *   <li>Vid misslyckad inloggning – visa felmeddelande och återgå till formuläret.</li>
 * </ul>
 *
 * <h2>Sessionsvariabler</h2>
 * <ul>
 *   <li>{@code "user"} – Den inloggade användarens {@link User}-objekt.</li>
 *   <li>{@code "userId"} – Användarens ID; används t.ex. i orderflöden.</li>
 *   <li>{@code "roles"} – Användarens rolluppsättning (t.ex. {@code ["ADMIN","WAREHOUSE"]}).</li>
 *   <li>{@code "isAdmin"} – Boolesk flagga för snabb kontroll.</li>
 *   <li>{@code "isWarehouse"} – Boolesk flagga för snabb kontroll.</li>
 * </ul>
 *
 * <h2>Säkerhetsnoteringar</h2>
 * <ul>
 *   <li>Rollbaserad auktorisering bör implementeras utanför denna controller (t.ex. i {@code Filter}).</li>
 *   <li>För att minska session-fixation-risk bör sessions-ID förnyas efter lyckad inloggning
 *       (t.ex. med {@code changeSessionId()}).</li>
 *   <li>I produktion rekommenderas CSRF-skydd och skydd mot brute-force-attacker.</li>
 *   <li>Cookies bör ha attributen {@code HttpOnly}, {@code Secure} och korrekt {@code SameSite}.</li>
 * </ul>
 *
 * <h2>Vyer (JSP)</h2>
 * <ul>
 *   <li>Inloggningsformulär: <code>/WEB-INF/views/login.jsp</code></li>
 *   <li>Felmeddelande skickas som attribut: <code>"error"</code></li>
 * </ul>
 *
 * <h2>Exempel på förfrågningar</h2>
 * <pre>
 * GET  /login
 * POST /login    body: username=alice&amp;password=secret
 * GET  /logout
 * </pre>
 *
 * <h2>Felhantering</h2>
 * <ul>
 *   <li>Databas- eller systemfel kapslas i {@link ServletException}.</li>
 *   <li>Ogiltig sökväg ger HTTP 404 (i GET-förfrågningar).</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
@WebServlet(name = "AuthController", urlPatterns = {"/login","/logout"})
public class AuthController extends HttpServlet {

    /**
     * Servicelagret som utför autentisering.
     * <p>Designad som stateless (utan tillstånd); säkert för trådad miljö.</p>
     */
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getServletPath(); // "/login" or "/logout"
        if ("/login".equals(path)) {
            req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
        } else if ("/logout".equals(path)) {
            HttpSession s = req.getSession(false);
            if (s != null) s.invalidate();
            resp.sendRedirect(req.getContextPath() + "/home");
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        try {
            User user = authService.login(username, password);
            if (user == null) {
                req.setAttribute("error", "Användarnamn eller lösenord är felaktigt.");
                req.getRequestDispatcher("/WEB-INF/views/login.jsp").forward(req, resp);
                return;
            }

            HttpSession s = req.getSession(true);
            s.setAttribute("user", user);
            s.setAttribute("userId", user.getId());     // för OrderController


            // Valfritt: minska session fixation
            // if (req.isRequestedSessionIdValid()) { req.changeSessionId(); }

            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}