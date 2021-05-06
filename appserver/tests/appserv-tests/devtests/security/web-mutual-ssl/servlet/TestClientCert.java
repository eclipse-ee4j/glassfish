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

package devtests.security;

import java.io.*;
import java.security.cert.X509Certificate;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestClientCert extends HttpServlet {

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {

        System.out.println("start service(...) of TestClientCert" );

        X509Certificate[] certs = (X509Certificate[])req.getAttribute(
            "jakarta.servlet.request.X509Certificate");
        String clName = null;
        if (certs != null) {
            for (X509Certificate cert : certs) {
               getServletContext().log(cert.toString());
            }
            clName = certs.getClass().getName();
        } else {
            clName = "cert is null";
        }
        res.getWriter().print(clName);
    }
}
