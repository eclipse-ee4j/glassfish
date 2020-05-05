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

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.HttpConstraint;
import jakarta.servlet.annotation.HttpMethodConstraint;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.annotation.ServletSecurity.EmptyRoleSemantic;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ServletSecurity(value=@HttpConstraint(rolesAllowed={ "javaee" }),
    httpMethodConstraints={ @HttpMethodConstraint("GET"),
        @HttpMethodConstraint(value="OPTIONS", emptyRoleSemantic=ServletSecurity.EmptyRoleSemantic.DENY) })
public class NewServlet2 extends HttpServlet {
    private String initParamValue;
    private String myParamValue;

    public void init() throws ServletException {
        initParamValue = getServletConfig().getInitParameter(
            "servletInitParamName");
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        if (!"myServletParamValue2".equals(myParamValue)) {
            throw new ServletException("Wrong servlet instance");
        }

        if (!"servletInitParamValue2".equals(initParamValue)) {
            throw new ServletException("Missing servlet init param");
        }

        PrintWriter writer = res.getWriter();
        writer.write("g2:Hello");
    }

    public void doPost(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("p2:Hello, " + req.getRemoteUser() + "\n");
    }

    protected void doOptions(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("o2:Hello, " + req.getRemoteUser() + "\n");
    }

    public void setMyParameter(String value) {
        myParamValue = value;
    }

}
