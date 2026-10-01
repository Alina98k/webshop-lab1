<%--
    JSP-sida som omdirigerar besökaren till startsidan
--%>
<%@ page contentType="text/html; charset=UTF-8" %>

<%
    // Omdirigerar besökaren till startsidan
    response.sendRedirect(request.getContextPath() + "/home");
%>