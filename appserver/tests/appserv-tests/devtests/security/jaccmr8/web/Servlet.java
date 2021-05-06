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

package org.glassfish.jacc.test.mr8;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.naming.*;
import jakarta.ejb.EJB;
import jakarta.ejb.EJBs;
import java.net.URLEncoder;
import java.net.URLDecoder;
import java.security.Principal;

import jakarta.servlet.annotation.WebServlet;

@WebServlet(name = "Servlet", urlPatterns = {"/servlet","/authuser","/anyauthuser","/star","/denyuncoveredpost"})
public class Servlet extends HttpServlet {

        @EJB(beanName = "HelloEJB", beanInterface = Hello.class)
        private Hello helloStateless;

        @EJB(beanName = "HelloStatefulEJB", beanInterface = HelloStateful.class)
        private HelloStateful helloStateful;

        public void init(ServletConfig config) throws ServletException {
                super.init(config);
                System.out.println("In jaccmr8::Servlet... init()");
        }

        public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                resp.setContentType("text/html");
                PrintWriter out = resp.getWriter();

                String mode = req.getParameter("mode");
                if (mode == null) mode = "stateful";
                mode = URLDecoder.decode(mode,"UTF-8");
                String name = req.getParameter("name");
                if (name == null) name = "NotDeclared";
                name = URLDecoder.decode(name,"UTF-8");

                // EJB information
                String callerPrincipal = "NONE";
                boolean isInEJBRole = false;
                boolean isAnyAuthUserEJB = false;
                String invokeAnyAuthUser = "No";
                String invokeAuthUser = "No";
                if ("stateful".equals(mode)) {
                        System.out.println("Invoking Stateful EJB");
                        callerPrincipal = helloStateful.hello(name);
                        isAnyAuthUserEJB = helloStateful.inRole("**");
                        isInEJBRole = helloStateful.inRole(name);
                        try {
                                helloStateful.methodAnyAuthUser();
                                invokeAnyAuthUser = "Yes";
                        }
                        catch (Exception exc) {
                                System.out.println("FAILED invoke of methodAnyAuthUser()");
                                invokeAnyAuthUser = exc.toString();
                        }
                        try {
                                helloStateful.methodAuthUser();
                                invokeAuthUser = "Yes";
                        }
                        catch (Exception exc) {
                                System.out.println("FAILED invoke of methodAuthUser()");
                                invokeAuthUser = exc.toString();
                        }
                        System.out.println("Successfully invoked Stateful EJB");
                } else if ("stateless".equals(mode)) {
                        System.out.println("Invoking Stateless EJB");
                        callerPrincipal = helloStateless.hello(name);
                        isAnyAuthUserEJB = helloStateless.inRole("**");
                        isInEJBRole = helloStateless.inRole(name);
                        try {
                                helloStateless.methodAnyAuthUser();
                                invokeAnyAuthUser = "Yes";
                        }
                        catch (Exception exc) {
                                System.out.println("FAILED invoke of methodAnyAuthUser()");
                                invokeAnyAuthUser = exc.toString();
                        }
                        try {
                                helloStateless.methodAuthUser();
                                invokeAuthUser = "Yes";
                        }
                        catch (Exception exc) {
                                System.out.println("FAILED invoke of methodAuthUser()");
                                invokeAuthUser = exc.toString();
                        }
                        System.out.println("Successfully invoked Stateless EJB");
                } else {
                        System.out.println("Mode: " + mode);
                }

                // Servlet information
                String principalName = "NONE";
                String principalType = "UNKNOWN";
                Principal p = req.getUserPrincipal();
                if (p != null) {
                        principalName = p.getName();
                        principalType = p.getClass().getName();
                }
                String userPrincipal = principalName + " is " + principalType;
                boolean isAnyAuthUserWeb = req.isUserInRole("**");
                boolean isInWebRole = req.isUserInRole(name);

                out.println("<HTML> <HEAD> <TITLE>Servlet Output</TITLE> </HEAD> <BODY>");
                out.println("<CENTER>JACC MR8 Servlet</CENTER> <p> ");
                out.println(" Request URL: " + req.getRequestURL() + "<br>");
                out.println(" HTTP Method: " + req.getMethod() + "<br>");
                out.println("Context Path: " + req.getContextPath() + "<br>");
                out.println("Servlet Path: " + req.getServletPath() + "<br>");
                out.println("<br> <CENTER>Results</CENTER> <p> ");
                out.println("EJB Caller Principal: " + callerPrincipal + "<br>");
                out.println("EJB isCallerInRole: " + isInEJBRole + "<br>");
                out.println("EJB isUserInAnyAuthUserRole: " + isAnyAuthUserEJB + "<br>");
                out.println("EJB Invoke AnyAuthUser: " + invokeAnyAuthUser + "<br>");
                out.println("EJB Invoke AuthUser: " + invokeAuthUser + "<br>");
                out.println("WEB User Principal: " + userPrincipal + "<br>");
                out.println("WEB isUserInRole: " + isInWebRole + "<br>");
                out.println("WEB isUserInAnyAuthUserRole: " + isAnyAuthUserWeb + "<br>");
                out.println("</BODY> </HTML> ");
        }

        public void destroy() {
                System.out.println("In jaccmr8::Servlet destroy");
        }
}
