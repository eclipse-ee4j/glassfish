/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package org.glassfish.main.test.jdbc.jta.timeout.war.stopwatch;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;

import java.lang.System.Logger;

import static jakarta.interceptor.Interceptor.Priority.APPLICATION;
import static java.lang.System.Logger.Level.INFO;

@Interceptor
@Stopwatch
@Priority(APPLICATION)
public class StopwatchInterceptor {
    private static final Logger LOG = System.getLogger(StopwatchInterceptor.class.getName());

    @AroundInvoke
    public Object time(InvocationContext invocationContext) throws Exception {
        final String systemClassName = invocationContext.getMethod().getDeclaringClass().getCanonicalName();
        final String systemMethodName = invocationContext.getMethod().getName();
        final long startTime = System.currentTimeMillis();
        try {
            return invocationContext.proceed();
        } finally {
            LOG.log(INFO, "Calling method {0}.{1} took {2} ms", systemClassName, systemMethodName,
                System.currentTimeMillis() - startTime);
        }
    }
}
