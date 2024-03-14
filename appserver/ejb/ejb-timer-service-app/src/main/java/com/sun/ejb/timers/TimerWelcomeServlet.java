/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ejb.timers;

import java.io.*;
import java.util.Set;

import jakarta.servlet.*;
import jakarta.ejb.*;
import jakarta.servlet.http.*;

import org.glassfish.ejb.persistent.timer.TimerLocal;
import com.sun.ejb.containers.EJBTimerService;

/**
 *
 * @author mvatkina
 */

public class TimerWelcomeServlet extends HttpServlet {

    @EJB
    private transient TimerLocal timer;

    /**
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Timer Application</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h3>Welcome to Timer Application</h3>");
            out.println("<br>");

            // Persistent timers
            Set persistenttimers = timer.findActiveTimersOwnedByThisServer();
            // Non-persistent timers get directly from the service

            EJBTimerService ejbTimerService = EJBTimerService.getEJBTimerService();
            Set nonpersistenttimers = ejbTimerService.getNonPersistentActiveTimerIdsByThisServer();
            int persistentsize = persistenttimers.size();
            int nonpersistentsize = nonpersistenttimers.size();

            out.println("There " + ((persistentsize == 1)? "is " : "are  ")
                    + persistentsize
                    + " active persistent timer" + ((persistentsize == 1)? "" : "s")
                    + " on this container");
            out.println("<br>");
            out.println("There " + ((nonpersistentsize == 1)? "is " : "are  ")
                    + nonpersistentsize
                    + " active non-persistent timer" + ((nonpersistentsize == 1)? "" : "s")
                    + " on this container");
            out.println("<br>");

        }catch(Throwable e){
            out.println("Problem accessing timers... ");
            out.println(e);
            e.printStackTrace();
        }
        finally {
            out.println("</body>");
            out.println("</html>");

            out.close();
            out.flush();

        }
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
    * Returns a short description of the servlet.
    */
    public String getServletInfo() {
        return "Timer Application Servlet";
    }
    // </editor-fold>
}
