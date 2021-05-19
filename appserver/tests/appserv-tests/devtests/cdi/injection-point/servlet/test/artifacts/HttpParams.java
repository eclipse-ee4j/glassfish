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

package test.artifacts;

import java.util.Enumeration;

import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.InjectionPoint;
import jakarta.servlet.ServletRequest;

@RequestScoped
public class HttpParams {
    //A thread local variable to house the servlet request. This is set by the
    //servlet filter. This is not a recommended way to get hold of HttpServletRequest
    //in a CDI bean but a workaround that is described in
    //http://www.seamframework.org/Community/HowToReachHttpServletRequestAndHttpServletResponseFromBean
    //The right approach is to use JSF's FacesContext as described in
    //https://docs.jboss.org/weld/reference/snapshot/en-US/html/injection.html#d0e1635
    public static ThreadLocal<ServletRequest> sr = new ThreadLocal<ServletRequest>();

    @Produces
    @HttpParam("")
    String getParamValue(InjectionPoint ip) {
        ServletRequest req = sr.get();
        String parameterName = ip.getAnnotated().getAnnotation(HttpParam.class).value();
        if (parameterName.trim().equals("")) parameterName = ip.getMember().getName();
        return req.getParameter(parameterName);
    }

}
