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

package jsfinjection;

import jakarta.annotation.Resource;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import javax.sql.DataSource;
import javax.naming.*;

public class FilterTest implements Filter{

    private ServletContext context;
    private @Resource(name="jdbc/__default") DataSource ds;
//    private DataSource ds;

    public void destroy() {
        System.out.println("[Filter.destroy]");
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws java.io.IOException, jakarta.servlet.ServletException {
        System.out.println("[Filter.doFilter]");

        String msg = "PASS";
/*
        try {
            InitialContext ic = new InitialContext();
            ic.lookup("jdbc/__default");
            msg += "=:iclookup";
            System.out.println("XXX ic lookup DONE");
        } catch(Exception ex) {
        }
*/
        if (ds != null) {
            try {
                msg = "PASS-:" + ds.getLoginTimeout();
            } catch(Throwable ex) {
                msg = "FAIL-:" + ex.toString();
            }
        } else {
            msg = "FAIL-: ds is null";
        }
        System.out.println("[Filter.doFilter.msg = " + msg + "]");

        ((HttpServletRequest)request).getSession().setAttribute("FILTER", msg);
        filterChain.doFilter(request, response);

    }


    public void init(jakarta.servlet.FilterConfig filterConfig) throws jakarta.servlet.ServletException {
        System.out.println("[Filter.init]");
        context = filterConfig.getServletContext();
    }

}
