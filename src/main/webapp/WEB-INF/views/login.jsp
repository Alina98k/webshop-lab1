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
    String username = request.getParameter("username");
    String password = request.getParameter("password");
    if (username != null && password != null) {
        var user = new AuthService().login(username, password);

        if (user != null) {
            session.setAttribute("user", user);
            session.setAttribute("userId", user.getId());
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        request.setAttribute("error", "Fel användarnamn eller lösenord");
    }
%>
<h2>Logga in</h2>

<!-- Felmeddelande vid misslyckad inloggning -->
<c:if test="${not empty error}">
    <div style="color:red">${error}</div>
</c:if>

<form method="post">
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
