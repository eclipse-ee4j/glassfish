/*
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

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.interceptor.AroundConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.AroundTimeout;
import jakarta.interceptor.InvocationContext;
import com.sun.enterprise.deployment.InterceptorDescriptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author Kenneth Saks
 */

public class SystemInterceptorProxy
{
    // Won't actually be Serialized since it only applies to Stateless/Singleton

    public Object delegate;

    private Method aroundConstruct;
    private Method postConstruct;
    private Method preDestroy;
    private Method aroundInvoke;
    private Method aroundTimeout;


    public void setDelegate(Object d) {

        Class delegateClass = d.getClass();

        try {

           for(Method m : delegateClass.getDeclaredMethods() ) {

               if( m.getAnnotation(PostConstruct.class) != null ) {
                   postConstruct = m;
                   prepareMethod(m);
               } else if( m.getAnnotation(PreDestroy.class) != null ) {
                   preDestroy = m;
                   prepareMethod(m);
               } else if( m.getAnnotation(AroundInvoke.class) != null ) {
                   aroundInvoke = m;
                   prepareMethod(m);
               } else if( m.getAnnotation(AroundTimeout.class) != null ) {
                   aroundTimeout = m;
                   prepareMethod(m);
               } else if( m.getAnnotation(AroundConstruct.class) != null ) {
                   aroundConstruct = m;
                   prepareMethod(m);
               }
           }

        } catch(Exception e) {
            throw new IllegalArgumentException(e);
        }

         delegate = d;

    }

    private void prepareMethod(final Method m) throws Exception {

         java.security.AccessController
                        .doPrivileged(new java.security.PrivilegedExceptionAction() {
                    public java.lang.Object run() throws Exception {
                        if (!m.isAccessible()) {
                            m.setAccessible(true);
                        }
                        return null;
                    }});

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

    private Object doCall(InvocationContext ctx, Method m) throws Exception {
        Object returnValue = null;

        if( (delegate != null) && (m != null) ) {
            try {
                returnValue = m.invoke(delegate, ctx);
            } catch(InvocationTargetException ite) {
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

        Class interceptorClass = SystemInterceptorProxy.class;
        String interceptorName = interceptorClass.getName();

        interceptor.setInterceptorClass(interceptorClass);

        {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(interceptorName);
               desc.setLifecycleCallbackMethod("create");
               interceptor.addCallbackDescriptor(CallbackType.AROUND_CONSTRUCT, desc);
        }

        {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(interceptorName);
               desc.setLifecycleCallbackMethod("init");
               interceptor.addCallbackDescriptor(CallbackType.POST_CONSTRUCT, desc);
        }

        {
               LifecycleCallbackDescriptor desc = new LifecycleCallbackDescriptor();
               desc.setLifecycleCallbackClass(interceptorName);
               desc.setLifecycleCallbackMethod("destroy");
               interceptor.addCallbackDescriptor(CallbackType.PRE_DESTROY, desc);
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


}
