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

<html>
<%-- A test for tag plugins for <c:if>, <c:forEach>, and <c:choose> --%>

Testing tag plugins for &lt;c:if>, &lt;c:forEach>, and &lt;c:choose>
<br/>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<c:set var="count" value="1"/>

<c:forEach var="index" begin="1" end="3">
  <c:choose>
    <c:when test="${index==3}">
      <c:set var="count" value="${count+30}"/>
    </c:when>
    <c:when test="${index==1}">
      <c:set var="count" value="${count+10}"/>
    </c:when>
    <c:otherwise>
      <c:set var="count" value="${count+100}"/>
    </c:otherwise>
  </c:choose>
</c:forEach>

Count is ${count}, should be 141
<br/>
<c:if test="${count==141}">
  Tag Plugin Test: PASS
</c:if>
</html>
