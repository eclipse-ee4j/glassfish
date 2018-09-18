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

<form action="${pageContext.request.contextPath}/orderForm" method=post>
<center>

<table cellpadding=4 cellspacing=2 border=0>

<tr>
<td colspan=4><fmt:message key="OrderInstructions"/></td>
</tr>

<tr>
<td colspan=4>
&nbsp;</td>
</tr>

<tr bgcolor="#CC9999">
<td align="center" colspan=4><font size=5><b><fmt:message key="OrderForm"/><b></font></td>
</tr>

<tr bgcolor="#CC9999">
<td align=center><B><fmt:message key="Coffee"/></B></td>
<td align=center><B><fmt:message key="Price"/></B></td>
<td align=center><B><fmt:message key="Quantity"/></B></td>
<td align=center><B><fmt:message key="Total"/></B></td>
</tr>

<c:forEach var="sci" items="${sessionScope.cart.items}" >
<tr bgcolor="#CC9999">
<td>${sci.item.coffeeName}</td>
<td align=right>\$${sci.item.retailPricePerPound}</td>
<td align=center><input type="text" name="${sci.item.coffeeName}_pounds" value="${sci.pounds}" size="3"  maxlength="3"></td> 
<td align=right>\$${sci.price}</td>
</tr>
</c:forEach>

<tr>
<td>&nbsp;</td>
<td> 
<a href="${pageContext.request.contextPath}/checkoutForm?firstName=Coffee&lastName=Lover&email=jane@home&areaCode=123&phoneNumber=456-7890&street=99&city=Somewhere&state=CA&zip=95050&CCNumber=1234-2345-5678&CCOption=0"><fmt:message key='Checkout'/></a>
</td>
<td><input type="submit" value="<fmt:message key='Update'/>"></td>
<td align=right>\$${sessionScope.cart.total}</td>
<td>&nbsp;</td>
</tr>

<tr>
<td colspan=5>${requestScope.orderError}</td>
</tr>


</table>
</center>
</form>




