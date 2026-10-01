package com.example.webshop.service;

import com.example.webshop.dao.ProductDaoJdbc;
import com.example.webshop.model.Product;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    private final ProductDaoJdbc productDao = new ProductDaoJdbc();
    public static class CartItem {
        private Product product;
        private int qty;

        public Product getProduct() {
            return product;
        }

        public void setProduct(Product product) {
            this.product = product;
        }

        public int getQty() {
            return qty;
        }

        public void setQty(int qty) {
            this.qty = qty;
        }
    }

    public List<CartItem> getOrCreateCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        return cart;
    }

    public boolean addToCart(HttpSession session, Long productId, int qty)
            throws SQLException {

        qty = Math.max(1, qty);

        Product product = productDao.findById(productId);

        if (product == null) {
            return false;
        }

        List<CartItem> cart = getOrCreateCart(session);

        for (CartItem item : cart) {
            if (item.getProduct().getId().equals(productId)) {
                item.setQty(item.getQty() + qty);
                return true;
            }
        }

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQty(qty);
        cart.add(item);

        return true;
    }


    public BigDecimal calcTotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart) {
            total = total.add(
                    item.getProduct().getPrice()
                            .multiply(BigDecimal.valueOf(item.getQty()))
            );
        }

        return total;
    }
}