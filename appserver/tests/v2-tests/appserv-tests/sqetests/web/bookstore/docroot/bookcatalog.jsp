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

<%@ taglib prefix="sc" tagdir="/WEB-INF/tags" %>

<jsp:useBean id="bookDB" class="database.BookDB" scope="page" >
  <jsp:setProperty name="bookDB" property="database" value="${bookDBAO}" />
</jsp:useBean>


<c:if test="${!empty param.Add}">
  <c:set var="bid" value="${param.Add}"/>
  <jsp:setProperty name="bookDB" property="bookId" value="${bid}" />
  <c:set var="addedBook" value="${bookDB.bookDetails}" />
    <p><h3><font color="red" size="+2"> 
    <fmt:message key="CartAdded1"/> <em>${addedBook.title}</em> <fmt:message key="CartAdded2"/></font></h3>
</c:if>

<c:if test="${sessionScope.cart.numberOfItems > 0}">     
  <c:url var="url" value="/bookshowcart" >
    <c:param name="Clear" value="0" />
    <c:param name="Remove" value="0" />
  </c:url>
<p><strong><a href="${url}"><fmt:message key="CartCheck"/></a>&nbsp;&nbsp;&nbsp;
    <c:url var="url" value="/bookcashier" />
    <a href="${url}"><fmt:message key="Buy"/></a></p></strong>
</c:if>

<br>&nbsp;
<br>&nbsp;
<h3><fmt:message key="Choose"/></h3>



<sc:catalog bookDB ="${bookDB}" color="#cccccc">
  <jsp:attribute name="normalPrice">
    <fmt:formatNumber value="${price}" type="currency"/>
  </jsp:attribute>
  <jsp:attribute name="onSale">
    <strike><fmt:formatNumber value="${price}" type="currency"/></strike><br/>
    <font color="red"><fmt:formatNumber value="${salePrice}" type="currency"/></font>
  </jsp:attribute>
</sc:catalog>

