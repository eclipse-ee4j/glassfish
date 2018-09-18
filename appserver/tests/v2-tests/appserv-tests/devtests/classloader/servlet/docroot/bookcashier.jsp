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

<p><fmt:message key="Amount"/>
<strong><fmt:formatNumber value="${sessionScope.cart.total}" type="currency"/></strong>
</strong>
<p><fmt:message key="Purchase"/>
<c:url var="url" value="/bookreceipt" />
<form action="${url}" method="post">
<table summary="layout">
<tr>
<td><strong><fmt:message key="Name"/></strong></td>
<td><input type="text" name="cardname" value="Gwen Canigetit" size="20"></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>
<tr>
<td><strong><fmt:message key="CCNumber"/></strong></td>
<td><input type="text" name="cardnum" value="xxxx xxxx xxxx xxxx" size="20"></td>
<td>&nbsp;</td>
<td>&nbsp;</td>
</tr>
<tr>
<td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td>
<tr>
<td><strong><fmt:message key="Shipping"/></strong></td>
<td><select name="shipping"/>
    <option value="QuickShip"><fmt:message key="QuickShip"/>
    <option value="NormalShip" selected><fmt:message key="NormalShip"/>
    <option value="SaverShip"><fmt:message key="SaverShip"/>
    </select>
<td>&nbsp;</td>
<td>&nbsp;</td></tr>
<tr>
<td><input type="submit" value="<fmt:message key="Submit"/>"></td>
<td>&nbsp;</td>
<td>&nbsp;</td> <td>&nbsp;</td>
</tr>
</table>
</form>
