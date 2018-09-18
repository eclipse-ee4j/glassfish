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

<%-- $Id: chart.jsp,v 1.2 2004/11/14 07:33:19 tcfujii Exp $ --%>



<%@ taglib uri="http://java.sun.com/jsf/core"  prefix="f" %>
<%@ taglib uri="http://java.sun.com/jsf/html"  prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/demo/components" prefix="d" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<f:view>
<html>
<head>
  <title>Chart Example</title>
</head>
<body bgcolor="white">

<h:form>
       
     <hr>
     <table> 
      <tr>
         <th align="left"><h:outputText value="Vertical Bar Chart with data specifed via JSP" /></th>
      </tr> 
      <tr> 
      <td>
      <d:chart width="300" height="300" title="Employee Number By Department" xlabel="Departments" ylabel="Employees">
         <d:chartItem itemLabel="Eng" itemValue="200" itemColor="red" />
         <d:chartItem itemLabel="Mktg" itemValue="400" itemColor="green" />
         <d:chartItem itemLabel="Sales" itemValue="250" itemColor="blue" />
         <d:chartItem itemLabel="R&D" itemValue="350" itemColor="orange" />
         <d:chartItem itemLabel="HR" itemValue="450" itemColor="cyan" />
      </d:chart> 
      </td>
      <td>
      <table>
      <tr>
         <th align="left"><h:outputText value="Horizontal Bar Chart with data specifed via JSP" /></th>
      </tr>
      <tr>
      <td>
      <d:chart width="300" height="300" type="bar" orientation="horizontal" 
         title="Employee Number By Department" xlabel="Employees" ylabel="Departments">
         <d:chartItem itemLabel="Eng" itemValue="200" itemColor="red" />
         <d:chartItem itemLabel="Mktg" itemValue="400" itemColor="green" />
         <d:chartItem itemLabel="Sales" itemValue="250" itemColor="blue" />
         <d:chartItem itemLabel="R&D" itemValue="350" itemColor="orange" />
         <d:chartItem itemLabel="HR" itemValue="450" itemColor="cyan" />
      </d:chart>
      </td>
      </tr>
      <tr>
         <th align="left"><h:outputText value="Pie Chart with data specifed via JSP" /></th>

      </tr>
      <tr>
     <td>
     <d:chart width="400" height="200" type="pie" 
         title="Employee Number By Department">
         <d:chartItem itemLabel="Eng" itemValue="200" itemColor="red" />
         <d:chartItem itemLabel="Mktg" itemValue="400" itemColor="green" />
         <d:chartItem itemLabel="Sales" itemValue="600" itemColor="blue" />
         <d:chartItem itemLabel="R&D" itemValue="700" itemColor="orange" />
         <d:chartItem itemLabel="HR" itemValue="800" itemColor="cyan" />
     </d:chart> </td>
     </tr>
      </table>
      <td>
      </tr> 
    </table>

</h:form>
</f:view>

  <hr>
  <a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.

<h1>How to Use this Component</h1>

<p>This component generates different types of charts like Bar and Pie.
</p>

<h2>JSP Attributes</h2>

<table border="1">

<tr>
<th>JSP Attribute Name
</th>
<th>What it Does
</th>
</tr>

<tr>

<td><code>width</code>
</td>
<td>
A value binding expression or a literal value corresponding to the width of the chart.
</td>
</tr>

<tr>
<td><code>height</code>
</td>
<td>A value binding expression or a literal value corresponding to the height of the chart.
</td>
</tr>

<tr>
<td><code>orientation</code></td>
<td>A value binding expression or a literal value corresponding to the orientation of the chart.
This attribute applies to bar charts only. Valid values are "horizontal" and "vertical".
Default orientation is "vertical".
</td>
</tr>

<tr>
<td><code>title</code></td>
<td>A value binding expression or a literal value corresponding to the title of the chart.
</td>
</tr>

<tr>
<td><code>xlabel</code></td>
<td>A value binding expression or a literal value that represents the label for x-axis.
</td>
</tr>

<tr>
<td><code>ylabel</code></td>
<td>A value binding expression or a literal value that represents the label for y-axis.
</td>
</tr>

<tr>
<td><code>type</code></td>
<td>A value binding expression or a literal value that specifies what type of chart to generate.
Supported types are "bar" and "pie". Default type is "bar".
</td>
</tr>

</table>

<h2>How to specify data for chart</h2>

<p>Chart can be described using an array of <code>ChartItem</code> specified in the model or it can be described using <code>JSP</code> tags.</p>

<h3>Described From JSP</h3>

<p>The <code>chart</code> tag consists of multiple <code>chartItem</code> tags.  
Each <code>chartItem</code> tag corresponds to a single column of the chart. 
The <code>chartItem</code> tag supports attributes like itemLabel, itemColor and itemValue
which can be literal value or a valueBinding expression that points to a ChartItem.</p>

<h3>Described From Model</h3>

<p>The <code>value</code> attribute specified on a chart tag refers to an array of <code>ChartItem</code>. 
Each element of the array represents a single column of the chart.</p>

<hr>
<a href='<%= request.getContextPath() + "/index.jsp" %>'>Back</a> to home page.
</body>
</html>

