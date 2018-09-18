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

<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html"%>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core"%>

<f:view>
 <html>
  <head>
    <title>JSF Hello</title>
  </head>
  <body>
    <h:form id="form">
      <h3>
        Hello, and welcome<br>
        <h:outputText value="If injection worked, this sentence should be followed by the injected words -> #{bean.entry} "/><br>
        <h:outputText value="If second injection worked, this sentence should be followed by 'a non-negative number' === #{bean.number} ==="/><br>
        <h:outputText value="If @Postconstruct worked, this sentence should be followed by #{bean.init} "/>
      </h3>
    </h:form>
  </body>
 </html>
</f:view>

