/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.io.InputStream;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class EchoHeaderServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();
        // Header not visible yet, body not processed
        String value = req.getHeader("x-trailer");
        if (value == null) {
            value = "null";
        }
        pw.write(value);

        // Read the body - quick and dirty
        InputStream is = req.getInputStream();
        int count = 0;
        while (is.read() > -1) {
            count++;
        }

        pw.write(Integer.valueOf(count).toString());

        // Header should be visible now
        value = req.getHeader("x-trailer");
        if (value == null) {
            value = "null";
        }
        pw.write(value);
    }
}
