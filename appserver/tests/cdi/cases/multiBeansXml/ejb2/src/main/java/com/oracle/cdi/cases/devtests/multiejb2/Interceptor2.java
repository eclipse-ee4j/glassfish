/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.oracle.cdi.cases.devtests.multiejb2;

import java.util.List;

import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

/**
 * @author jwells
 *
 */
@Interceptor
@RecordingInterceptor
public class Interceptor2 {
    @SuppressWarnings("unchecked")
    @AroundInvoke
    public Object intercept(InvocationContext context) throws Exception {
        Object params[] = context.getParameters();
        if (params.length != 1 || !(params[0] instanceof List)) {
            return context.proceed();
        }
        
        List<String> param = (List<String>) params[0];
        param.add(MultiBeansXmlEjb2.INTERCEPTOR2);
        
        return context.proceed();
    }

}
