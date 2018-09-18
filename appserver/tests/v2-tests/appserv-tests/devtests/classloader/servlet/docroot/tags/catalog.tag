<%--

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

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



<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<%@ attribute name="bookDB" required="true" type="database.BookDB" %>
<%@ attribute name="color" required="true" %>

<%@ attribute name="normalPrice" fragment="true" %>
<%@ attribute name="onSale" fragment="true" %>
<%@ variable name-given="price" %>
<%@ variable name-given="salePrice" %>

<center>
<table summary="layout">
<c:forEach var="book" begin="0" items="${bookDB.books}">
  <tr>
  <c:set var="bookId" value="${book.bookId}" />
  <td bgcolor="${color}"> 
      <c:url var="url" value="/bookdetails" >
        <c:param name="bookId" value="${bookId}" />
      </c:url>
      <a href="${url}"><strong>${book.title}&nbsp;</strong></a></td> 
  <td bgcolor="${color}" rowspan=2>
    

  <c:set var="salePrice" value="${book.price * .85}" />
  <c:set var="price" value="${book.price}" />

    <c:choose>
      <c:when test="${book.onSale}" >
        <jsp:invoke fragment="onSale" />
      </c:when>
      <c:otherwise>
        <jsp:invoke fragment="normalPrice" />
      </c:otherwise>
    </c:choose>
    
  &nbsp;</td> 

  <td bgcolor="${color}" rowspan=2> 
  <c:url var="url" value="/bookcatalog" >
    <c:param name="Add" value="${bookId}" />
  </c:url> 
  <p><strong><a href="${url}">&nbsp;<fmt:message key="CartAdd"/>&nbsp;</a></td></tr> 

  <tr> 
  <td bgcolor="#ffffff"> 
  &nbsp;&nbsp;<fmt:message key="By"/> <em>${book.firstName}&nbsp;${book.surname}</em></td></tr>
</c:forEach>

</table>
</center>

