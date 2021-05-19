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

package test;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;

@WebFilter(urlPatterns={"/test"})
public class FilterTest implements Filter {
    public FilterTest() {
        Record.addData("F");
    }

    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("[Filter.init]");
        Record.addData("Fi");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        System.out.println("[Filter.doFilter]");
        filterChain.doFilter(request, response);

    }

    public void destroy() {
        System.out.println("[Filter.destroy]");
    }
}
