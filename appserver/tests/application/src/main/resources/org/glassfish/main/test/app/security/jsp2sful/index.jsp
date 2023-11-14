<%--

    Copyright (c) 2023 Contributors to the Eclipse Foundation.
    Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.

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

<%@ page language="java"%>
<%@ page contentType="text/html" %>
<%@ page import="javax.naming.*" %>
<%@ page import="java.rmi.*" %>
<%@ page import="javax.rmi.*" %>
<%@ page import="org.glassfish.main.test.app.security.jsp2sful.*" %>

<html>
<head><title>JSP Page Access Profile</title></head>
<body>
<%
    out.println("The web user principal = "+request.getUserPrincipal().getName() );
    out.println();
%>
<H3> Calling the ProfileInfoBean </H3>
<%
  try {
      InitialContext ic = new InitialContext();
      java.lang.Object obj = ic.lookup("jsp2sful");
      out.println("Looked up home!!");
      ProfileInfoHome home = (ProfileInfoHome) PortableRemoteObject.narrow(obj, ProfileInfoHome.class);
      out.println("Narrowed home!!");
      ProfileInfoRemote hr = home.create("a name");
      out.println("Got the EJB!!");
      out.println("<li>User profile: ");
      try {
          out.println(hr.getCallerInfo());
      } catch (AccessException ex) {
          out.println("CANNOT ACCESS getCallerInfo()");
      }
      out.println("<li>Secret info: ");
      try {
          out.println(hr.getSecretInfo());
      } catch (AccessException ex) {
          out.println("CANNOT ACCESS getSecretInfo()");
      }
  } catch (java.rmi.RemoteException e) {
      e.printStackTrace();
      out.println(e.toString());
  }
%>
</body>
</html>
