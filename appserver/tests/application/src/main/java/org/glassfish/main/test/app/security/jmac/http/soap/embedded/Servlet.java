/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jmac.http.soap.embedded;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.ws.BindingProvider;
import jakarta.xml.ws.WebServiceRef;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.glassfish.main.test.app.hello.ejb.HelloEjbPort;
import org.glassfish.main.test.app.hello.ejb.HelloEjbService;
import org.glassfish.main.test.app.hello.servlet.HelloServletPort;
import org.glassfish.main.test.app.hello.servlet.HelloServletService;

public class Servlet extends HttpServlet {
    private static final Logger LOG = System.getLogger(Servlet.class.getName());

    @WebServiceRef(wsdlLocation="META-INF/wsdl/hello-ejb.wsdl")
    private HelloEjbService ejbService;

    @WebServiceRef(wsdlLocation="META-INF/wsdl/hello-servlet.wsdl")
    private HelloServletService servletService;

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LOG.log(Level.DEBUG, "service(req={0}, resp={1})", req, resp);
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<HTML><HEAD><TITLE>Servlet Output</TTILE></HEAD><BODY>");
        HelloEjbPort ejbPort = ejbService.getHelloEjbPort();
        configureBindingProvider((BindingProvider) ejbPort, "http://localhost:8080/webservice/HelloEjb");

        String ejbMsg = ejbPort.hello("Sun");
        out.println(ejbMsg);
        out.println("<br>");

        HelloServletPort servletPort = servletService.getHelloServletPort();
        configureBindingProvider((BindingProvider) servletPort,
            "http://localhost:8080/security-jmac-soap-embedded/webservice/HelloServlet");
        String servletMsg = servletPort.hello("Sun");
        out.println(servletMsg);
        out.println("</BODY></HTML>");
        out.flush();
        LOG.log(Level.INFO, "Response sent successfuly.");
    }


    private void configureBindingProvider(final BindingProvider port, String endpoint) {
        LOG.log(Level.INFO, "configureBindingProvider(port, keys, values)");
        port.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);
    }
}
