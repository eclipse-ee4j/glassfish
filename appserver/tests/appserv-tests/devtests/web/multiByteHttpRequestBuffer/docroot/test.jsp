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

<%@ page pageEncoding="UTF-8"%>
<%@ page import="java.io.*"%>
<HTML>
<HEAD>
<META http-equiv="Content-Type" content="text/html; charset=UTF-8">
<TITLE>request#getReader test.</TITLE>
</HEAD>
<BODY>

<%!//static char JP[] = "あいうえおかきくけこさしすせそたちつてとなにぬねのはひふへほまみむめもやゆよわゐゑをん"
   static char JP[] = "\u3068\u4eba\u6587"
            .toCharArray();

    static char ASCII[] = "abcdefghijklmnopqrstuvwxyz".toCharArray();

    static String formName = "n";%>

<%
            response.setContentType("text/html; charset=UTF-8");

            int size = 8192;
            String sizeS = request.getParameter("size");
            if (sizeS != null) {
                size = Integer.parseInt(sizeS);
            }

            boolean isAscii = true;
            String ascii = request.getParameter("ascii");
            if (ascii != null) {
                isAscii = Boolean.parseBoolean(ascii);
            }

            char[] chars = isAscii ? ASCII : JP;

            StringBuffer sb = new StringBuffer(size + formName.length() + 1
                    + chars.length);
            while (sb.length() < size) {
                sb.append(chars);
            }
            if (sb.length() > size) {
                sb.delete(size, sb.length());
            }
%>

<FORM method="POST" action="<%= response.encodeURL("readLine.jsp") %>" enctype="multipart/form-data">
request#getReader()#readLine test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<FORM method="POST" action="<%= response.encodeURL("read.jsp") %>" enctype="multipart/form-data">
request#getReader()#read() test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<FORM method="POST" action="<%= response.encodeURL("readCharB.jsp") %>" enctype="multipart/form-data" >
request#getReader()#read(char[1]) test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<FORM method="POST" action="<%= response.encodeURL("readInputStream.jsp") %>" enctype="multipart/form-data" >
request#getInputStream()#read(bytes[]) test<BR>
<input type="text" name="<%= formName %>" value="<%= sb.toString() %>" />
<input type="submit" value="send" /></FORM>

<%
            session.setAttribute("expected", sb.toString());
            session.setAttribute("formName", formName);
%>
</BODY>
</HTML>
