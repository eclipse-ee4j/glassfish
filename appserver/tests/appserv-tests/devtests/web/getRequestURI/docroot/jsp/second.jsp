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

<%@ page import="java.util.*" %>
<%@ page import="java.net.*" %>

<html>
  <body>
    <title>Second Page </title>

    <%
      String thisURI = request.getRequestURI();

      //
      // retrieve the one set in first.
      //
      String firstURI = (String)request.getAttribute ("uri_in_first_jsp");

      request.setAttribute("uri_in_first_jsp", firstURI);
      request.setAttribute("uri_in_second_jsp", thisURI);

      out.println ("<br/> request.getRequestURI() of first.jsp: " + firstURI);
      out.println ("<br/> request.getRequestURI() in second.jsp: " + thisURI);
    %>


  </body>
</html>

