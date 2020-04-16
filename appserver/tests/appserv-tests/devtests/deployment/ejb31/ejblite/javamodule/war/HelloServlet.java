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
import javax.annotation.Resource;
import javax.naming.*;

@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
@EJB(name="java:module/ES1", beanName="SingletonBean", beanInterface=SingletonBean.class)
public class HelloServlet extends HttpServlet {

    @EJB(name="java:module/env/ES2")
    private SingletonBean simpleSingleton;

    @EJB(name="java:app/EL1")
    private StatelessBean simpleStateless;

    @EJB(name="java:app/env/EL2")
    private StatelessBean simpleStateless2;

    private SingletonBean sb2;
    private SingletonBean sb3;
    private SingletonBean sb4;
    private SingletonBean sb5;
    private StatelessBean slsb;
    private StatelessBean slsb2;
    private StatelessBean slsb3;
    private StatelessBean slsb4;
    private StatelessBean slsb5;

    @Resource
    private FooManagedBean foo;

    @Resource(name="foo2ref", mappedName="java:module/foomanagedbean")
    private FooManagedBean foo2;

    @Resource(mappedName="java:app/ejb-ejb31-ejblite-javamodule-web/foomanagedbean")
    private FooManagedBean foo3;

    private FooManagedBean foo4;
    private FooManagedBean foo5;
    private FooManagedBean foo6;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

	System.out.println("In HelloServlet::init");

	try {
	    InitialContext ic = new InitialContext();
	    sb2 = (SingletonBean) ic.lookup("java:module/SingletonBean");
	    sb3 = (SingletonBean) ic.lookup("java:module/SingletonBean!com.acme.SingletonBean");

	    sb4 = (SingletonBean) ic.lookup("java:module/ES1");
	    sb5 = (SingletonBean) ic.lookup("java:module/env/ES2");

	    slsb = (StatelessBean) ic.lookup("java:module/StatelessBean");
	    slsb2 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean");
	    slsb3 = (StatelessBean) ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/StatelessBean!com.acme.StatelessBean");

	    slsb4 = (StatelessBean) ic.lookup("java:app/EL1");
	    slsb5 = (StatelessBean) ic.lookup("java:app/env/EL2");

	    foo4 = (FooManagedBean) 
		ic.lookup("java:module/foomanagedbean");

	    foo5 = (FooManagedBean) 
		ic.lookup("java:app/ejb-ejb31-ejblite-javamodule-web/foomanagedbean");

	    foo6 = (FooManagedBean)
		ic.lookup("java:comp/env/foo2ref");

	    System.out.println("My AppName = " + 
			       ic.lookup("java:app/AppName"));

	    System.out.println("My ModuleName = " + 
			       ic.lookup("java:module/ModuleName"));

	} catch(Exception e) {
	    e.printStackTrace();
	    throw new ServletException(e);
	}
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {

	resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

	System.out.println("In HelloServlet::doGet");

	foo.foobar("foobar");

	simpleSingleton.hello();

	simpleStateless.hello();
	simpleStateless2.hello();

	sb2.hello();

	sb3.hello();

	sb4.hello();

	sb5.hello();

	slsb.hello();

	slsb2.hello();

	slsb3.hello();

	slsb4.hello();

	slsb5.hello();

	foo.foo();
	foo.foobar("foobar");
	foo2.foo();
	foo3.foo();
	foo4.foo();
	foo5.foo();	
	foo6.foo();

	out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" ); 
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" ); 
            out.println("</BODY> </HTML> ");

    }


}
