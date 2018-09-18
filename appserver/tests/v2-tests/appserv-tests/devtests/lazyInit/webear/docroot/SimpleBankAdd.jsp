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

<%
  String message = (String)request.getAttribute("message");
%>
        <html>
          <head>
            <title>Simple Bank Application</title>
          </head>
          <body>
            <b><%=message%></b>
            <table border="1">
            <form method="post" action="/subclassing/servlet/SimpleBankServlet">
            <tr>
              <td>Social Security Number</td>
              <td><input type="text" name="SSN"></td>
            </tr>
            <tr>
              <td>Last Name</td>
              <td><input type="text" name="lastName"></td>
            </tr>
            <tr>
              <td>First Name</td>
              <td><input type="text" name="firstName"></td>
            </tr>
            <tr>
              <td>Address1</td>
              <td><input type="text" name="address1"></td>
            </tr>
            <tr>
              <td>Address2</td>
              <td><input type="text" name="address2"></td>
            </tr>
            <tr>
              <td>City</td>
              <td><input type="text" name="city"></td>
            </tr>
            <tr>
              <td>State</td>
              <td><input type="text" name="state" maxlength="2"></td>
            </tr>
            <tr>
              <td>Zip Code</td>
              <td><input type="text" name="zipCode"></td>
            </tr>
            <tr>
              <td colspan="2">
                <input type="submit" name="action" value="<%=message%>">
              </td>
            </tr>
          </form>
          </table>
          <a href="/subclassing/index.html">Return to Main Page</a>
          </body>
        </html>

                
         
