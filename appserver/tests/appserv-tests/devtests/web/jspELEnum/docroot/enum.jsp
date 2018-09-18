<%--

    Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.

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

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%! enum Suit {club, diamond, heart, spade} %>
<%
   Suit h = Suit.heart;
   Suit h2 = Suit.heart;
   Suit d = Suit.diamond;

   pageContext.setAttribute("H", h);
   pageContext.setAttribute("H2", h2);
   pageContext.setAttribute("D", d);
%>

<c:if test="${H == H2}">
PASS
</c:if>
<c:if test="${H == 'heart'}">
PASS
</c:if>
${D}
<c:choose>
  <c:when test="${D == 'club'}"> club </c:when>
  <c:when test="${D == 'diamond'}"> diamond </c:when>
  <c:otherwise>FAIL</c:otherwise>
</c:choose>
