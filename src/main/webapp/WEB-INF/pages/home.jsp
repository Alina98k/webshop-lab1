<%--
    JSP-sida som visar webshoppens produkter och låter användaren
    lägga till produkter i varukorgen
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.example.webshop.service.ProductService" %>

<html>
<head>
    <title>Matcha – Produkter</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<%
    // Skapar en service för hantering av produkter
    ProductService productService = new ProductService();

    // Hämtar produktlistan och gör den tillgänglig för sidans visning
    request.setAttribute("products", productService.listActive());
%>

<%-- Visar användarens namn och utloggningslänk, annars en inloggningslänk --%>
<div>
    <c:choose>
        <c:when test="${not empty sessionScope.user}">
            Hej, ${sessionScope.user.fullName} |
            <a href="${pageContext.request.contextPath}/logout">Logga ut</a>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/login">Logga in</a>
        </c:otherwise>
    </c:choose>
</div>
<hr/>

<h2>Produkter</h2>

<%-- Visar ett meddelande när produktlistan är tom --%>
<c:if test="${empty products}">
    <p>Det finns inga produkter just nu.</p>
</c:if>

<%-- Visar varje produkt i produktlistan --%>
<ul class="product-list">
    <c:forEach var="p" items="${products}">
        <li>

                <%-- Visar produktbild utifrån produktens id --%>
            <c:if test="${p.id == 1}">
                <img class="product-image"
                     src="${pageContext.request.contextPath}/images/matchapulver.png"
                     alt="Burk med matchapulver">
            </c:if>

            <c:if test="${p.id == 2}">
                <img class="product-image"
                     src="${pageContext.request.contextPath}/images/matchavisp.png"
                     alt="Matchavisp av bambu">
            </c:if>

            <c:if test="${p.id == 3}">
                <img class="product-image"
                     src="${pageContext.request.contextPath}/images/matchaskal.png"
                     alt="Matchagrön keramikskål">
            </c:if>

            <c:if test="${p.id == 4}">
                <img class="product-image"
                     src="${pageContext.request.contextPath}/images/matchakopp.png"
                     alt="Matchagrön keramikkopp">
            </c:if>

                <%-- Visar produktens namn och pris --%>
            <strong><c:out value="${p.name}"/></strong>
            <p>Pris: ${p.price} kr</p>

                <%-- Skickar produktens id och valt antal till varukorgen via POST --%>
            <form method="post"
                  action="${pageContext.request.contextPath}/cart">
                <input type="hidden" name="action" value="add">
                <input type="hidden" name="productId" value="${p.id}">

                <label for="qty-${p.id}">Antal</label>
                <input type="number"
                       id="qty-${p.id}"
                       name="qty"
                       value="1"
                       min="1"
                       required>

                <button type="submit">Lägg i varukorgen</button>
            </form>
        </li>
    </c:forEach>
</ul>

<%-- Visar en länk till användarens varukorg --%>
<p>
    <a href="${pageContext.request.contextPath}/cart">Visa varukorg</a>
</p>

</body>
</html>