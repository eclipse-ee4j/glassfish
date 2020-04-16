/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.acme;

import javax.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import javax.naming.*;
import javax.annotation.Resource;

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    @Resource    
    private ManagedBeanExtra mbExtra;


    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
	System.out.println("In HelloServlet::init");
	mbExtra.hello();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");

	int numIter = 2;

	try {

	    doTest(numIter, "java:module/ManagedBeanNoInt", 0, "");
	    doTest(numIter, "java:module/ManagedBean1Int", numIter, "A");
	    doTest(numIter, "java:module/ManagedBean2Int", numIter*2, "AB");
	    doTest(numIter, "java:module/ManagedBean2IntPlusBean", numIter*2, "ABM");
	    doTest(numIter, "java:module/ManagedBeanNoIntPlusBean", 0, "M");

	    doTest(numIter, "java:module/ManagedBean2IntExcludeClass", numIter*2, "");

	    doTest(numIter, "java:module/ManagedBean1Class1MethodLevelInt", numIter*1, "BA");

	    doTest(numIter, "java:module/ManagedBean1MethodLevelIntExcludeClass",
		   numIter*1, "A");

	    doTest(numIter, "java:module/ManagedBean2MethodLevelInt", 0, "AB");
	    

	} catch(Exception e) {
	    throw new RuntimeException(e);
	}
	    


	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }

    private void doTest(int numIter, String lookup, int numInterceptors,
			String aroundInvoke) throws Exception {

	System.out.println("Test " + lookup + " Expect numInstances = " + numIter + 
			   " , numInterceptors = " + numInterceptors + 
			   " , aroundInvoke = " + aroundInvoke);
	for(int i = 0; i < numIter; i++) {
	    ManagedBeanSuper mb = (ManagedBeanSuper) new InitialContext().lookup(lookup);

	    try {
		mb.throwAppException();
		throw new RuntimeException("Expected AppException , got nothing");
	    } catch(AppException ae) {
		System.out.println("Successfully caught AppException");
	    } catch(Throwable t) {
		throw new RuntimeException("Expected AppException , got " + t, t);
	    }

	    try {
		mb.throwIllegalArgumentException();
		throw new RuntimeException("Expected IllegalArgumentException , got nothing");
	    } catch(IllegalArgumentException iae) {
		System.out.println("Successfully caught IllegalArgumentException");
	    } catch(Throwable t) {
		throw new RuntimeException("Expected IllegalArgumentException , got " + t, t);
	    }

	    if( i ==  (numIter - 1) ) {
		int actualNumInst =  mb.getNumInstances();
		int actualNumInter =  mb.getNumInterceptorInstances();
		String actualAround = mb.getAroundInvokeSequence();
		System.out.println("actual num Instances = " + actualNumInst);
		System.out.println("actual num Interceptors = " + actualNumInter);
		System.out.println("actual around invoke sequence = " + actualAround);

		if( ( actualNumInst != numIter ) || (actualNumInter != numInterceptors) ||
		    !actualAround.equals(aroundInvoke) ) {
		    throw new RuntimeException("actual results failure for " + lookup);
		} 
	    }

	}
    }


}
