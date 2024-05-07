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

