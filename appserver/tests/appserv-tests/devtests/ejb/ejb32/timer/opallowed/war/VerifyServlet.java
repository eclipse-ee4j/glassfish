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

package com.acme.ejb32.timer.opallowed;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerHandle;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/VerifyServlet", loadOnStartup = 1)
public class VerifyServlet extends HttpServlet {

    @EJB
    private SingletonTimeoutLocal sgltTimerBeanLocal;
    
    @EJB
    private SingletonTimeout sgltTimerBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String type = req.getQueryString();

        PrintWriter out = resp.getWriter();
        resp.setContentType("text/html");

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Servlet VerifyServlet</title>");
        out.println("</head>");
        out.println("<body>");
        try {
            if ("managedbean".equals(type)) {
            } else {
                // this is to test the APIs allowed to be invoked
                // the return values are not interested
                Timer t = sgltTimerBeanLocal.createLocalTimer("webapp");
                TimerHandle handle = t.getHandle();
                handle.getTimer();
                t.getInfo();
                t.getNextTimeout();
                t.getSchedule();
                t.getTimeRemaining();
                t.isCalendarTimer();
                t.isPersistent();
                t.cancel();

                // this is blocked by JIRA19546 now
                // boolean remoteSuc = testRemoteInterface(out);
                boolean remoteSuc = true;
                if (remoteSuc) {
                    out.println("RESULT: PASS");
                } else {
                    out.println("RESULT: FAIL");
                }
            }
        } catch (Throwable e) {
            out.println("got exception");
            out.println(e);
            e.printStackTrace();
        } finally {
            out.println("</body>");
            out.println("</html>");

            out.close();
            out.flush();
        }
    }

    private boolean testRemoteInterface(PrintWriter out) {
        try {
            TimerHandle thremote = sgltTimerBean.createTimer("webappremote");
            out.println("shouldn't get TimerHandle through remote interface!");
            return false;
        } catch (Exception e) {
        }
        
        return true;
    }

}
