/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import java.io.*;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.jms.Queue;
import javax.naming.InitialContext;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import javax.sql.DataSource;

@WebServlet("/mytest")
public class TestServlet extends HttpServlet {
    private @Resource(name="myds") DataSource ds;
    private Queue queue;
    private @EJB Sless sless;

    public void init() throws ServletException {
        try {
            InitialContext ic = new InitialContext();
            queue = (Queue)ic.lookup("java:comp/env/jms/QueueName");   
        } catch(Exception ex) {
            throw new ServletException(ex);
        }
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        String queueName = null;
        try {
            int loginTimeout = ds.getLoginTimeout();
            queueName = queue.getQueueName();
        } catch(Exception ex) {
            throw new RuntimeException(ex);
        }
        res.getWriter().println(sless.hello() + ":" + queueName);
    }
}
