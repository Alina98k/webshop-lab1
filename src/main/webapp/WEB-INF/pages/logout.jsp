<%--
    JSP-sida som loggar ut användaren och omdirigerar till startsidan
--%>
<%@ page contentType="text/html; charset=UTF-8" %>

<html>
<body>

<%
    // Avslutar sessionen och tar bort dess sparade uppgifter
    session.invalidate();

    // Omdirigerar användaren till startsidan
    response.sendRedirect(request.getContextPath() + "/home");
%>

</body>
</html>