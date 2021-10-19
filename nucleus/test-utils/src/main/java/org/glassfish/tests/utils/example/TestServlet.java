/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.tests.utils.example;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.logging.Logger;


/**
 * Useful for simple testing of the server.
 *
 * @author David Matejcek
 */
@WebServlet(urlPatterns = "/")
public class TestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    public static final String RESPONSE_TEXT = "This is a response from " + TestServlet.class;

    @Override
    public void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        Logger.getLogger(TestServlet.class.getName()).info("Servlet accepted the request!");
        resp.setStatus(200);
        resp.setContentType("text/plain");
        resp.getOutputStream().println(RESPONSE_TEXT);
    }
}
