<%--

    Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.

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

<%@ page language="java" %>
<%@ page import="javax.naming.*" %>
<%@ page import="java.rmi.RemoteException" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%@ page import="com.sun.ts.lib.util.*" %>
<%@ page import="com.sun.ts.lib.harness.*" %>
<%@ page import="com.sun.ts.lib.porting.*" %>
<%@ page import="com.sun.javatest.Status" %>
<%@ page session="false" %>

<%! Properties properties = null;
    String[] arguments = null;
    EETest testObj = null;
	Properties stp=new Properties();
 	StringBuffer propsData = new StringBuffer();

%>

<%! private RemoteStatus runTest() throws RemoteException {
        RemoteStatus sTestStatus = new RemoteStatus(Status.passed(""));
        try
        {
            //call EETest impl's run method
            sTestStatus = new RemoteStatus(testObj.run(arguments, properties));
            if(sTestStatus.getType() == Status.PASSED)
				TestUtil.logMsg("Test running in jsp vehicle passed");
			else
				TestUtil.logMsg("Test running in jsp vehicle failed");
        }
        catch(Throwable e)
        {
            TestUtil.logErr("Test running in jsp vehicle failed", e);
            sTestStatus =
                new RemoteStatus(Status.failed("Test running in jsp vehicle failed"));
        }
	return sTestStatus;
    }

%>

<%
		try {
            //get the inputstream and read any objects passed from the
            //client, e.g. properties, args, etc.
            //wrap the Inputstream in an ObjectInputstream and read
            //the properties and args.
            TestUtil.logTrace("JSPVehicle - In doJSPGet");
            ObjectInputStream objInStream =
                new ObjectInputStream(new BufferedInputStream(request.getInputStream()));
            TestUtil.logTrace("JSPVehicle - got InputStream");
            properties = (Properties)objInStream.readObject();
            TestUtil.logTrace("JSP Vehicle -read properties!!!");
            TestUtil.logTrace("JSP Vehicle - list the props ");
            TestUtil.list(properties);
            
            //create an instance of the test client and run here
            Class c =
            Class.forName(properties.getProperty("test_classname"));
            testObj = (EETest) c.newInstance();

            arguments = (String[])objInStream.readObject();
            //arguments = new String[1];
            //arguments[0] = "";
            TestUtil.logTrace("JSPVehicle - read Objects");
            try
            {
                TestUtil.init(properties);
                TestUtil.logTrace("Remote logging set for JSP Vehicle");
                TestUtil.logTrace("JSPVehicle - Here are the props");
                //TestUtil.list(properties);
            }
            catch (Exception e)
            {
                throw new ServletException("unable to initialize remote logging");
            }
            //now run the test and return the result
            RemoteStatus finalStatus = runTest();
			// Create properties object
			stp.setProperty("type", String.valueOf(finalStatus.toStatus().getType()));
			stp.setProperty("reason", finalStatus.toStatus().getReason());
    		java.util.Enumeration key = stp.keys();
    		String name;
    		while (key.hasMoreElements())
    		{
     			name = (String)key.nextElement();
     			propsData.append(name+"="+stp.getProperty(name)+"\n");
    		}

        }
        catch(Exception e2)
        {
            System.out.println(e2.getMessage());
            TestUtil.logTrace(e2.getMessage());
            e2.printStackTrace();
            throw new ServletException("test failed to run within the Servlet Vehicle");
        }
%>

<%= propsData.toString() %>
