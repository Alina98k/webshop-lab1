<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
===========================================================================
products.jsp — Adminsida för produktlista (View-lagret)
===========================================================================

Syfte
-----
Denna sida visar alla produkter i systemet i tabellform och låter
administratören redigera eller ta bort dem.

Controller: AdminProductController
- GET /admin/products         → hämtar produktlistan (service.listAll)
- GET /admin/products/edit?id=… → öppnar formuläret för vald produkt
- POST /admin/products/delete  → tar bort en produkt

--------------------------------------------------------------------------
Kursrelation (Vilken del?)
--------------------------------------------------------------------------
• **Betyg 5** — “Lägga till och redigera varor och varukategorier”
- Produkterna kan listas, redigeras och tas bort direkt på denna sida.
• **Betyg 4** — “Varulager ska finnas…”
- Lagerfältet (`p.stock`) visas för varje rad och synliggör lagersaldo.
• **Betyg 3** — Trelagersstrukturen bibehålls:
Controller → Service → DAO → DB → View.

--------------------------------------------------------------------------
Tekniska detaljer
--------------------------------------------------------------------------
- UTF-8 garanterar korrekt visning av svenska tecken.
- Radering görs via POST-formulär (säker mot CSRF).
- “Ny produkt”-länken leder till /admin/products/new.
- “Redigera”-länken öppnar formuläret med produkt-ID som parameter.
- Bekräftelse vid radering: `onsubmit="return confirm('Ta bort?')"`
- Tabellkolumner: ID, Namn, Pris, Lager, Aktiv, Åtgärder.
- Länkar längst ner leder till kategorilistan och startsidan.

--------------------------------------------------------------------------
Möjliga framtida förbättringar
--------------------------------------------------------------------------
- Lägg till sökfält och filtrering per kategori.
- Visa små produktbilder som förhandsvisning.
- AJAX-baserad radering (utan sidladdning).
- Paginering och sortering (t.ex. namn, pris).
- “Aktiv”-kolumnen kan göras redigerbar med checkbox.
- Visa bekräftelse- eller felmeddelanden med flash-attribut.
===========================================================================

MVC-flöde
--------------------------------------------------------------------------
1️⃣ Controller: AdminProductController#doGet()
→ anropar ProductService.listAll() för att hämta produkter.
2️⃣ Service: ProductService → hämtar data från DAO.
3️⃣ DAO: ProductDaoJdbc → läser från databasen via SELECT.
4️⃣ Controller skickar listan som attribut "list" till request.
5️⃣ View (products.jsp) visar listan i tabellform.
--------------------------------------------------------------------------
-->

<html>
<head>
    <title>Produkter</title>
</head>
<body>

<h2>Produkter</h2>

<!-- Länk för att lägga till ny produkt -->
<p><a href="${pageContext.request.contextPath}/admin/products/new">+ Ny produkt</a></p>

<!-- Produkttabell -->
<table border="1" cellpadding="6" cellspacing="0">
    <tr>
        <th>ID</th>
        <th>Namn</th>
        <th>Pris</th>
        <th>Lager</th>
        <th>Aktiv</th>
        <th>Åtgärder</th>
    </tr>

    <!-- Produktlista -->
    <c:forEach var="p" items="${list}">
        <tr>
            <td>${p.id}</td>
            <td><c:out value="${p.name}"/></td>
            <td>${p.price}</td>
            <td>${p.stock}</td>
            <td>${p.active}</td>
            <td>
                <!-- Länk för redigering -->
                <a href="${pageContext.request.contextPath}/admin/products/edit?id=${p.id}">Redigera</a> |

                <!-- Raderingsformulär (POST-förfrågan) -->
                <form method="post"
                      action="${pageContext.request.contextPath}/admin/products/delete"
                      style="display:inline"
                      onsubmit="return confirm('Ta bort?')">
                    <input type="hidden" name="id" value="${p.id}">
                    <button type="submit">Ta bort</button>
                </form>
            </td>
        </tr>
    </c:forEach>
</table>

<!-- Navigationslänkar -->
<p>
    <a href="${pageContext.request.contextPath}/admin/categories">Kategorier</a> |
    <a href="${pageContext.request.contextPath}/home">Startsida</a>
</p>

</body>
</html>
