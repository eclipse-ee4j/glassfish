/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.inject.Inject;
import javax.jms.Queue;
import javax.jms.Session;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.TestBeanInterface;
import test.beans.artifacts.Preferred;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class JMSResourceInjectionInLibraryBeanArchiveTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBeanInterface tb;
    
    @Inject
    private Queue queue;

    @Inject
    private Session session;
    
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        
        if (queue == null)
            msg += "typesafe Injection of queue into a servlet failed";

        if (session == null)
            msg += "typesafe Injection of Session into a servlet failed";
        
        if (tb == null)
            msg += "Injection of request scoped bean failed";
        
        if (tb.testDatasourceInjection().trim().length() != 0)
            msg += tb.testDatasourceInjection();

        writer.write(msg + "\n");
    }

}
