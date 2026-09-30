<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

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
                        <input type="number" name="qty" value="${ci.qty}" min="0">
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
    </p>
</c:if>

</body>
</html>
