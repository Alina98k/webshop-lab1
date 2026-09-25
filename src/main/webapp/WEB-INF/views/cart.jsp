<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
=============================================================================
cart.jsp — Kundens varukorg (View-lagret)
=============================================================================

Syfte
-----
• Visa användarens varukorg (lagrad i sessionen).
• Möjlighet att uppdatera antal eller ta bort produkter.
• “Lägg beställning” startar OrderController → OrderService-flödet.

Kurskoppling
------------
• Betyg 3:
- “Shoppingkorg” – detta är vyn för kundens varukorg.
- Fullt flöde: lägga till / visa / uppdatera / ta bort produkt i korgen.
• Betyg 4:
- “Lägg beställning” → /orders/place (transaktion: order + lageruppdatering).
• MVC-sammanhang:
- Controller : CartController (GET /cart/view, POST /cart/add|update|remove)
- Service    : CartService (hantering av korg i session + totalbelopp)
- View       : Denna JSP (cart.jsp)

Tekniska detaljer
-----------------
• Korgen lagras i sessionScope.cart (skapad av CartService).
• “Delsumma” skickas som attributet "total" från controllern.
• Antalsfältet har min=0 och max=stock → förhindrar överskridande i frontend.
(Slutlig lagerkontroll sker på serversidan.)
• Talhantering sker via BigDecimal/Integer; formatering kan göras med fmt-taggar.

Säkerhet / Användarvänlighet
----------------------------
• CSRF-token bör läggas till i POST-formulär (framtida förbättring).
• Felmeddelanden eller “flash”-notiser (t.ex. vid otillräckligt lager) kan visas högst upp.

Förslag till vidareutveckling
-----------------------------
• Valutaformat (fmt:formatNumber) med symbol.
• Produktbilder och länkar.
• Summering med frakt, rabatt, kuponger.
• Förslag på produkter när korgen är tom.
=============================================================================
-->

<html>
<head><title>Min varukorg</title></head>
<body>
<h2>Min varukorg</h2>

<!-- Hämta varukorgen från sessionen -->
<c:set var="cart" value="${sessionScope.cart}" />

<!-- Om korgen är tom -->
<c:if test="${empty cart}">
    <p>Varukorgen är tom.</p>
    <p><a href="${pageContext.request.contextPath}/home">Tillbaka till butiken</a></p>
</c:if>

<!-- Om korgen innehåller produkter -->
<c:if test="${not empty cart}">
    <table border="1" cellpadding="6" cellspacing="0">
        <tr>
            <th>Produkt</th>
            <th>Antal</th>
            <th>Enhetspris</th>
            <th>Totalt</th>
            <th>Åtgärd</th>
        </tr>

        <c:forEach var="ci" items="${cart}">
            <tr>
                <!-- Produktnamn -->
                <td><c:out value="${ci.product.name}"/></td>

                <!-- Uppdatera antal -->
                <td>
                    <form method="post" action="${pageContext.request.contextPath}/cart/update">
                        <input type="hidden" name="productId" value="${ci.product.id}">
                        <input type="number" name="qty" value="${ci.qty}" min="0" max="${ci.product.stock}">
                        <button type="submit">Uppdatera</button>
                    </form>
                </td>

                <!-- Enhetspris -->
                <td>${ci.product.price}</td>

                <!-- Radens totalsumma: pris × antal -->
                <td>${ci.product.price * ci.qty}</td>

                <!-- Ta bort produkt -->
                <td>
                    <form method="post" action="${pageContext.request.contextPath}/cart/remove">
                        <input type="hidden" name="productId" value="${ci.product.id}">
                        <button type="submit">Ta bort</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>

    <!-- Delsumma (beräknad i CartService och satt som "total" av controllern) -->
    <p><strong>Delsumma:</strong> ${total}</p>

    <p>
        <a href="${pageContext.request.contextPath}/home">Fortsätt handla</a>
        &nbsp;|&nbsp;
        <!-- Lägg beställning: OrderController → OrderService (transaktion) -->
    <form method="post" action="${pageContext.request.contextPath}/orders/place" style="display:inline">
        <button type="submit">Lägg beställning</button>
    </form>
    </p>
</c:if>

</body>
</html>
