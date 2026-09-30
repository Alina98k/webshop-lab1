<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page import="com.example.webshop.service.ProductService" %>
<%@ page import="java.sql.SQLException" %>


<html>
<head>
    <title>Matcha – Produkter</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>

<%
    try {
        ProductService productService = new ProductService();
        request.setAttribute("products", productService.listActive());
    } catch (SQLException e) {
        throw new jakarta.servlet.ServletException(e);
    }
%>  <%-- Hämtar aktiva produkter från ProductService--%>

<!-- Meny baserat på användarens session -->
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

<!-- Om inga produkter finns -->
<c:if test="${empty products}">
    <p>Det finns inga produkter just nu.</p>
</c:if>

<!-- Produktlista -->
<ul class="product-list">
    <c:forEach var="p" items="${products}">
        <li>
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

            <strong><c:out value="${p.name}"/></strong>
            <p>Pris: ${p.price} kr</p>

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

<!-- Länk till varukorgen -->
<p><a href="${pageContext.request.contextPath}/cart">Visa varukorg</a></p>
</body>
</html>
