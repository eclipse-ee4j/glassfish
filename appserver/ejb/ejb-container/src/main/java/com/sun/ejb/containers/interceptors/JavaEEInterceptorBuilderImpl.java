/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers.interceptors;

import com.sun.ejb.codegen.EjbOptionalIntfGenerator;
import com.sun.ejb.spi.container.OptionalLocalInterfaceProvider;
import com.sun.enterprise.container.common.spi.InterceptorInvoker;
import com.sun.enterprise.container.common.spi.JavaEEInterceptorBuilder;
import com.sun.enterprise.container.common.spi.util.InterceptorInfo;

import java.lang.reflect.Proxy;

/**
 *
 */

public class JavaEEInterceptorBuilderImpl implements JavaEEInterceptorBuilder {

    private InterceptorInfo interceptorInfo;
    private InterceptorManager interceptorManager;

    private Class<?> subClassInterface;
    private Class<?> subClass;

    public JavaEEInterceptorBuilderImpl(InterceptorInfo intInfo, InterceptorManager manager, EjbOptionalIntfGenerator gen, Class<?> subClassIntf, Class<?> subClass) {
        interceptorInfo = intInfo;
        interceptorManager = manager;
        this.subClassInterface = subClassIntf;
        this.subClass = subClass;
    }

    @Override
    public InterceptorInvoker createInvoker(Object instance) throws Exception {
        interceptorInfo.setTargetObjectInstance(instance);

        // Proxy invocation handler. Also implements InterceptorInvoker.
        InterceptorInvocationHandler invoker = new InterceptorInvocationHandler();

        Proxy proxy = (Proxy) Proxy.newProxyInstance(subClass.getClassLoader(), new Class[] { subClassInterface }, invoker);

        // Object passed back to the caller.
        OptionalLocalInterfaceProvider provider = (OptionalLocalInterfaceProvider) subClass.getDeclaredConstructor().newInstance();
        provider.setOptionalLocalIntfProxy(proxy);

        invoker.init(instance, interceptorManager.createInterceptorInstances(), provider, interceptorManager);

        return invoker;
    }

    @Override
    public void addRuntimeInterceptor(Object interceptor) {
        interceptorManager.registerRuntimeInterceptor(interceptor);
    }

}
