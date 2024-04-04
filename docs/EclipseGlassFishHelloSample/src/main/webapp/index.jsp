<%-- 
    Document   : index
    Created on : 3 avr. 2024, 02 h 06 min 14 s
    Author     : SOULEY Afelete Samson <samsonsouley@gmail.com>
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<!DOCTYPE html>
<html>
    <head><title>Hello</title></head>
    <body bgcolor="white">
        <img src="images/duke.waving.gif"> 

        <fmt:requestEncoding value="UTF-8"/>

        <fmt:setBundle basename="LocalStrings" var="resourceBundle" scope="page"/>

        <h2><fmt:message key="greeting_message" bundle="${resourceBundle}"/></h2>
        <form method="get">
            <input type="text" name="username" size="25">
            <p></p>
            <input type="submit" value="Submit">
            <input type="reset" value="Reset">
        </form>

        <c:if test="${not empty param['username']}">
            <%@include file="response.jsp" %>
        </c:if>

    </body>
</html>


