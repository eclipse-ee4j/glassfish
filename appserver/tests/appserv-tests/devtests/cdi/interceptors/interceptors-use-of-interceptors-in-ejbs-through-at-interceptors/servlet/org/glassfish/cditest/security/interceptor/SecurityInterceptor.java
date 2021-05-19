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

package org.glassfish.cditest.security.interceptor;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.annotation.Resource;
import jakarta.ejb.EJBContext;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.io.Serializable;
import org.glassfish.cditest.security.api.Secure;

/**
 * Realizes security for EJBs.
 *
 * @author ifischer
 *
 */
@Secure
@Interceptor
public class SecurityInterceptor implements Serializable
{
    private static final Logger LOG = Logger.getLogger(SecurityInterceptor.class.getName());

    public static boolean aroundInvokeCalled = false;

    @Resource
    private EJBContext ejbCtx;

    /**
     * Perform lookup for permissions.
     * Does the caller has the permission to call the method?
     * TODO: implement lookup
     *
     * @param InvocationContext of intercepted method
     * @return
     * @throws Exception
     */
    @AroundInvoke
    protected Object invoke(final InvocationContext ctx) throws Exception
    {
        Principal p = ejbCtx.getCallerPrincipal();
        Method interfaceMethod = ctx.getMethod();

        LOG.log(Level.INFO, "EJB Method called [Full]:\"{0}\" by Principal:{1}", new Object[]{getFullEJBClassName(interfaceMethod), p.toString()});
        LOG.log(Level.INFO, "EJB Method called [Methodonly]:{0} by Principal:{1}", new Object[]{interfaceMethod.getName(), p.toString()});

        SecurityInterceptor.aroundInvokeCalled = true;
        return ctx.proceed();
    }

    /**
     * The EJBContext interface doesn't provide convenient methods to get the name of the EJB class,
     * so the classname has to be extracted from the method.
     *
     * @param the method whose classname is needed
     * @return classname (fully qualified) of given method, e.g. "com.profitbricks.user.api.UserService"
     */
    private String getFullEJBClassName(Method method) {
        // extract className from methodName
        // methodName format example:"public void com.profitbricks.user.api.UserService.testMe()"
        String methodName = method.toString();

        int start = methodName.lastIndexOf(' ') + 1;
        int end = methodName.lastIndexOf('.');

        String className = methodName.substring(start, end);

        return className;
    }

    public static void reset(){
        //reset invocation status
        SecurityInterceptor.aroundInvokeCalled = false;
    }
}
