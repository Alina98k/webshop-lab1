<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
============================================================================
home.jsp — Kundens startsida / produktlista (View-lagret)
============================================================================

Syfte
-----
• Visa alla aktiva produkter och erbjuda ett formulär för att lägga dem i varukorgen.
• Visa meny-länkar beroende på inloggad användare och roll (login/roles).

Kurskoppling
------------
• Betyg 3:
- “Möjlighet att lägga saker i korgen och titta i den.” → formulär för att lägga till i korgen
samt länk till “Visa varukorg”.
• Betyg 4:
- Lagerinformation (p.stock) visas, och max-attributet i formuläret förhindrar överskridande.
• Betyg 5:
- Rollbaserad meny: ADMIN får adminlänkar, WAREHOUSE får lagerlänkar.

MVC-sammanhang
--------------
• Controller: HomeController (GET /home → products attribute)
• Service   : ProductService.listActive()
• DAO       : ProductDaoJdbc.findAllActive()
• View      : Denna JSP (home.jsp)

Tekniska detaljer
-----------------
• UTF-8 → korrekt visning av svenska tecken.
• “flash”-meddelanden (sessionScope.flash) visas högst upp och tas sedan bort.
• Rollbaserade länkar: sessionScope.isAdmin / isWarehouse (sätts av AuthController).
• Lägg till i korgen: POST /cart/add (CartController), qty min=1 max=stock.
• Valutaformat och datumformatering kan senare göras med fmt-taggar.

Säkerhet / Användarvänlighet
----------------------------
• POST-formulär bör skyddas med CSRF-token (för framtida versioner).
• Backend kontrollerar att antalet inte överstiger lager (CartController / OrderService).
============================================================================
-->

<html>
<head><title>Webshop - Produkter</title></head>
<body>

<!-- Meny baserat på användarens session -->
<div>
    <c:choose>
        <c:when test="${not empty sessionScope.user}">
            Hej, ${sessionScope.user.fullName} |
            <a href="${pageContext.request.contextPath}/logout">Logga ut</a>
            <c:if test="${sessionScope.isAdmin}">
                | <a href="${pageContext.request.contextPath}/admin/categories">Admin: Kategorier</a>
                | <a href="${pageContext.request.contextPath}/admin/products">Admin: Produkter</a>
            </c:if>
            <c:if test="${sessionScope.isWarehouse}">
                | <a href="${pageContext.request.contextPath}/warehouse/orders">Lager: Beställningar</a>
            </c:if>
        </c:when>
        <c:otherwise>
            <a href="${pageContext.request.contextPath}/login">Logga in</a>
        </c:otherwise>
    </c:choose>
</div>
<hr/>

<!-- Flash-meddelande (t.ex. lager saknas) -->
<c:if test="${not empty sessionScope.flash}">
    <div style="color:red">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session" />
</c:if>

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
            — Lager: ${p.stock}

            <!-- Lägg till i varukorgen -->
            <form method="post" action="${pageContext.request.contextPath}/cart/add" style="display:inline">
                <input type="hidden" name="productId" value="${p.id}">
                <input type="number" name="qty" value="1" min="1" max="${p.stock}">
                <button type="submit">Lägg i varukorgen</button>
            </form>
        </li>
    </c:forEach>
</ul>

<!-- Länk till varukorgen -->
<p><a href="${pageContext.request.contextPath}/cart/view">Visa varukorg</a></p>
</body>
</html>
