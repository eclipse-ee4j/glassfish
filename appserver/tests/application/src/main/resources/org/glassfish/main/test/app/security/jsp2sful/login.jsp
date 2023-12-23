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
<html>
    <head>
        <title>Login Page</title>
    </head>
    
    <h2>Welcome</h2>
    <BR> Please login
    <BR>
    <HR>
    
    <FORM ACTION="j_security_check" METHOD=POST>
        <table border=0>
            <tr>
                <td align="right">UserName:
                <td><INPUT TYPE="text" NAME="j_username" VALUE=""> <BR>
            <tr>
                <td align="right">Password:
                <td><INPUT TYPE="password" NAME="j_password" VALUE=""> <BR>
        </table>
        <BR> <INPUT TYPE="submit" value="Login"> <INPUT TYPE="reset" value="Clear">
    
    </FORM>
</html>
