package com.example.webshop.service;

import com.example.webshop.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CartService {

    public static class CartItem {
        private Product product;
        private int qty;

        public Product getProduct() { return product; }

        public void setProduct(Product product) { this.product = product; }

        public int getQty() { return qty; }

        public void setQty(int qty) { this.qty = qty; }
    }

    public List<CartItem> getOrCreateCart(jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    public BigDecimal calcTotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart) {
            total = total.add(ci.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(ci.getQty())));
        }
        return total;
    }
}
