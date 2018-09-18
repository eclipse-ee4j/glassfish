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
  String SSN = (String)request.getAttribute("SSN");
  String lastName = (String)request.getAttribute("lastName");
  String firstName = (String)request.getAttribute("firstName");
  String address1 = (String)request.getAttribute("address1");
  String address2 = (String)request.getAttribute("address2");
  String city = (String)request.getAttribute("city");
  String state = (String)request.getAttribute("state");
  String zipCode = (String)request.getAttribute("zipCode");
  String currentSavingsBalance = (String)request.getAttribute("currentSavingsBalance");
  String currentCheckingBalance = (String)request.getAttribute("currentCheckingBalance");
  String message = (String)request.getAttribute("message");
%>
        <html>
          <head>
            <title>Simple Bank Application</title>
          </head>
          <body>
            <%=message%>
            <table border="1">
            <form method="post" action="/installed_libraries_embedded/servlet/SimpleBankServlet">
            <tr>
              <td>Social Security Number</td>
              <td><%=SSN%></td>
            </tr>
            <tr>
              <td>Last Name</td>
              <td><%=lastName%></td>
            </tr>
            <tr>
              <td>First Name</td>
              <td><%=firstName%></td>
            </tr>
            <tr>
              <td>Address1</td>
              <td><%=address1%></td>
            </tr>
            <tr>
              <td>Address2</td>
              <td><%=address2%></td>
            </tr>
            <tr>
              <td>City</td>
              <td><%=city%></td>
            </tr>
            <tr>
              <td>State</td>
              <td><%=state%></td>
            </tr>
            <tr>
              <td>Zip Code</td>
              <td><%=zipCode%></td>
            </tr>
            <tr>
              <td>Savings Balance</td>
              <td><%=currentSavingsBalance%></td>
            </tr>
            <tr>
              <td>Checking Balance</td>
              <td><%=currentCheckingBalance%></td>
            </tr>
            <tr>
              <td colspan="2">
                <input type="submit" name="action" value="<%=message%>">
              </td>
            </tr>
            <input type="hidden" name="SSN" value="<%=SSN%>">
          </form>
          </table>
          <a href="/installed_libraries_embedded/index.html">Return to Main Page</a>
          </body>
        </html>

                
         
