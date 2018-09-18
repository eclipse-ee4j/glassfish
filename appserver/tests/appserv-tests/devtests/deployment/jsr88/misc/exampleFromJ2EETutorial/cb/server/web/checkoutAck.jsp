<%--

    Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.

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

<c:remove var="cart" scope="session"/>
<center>
<table cellpadding=4 cellspacing=2 border=0>
<tr>
<td align=center colspan=3><fmt:message key="OrderConfirmed"/></td>
</tr>
<tr>
<td colspan=3>&nbsp;</td>
</tr>
<tr bgcolor="#CC9999">
<td rowspan=2 align=center><b><fmt:message key="ShipDate"/></b>
<td colspan=2 align=center><b><fmt:message key="Items"/></b>
</tr>
<tr bgcolor="#CC9999">
<td><b><fmt:message key="Coffee"/></b></td>
<td><b><fmt:message key="Pounds"/></b></td>
</tr>
<c:forEach var="oc" items="${requestScope.checkoutFormBean.orderConfirmations.items}" >
  <tr bgcolor="#CC9999">
  <td rowspan=${fn:length(oc.orderBean.lineItems)} align=center><fmt:formatDate value="${oc.confirmationBean.shippingDate.time}" type="date" dateStyle="full" /></td>
  <c:forEach var="item" items="${oc.orderBean.lineItems}" >
    <td bgcolor="#CC9999">${item.coffeeName}</td>
    <td bgcolor="#CC9999" align=right>${item.pounds}</td>
  </tr>
  </c:forEach>
</c:forEach>
<tr>
<td colspan=3>&nbsp;</td>
</tr>
<tr>
<td align=center colspan=3><a href="${pageContext.request.contextPath}/orderForm"><fmt:message key="ContinueShopping"/></a>
<tr>
<td colspan=3>&nbsp;</td>
</tr>
</table>
</center>
