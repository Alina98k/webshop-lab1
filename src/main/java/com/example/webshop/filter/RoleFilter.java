package com.example.webshop.filter;

import com.example.webshop.model.User;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

/**
 * {@code RoleFilter} är ett Servlet-filter som kontrollerar inkommande förfrågningar
 * och endast tillåter åtkomst för användare med en specifik roll.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong> –
 *       <em>Användaridentifiering</em>:
 *       Filtrerar förfrågningar och läser autentiserade användare från sessionen.</li>
 *   <li><strong>Betyg 4</strong> –
 *       <em>Utbyggd användarhantering och behörighetsklasser</em>:
 *       Filtren kontrollerar roller som “customer / admin / warehouse” och uppfyller därmed kraven.</li>
 *   <li><strong>Betyg 5</strong> –
 *       Avancerad rättighetsuppdelning (t.ex. adminpaneler, lagergränssnitt)
 *       skyddas genom detta filter.</li>
 * </ul>
 *
 * <h2>Ansvar</h2>
 * <ul>
 *   <li>Läser användarens rollista från HTTP-sessionen
 *       (<code>session.getAttribute("roles")</code>).</li>
 *   <li>Jämför med init-parametern <code>role</code> som definieras i filterkonfigurationen.</li>
 *   <li>Om rollen saknas eller användaren inte är inloggad, blockeras åtkomst.</li>
 * </ul>
 *
 * <h2>Arbetsflöde</h2>
 * <ol>
 *   <li>Filtret deklareras i <code>web.xml</code> för en specifik Servlet eller URL-pattern:
 *     <pre>{@code
 * <filter>
 *   <filter-name>AdminFilter</filter-name>
 *   <filter-class>com.example.webshop.filter.RoleFilter</filter-class>
 *   <init-param>
 *     <param-name>role</param-name>
 *     <param-value>ADMIN</param-value>
 *   </init-param>
 * </filter>
 *
 * <filter-mapping>
 *   <filter-name>AdminFilter</filter-name>
 *   <url-pattern>/admin/*</url-pattern>
 * </filter-mapping>
 * }</pre></li>
 *   <li>När en begäran tas emot:
 *     <ul>
 *       <li>Om ingen session finns → användaren omdirigeras till <code>/login</code>.</li>
 *       <li>Om rolllistan saknas eller inte innehåller den nödvändiga rollen → returnera HTTP 403 FORBIDDEN.</li>
 *       <li>Om rollen matchar → kedjan fortsätter (<code>chain.doFilter</code>).</li>
 *     </ul>
 *   </li>
 * </ol>
 *
 * <h2>Säkerhetsanmärkningar</h2>
 * <ul>
 *   <li>Filtret hanterar endast åtkomst på presentationsnivå; känsliga operationer
 *       bör dessutom kontrolleras i service-lagret.</li>
 *   <li>Standardbeteendet är att returnera 403 FORBIDDEN; detta kan ersättas med en
 *       omdirigering till en anpassad felsida om så önskas.</li>
 *   <li>För flera roller kan man använda flera filterinstanser eller utökad logik
 *       (t.ex. <code>anyMatch</code>).</li>
 * </ul>
 *
 * <h2>Exempel på förfrågningar</h2>
 * <pre>
 * GET /admin/products      → endast ADMIN-roll
 * GET /warehouse/orders    → endast WAREHOUSE-roll
 * </pre>
 *
 * @author Your Name
 * @since 1.0
 */
public class RoleFilter implements Filter {

    /** Den roll som krävs för att få åtkomst (hämtas från init-parametern). */
    private String requiredRole;

    /**
     * Läser in den nödvändiga rollen från filterkonfigurationen vid initiering.
     *
     * @param filterConfig filterkonfigurationen från web.xml
     */
    @Override
    public void init(FilterConfig filterConfig) {
        requiredRole = filterConfig.getInitParameter("role");
    }

    /**
     * Filtrerar begäran och svaret; blockerar åtkomst om session eller roll saknas.
     *
     * @param request  inkommande begäran
     * @param response utgående svar
     * @param chain    filterkedjan
     * @throws IOException        vid I/O-fel
     * @throws ServletException   vid fel i andra filter eller servlets
     */
    @Override
    @SuppressWarnings("unchecked")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) request;
        HttpServletResponse s = (HttpServletResponse) response;
        HttpSession session = r.getSession(false);

        // 1. Ingen session → omdirigera till inloggningssidan
        if (session == null) {
            s.sendRedirect(r.getContextPath() + "/login");
            return;
        }

        // 2. Rollkontroll
        List<String> roles = (List<String>) session.getAttribute("roles");
        if (roles == null || requiredRole == null || !roles.contains(requiredRole)) {
            s.sendError(HttpServletResponse.SC_FORBIDDEN); // 403
            return;
        }

        // 3. Användaren har rätt roll → fortsätt kedjan
        chain.doFilter(request, response);
    }
}
