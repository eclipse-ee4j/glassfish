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
import java.rmi.RemoteException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import javax.rmi.PortableRemoteObject;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;
import jakarta.ejb.EJBException;
import jakarta.annotation.Resource;
import jakarta.annotation.Resources;
import javax.sql.DataSource;
import java.sql.Connection;
import jakarta.transaction.UserTransaction;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceContextType;
import jakarta.persistence.EntityManager;

@EJB(name="helloStateless3", beanInterface=Hello.class)
@EJBs({ @EJB(name="helloStateless4", beanName="HelloEJB",
             beanInterface=Hello.class),
        @EJB(name="helloStateful3",  beanInterface=HelloStateful.class) })

@Resource(name="myDataSource4", type=DataSource.class)
@Resources({ @Resource(name="myDataSource5", type=DataSource.class),
             @Resource(name="jdbc/myDataSource6", type=DataSource.class) })

public class Servlet extends HttpServlet {

    private @EJB Hello helloStateless;
    private @EJB(beanName="HelloStatefulEJB") HelloStateful helloStateful;

    private Hello helloStateless2;
    private HelloStateful helloStateful2;

    private @Resource UserTransaction ut;
    private @Resource ORB orb;

    @EJB(beanName="HelloEJB")
    private void setHelloStateless2(Hello h) {
        helloStateless2 = h;
    }

    @EJB
    private void setHelloStateful2(HelloStateful hf) {
        helloStateful2 = hf;
    }

    private @Resource(mappedName="jdbc/__default") DataSource ds1;
    private @Resource(name="myDataSource2", mappedName="foobar") DataSource ds2;
    private DataSource ds3;

    @Resource
    private void setDataSource3(DataSource ds) {
        ds3 = ds;
    }


    @PersistenceUnit
        private EntityManagerFactory emf1;

    @PersistenceUnit(name="myemf", unitName="foo")
        private EntityManagerFactory emf2;

    @PersistenceContext
        private EntityManager em1;

    @PersistenceContext(name="myem",
                        unitName="foo", type=PersistenceContextType.TRANSACTION)
        private EntityManager em2;

    public void  init( ServletConfig config) throws ServletException {

        super.init(config);
        System.out.println("In webclient::servlet... init()");
    }

    public void service ( HttpServletRequest req , HttpServletResponse resp ) throws ServletException, IOException {

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {

            InitialContext ic = new InitialContext();

            if( (emf1 != null) && (emf2 != null) && (em1 != null) &&
                (em2 != null) ) {

                EntityManagerFactory lookupemf1 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.Servlet/emf1");

                EntityManagerFactory lookupemf2 = (EntityManagerFactory)
                    ic.lookup("java:comp/env/myemf");

                EntityManager lookupem1 = (EntityManager)
                    ic.lookup("java:comp/env/com.sun.s1asdev.ejb.ejb30.hello.session3.Servlet/em1");

                EntityManager lookupem2 = (EntityManager)
                    ic.lookup("java:comp/env/myem");

                System.out.println("Servlet successful injection of EMF/EM references!");
            } else {
                throw new Exception("One or more EMF/EM references" +
                                    " was not injected in Servlet");
            }


            System.out.println("beginning tx");
            ut.begin();

            // invoke method on the EJB
            System.out.println("invoking stateless ejb");
            helloStateless.hello();
            helloStateless2.hello();

            System.out.println("committing tx");
            ut.commit();
            System.out.println("committed tx");

            System.out.println("doing orb test");
            System.out.println("ORB = " + orb);
            POA poa = (POA) orb.resolve_initial_references("RootPOA");
            System.out.println("POA = " + poa);

            System.out.println("invoking stateless ejb");
            helloStateful.hello();
            helloStateful2.hello();

            Hello helloStateless3 = (Hello)
                ic.lookup("java:comp/env/helloStateless3");

            helloStateless3.hello();

            Hello helloStateless4 = (Hello)
                ic.lookup("java:comp/env/helloStateless4");

            helloStateless4.hello();

            HelloStateful helloStateful3 = (HelloStateful)
                ic.lookup("java:comp/env/helloStateful3");

            helloStateful3.hello();

            System.out.println("successfully invoked ejbs");

            System.out.println("accessing connections");

            int loginTimeout = ds1.getLoginTimeout();
            System.out.println("ds1 login timeout = " + loginTimeout);
            loginTimeout = ds2.getLoginTimeout();
            System.out.println("ds2 login timeout = " + loginTimeout);
            loginTimeout = ds3.getLoginTimeout();
            System.out.println("ds3 login timeout = " + loginTimeout);

            DataSource ds4 = (DataSource)
                ic.lookup("java:comp/env/myDataSource4");
            loginTimeout = ds4.getLoginTimeout();
            System.out.println("ds4 login timeout = " + loginTimeout);

            DataSource ds5 = (DataSource)
                ic.lookup("java:comp/env/myDataSource5");
            loginTimeout = ds5.getLoginTimeout();
            System.out.println("ds5 login timeout = " + loginTimeout);

            DataSource ds6 = (DataSource)
                ic.lookup("java:comp/env/jdbc/myDataSource6");
            loginTimeout = ds6.getLoginTimeout();
            System.out.println("ds6 login timeout = " + loginTimeout);


            try {
                MyThread thread = new MyThread(helloStateful2);
                thread.start();

                sleepFor(10);
                helloStateful2.ping();
                throw new EJBException("Did not get ConcurrentAccessException");
            } catch (jakarta.ejb.ConcurrentAccessException conEx) {
                ;   //Everything is fine
            } catch (Throwable th) {
                throw new EJBException("Got some wierd exception: " + th);
            }

            System.out.println("successfully accessed connections");

            out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" );
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" );
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" );
            out.println("</BODY> </HTML> ");

        }catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("webclient servlet test failed");
            throw new ServletException(ex);
        }
    }



    public void  destroy() {
        System.out.println("in webclient::servlet destroy");
    }

    class MyThread extends Thread {
        HelloStateful ref;

        MyThread(HelloStateful ref) {
            this.ref = ref;
        }

        public void run() {
            try {
                ref.sleepFor(20);
            } catch (Throwable th) {
                throw new RuntimeException("Could not invoke waitfor() method");
            }
        }
    }


    private void sleepFor(int sec) {
        try {
            for (int i=0 ; i<sec; i++) {
                Thread.currentThread().sleep(1000);
                System.out.println("[" + i + "/" + sec + "]: Sleeping....");
            }
        } catch (Exception ex) {
        }
    }

}
