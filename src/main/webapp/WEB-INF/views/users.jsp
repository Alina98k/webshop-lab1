<%--
/**
 * Admin användarlista (users.jsp)
 *
 * <h2>Beskrivning</h2>
 * Den här sidan visar alla användare i systemet via adminpanelen.
 * Data kommer från {@link com.example.webshop.controller.AdminUserController}
 * och skickas som attributet `list`.
 *
 * <h2>Visningsdata (Request Attributes)</h2>
 * <ul>
 *   <li><code>list</code> – en {@code List&lt;User&gt;} med alla användare</li>
 *   <li>Varje {@code User}-objekt innehåller följande fält:
 *     <ul>
 *       <li><code>id</code> – användarens ID</li>
 *       <li><code>username</code> – användarnamn</li>
 *       <li><code>fullName</code> – fullständigt namn</li>
 *       <li><code>email</code> – e-postadress</li>
 *       <li><code>active</code> – aktivstatus</li>
 *       <li><code>roles</code> – användarens roller (t.ex. "ADMIN", "WAREHOUSE")</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h2>Funktioner</h2>
 * <ul>
 *   <li>"Ny användare" → <code>/admin/users/new</code></li>
 *   <li>"Redigera" → <code>/admin/users/edit?id={id}</code></li>
 *   <li>"Ta bort" → <code>POST /admin/users/delete</code></li>
 * </ul>
 *
 * <h2>Relaterade klasser</h2>
 * <ul>
 *   <li>{@link com.example.webshop.controller.AdminUserController}</li>
 *   <li>{@link com.example.webshop.service.UserService}</li>
 *   <li>{@link com.example.webshop.model.User}</li>
 * </ul>
 *
 * @author Your Name
 * @since 1.0
 */
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<html>
<head><title>Användare</title></head>
<body>

<h2>Användare</h2>

<p><a href="${pageContext.request.contextPath}/admin/users/new">+ Ny användare</a></p>

<table border="1" cellpadding="6" cellspacing="0">
    <tr>
        <th>ID</th>
        <th>Användarnamn</th>
        <th>Fullständigt namn</th>
        <th>E-post</th>
        <th>Aktiv</th>
        <th>Roller</th>
        <th>Åtgärder</th>
    </tr>

    <c:forEach var="u" items="${list}">
        <tr>
            <td>${u.id}</td>
            <td><c:out value="${u.username}"/></td>
            <td><c:out value="${u.fullName}"/></td>
            <td><c:out value="${u.email}"/></td>
            <td>${u.active}</td>
            <td>
                <c:forEach var="r" items="${u.roles}">
                    <span>[<c:out value="${r}"/>]</span>
                </c:forEach>
            </td>
            <td>
                <a href="${pageContext.request.contextPath}/admin/users/edit?id=${u.id}">Redigera</a> |
                <form method="post" action="${pageContext.request.contextPath}/admin/users/delete"
                      style="display:inline" onsubmit="return confirm('Vill du ta bort den här användaren?')">
                    <input type="hidden" name="id" value="${u.id}"/>
                    <button type="submit">Ta bort</button>
                </form>
            </td>
        </tr>
    </c:forEach>
</table>

<p>
    <a href="${pageContext.request.contextPath}/admin/products">Produkter</a> |
    <a href="${pageContext.request.contextPath}/admin/categories">Kategorier</a> |
    <a href="${pageContext.request.contextPath}/home">Startsida</a>
</p>

</body>
</html>
