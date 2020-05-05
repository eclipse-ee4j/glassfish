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

import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;

@WebFilter(urlPatterns = "/*")
public class TestFilter implements Filter {
    @Inject
    TestBean tb;
    @Inject
    BeanManager bm;

    @Override
    public void destroy() {
        System.out.println("TestFilter:destroy()");

    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain fc) throws IOException, ServletException {
        System.out.println("TestFilter:doFilter()");
        System.out.println("TestBean injected:"+ tb);
        System.out.println("BeanManager injected:"+ bm);
        if (tb == null)
            throw new ServletException("Injection of TestBean in ServletFilter failed");
        if (bm == null)
            throw new ServletException("Injection of BeanManager in ServletFilter failed");
        fc.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        System.out.println("TestFilter:init()");
    }

}

