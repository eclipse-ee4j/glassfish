<%--

    Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.

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

<%@ page errorPage="/template/errorpage.jsp" %>
<html>
<head>
<title>Order Error</title>
</head>
<body  bgcolor="#FFFFFF">
<center> 
<hr>
<br>&nbsp;
<h1> 
<font size="+3" color="#CC0066">Duke's </font> 
<img src="template/duke.books.gif">
<font size="+3" color="black">Bookstore</font> 
</h1> 
<br>&nbsp;
<hr>
</center>
<h3><fmt:message key="OrderError"/></h3><br>


<c:remove var="cart" scope="session" />
<c:url var="url" value="/bookstore" />
<strong><a href="${url}"><fmt:message key="ContinueShopping"/></a>&nbsp;&nbsp;&nbsp;</strong>  

</body>
</html>
