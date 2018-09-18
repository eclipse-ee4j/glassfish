<%--

    Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.

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

<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>
<fmt:setBundle basename="LocalStrings"/>
<% String nameString = request.getParameter("name"); %> 
<% String messageString = (String) request.getAttribute("message"); %> 

<HTML> 
  <HEAD><TITLE><fmt:message key="greeter_title"/></TITLE></HEAD> 
  <BODY BGCOLOR=#FFFFFF> 
    <H2><fmt:message key="hello_world"/> !</H2> 
    <p> 
      <fmt:message key="good"/>  <%= messageString%>, <%= nameString%>.  <fmt:message key="enjoy_your"/> <%= messageString%>. 
    </p> 
  </BODY> 
</HTML> 
