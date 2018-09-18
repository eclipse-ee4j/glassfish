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

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0//EN">
<html>
<head>
<META HTTP-EQUIV="pragma" CONTENT="no-cache">

<title>simple auth quicklook test</title>
</head>
<body>

<%@ page import="java.security.Principal" %>

<HR>
<BR>

<P>Will attempt to retrieve getUserPrincipal() now.

<%
        String user = null;
        Principal userp = request.getUserPrincipal();
        String method = request.getAuthType();
        if (userp != null) {
                user = userp.toString();
        }

%>

<!-- the lines within pre block are parsed by WebTest. -->
<br>
<pre>
RESULT: principal: <%= user %>
RESULT: authtype: <%= method %>
</pre>

</body>
</html>
