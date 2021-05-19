/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.hk2.devtest.isolation.web.iso1;

import java.io.IOException;
import java.io.PrintWriter;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.glassfish.hk2.api.ServiceLocator;

/**
 * Simple returns the name of the HABITAT property
 *
 * @author jwells
 */
public class Iso1Servlet extends HttpServlet {
    /**
     * For serialization
     */
    private static final long serialVersionUID = -9177540431267005946L;

    private static final String HABITAT_ATTRIBUTE = "org.glassfish.servlet.habitat";
    private static final String JNDI_APP_LOOKUP = "java:app/hk2/ServiceLocator";

    private static final String SERVLET_CONTEXT_LOCATOR = "ServletContextLocator=";
    private static final String JNDI_APP_LOCATOR = "JndiAppLocator=";

    private String getJndiAppLocatorName() {

        try {
          Context context = new InitialContext();

          ServiceLocator retVal = (ServiceLocator) context.lookup(JNDI_APP_LOOKUP);

          return retVal.getName();
        }
        catch (NamingException ne) {
            return null;
        }
    }

    /**
     * Just prints out the value of the ServiceLocator getName
     */
    @Override
    public void doGet(HttpServletRequest request,
            HttpServletResponse response)
        throws IOException, ServletException {


        ServletContext context = getServletContext();

        ServiceLocator locator = (ServiceLocator) context.getAttribute(HABITAT_ATTRIBUTE);

        String reply1 = SERVLET_CONTEXT_LOCATOR + ((locator == null) ? "null" : locator.getName());

        String jndiAppLocatorName = getJndiAppLocatorName();
        String reply2 = JNDI_APP_LOCATOR + ((jndiAppLocatorName == null) ? "null" : jndiAppLocatorName);

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        writer.println("<html>");
        writer.println("<head>");
        writer.println("<title>Iso1 WebApp</title>");
        writer.println("</head>");
        writer.println("<body>");

        writer.println(reply1);
        writer.println(reply2);

        writer.println("</body>");
        writer.println("</html>");
    }
}
