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

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class MyServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        String reqEncoding = context.getRequestCharacterEncoding();
        String resEncoding = context.getResponseCharacterEncoding();
        if (!"UTF-8".equals(reqEncoding)) {
            throw new IllegalStateException("Unexpected encoding: " + reqEncoding);
        }
        if (!"US-ASCII".equals(resEncoding)) {
            throw new IllegalStateException("Unexpected encoding: " + resEncoding);
        }

        String jEncoding = "Shift_JIS";
        context.setRequestCharacterEncoding(jEncoding);
        context.setResponseCharacterEncoding(jEncoding);
    }
}
