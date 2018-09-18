<%--

    Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.

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

<HTML>
    <HEAD> <TITLE> JSF Basic Components Test Page </TITLE> </HEAD>

    <%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
    <%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>

    <H3> JavaServer Faces custom components test page </H3>
    <hr>

       <f:view>
        <h:form id="demotestForm" >

            <table> 
            <tr> 
              <td> <h:outputText id="testLabel" value="JavaServer Faces custom components test page..." /> </td>
            </tr>

          </TR>
           <td> <a href='<%= request.getContextPath() + "/menu.faces" %>'>Back</a> </td>
          </TR>

          </table>


        </h:form>
     </f:view>
</HTML>
