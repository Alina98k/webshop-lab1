<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
===========================================================================
product_form.jsp — Adminformulär för att lägga till/redigera produkter (View-lagret)
===========================================================================

Syfte
-----
• Gör det möjligt för administratörer att lägga till en ny produkt
eller redigera en befintlig.
• Controller: AdminProductController
- GET /admin/products/new   → öppnar formuläret tomt (p=null)
- GET /admin/products/edit  → öppnar formuläret med befintlig data ("p")
- POST /admin/products/save → behandlar formuläret (create/update)

Kursrelation (Vilken del?)
--------------------------
• Betyg 5
- Uppfyller direkt kravet “Lägga till och redigera varor och varukategorier”.
- Hanterar produkt–kategori-relation via kategoriurvalet (cats).
• Betyg 4
- Fältet "stock" motsvarar lagerhanteringens (varulager) användargränssnitt.
• Betyg 3
- MVC + trelagersstruktur (Controller → Service → DAO → DB → View) bibehålls.

Tekniska anteckningar
---------------------
• UTF-8 gör att svenska tecken visas korrekt.
• Prisfältet använder type="number" + step="0.01" för decimaltal.
• Checkboxen “aktiv” är förkryssad som standard; i redigeringsläge styrs den av p.active.
• categoryId kan lämnas tomt (produkten kan sakna kategori).
• Backend-validering hanteras i AdminProductController (obligatoriska fält, numeriska värden, BigDecimal).
• Behörighet: Åtkomst till sidan bör begränsas med RoleFilter (ADMIN).

Möjliga framtida förbättringar
-------------------------------
• Visa serversidiga felmeddelanden (request attribute "errors").
• Lägga till fält för att ladda upp produktbilder (multipart/form-data).
• Utökad validering: pris ≥ 0, lager ≥ 0, varning för dubblettnamn.
• Förbättrad användarupplevelse: Bootstrap/CSS, klientvalidering.
===========================================================================
-->

<html>
<head>
    <title>Produkt</title>
</head>
<body>

<!-- Rubrik: Ny eller Redigera -->
<h2>
    <c:if test="${empty p}">Ny</c:if>
    <c:if test="${not empty p}">Redigera</c:if>
    produkt
</h2>

<!-- Formulär -->
<form method="post" action="${pageContext.request.contextPath}/admin/products/save">
    <!-- Dolt ID vid redigering -->
    <c:if test="${not empty p}">
        <input type="hidden" name="id" value="${p.id}">
    </c:if>

    <!-- Produktnamn -->
    <p>Namn:
        <input type="text" name="name" value="${p.name}" required>
    </p>

    <!-- Beskrivning -->
    <p>Beskrivning:
        <textarea name="description" rows="4" cols="40">${p.description}</textarea>
    </p>

    <!-- Pris (decimal) -->
    <p>Pris:
        <input type="number" step="0.01" name="price" value="${p.price}" required>
    </p>

    <!-- Lager (heltal) -->
    <p>Lager:
        <input type="number" name="stock" value="${p.stock}" required>
    </p>

    <!-- Kategorival -->
    <p>Kategori:
        <select name="categoryId">
            <option value="">— Ingen vald —</option>
            <c:forEach var="c" items="${cats}">
                <option value="${c.id}"
                        <c:if test="${not empty p && p.categoryId == c.id}">selected</c:if>>
                    <c:out value="${c.name}"/>
                </option>
            </c:forEach>
        </select>
    </p>

    <!-- Aktivitet -->
    <p>Aktiv:
        <input type="checkbox" name="active"
               <c:if test="${empty p || p.active}">checked</c:if> >
    </p>

    <!-- Knappar -->
    <p>
        <button type="submit">Spara</button>
        <a href="${pageContext.request.contextPath}/admin/products">Avbryt</a>
    </p>
</form>

</body>
</html>
