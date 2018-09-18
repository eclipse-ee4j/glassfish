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

<jsp:useBean id="bookDB" class="database.BookDB" scope="page" >
  <jsp:setProperty name="bookDB" property="database" value="${bookDBAO}" />
</jsp:useBean>

<c:if test="${!empty param.bookId}">
  <c:set var="bid" value="${param.bookId}"/>
  <jsp:setProperty name="bookDB" property="bookId" value="${bid}" />
  <c:set var="book" value="${bookDB.bookDetails}" />
    <h2>${book.title}</h2>
    &nbsp;<fmt:message key="By"/> <em>${book.firstName}&nbsp;${book.surname}</em>&nbsp;&nbsp;
    (${book.year})<br> &nbsp; <br>
    <h4><fmt:message key="Critics"/></h4>
    <blockquote>${book.description}</blockquote>
    <c:set var="price" value="${book.price}" />		
    <c:if test="${book.onSale}" >
      <c:set var="price" value="${book.price * .85}" />	
    </c:if>
    <h4><fmt:message key="ItemPrice"/>: <fmt:formatNumber value="${price}" type="currency"/></h4>
    <c:url var="url" value="/bookcatalog" >
      <c:param name="Add" value="${bid}" />
    </c:url> 
    <p><strong><a href="${url}"><fmt:message key="CartAdd"/></a>&nbsp; &nbsp; &nbsp;
</c:if>


<c:url var="url" value="/bookcatalog" >
  <c:param name="Add" value="" />
</c:url>
<a href="${url}"><fmt:message key="ContinueShopping"/></a></p></strong>






