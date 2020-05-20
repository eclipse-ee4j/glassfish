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

<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<tt:definition name="coffeebreak" screen="${requestScope['jakarta.servlet.forward.servlet_path']}">
  <tt:screen screenId="/orderForm">
    <tt:parameter name="title" value="Coffee Break" direct="true"/>
    <tt:parameter name="banner" value="/template/banner.jsp" direct="false"/>
    <tt:parameter name="body" value="/orderForm.jsp" direct="false"/>
  </tt:screen>
  <tt:screen screenId="/checkoutForm">
    <tt:parameter name="title" direct="true">
      <jsp:attribute name="value" >
        <fmt:message key="TitleCheckoutForm"/>
      </jsp:attribute>
    </tt:parameter>
    <tt:parameter name="banner" value="/template/banner.jsp" direct="false"/>
    <tt:parameter name="body" value="/checkoutForm.jsp" direct="false"/>
  </tt:screen>
  <tt:screen screenId="/checkoutAck">
    <tt:parameter name="title" direct="true">
      <jsp:attribute name="value" >
        <fmt:message key="TitleCheckoutAck"/>
      </jsp:attribute>
    </tt:parameter>
    <tt:parameter name="banner" value="/template/banner.jsp" direct="false"/>
    <tt:parameter name="body" value="/checkoutAck.jsp" direct="false"/>
  </tt:screen>
</tt:definition>
