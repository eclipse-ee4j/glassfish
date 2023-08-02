/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

// The package name is important - the name is used as a default when compiling JSP files.
// Therefore also the JSP file must have the same name (without _jsp suffix).
// Test is testing redeployments.
package org.apache.jsp.reload;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.jsp.JspFactory;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.SkipPageException;

public final class JspReloadGeneratedServletIfUpdated_jsp extends org.glassfish.wasp.runtime.HttpJspBase implements org.glassfish.wasp.runtime.JspSourceDependent {

    private static final JspFactory _jspxFactory = JspFactory.getDefaultFactory();

    private static java.util.List<String> _jspx_dependants;

    private org.glassfish.jsp.api.ResourceInjector _jspx_resourceInjector;

    @Override
    public java.util.List<String> getDependants() {
        return _jspx_dependants;
    }

    @Override
    public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {

        PageContext pageContext = null;
        HttpSession session = null;
        ServletContext application = null;
        ServletConfig config = null;
        JspWriter out = null;
        Object page = this;
        JspWriter _jspx_out = null;
        PageContext _jspx_page_context = null;

        try {
            response.setContentType("text/html");
            response.setHeader("X-Powered-By", "JSP/2.3");
            pageContext = _jspxFactory.getPageContext(this, request, response, null, true, 8192, true);
            _jspx_page_context = pageContext;
            application = pageContext.getServletContext();
            config = pageContext.getServletConfig();
            session = pageContext.getSession();
            out = pageContext.getOut();
            _jspx_out = out;
            _jspx_resourceInjector = (org.glassfish.jsp.api.ResourceInjector)
                application.getAttribute("com.sun.appserv.jsp.resource.injector");

            out.write("This is my UPDATED output\n\n");
        } catch (Throwable t) {
            if (!(t instanceof SkipPageException)) {
                out = _jspx_out;
                if (out != null && out.getBufferSize() != 0) {
                    out.clearBuffer();
                }
                if (_jspx_page_context != null) {
                    _jspx_page_context.handlePageException(t);
                } else {
                    throw new ServletException(t);
                }
            }
        } finally {
            _jspxFactory.releasePageContext(_jspx_page_context);
        }
    }
}
