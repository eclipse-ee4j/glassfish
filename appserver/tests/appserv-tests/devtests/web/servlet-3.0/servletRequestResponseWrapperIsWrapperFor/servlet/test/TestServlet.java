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

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        ServletRequestWrapper reqWrapper1 = new SubFirstLevelRequestWrapper(req);
        ServletRequestWrapper reqWrapper2 = new SecondLevelRequestWrapper(reqWrapper1);
        ServletRequestWrapper reqWrapper3 = new ThirdLevelRequestWrapper(reqWrapper2);
       
        if (!reqWrapper3.isWrapperFor(reqWrapper1) ||
                !reqWrapper3.isWrapperFor(FirstLevelRequestWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

        MyRequestWrapper myReqWrapper = new MyRequestWrapper(req);

        if (reqWrapper3.isWrapperFor(myReqWrapper) ||
                reqWrapper3.isWrapperFor(MyRequestWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

        ServletResponseWrapper resWrapper1 = new SubFirstLevelResponseWrapper(res);
        ServletResponseWrapper resWrapper2 = new SecondLevelResponseWrapper(resWrapper1);
        ServletResponseWrapper resWrapper3 = new ThirdLevelResponseWrapper(resWrapper2);
       
        if (!resWrapper3.isWrapperFor(resWrapper1) ||
                !resWrapper3.isWrapperFor(FirstLevelResponseWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

        MyResponseWrapper myResWrapper = new MyResponseWrapper(res);

        if (resWrapper3.isWrapperFor(myResWrapper) ||
                resWrapper3.isWrapperFor(MyResponseWrapper.class)) {
            throw new ServletException("Unexpected result");
        }

    }

    static class FirstLevelRequestWrapper extends ServletRequestWrapper {
        public FirstLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class SubFirstLevelRequestWrapper extends FirstLevelRequestWrapper {
        public SubFirstLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class SecondLevelRequestWrapper extends ServletRequestWrapper {
        public SecondLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class ThirdLevelRequestWrapper extends ServletRequestWrapper {
        public ThirdLevelRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class MyRequestWrapper extends ServletRequestWrapper {
        public MyRequestWrapper(ServletRequest req) {
            super(req);
        }
    }

    static class FirstLevelResponseWrapper extends ServletResponseWrapper {
        public FirstLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class SubFirstLevelResponseWrapper extends FirstLevelResponseWrapper {
        public SubFirstLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class SecondLevelResponseWrapper extends ServletResponseWrapper {
        public SecondLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class ThirdLevelResponseWrapper extends ServletResponseWrapper {
        public ThirdLevelResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

    static class MyResponseWrapper extends ServletResponseWrapper {
        public MyResponseWrapper(ServletResponse res) {
            super(res);
        }
    }

}
