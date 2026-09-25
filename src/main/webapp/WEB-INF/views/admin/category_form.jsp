<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
========================================================================
category_form.jsp — Adminformulär för att lägga till/redigera kategori (View-lagret)
========================================================================

Denna JSP-sida gör det möjligt för administratörer att lägga till
en ny kategori eller redigera en befintlig.
Attributet "cat" tilldelas av controllern:
• cat är tomt → läge för att skapa ny kategori
• cat finns → redigeringsläge

------------------------------------------------------------------------
Kursrelation (Vilken del?)
------------------------------------------------------------------------
• Betyg 5
- Uppfyller direkt kravet “Man ska kunna lägga till och redigera varor
och varukategorier”.
- Admin kan redigera kategoridata via denna sida.
• I MVC-strukturen utgör denna fil “View”-lagret.
Controller (AdminCategoryController)
↓
Service (CategoryService)
↓
DAO (CategoryDaoJdbc)
↓
Database (tabellen categories)
↑
View (category_form.jsp)
------------------------------------------------------------------------

Tekniska detaljer
------------------------------------------------------------------------
- UTF-8-innehåll möjliggör svenska tecken.
- Formuläret skickas via POST till /admin/categories/save.
- <input type="hidden"> används i redigeringsläge för att bära befintligt ID.
- “required”-attributet hindrar att tomma kategorinamn skickas.
- “Avbryt”-länken leder tillbaka till kategorilistan.
------------------------------------------------------------------------

Möjliga framtida förbättringar
------------------------------------------------------------------------
- Lägg till fält för kategoribeskrivning eller ikon.
- Använd Bootstrap eller annat CSS-ramverk för ett mer användarvänligt gränssnitt.
- Validering och felmeddelanden (t.ex. varning vid duplicerat kategorinamn).
- AJAX-baserad sparfunktion utan sidladdning.
========================================================================
-->

<html>
<head>
    <title>Kategori</title>
</head>
<body>

<!-- Rubrik: Ny eller Redigera -->
<h2>
    <c:if test="${empty cat}">Ny</c:if>
    <c:if test="${not empty cat}">Redigera</c:if>
    kategori
</h2>

<!-- Kategoriformulär -->
<form method="post" action="${pageContext.request.contextPath}/admin/categories/save">

    <!-- Om i redigeringsläge, inkludera befintligt kategori-ID som dolt fält -->
    <c:if test="${not empty cat}">
        <input type="hidden" name="id" value="${cat.id}">
    </c:if>

    <!-- Kategorinamn -->
    <p>Namn:
        <input type="text" name="name" value="${cat.name}" required>
    </p>

    <!-- Spara och avbryt-knappar -->
    <p>
        <button type="submit">Spara</button>
        <a href="${pageContext.request.contextPath}/admin/categories">Avbryt</a>
    </p>

</form>
</body>
</html>
