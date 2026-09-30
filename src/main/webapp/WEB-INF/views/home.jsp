<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


<html>
<head><title>Webshop - Produkter</title></head>
<body>

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
<ul>
    <c:forEach var="p" items="${products}">
        <li>
            <strong><c:out value="${p.name}"/></strong>
            — Pris: ${p.price}
            <!-- Lägg till i varukorgen -->
            <form method="post" action="${pageContext.request.contextPath}/cart/add" style="display:inline">
                <input type="hidden" name="productId" value="${p.id}">
                <input type="number" name="qty" value="1" min="1">
                <button type="submit">Lägg i varukorgen</button>
            </form>
        </li>
    </c:forEach>
</ul>

<!-- Länk till varukorgen -->
<p><a href="${pageContext.request.contextPath}/cart/view">Visa varukorg</a></p>
</body>
</html>
