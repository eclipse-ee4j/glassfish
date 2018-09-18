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

<%@ page contentType="text/plain" %>
<%@ page import="java.util.*" %>

<%
Object attrValue = request.getAttribute("javax.servlet.include.request_uri");
if (!"/web-get-attribute-after-forward/process.jsp".equals(attrValue)) {
    throw new Exception("Missing or wrong value for request attribute " +
        "javax.servlet.include.request_uri_name. Expected: " +
        "\"/web-get-attribute-after-forward/process.jsp\", received: " +
        attrValue);
}

attrValue = request.getAttribute("javax.servlet.forward.request_uri");
if (!"/web-get-attribute-after-forward/forward.jsp".equals(attrValue)) {
    throw new Exception("Missing or wrong value for request attribute " +
        "javax.servlet.forward.request_uri_name. Expected: " +
        "\"/web-get-attribute-after-forward/forward.jsp\", received: " +
        attrValue);
}
%>




