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

package org.glassfish.soteria.test;

import org.glassfish.soteria.SecurityContextImpl;

import jakarta.annotation.security.DeclareRoles;
import jakarta.ejb.EJB;
import javax.security.enterprise.AuthenticationStatus;
import javax.security.enterprise.SecurityContext;
import javax.security.enterprise.credential.CallerOnlyCredential;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import jakarta.inject.Inject;

import static javax.security.enterprise.authentication.mechanism.http.AuthenticationParameters.withParams;
import static org.glassfish.soteria.Utils.notNull;

/**
 * Test Servlet that prints out the name of the authenticated caller and whether
 * this caller is in any of the roles {foo, bar, kaz}
 */
@DeclareRoles({"foo", "bar", "kaz"})
@WebServlet("/servlet")
public class Servlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @EJB
    private TestEJB bean;
    @Inject
    private SecurityContext securityContext;

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("This is a servlet \n");
        String name = request.getParameter("name");

        if (notNull(name)) {

            AuthenticationStatus status = securityContext.authenticate(
                    request, response,
                    withParams()
                            .credential(
                                    new CallerOnlyCredential(name)));

            response.getWriter().write("Authenticated with status: " + status.name() + "\n");
        }

        String ejbName = null;
        if (bean.getUserPrincipalFromEJBContext() != null) {
            ejbName = bean.getUserPrincipalFromEJBContext().getName();
        }

        response.getWriter().write("ejb username: " + ejbName + "\n");

        response.getWriter().write("ejb user has role \"foo\": " + bean.isCallerInRoleFromEJBContext("foo") + "\n");
        response.getWriter().write("ejb user has role \"bar\": " + bean.isCallerInRoleFromEJBContext("bar") + "\n");
        response.getWriter().write("ejb user has role \"kaz\": " + bean.isCallerInRoleFromEJBContext("kaz") + "\n");

        String contextName = null;
        if (bean.getUserPrincipalFromSecContext() != null) {
            contextName = bean.getUserPrincipalFromSecContext().getName();
        }

        response.getWriter().write("context username: " + contextName + "\n");

        response.getWriter().write("context user has role \"foo\": " + bean.isCallerInRoleFromSecContext("foo") + "\n");
        response.getWriter().write("context user has role \"bar\": " + bean.isCallerInRoleFromSecContext("bar") + "\n");
        response.getWriter().write("context user has role \"kaz\": " + bean.isCallerInRoleFromSecContext("kaz") + "\n");

        response.getWriter().write("web user has access to /protectedServlet: " + securityContext.hasAccessToWebResource("/protectedServlet") + "\n");

        Set<String> roles = bean.getAllDeclaredCallerRoles();

        response.getWriter().write("All declared roles of user " + roles + "\n");

        response.getWriter().write("all roles has role \"foo\": " + roles.contains("foo") + "\n");
        response.getWriter().write("all roles has role \"bar\": " + roles.contains("bar") + "\n");
        response.getWriter().write("all roles has role \"kaz\": " + roles.contains("kaz") + "\n");
    }


    /**
     * Handles the HTTP <code>GET</code> method.
     *
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
     *
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
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
