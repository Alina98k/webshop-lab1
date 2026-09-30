<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head>
    <title>Logga in</title>
    <link rel="stylesheet"
          href="${pageContext.request.contextPath}/css/style.css">
</head>
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
