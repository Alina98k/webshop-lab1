<<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
========================================================================
categories.jsp  —  Admin-sida för kategorihantering (View-lagret)
========================================================================

Denna JSP-sida låter administratören visa, redigera och ta bort
befintliga kategorier. Gränssnittet använder JSTL-biblioteket
(JavaServer Pages Standard Tag Library) för att bearbeta attributet
"list" som kommer från controllern (AdminCategoryController).

------------------------------------------------------------------------
Kursrelation (Vilken del?)
------------------------------------------------------------------------
• Betyg 5
- Uppfyller direkt kravet "Man ska kunna lägga till och redigera
varor och varukategorier".
- Administratören kan utföra kategori-CRUD-operationer via denna sida.
• I MVC-sammanhang är denna fil "View"-lagret:
Controller (AdminCategoryController)
↓
Service (CategoryService)
↓
DAO (CategoryDaoJdbc)
↓
Database (tabellen categories)
↑
View (categories.jsp)
------------------------------------------------------------------------

Tekniska detaljer
------------------------------------------------------------------------
- UTF-8 teckenuppsättning möjliggör korrekta svenska tecken.
- <form method="post"> används för att skicka borttagningar som POST-förfrågningar.
- "onsubmit=confirm(...)" ber användaren bekräfta på klientsidan.
- ${pageContext.request.contextPath} används för att skapa relativa länkar
baserade på applikationens rotväg.
------------------------------------------------------------------------

Möjliga framtida förbättringar
------------------------------------------------------------------------
- Lägg till paginering (sidnumrering) och sökfilter.
- Använd Bootstrap eller ett CSS-ramverk för ett modernare utseende.
- Implementera AJAX-baserad borttagning och inline-redigering.
========================================================================
-->

<html>
<head>
    <title>Kategorier</title>
</head>
<body>
<h2>Kategorier</h2>

<!-- Länk för att lägga till en ny kategori -->
<p><a href="${pageContext.request.contextPath}/admin/categories/new">+ Ny kategori</a></p>

<!-- Kategoritabell -->
<table border="1" cellpadding="6" cellspacing="0">
    <tr>
        <th>ID</th>
        <th>Namn</th>
        <th>Åtgärder</th>
    </tr>

    <!-- JSTL-loop: använder attributet "list" som kommer från controllern -->
    <c:forEach var="x" items="${list}">
        <tr>
            <td>${x.id}</td>
            <td><c:out value="${x.name}"/></td>
            <td>
                <!-- Redigeringslänk -->
                <a href="${pageContext.request.contextPath}/admin/categories/edit?id=${x.id}">Redigera</a>
                |
                <!-- Formulär för borttagning (POST-förfrågan) -->
                <form method="post"
                      action="${pageContext.request.contextPath}/admin/categories/delete"
                      style="display:inline"
                      onsubmit="return confirm('Vill du ta bort den?')">
                    <input type="hidden" name="id" value="${x.id}">
                    <button type="submit">Ta bort</button>
                </form>
            </td>
        </tr>
    </c:forEach>
</table>

<!-- Länk tillbaka till startsidan -->
<p><a href="${pageContext.request.contextPath}/home">Startsida</a></p>
</body>
</html>
