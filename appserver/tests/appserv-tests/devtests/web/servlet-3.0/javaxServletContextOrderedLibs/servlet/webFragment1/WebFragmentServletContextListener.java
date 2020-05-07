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

package webFragment1;

import java.util.*;
import jakarta.servlet.*;

public class WebFragmentServletContextListener
        implements ServletContextListener {

    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();

        List<String> orderedLibs = (List<String>) sc.getAttribute(
            ServletContext.ORDERED_LIBS);
        if ((orderedLibs == null) || (orderedLibs.size() != 2)) {
            throw new RuntimeException(
                "Missing or wrong-sized " + ServletContext.ORDERED_LIBS +
                " attribute");
        }
        if (!"webFragment2.jar".equals(orderedLibs.get(0)) ||
                !"webFragment1.jar".equals(orderedLibs.get(1))) {
            throw new RuntimeException(
                ServletContext.ORDERED_LIBS +
                " attribute has wrong contents");
        }

        ServletRegistration sreg = sc.addServlet("WebFragment1Servlet",
            "webFragment1.WebFragmentServlet");
        sreg.addMapping("/webFragment1Servlet");
    }

    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
