/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;

import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.AROUND_CONSTRUCT;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY;

/**
 *
 * @author Kenneth Saks
 */
public class SystemInterceptorProxy {

    // Won't actually be Serialized since it only applies to Stateless/Singleton
    public Object delegate;

    private Method aroundConstruct;
    private Method postConstruct;
    private Method preDestroy;
    private Method aroundInvoke;
    private Method aroundTimeout;

    public void setDelegate(Object delegate) {
        try {
            for (Method delegateMethod : delegate.getClass().getDeclaredMethods()) {
                if (delegateMethod.getAnnotation(PostConstruct.class) != null) {
                    postConstruct = delegateMethod;
                    prepareMethod(delegateMethod);
                } else if (delegateMethod.getAnnotation(PreDestroy.class) != null) {
                    preDestroy = delegateMethod;
                    prepareMethod(delegateMethod);
                } else if (delegateMethod.getAnnotation(AroundInvoke.class) != null) {
                    aroundInvoke = delegateMethod;
                    prepareMethod(delegateMethod);
                } else if (delegateMethod.getAnnotation(AroundTimeout.class) != null) {
                    aroundTimeout = delegateMethod;
                    prepareMethod(delegateMethod);
                } else if (delegateMethod.getAnnotation(AroundConstruct.class) != null) {
                    aroundConstruct = delegateMethod;
                    prepareMethod(delegateMethod);
                }
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }

        this.delegate = delegate;
    }

    @PostConstruct
    public Object init(InvocationContext ctx) throws Exception {
        return doCall(ctx, postConstruct);
    }

    @PreDestroy
    public Object destroy(InvocationContext ctx) throws Exception {
        return doCall(ctx, preDestroy);
    }

    @AroundConstruct
    public Object create(InvocationContext ctx) throws Exception {
        return doCall(ctx, aroundConstruct);
    }

    @AroundInvoke
    public Object aroundInvoke(InvocationContext ctx) throws Exception {
        return doCall(ctx, aroundInvoke);
    }

    @AroundTimeout
    public Object aroundTimeout(InvocationContext ctx) throws Exception {
        return doCall(ctx, aroundTimeout);
    }

    private Object doCall(InvocationContext ctx, Method method) throws Exception {
        Object returnValue = null;

        if (delegate != null && method != null) {
            try {
                returnValue = method.invoke(delegate, ctx);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getCause();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                }

                throw new Exception(cause);
            }
        } else {
            returnValue = ctx.proceed();
        }

        return returnValue;

    }

    public static InterceptorDescriptor createInterceptorDesc() {
        InterceptorDescriptor interceptor = new InterceptorDescriptor();

        Class<?> interceptorClass = SystemInterceptorProxy.class;
        String interceptorName = interceptorClass.getName();

        interceptor.setInterceptorClass(interceptorClass);

        {
            LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
            desc.setLifecycleCallbackClass(interceptorName);
            desc.setLifecycleCallbackMethod("create");
            interceptor.addCallbackDescriptor(AROUND_CONSTRUCT, desc);
        }

        {
            LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
            desc.setLifecycleCallbackClass(interceptorName);
            desc.setLifecycleCallbackMethod("init");
            interceptor.addCallbackDescriptor(POST_CONSTRUCT, desc);
        }

        {
            LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
            desc.setLifecycleCallbackClass(interceptorName);
            desc.setLifecycleCallbackMethod("destroy");
            interceptor.addCallbackDescriptor(PRE_DESTROY, desc);
        }

        {
            LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
            desc.setLifecycleCallbackClass(interceptorName);
            desc.setLifecycleCallbackMethod("aroundInvoke");
            interceptor.addAroundInvokeDescriptor(desc);
        }

        {
            LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
            desc.setLifecycleCallbackClass(interceptorName);
            desc.setLifecycleCallbackMethod("aroundTimeout");
            interceptor.addAroundTimeoutDescriptor(desc);
        }

        return interceptor;

    }

    private void prepareMethod(final Method method) throws Exception {
        if (!method.trySetAccessible()) {
            throw new InaccessibleObjectException("Unable to make accessible: " + method);
        }
    }

}
