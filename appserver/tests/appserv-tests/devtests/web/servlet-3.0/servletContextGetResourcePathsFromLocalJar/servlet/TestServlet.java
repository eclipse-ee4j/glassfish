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
import java.net.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    private static final String CATALOG_OFFERS = "/catalog/offers/";
    private static final String CATALOG_MORE_OFFERS = "/catalog/moreOffers/";

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        Set<String> resPaths = getServletContext().getResourcePaths("/catalog");
        if (resPaths == null) {
            throw new ServletException("No resource paths");
        }

        if (!resPaths.contains(CATALOG_OFFERS)) {
            throw new ServletException(CATALOG_OFFERS +
                " missing from resource paths");
        }

        if (!resPaths.contains(CATALOG_MORE_OFFERS)) {
            throw new ServletException(CATALOG_MORE_OFFERS +
                " missing from resource paths");
        }
    }
}
