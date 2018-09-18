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

<p><b><fmt:message key="What"/></b></p>

<jsp:useBean id="bookDB" class="database.BookDB" scope="page" >
  <jsp:setProperty name="bookDB" property="database" value="${bookDBAO}" />
</jsp:useBean>

<jsp:setProperty name="bookDB" property="bookId" value="203" />

<p>
<c:url var="url" value="/bookdetails" />
<blockquote><p><em><a href="${url}?bookId=203">${bookDB.bookDetails.title}</a></em>,
<c:url var="url" value="/bookcatalog" />
<fmt:message key="Talk"/></blockquote>
<p><b><a href="${url}?Add="><fmt:message key="Start"/></a></b>


