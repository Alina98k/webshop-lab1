<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.example.webshop.service.CartService" %>

<html>
<head>
    <title>Min varukorg</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css?v=2">
</head>

<body>
<%
    CartService cartService = new CartService();
    try {
        if ("add".equals(request.getParameter("action"))) {
            Long productId = Long.valueOf(request.getParameter("productId"));
            int qty = Integer.parseInt(request.getParameter("qty"));

            cartService.addToCart(session, productId, qty);
        }

        var cart = cartService.getOrCreateCart(session);
        request.setAttribute("cart", cart);
        request.setAttribute("total", cartService.calcTotal(cart));

    } catch (java.sql.SQLException e) {
        throw new jakarta.servlet.ServletException(e);
    }
%>

<h2>Min varukorg</h2>

<!-- Om korgen är tom -->
<c:if test="${empty cart}">
    <p>Varukorgen är tom.</p>
    <p>
        <a href="${pageContext.request.contextPath}/home">
            Tillbaka till butiken
        </a>
    </p>
</c:if>

<!-- Om korgen innehåller produkter -->
<c:if test="${not empty cart}">
    <table border="1" cellpadding="6" cellspacing="0">
        <tr>
            <th>Produkt</th>
            <th>Antal</th>
            <th>Enhetspris</th>
            <th>Totalt</th>
        </tr>

        <c:forEach var="ci" items="${cart}">
            <tr>
                <td>
                    <div class="cart-product">
                        <c:choose>
                            <c:when test="${ci.product.id == 1}">
                                <img class="cart-image"
                                     src="${pageContext.request.contextPath}/images/matchapulver.png"
                                     alt="Matchapulver">
                            </c:when>

                            <c:when test="${ci.product.id == 2}">
                                <img class="cart-image"
                                     src="${pageContext.request.contextPath}/images/matchavisp.png"
                                     alt="Matchavisp">
                            </c:when>

                            <c:when test="${ci.product.id == 3}">
                                <img class="cart-image"
                                     src="${pageContext.request.contextPath}/images/matchaskal.png"
                                     alt="Matchaskål">
                            </c:when>

                            <c:when test="${ci.product.id == 4}">
                                <img class="cart-image"
                                     src="${pageContext.request.contextPath}/images/matchakopp.png"
                                     alt="Matchakopp">
                            </c:when>
                        </c:choose>

                        <span><c:out value="${ci.product.name}"/></span>
                    </div>
                </td>

                <td>${ci.qty}</td>

                <td>${ci.product.price}</td>

                <td>${ci.product.price * ci.qty}</td>
            </tr>
        </c:forEach>
    </table>

    <p>
        <strong>Delsumma:</strong> ${total}
    </p>

    <p>
        <a href="${pageContext.request.contextPath}/home">
            Fortsätt handla
        </a>
    </p>
</c:if>

</body>
</html>
