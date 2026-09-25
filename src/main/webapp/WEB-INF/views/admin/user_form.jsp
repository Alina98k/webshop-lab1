<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>Användare</title>
</head>
<body>

<h2>
    <c:if test="${empty u}">Ny</c:if>
    <c:if test="${not empty u}">Redigera</c:if>
    användare
</h2>

<form method="post" action="${pageContext.request.contextPath}/admin/users/save">
    <c:if test="${not empty u}">
        <input type="hidden" name="id" value="${u.id}"/>
    </c:if>

    <p>Användarnamn: <input type="text" name="username" value="${u.username}" required></p>
    <p>Fullständigt namn: <input type="text" name="fullName" value="${u.fullName}" required></p>
    <p>E-post: <input type="email" name="email" value="${u.email}" required></p>

    <!-- Lösenord: obligatoriskt vid ny användare; lämnas tomt vid redigering för att behålla det gamla -->
    <p>Lösenord: <input type="password" name="password" <c:if test="${empty u}">required</c:if>></p>

    <p>Aktiv: <input type="checkbox" name="active" <c:if test="${empty u || u.active}">checked</c:if>></p>

    <p>Roller:
        <select name="roles" multiple size="4">
            <c:forEach var="r" items="${allRoles}">
                <option value="${r}" <c:if test="${not empty userRoles && userRoles.contains(r)}">selected</c:if>>
                        ${r}
                </option>
            </c:forEach>
        </select>
        <br/><small>Håll ner Ctrl (Cmd) för att välja flera.</small>
    </p>

    <p>
        <button type="submit">Spara</button>
        <a href="${pageContext.request.contextPath}/admin/users">Avbryt</a>
    </p>
</form>

</body>
</html>
