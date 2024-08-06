<%-- 
    Copyright (c) 2024 Contributors to the Eclipse Foundation

    This program and the accompanying materials are made available under the
    terms of the Eclipse Public License v. 2.0, which is available at
    http://www.eclipse.org/legal/epl-2.0.

    This Source Code may also be made available under the following Secondary
    Licenses when the conditions for such availability set forth in the
    Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
    version 2 with the GNU Classpath Exception, which is available at
    https://www.gnu.org/software/classpath/license.html.

    SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
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


