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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package prober;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.glassfish.flashlight.client.ProbeClientMediator;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.glassfish.flashlight.provider.ProbeRegistry;

/**
 * @author Byron Nevins
 */
public class ProberServlet extends HttpServlet {
    @Resource
    private ProbeClientMediator listenerRegistrar;
    @Resource
    private ProbeRegistry probeRegistry;
    private PrintWriter out;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        out = response.getWriter();
        try {
            pr("<html>");
            pr("<head>");
            pr("<title>Servlet ProbeServlet</title>");
            pr("</head>");
            pr("<body>");
            pr("<h2>All Probes</h2>");
            pr("<ul>");
            // KISS
            for (Iterator<String> it = getAllProbes().iterator(); it.hasNext();) {
                String s = it.next();

                if(s.startsWith("fooblog"))
                    s = "<b>" + s + "</b>";

                pr("<li>" + s + "</li>");
            }
            pr("</ul>");
            pr("</body>");
            pr("</html>");
        }
        finally {
            out.close();
        }
    }

    @Override
    public void init() throws ServletException {
        if (listenerRegistrar == null)
            throw new ServletException("ProbeClientMediator was not injected.");

        try {
            listenerRegistrar.registerListener(new ConnectionProbeListener());
        }
        catch (Exception e) {
            throw new ServletException("Error initializing", e);
        }
    }

    private void pr(String s) {
        out.println(s);
    }

    private Set<String> getAllProbes() {
        Collection<FlashlightProbe> probes = probeRegistry.getAllProbes();
        NavigableSet sorted = new TreeSet<String>();

        for (FlashlightProbe flp : probes) {
            String probeString = flp.toString();
            sorted.add(probeString);
        }

        if (sorted.isEmpty()) {
            sorted.add("No Probes found!");
        }
        return sorted;
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    private final static AtomicInteger counter = new AtomicInteger();
}
