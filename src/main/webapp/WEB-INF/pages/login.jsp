<%--
    JSP-sida som hanterar inloggning och visar inloggningsformuläret
--%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page import="com.example.webshop.service.AuthService" %>

<html>
<head>
    <title>Logga in</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>

<body>
<%
    // Hämtar användarnamn och lösenord från anropet
    String username = request.getParameter("username");
    String password = request.getParameter("password");

    // Kontrollerar inloggningsuppgifterna när båda värdena finns
    if (username != null && password != null) {
        var user = new AuthService().login(username, password);

        // Sparar användaren och användarens id i sessionen vid lyckad inloggning
        if (user != null) {
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());

            // Omdirigerar användaren till startsidan och avslutar sidans körning
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        // Gör ett felmeddelande tillgängligt för visning vid misslyckad inloggning
        request.setAttribute("error", "Fel användarnamn eller lösenord");
    }
%>

<h2>Logga in</h2>

<%-- Visar ett felmeddelande om inloggningen misslyckades --%>
<c:if test="${not empty error}">
    <div style="color:red">${error}</div>
</c:if>

<%-- Skickar användarnamn och lösenord till samma sida via POST --%>
<form method="post">
    <p>
        <label>Användarnamn:
            <input type="text" name="username" required>
        </label>
    </p>
    <p>
        <label>Lösenord:
            <input type="password" name="password" required>
        </label>
    </p>
    <button type="submit">Logga in</button>
</form>

<%-- Visar en länk till startsidan --%>
<p>
    <a href="${pageContext.request.contextPath}/home">Start</a>
</p>

</body>
</html>