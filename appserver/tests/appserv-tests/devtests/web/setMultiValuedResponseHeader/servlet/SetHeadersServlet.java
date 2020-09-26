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
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class SetHeadersServlet extends HttpServlet {

    public void doGet(HttpServletRequest request,
                      HttpServletResponse response)
            throws ServletException, IOException {

        // The Cache-Control header often takes multiple values
        response.setHeader("Cache-Control","no-cache");

        // We have to use addHeader( ) to prevent setHeader( ) from replacing
        // the existing value.
        response.addHeader("Cache-Control","no-store");

        // We now have multiple values for the Cache-Control header.
        // If we try to replace the existing values with setHeader( ), only
        // one is replaced, contrary to the requirements of SRV.5.2
        // (second paragraph)
        response.setHeader("Cache-Control","public");
    }

}
