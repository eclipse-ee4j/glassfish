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

import java.io.IOException;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(filterName="testFilter", urlPatterns="/*")
public class TestFilter implements Filter {
    private static boolean init = false;

    public void init(FilterConfig config) throws ServletException {
        System.out.println("Calling TestFilter.init()");
        synchronized(TestFilter.class) {
            if (init) {
                throw new IllegalStateException("TestFilter already init");
            }

            init = true;
        }

        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
    }
}
