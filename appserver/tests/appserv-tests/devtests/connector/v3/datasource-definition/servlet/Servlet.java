/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.ejb.ejb30.hello.session3;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;
import jakarta.ejb.EJBException;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import javax.sql.DataSource;
import java.sql.Connection;
import jakarta.servlet.*;
import jakarta.servlet.annotation.*;
import jakarta.servlet.http.*;

import jakarta.transaction.UserTransaction;


import jakarta.annotation.sql.*;

@EJB(name = "helloStateless3", beanInterface = Hello.class)
@EJBs({@EJB(name = "helloStateless4", beanName = "HelloEJB",
        beanInterface = Hello.class),
        @EJB(name = "helloStateful3", beanInterface = HelloStateful.class)})


@DataSourceDefinitions(
        value = {

                @DataSourceDefinition(name = "java:global/env/Servlet_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-servlet",
                        properties = {"connectionAttributes=;create=true"}
                ),

                @DataSourceDefinition(name = "java:comp/env/Servlet_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-servlet-comp",
                        properties = {"connectionAttributes=;create=true"}
                ),
                @DataSourceDefinition(name = "java:app/env/Servlet_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-servlet-app",
                        properties = {"connectionAttributes=;create=true"}
                ),
                @DataSourceDefinition(name = "java:module/env/Servlet_DataSource",
                        minPoolSize = 0,
                        initialPoolSize = 0,
                        className = "org.apache.derby.jdbc.EmbeddedXADataSource",
                        user = "APP",
                        password = "APP",
                        databaseName = "hello-servlet-module",
                        properties = {"connectionAttributes=;create=true"}
                )
        }
)
@WebServlet(name = "Servlet",
        urlPatterns = {"/servlet"}
)
public class Servlet extends HttpServlet {

    private
    @EJB
    Hello helloStateless;
    private
    @EJB(beanName = "HelloStatefulEJB")
    HelloStateful helloStateful;

/*
    private Hello helloStateless2;
    private HelloStateful helloStateful2;

*/
    private
    @Resource
    UserTransaction ut;

/*
    @EJB(beanName = "HelloEJB")
    private void setHelloStateless2(Hello h) {
        helloStateless2 = h;
    }

    @EJB
    private void setHelloStateful2(HelloStateful hf) {
        helloStateful2 = hf;
    }
*/

    public void init(ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In DataSource-Definition-Test::servlet... init()");
    }

    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

            boolean global = lookupDataSource("java:global/env/Servlet_DataSource", true);
            boolean comp = lookupDataSource("java:comp/env/Servlet_DataSource", true);
            boolean appServletDataSource = lookupDataSource("java:app/env/Servlet_DataSource", true);
            boolean moduleServletDataSource = lookupDataSource("java:module/env/Servlet_DataSource", true);

            boolean globalHelloSfulEJB = lookupDataSource("java:global/env/HelloStatefulEJB_DataSource", true);
            boolean compHelloSfulEJB = lookupDataSource("java:comp/env/HelloStatefulEJB_DataSource", false);
            boolean appHelloStatefulEjb = lookupDataSource("java:app/env/HelloStatefulEJB_DataSource", true);

            boolean globalHelloEJB = lookupDataSource("java:global/env/HelloEJB_DataSource", true);
            boolean compHelloEJB = lookupDataSource("java:comp/env/HelloEJB_DataSource", false);
            boolean moduleHelloEjb = lookupDataSource("java:module/env/HelloEJB_DataSource", false);

            boolean globalServlet_DD_DataSource = lookupDataSource("java:global/env/Servlet_DD_DataSource", true);
            boolean compServlet_DD_DataSource = lookupDataSource("java:comp/env/Servlet_DD_DataSource", false);

            boolean globalHelloStateful_DD_DataSource = lookupDataSource("java:global/env/HelloStatefulEJB_DD_DataSource", true);
            boolean compHelloStateful_DD_DataSource = lookupDataSource("java:comp/env/HelloStatefulEJB_DD_DataSource", false);

            boolean globalHello_DD_DataSource = lookupDataSource("java:global/env/HelloEJB_DD_DataSource", true);
            boolean compHello_DD_DataSource = lookupDataSource("java:comp/env/HelloEJB_DD_DataSource", false);


            if (global && comp && appServletDataSource && globalHelloSfulEJB && globalServlet_DD_DataSource && compServlet_DD_DataSource
                    && !compHelloSfulEJB && globalHelloEJB && !compHelloEJB && globalHelloStateful_DD_DataSource
                    && !compHelloStateful_DD_DataSource && globalHello_DD_DataSource && !compHello_DD_DataSource
                    && appHelloStatefulEjb && !moduleHelloEjb && moduleServletDataSource) {
                System.out.println("Servlet successful lookup of datasource-definitions !");
            } else {
                throw new RuntimeException("Servlet failure");
            }


            System.out.println("beginning tx");
            ut.begin();

            // invoke method on the EJB
            System.out.println("invoking stateless ejb");
            helloStateless.hello();
//            helloStateless2.hello();

            System.out.println("committing tx");
            ut.commit();
            System.out.println("committed tx");


            System.out.println("invoking stateless ejb");
            helloStateful.hello();
  //          helloStateful2.hello();

    /*        Hello helloStateless3 = (Hello)
                    ic.lookup("java:comp/env/helloStateless3");

            helloStateless3.hello();

            Hello helloStateless4 = (Hello)
                    ic.lookup("java:comp/env/helloStateless4");

            helloStateless4.hello();

            HelloStateful helloStateful3 = (HelloStateful)
                    ic.lookup("java:comp/env/helloStateful3");

            helloStateful3.hello();
*/
            System.out.println("successfully invoked ejbs");

            System.out.println("accessing connections");
            try {
                MyThread thread = new MyThread(helloStateful);
                thread.start();

                sleepFor(2);
                helloStateful.ping();
                //throw new EJBException("Did not get ConcurrentAccessException");
            } catch (jakarta.ejb.ConcurrentAccessException conEx) {
                ;   //Everything is fine
            } catch (Throwable th) {
                throw new EJBException("Got some wierd exception: " + th);
            }

            System.out.println("successfully accessed datasource definitions");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServlet :: All information I can give </FONT> </CENTER> <p> ");
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
            out.println("</BODY> </HTML> ");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("DataSource-Definition-Test servlet test failed");
            throw new ServletException(ex);
        }
    }


    public void destroy() {
        System.out.println("in DataSource-Definition-Test client::servlet destroy");
    }

    class MyThread extends Thread {
        HelloStateful ref;

        MyThread(HelloStateful ref) {
            this.ref = ref;
        }

        public void run() {
            try {
                ref.sleepFor(2);
            } catch (Throwable th) {
                throw new RuntimeException("Could not invoke waitfor() method");
            }
        }
    }


    private void sleepFor(int sec) {
        try {
            for (int i = 0; i < sec; i++) {
                Thread.currentThread().sleep(1000);
                System.out.println("[" + i + "/" + sec + "]: Sleeping....");
            }
        } catch (Exception ex) {
        }
    }

    private boolean lookupDataSource(String dataSourceName, boolean expectSuccess) {
        Connection c = null;
        try {
            System.out.println("lookup dataSource : " + dataSourceName);
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource) ic.lookup(dataSourceName);
            c = ds.getConnection();
            System.out.println("got connection : " + c);
            return true;
        } catch (Exception e) {
            if(expectSuccess){
                e.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (Exception e) {
            }
        }
    }


}
