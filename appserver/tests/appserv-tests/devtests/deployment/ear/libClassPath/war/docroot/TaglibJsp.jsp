<%--

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

<%@ taglib uri="/WEB-INF/tlds/i18ntaglib.tld" prefix="i18n" %>
<%@page contentType="text/html; charset=UTF-8" %>
<html><head>
<body>
<br>
<H3>This is a localized text using tag library: <br> 
<i18n:message name="LocalStrings" key="thanks" language="ja" country="JP" /> </H3>
<P><BR><A HREF="/i18n-simple">Back to sample home</A></P>
<P STYLE="margin-bottom: 0cm"><FONT SIZE=1><FONT FACE="Verdana, Arial, Helvetica, sans-serif"><FONT COLOR="#000000">Copyright
(c) 2003 Sun Microsystems, Inc. All rights reserved.</FONT></FONT></FONT>
</body>
</html>
