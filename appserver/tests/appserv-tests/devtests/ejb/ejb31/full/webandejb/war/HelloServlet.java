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

import jakarta.ejb.EJB;
import jakarta.ejb.ConcurrentAccessException;
import jakarta.ejb.ConcurrentAccessTimeoutException;
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

@EJB(name="helloStateful", beanInterface=HelloStateful.class)
@WebServlet(urlPatterns="/HelloServlet", loadOnStartup=1)
public class HelloServlet extends HttpServlet {

    // Environment entries
    private String foo = null;

    @EJB HelloSingleton singleton;
    @EJB Hello hello;
    @EJB HelloRemote helloRemote;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("In HelloServlet::init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");

        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        try {
            HelloStateful sful = (HelloStateful) new InitialContext().lookup("java:comp/env/helloStateful");
            sful.hello();
            hello.foo();
        } catch(Exception e) {
            e.printStackTrace();
        }

        System.out.println("Remote intf bean says " +
                           helloRemote.hello());

        System.out.println("Calling testNoWait. This one should work since it's not a concurrent invocation");
        singleton.testNoWait();

        System.out.println("Call async wait, then sleep a bit to make sure it takes affect");
        singleton.asyncWait(1);
        try {
            // Sleep a bit to make sure async call processes before we proceed
            Thread.sleep(100);
        } catch(Exception e) {
            System.out.println(e);
        }

        try {
            System.out.println("Calling testNoWait");
            singleton.testNoWait();
            throw new RuntimeException("Expected ConcurrentAccessException");
        } catch(ConcurrentAccessTimeoutException cate) {
            throw new RuntimeException("Expected ConcurrentAccessException");
        } catch(ConcurrentAccessException cae) {
            System.out.println("Got expected exception for concurrent access on method with 0 wait");
        }

        singleton.wait(10);

        singleton.reentrantReadWrite();

        singleton.callSing2WithTxAndRollback();
        singleton.hello();

        singleton.read();
        singleton.write();
        singleton.reentrantReadRead();
        singleton.reentrantWriteWrite();
        singleton.reentrantWriteRead();

               out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
            out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> " );
            out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>" );
            out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>" );
            out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>" );
            out.println("</BODY> </HTML> ");

    }


}
