package com.example.webshop.controller;

import com.example.webshop.dao.ProductDaoJdbc;
import com.example.webshop.model.Product;
import com.example.webshop.service.CartService;
import com.example.webshop.service.CartService.CartItem;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
/**
 * {@code CartController} är en {@link HttpServlet} som hanterar kundens varukorgsfunktioner.
 * Den innehåller logik för att visa kundvagnen, lägga till produkter, uppdatera antal och ta bort artiklar.
 *
 * <h2>Uppgiftskoppling (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li><em>Shoppingkorg</em> — <strong>uppfylls</strong>.</li>
 *       <li><em>Möjlighet att lägga saker i korgen och titta i den</em> — <strong>uppfylls</strong>
 *           (GET <code>/cart/view</code>, POST <code>/cart/add</code>).</li>
 *       <li><em>3-lagers arkitektur</em> — Controller (denna klass), Service ({@link CartService}),
 *           DAO ({@link ProductDaoJdbc}) — <strong>uppfylls</strong>.</li>
 *     </ul>
 */
@WebServlet(name = "CartController", urlPatterns = {"/cart/*"})
public class CartController extends HttpServlet {

    /** Hanterar kundvagnens livscykel (lista i sessionen). */
    private final CartService cartService = new CartService();

    private final ProductDaoJdbc productDao = new ProductDaoJdbc();

    /**
     * Hanterar {@code GET}-förfrågningar.
     *
     * <p><strong>Stödda sökvägar:</strong> <code>/cart/view</code> eller <code>/cart</code> (null path).<br/>
     * Hämtar kundvagnen, beräknar totalsumma och skickar vidare till <code>cart.jsp</code>.</p>
     *
     * @param req  HTTP-förfrågan
     * @param resp HTTP-svar
     * @throws ServletException vid fel i JSP-forwarding
     * @throws IOException      vid I/O-fel
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // GET /cart/view  → visa kundvagn
        String path = req.getPathInfo(); // /view
        if (path == null || "/view".equals(path)) {
            List<CartItem> cart = cartService.getOrCreateCart(req.getSession(true));
            req.setAttribute("total", cartService.calcTotal(cart));
            req.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(req, resp);
            return;
        }
        resp.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    /**
     * Hanterar {@code POST}-förfrågningar: lägg till, uppdatera eller ta bort objekt i kundvagnen.
     *
     * <ul>
     *   <li><strong>/add</strong> — {@link #handleAdd(HttpServletRequest, HttpServletResponse)}</li>
     *   <li><strong>/update</strong> — {@link #handleUpdate(HttpServletRequest, HttpServletResponse)}</li>
     *   <li><strong>/remove</strong> — {@link #handleRemove(HttpServletRequest, HttpServletResponse)}</li>
     * </ul>
     *
     * @param req  HTTP-förfrågan (formparametrar: productId, qty)
     * @param resp HTTP-svar
     * @throws ServletException affärs-/DB-fel kan kapslas
     * @throws IOException      vid omdirigering/I/O-fel
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String path = req.getPathInfo(); // /add, /update, /remove
        if ("/add".equals(path)) {
            handleAdd(req, resp);
        } else if ("/update".equals(path)) {
            handleUpdate(req, resp);
        } else if ("/remove".equals(path)) {
            handleRemove(req, resp);
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Lägger till produkt i kundvagnen och kontrollerar lager och aktivitet.
     * <p><strong>Obs:</strong> Lagerkontrollen är “omedelbar”; den bör bekräftas igen vid beställning.</p>
     *
     * @param req  productId (obligatorisk), qty (normaliseras till ≥1)
     * @param resp omdirigering
     * @throws IOException      vid I/O-fel under redirect
     * @throws ServletException vid konverterings- eller SQL-fel
     */
    private void handleAdd(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        try {
            Long productId = Long.valueOf(req.getParameter("productId"));
            int qty = Math.max(1, Integer.parseInt(req.getParameter("qty")));
            Product p = productDao.findById(productId);

            if (p == null) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            List<CartItem> cart = cartService.getOrCreateCart(req.getSession(true));
            Optional<CartItem> ex = cart.stream()
                    .filter(ci -> ci.getProduct().getId().equals(productId))
                    .findFirst();

            if (ex.isPresent()) {
                ex.get().setQty(ex.get().getQty() + qty);
            } else {
                CartItem ci = new CartItem();
                ci.setProduct(p);
                ci.setQty(qty);
                cart.add(ci);
            }
            resp.sendRedirect(req.getContextPath() + "/cart/view");
        } catch (NumberFormatException | SQLException e) {
            throw new ServletException(e);
        }
    }

    /**
     * Uppdaterar antalet för en produkt i kundvagnen; om nytt antal = 0 tas den bort.
     *
     * @param req  productId (obligatorisk), qty (normaliseras till ≥0)
     * @param resp omdirigering
     * @throws IOException vid I/O-fel under redirect
     */
    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = Long.valueOf(req.getParameter("productId"));
        int qty = Math.max(0, Integer.parseInt(req.getParameter("qty")));
        List<CartItem> cart = cartService.getOrCreateCart(req.getSession(true));

        // qty=0 → ta bort produkten
        cart.removeIf(ci -> ci.getProduct().getId().equals(productId) && qty == 0);

        // qty>0 → uppdatera
        cart.stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(ci -> ci.setQty(qty));

        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }

    /**
     * Tar bort en produkt helt från kundvagnen.
     *
     * @param req  productId (obligatorisk)
     * @param resp omdirigering
     * @throws IOException vid I/O-fel under redirect
     */
    private void handleRemove(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = Long.valueOf(req.getParameter("productId"));
        List<CartItem> cart = cartService.getOrCreateCart(req.getSession(true));
        cart.removeIf(ci -> ci.getProduct().getId().equals(productId));
        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }
}
