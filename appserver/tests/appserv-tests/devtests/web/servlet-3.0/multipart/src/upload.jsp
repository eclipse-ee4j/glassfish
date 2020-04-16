<%--

    Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.

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

<%@ page import="jakarta.servlet.http.Part" %>

Beginning
<%
  for (Part p: request.getParts()) {

    out.write("Part: " + p.toString() + "<br/>\n");
    out.write("Part name: " + p.getName() + "<br/>\n");
    out.write("Size: " + p.getSize() + "<br/>\n");
    out.write("Content Type: " + p.getContentType() + "<br/>\n");
    out.write("Header Names:");
    for (String name: p.getHeaderNames()) {
        out.write(" " + name);
    }
    out.write("<br/><br/>\n");
/*
    java.io.InputStreamReader in =
      new java.io.InputStreamReader(p.getInputStream());

    int c = in.read();
    while (c != -1) {
      if (c == '\n') out.write("<br/>");
      out.write(c);
      c = in.read();
    }
*/
  }
%>
End

