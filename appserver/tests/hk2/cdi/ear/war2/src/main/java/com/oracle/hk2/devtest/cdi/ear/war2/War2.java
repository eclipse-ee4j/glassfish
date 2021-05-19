/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.cdi.ear.war2;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.oracle.hk2.devtest.cdi.ear.ejb1.Ejb1HK2Service;
import com.oracle.hk2.devtest.cdi.ear.ejb2.Ejb2HK2Service;
import com.oracle.hk2.devtest.cdi.ear.lib1.HK2Service;
import com.oracle.hk2.devtest.cdi.ear.lib1.Lib1HK2Service;

/**
 *
 * @author jwells
 *
 */
public class War2 extends HttpServlet {
    /**
     *
     */
    private static final long serialVersionUID = -2151912159297931882L;

    @Inject
    private Lib1HK2Service lib1Hk2Service;

    @Inject
    private Ejb1HK2Service ejb1Hk2Service;

    @Inject
    private Ejb2HK2Service ejb2Hk2Service;

    @Inject
    private War2HK2Service war2Hk2Service;

    /**
     * Just prints out the value of the ServiceLocator getName
     */
    @Override
    public void doGet(HttpServletRequest request,
            HttpServletResponse response)
        throws IOException, ServletException {

        if (lib1Hk2Service == null || !lib1Hk2Service.getComponentName().equals(HK2Service.LIB1)) {
            throw new ServletException("lib1HK2Service from lib1 was invalid: " + lib1Hk2Service);
        }

        if (ejb1Hk2Service == null || !ejb1Hk2Service.getComponentName().equals(HK2Service.EJB1)) {
            throw new ServletException("ejb2HK2Service from ejb2 was invalid: " + ejb2Hk2Service);
        }

        if (ejb2Hk2Service == null || !ejb2Hk2Service.getComponentName().equals(HK2Service.EJB2)) {
            throw new ServletException("ejb1HK2Service from ejb1 was invalid: " + ejb1Hk2Service);
        }

        if (war2Hk2Service == null || !war2Hk2Service.getComponentName().equals(HK2Service.WAR2)) {
            throw new ServletException("war2HK2Service from war2 was invalid: " + war2Hk2Service);
        }

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Iso1 WebApp</title>");
        writer.println("</head>");
        writer.println("<body>");

        writer.println("success");

        writer.println("</body>");
        writer.println("</html>");
    }
}
