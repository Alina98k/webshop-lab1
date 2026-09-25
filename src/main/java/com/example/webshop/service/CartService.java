package com.example.webshop.service;

import com.example.webshop.model.Product;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
/**
 * {@code CartService} hanterar användarens kundvagn (shopping cart)
 * som lagras i sessionen. Den innehåller logik för att skapa en kundvagn,
 * lägga till produkter och beräkna totalpriset.
 *
 * <h2>Uppgiftsrelation (Vilken del?)</h2>
 * <ul>
 *   <li><strong>Betyg 3</strong>:
 *     <ul>
 *       <li><em>Shoppingkorg</em> – denna service uppfyller kravet genom att hantera
 *           varukorgens innehåll och totalbeloppet.</li>
 *       <li>Representerar <strong>Service-lagret</strong> i en trelagersarkitektur:
 *           Controller → Service → Model.</li>
 *     </ul>
 *   </li>
 *   <li><strong>Betyg 4</strong>:
 *     <ul>
 *       <li>Kundvagnens innehåll används senare av <code>OrderService</code>
 *           vid skapandet av en order (inom en transaktion).</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Arbetsprincip</h2>
 * <ul>
 *   <li>Kundvagnen lagras i användarens session (<code>session</code>).</li>
 *   <li>Om ingen kundvagn finns skapas en ny och läggs till i sessionen.</li>
 *   <li>Totalt belopp beräknas som pris × kvantitet för varje produkt.</li>
 *   <li>Produktpriser hanteras som <code>BigDecimal</code> för att undvika avrundningsfel.</li>
 * </ul>
 *
 * <h2>Modellrelation</h2>
 * <ul>
 *   <li><code>CartItem</code>: inre klass som lagrar produkt och kvantitet.</li>
 *   <li><code>Product</code>: modellklass som innehåller produktinformation som namn, pris och lager.</li>
 * </ul>
 *
 * <h2>Sessionshantering</h2>
 * <ul>
 *   <li>Kundvagnen kan skapas även utan inloggning (anonym shopping stöds).</li>
 *   <li>Efter inloggning kan vagnen kopplas till användaren eller nollställas
 *       (beroende på systemdesign).</li>
 * </ul>
 *
 * <h2>Framtida utbyggnad</h2>
 * <ul>
 *   <li>Lagerkontroll vid tillägg av produkt.</li>
 *   <li>Beräkning av delsumma per produkt.</li>
 *   <li>Stöd för rabattkoder, fraktkostnader eller moms.</li>
 *   <li>Permanent varukorg (lagring i databas eller cookie).</li>
 * </ul>
 *
 * <h2>Exempel på användning</h2>
 * <pre>{@code
 * CartService cartService = new CartService();
 * List<CartService.CartItem> cart = cartService.getOrCreateCart(session);
 *
 * // Lägg till produkt
 * CartService.CartItem ci = new CartService.CartItem();
 * ci.setProduct(product);
 * ci.setQty(2);
 * cart.add(ci);
 *
 * // Beräkna totalpris
 * BigDecimal total = cartService.calcTotal(cart);
 * }</pre>
 *
 * @author Your Name
 * @since 1.0
 */
public class CartService {

    /**
     * {@code CartItem} representerar en produkt i kundvagnen
     * med tillhörande kvantitet.
     */
    public static class CartItem {
        private Product product;
        private int qty;

        /** Produkt i kundvagnen. */
        public Product getProduct() { return product; }

        public void setProduct(Product product) { this.product = product; }

        /** Antal av produkten. */
        public int getQty() { return qty; }

        public void setQty(int qty) { this.qty = qty; }
    }

    /**
     * Returnerar kundvagnen från sessionen om den finns,
     * annars skapas en ny lista och sparas i sessionen.
     *
     * @param session HTTP-session
     * @return {@code List<CartItem>} – lista över varukorgens innehåll
     */
    public List<CartItem> getOrCreateCart(jakarta.servlet.http.HttpSession session) {
        @SuppressWarnings("unchecked")
        List<CartItem> cart = (List<CartItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    /**
     * Beräknar totalbeloppet för kundvagnen.
     * <p>Varje produkts pris multipliceras med kvantitet och summan
     * av alla produkter returneras.</p>
     *
     * @param cart kundvagnens innehåll
     * @return totalpris som {@link BigDecimal}
     */
    public BigDecimal calcTotal(List<CartItem> cart) {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart) {
            total = total.add(ci.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(ci.getQty())));
        }
        return total;
    }
}
