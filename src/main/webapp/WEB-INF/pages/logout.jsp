<%@ page contentType="text/html; charset=UTF-8" %>

<html>
<body>

<%
    session.invalidate();
    response.sendRedirect(request.getContextPath() + "/home");
%>

</body>
</html>