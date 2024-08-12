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

package org.glassfish.flashlight.client;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.flashlight.FlashlightUtils;
import org.glassfish.flashlight.impl.client.DTraceClientInvoker;
import org.glassfish.flashlight.impl.client.ReflectiveClientInvoker;
import org.glassfish.flashlight.provider.FlashlightProbe;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service
public class ProbeClientInvokerFactory {

    private static AtomicInteger clientMethodIdCounter = new AtomicInteger();

    protected static int getNextId() {
        return clientMethodIdCounter.incrementAndGet();
    }


    public static ProbeClientInvoker createInvoker(Object target, Method method, FlashlightProbe probe,
        String[] paramNames) {
        int invokerId = clientMethodIdCounter.incrementAndGet();
        return new ReflectiveClientInvoker(invokerId, target, method, paramNames, probe);
    }


    public static ProbeClientInvoker createInvoker(Object target, Method method, FlashlightProbe probe) {
        return createInvoker(target, method, probe, FlashlightUtils.getParamNames(method));
    }


    public static ProbeClientInvoker createDTraceInvoker(FlashlightProbe probe) {
        int invokerId = clientMethodIdCounter.incrementAndGet();
        return new DTraceClientInvoker(invokerId, probe);
    }
}
