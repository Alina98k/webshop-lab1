<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!--
============================================================================
login.jsp — Inloggningssida (View-lagret)
============================================================================

Syfte
-----
• Hämta användarnamn och lösenord från användaren och skicka till servern via POST /login.
• Visa ett felmeddelande (error attribute) vid misslyckad inloggning.

Kurskoppling
------------
• Betyg 3:
- Användaridentifiering → Login-formulär + AuthController → AuthService (BCrypt) → UserDaoJdbc.
• Betyg 4:
- Vid lyckad inloggning sätts roller (ADMIN/WAREHOUSE/CUSTOMER) i sessionen;
sidor skyddade av RoleFilter blir då tillgängliga.

MVC-sammanhang
--------------
• Controller: AuthController
- GET  /login  → visar denna JSP
- POST /login  → autentisering (AuthService.login)
- Misslyckad → request.setAttribute("error", "..."), laddar denna sida igen
- Lyckad → lägger till user/roles/isAdmin/isWarehouse i sessionen, redirect till /home
• Service: AuthService (BCrypt.checkpw + active-kontroll)
• DAO: UserDaoJdbc (findByUsername + getRoles)

Säkerhet
--------
• Lösenord verifieras på serversidan via BCrypt-hashning.
• CSRF-skydd bör läggas till för POST-formulär.
• Felmeddelandet hålls generellt (“Fel användarnamn eller lösenord”) för att undvika informationsläckage.
• I produktion rekommenderas skydd mot brute-force (t.ex. rate limiting / kontolåsning).

UX / Förbättringsidéer
----------------------
• Behåll inmatat användarnamn vid fel (value="${param.username}").
• “Kom ihåg mig”-alternativ, länk för att återställa lösenord.
• Automatisk omdirigering till önskad sida efter inloggning (redirectTo-param).
============================================================================
-->

<html>
<head><title>Logga in</title></head>
<body>
<h2>Logga in</h2>

<!-- Felmeddelande vid misslyckad inloggning -->
<c:if test="${not empty error}">
    <div style="color:red">${error}</div>
</c:if>

<!-- Inloggningsformulär: AuthController#doPost('/login') -->
<form method="post" action="${pageContext.request.contextPath}/login">
    <p>
        <label>Användarnamn:
            <input type="text" name="username" required>
            <!-- Valfritt: value="${param.username}" -->
        </label>
    </p>
    <p>
        <label>Lösenord:
            <input type="password" name="password" required>
        </label>
    </p>
    <button type="submit">Logga in</button>
</form>

<p><a href="${pageContext.request.contextPath}/home">Start</a></p>
</body>
</html>
