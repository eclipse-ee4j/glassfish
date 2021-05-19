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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import test.artifacts.HttpParams;


//A web filter to set ServletRequest in the thread local context.
@WebFilter(urlPatterns="/*")
public class InjectionPointFilter implements Filter{

    @Override
    public void destroy() {
        System.out.println("InjectionPointFilter::destroy");

    }

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain arg2) throws IOException, ServletException {
        System.out.println("InjectionPointFilter::doFilter");
        //setting the ServletRequest in the thread-local
        //variable so that it could be used in HttpParams
        HttpParams.sr.set(req);
        arg2.doFilter(req, res);
    }

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        System.out.println("InjectionPointFilter::init");
    }

}
