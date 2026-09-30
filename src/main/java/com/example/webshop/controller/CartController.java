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
//@WebServlet(name = "CartController", urlPatterns = {"/cart/*"})
public class CartController extends HttpServlet {

    private final CartService cartService = new CartService();

    private final ProductDaoJdbc productDao = new ProductDaoJdbc();

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
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (NumberFormatException | SQLException e) {
            throw new ServletException(e);
        }
    }


    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = Long.valueOf(req.getParameter("productId"));
        int qty = Math.max(0, Integer.parseInt(req.getParameter("qty")));
        List<CartItem> cart = cartService.getOrCreateCart(req.getSession(true));

        cart.removeIf(ci -> ci.getProduct().getId().equals(productId) && qty == 0);

        cart.stream()
                .filter(ci -> ci.getProduct().getId().equals(productId))
                .findFirst()
                .ifPresent(ci -> ci.setQty(qty));

        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }

    private void handleRemove(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long productId = Long.valueOf(req.getParameter("productId"));
        List<CartItem> cart = cartService.getOrCreateCart(req.getSession(true));
        cart.removeIf(ci -> ci.getProduct().getId().equals(productId));
        resp.sendRedirect(req.getContextPath() + "/cart/view");
    }
}
