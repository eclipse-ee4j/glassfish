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

import java.io.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import jakarta.servlet.annotation.ServletSecurity.TransportGuarantee;

@jakarta.servlet.annotation.WebListener
public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("MyListener.contextInitialized");
        try {
            ServletContext sc = sce.getServletContext();

            Class<NewServlet> servletCl = (Class<NewServlet>)Class.forName("NewServlet");
            NewServlet servlet = sc.createServlet(servletCl);
            servlet.setMyParameter("myServletParamValue");
            ServletRegistration.Dynamic sr = sc.addServlet("NewServlet", servlet);
            sr.setInitParameter("servletInitParamName", "servletInitParamValue");
            sr.addMapping("/newServlet");

            HttpConstraintElement constraint = new HttpConstraintElement();
            List<HttpMethodConstraintElement> methodConstraints = new ArrayList<HttpMethodConstraintElement>();
            methodConstraints.add(new HttpMethodConstraintElement("GET"));
            methodConstraints.add(new HttpMethodConstraintElement("POST",
                    new HttpConstraintElement(TransportGuarantee.NONE, new String[] {"javaee"})));
            methodConstraints.add(new HttpMethodConstraintElement("OPTIONS",
                    new HttpConstraintElement(EmptyRoleSemantic.DENY)));
            ServletSecurityElement servletSecurityElement =
                new ServletSecurityElement(constraint, methodConstraints);
            sr.setServletSecurity(servletSecurityElement);


            Class<NewServlet2> servletCl2 = (Class<NewServlet2>)Class.forName("NewServlet2");
            NewServlet2 servlet2 = sc.createServlet(servletCl2);
            servlet2.setMyParameter("myServletParamValue2");
            ServletRegistration.Dynamic sr2 = sc.addServlet("NewServlet2", servlet2);
            sr2.setInitParameter("servletInitParamName", "servletInitParamValue2");
            sr2.addMapping("/newServlet2");


            NewServlet2 servlet2_1 = sc.createServlet(servletCl2);
            servlet2_1.setMyParameter("myServletParamValue2");
            ServletRegistration.Dynamic sr2_1 = sc.addServlet("NewServlet2_1", servlet2_1);
            sr2_1.setInitParameter("servletInitParamName", "servletInitParamValue2");
            HttpConstraintElement constraint2_1 = new HttpConstraintElement(TransportGuarantee.NONE, "javaee");
            List<HttpMethodConstraintElement> methodConstraint2_1 = new ArrayList<HttpMethodConstraintElement>();
            methodConstraint2_1.add(new HttpMethodConstraintElement("GET",
                    new HttpConstraintElement(EmptyRoleSemantic.DENY)));
            methodConstraint2_1.add(new HttpMethodConstraintElement("OPTIONS"));
            ServletSecurityElement servletSecurityElement2_1 =
                new ServletSecurityElement(constraint2_1, methodConstraint2_1);
            sr2_1.setServletSecurity(servletSecurityElement2_1);
            sr2_1.addMapping("/newServlet2_1");

        } catch (Exception e) {
            sce.getServletContext().log("Error during contextInitialized");
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }
}
