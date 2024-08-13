/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author bhavanishankar@java.net
 */

@WebServlet(name="SecureWebAppTestServlet", urlPatterns = "/SecureWebAppTestServlet")
public class SecureWebAppTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest httpServletRequest,
                         HttpServletResponse httpServletResponse) throws ServletException, IOException {
        PrintWriter out = httpServletResponse.getWriter();

        print("\n[OUTPUT from SecureWebAppTestServlet]", out);
        print("[Hi from SecureWebAppTestServlet]", out);

        String sysProp = System.getProperty("org.glassfish.embedded.greeting");
        print("[System property org.glassfish.embedded.greeting = " + sysProp + "]", out);
        if(!"Hi from BHAVANI".equals(sysProp)) {
            httpServletResponse.sendError(500,
                    "System property org.glassfish.embedded.greeting not found");
            return;
        }

        Boolean directClassLoading = Boolean.getBoolean("ANTLR_USE_DIRECT_CLASS_LOADING");
        print("[System property ANTLR_USE_DIRECT_CLASS_LOADING = " +
                System.getProperty("ANTLR_USE_DIRECT_CLASS_LOADING") + "]", out);
        if(!directClassLoading) {
            httpServletResponse.sendError(500,
                    "System property ANTLR_USE_DIRECT_CLASS_LOADING is not set");
            return;
        }
        print("[End of OUTPUT from SecureWebAppTestServlet]", out);

        out.flush();
        out.close();
    }

    private void print(String msg, PrintWriter out) {
        out.println(msg);
        System.out.println(msg);
    }
}
