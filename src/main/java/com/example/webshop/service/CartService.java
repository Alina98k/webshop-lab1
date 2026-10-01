package com.example.webshop.service;

import com.example.webshop.dao.ProductDaoJdbc;
import com.example.webshop.entities.Product;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Service-klass som hanterar webshoppens shoppingkorg.
 */
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

    /**
     * Hämtar den befintliga shoppingkorgen eller skapar en ny i sessionen.
     *
     * @param session användarens session
     * @return shoppingkorgen
     */
    public List<CartItem> getOrCreateCart(HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");

        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }

        return cart;
    }

    /**
     * Lägger till en produkt i shoppingkorgen.
     * Om produkten redan finns ökas antalet.
     *
     * @param session användarens session
     * @param productId produktens id
     * @param qty antal som ska läggas till
     * @return true om produkten kunde läggas till, annars false
     */
    public boolean addToCart(HttpSession session, Long productId, int qty) {
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

    /**
     * Räknar ut den totala kostnaden för produkterna i shoppingkorgen.
     *
     * @param cart shoppingkorgen
     * @return den totala kostnaden
     */
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