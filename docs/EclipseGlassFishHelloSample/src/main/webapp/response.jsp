<%-- 
    Document   : response
    Created on : 3 avr. 2024, 02 h 14 min 47 s
    Author     : samito
--%>

<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>

<%@ page import="jakarta.servlet.http.*" %>

<%
    String user = (String)request.getParameter("username");
    HttpSession httpSession = request.getSession();
    String users = (String)httpSession.getAttribute("users");
    if ( users == null ) {
        users = user;
    }
    else {
        users = users + ", " + user;
    }
    httpSession.setAttribute("users", users);
%>


<h2><font color="black"><fmt:message key="greeting_response" bundle="${resourceBundle}"/>, <%= users %>!</font></h2>

