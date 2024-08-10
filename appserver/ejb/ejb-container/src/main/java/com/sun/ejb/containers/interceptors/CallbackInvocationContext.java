/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package com.sun.ejb.containers.interceptors;

import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.enterprise.container.common.spi.util.InterceptorInfo;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType;

import jakarta.interceptor.InvocationContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.sun.ejb.containers.interceptors.InterceptorUtil.hasCompatiblePrimitiveWrapper;
import static com.sun.enterprise.deployment.LifecycleCallbackDescriptor.CallbackType.AROUND_CONSTRUCT;

/**
 * Concrete InvocationContext implementation passed to callback methods defined in interceptor classes.
 */
public class CallbackInvocationContext implements InvocationContext {

    private Map<String, Object> contextData;
    private int callbackIndex = 0;
    private CallbackChainImpl callbackChain;
    private Object[] interceptorInstances;
    private Object targetObjectInstance;
    private CallbackType eventType;
    Method method;

    // For AroundConstruct callback
    private Class<?> targetObjectClass;
    private Constructor<?> ctor;
    private Class<?>[] ctorParamTypes;
    private Object[] ctorParams;
    private BaseContainer container;
    private EJBContextImpl ctx;
    private InterceptorInfo interceptorInfo;

    public CallbackInvocationContext(Object targetObjectInstance, Object[] interceptorInstances, CallbackChainImpl chain) {
        this.targetObjectInstance = targetObjectInstance;
        this.interceptorInstances = interceptorInstances;
        callbackChain = chain;
    }

    public CallbackInvocationContext(Object targetObjectInstance, Object[] interceptorInstances, CallbackChainImpl chain, CallbackType eventType) {
        this(targetObjectInstance, interceptorInstances, chain);
        this.eventType = eventType;
    }

    /**
     * AroundConstruct
     */
    public CallbackInvocationContext(Class<?> targetObjectClass, Object[] interceptorInstances, CallbackChainImpl chain, CallbackType eventType, InterceptorInfo interceptorInfo) {
        this(null, interceptorInstances, chain, eventType);

        this.targetObjectClass = targetObjectClass;

        Constructor<?>[] ctors = targetObjectClass.getConstructors();
        for (Constructor<?> ctor0 : ctors) {
            ctor = ctor0;
            if (ctor0.getParameterTypes().length == 0) {
                // We are looking for a no-arg constructor
                break;
            }
        }

        ctorParamTypes = ctor.getParameterTypes();
        ctorParams = new Object[ctorParamTypes.length];

        this.interceptorInfo = interceptorInfo;
    }

    /**
     * AroundConstruct
     */
    public CallbackInvocationContext(Class<?> targetObjectClass, Object[] interceptorInstances, CallbackChainImpl chain, CallbackType eventType, BaseContainer container, EJBContextImpl ctx) {
        this(targetObjectClass, interceptorInstances, chain, eventType, null);

        this.container = container;
        this.ctx = ctx;
    }

    // InvocationContext methods

    @Override
    public Object getTarget() {
        return targetObjectInstance;
    }

    public Object[] getInterceptorInstances() {
        return interceptorInstances;
    }

    @Override
    public Object getTimer() {
        return null;
    }

    @Override
    public Constructor<?> getConstructor() {
        if (eventType == AROUND_CONSTRUCT) {
            return ctor;
        }
        return null;
    }

    @Override
    public Method getMethod() {
        if (eventType == AROUND_CONSTRUCT) {
            return null;
        }
        return method;
    }

    @Override
    public Object[] getParameters() {
        if (eventType == AROUND_CONSTRUCT) {
            return ctorParams;
        } else {
            throw new IllegalStateException("not applicable to Callback methods");
        }
    }

    @Override
    public void setParameters(Object[] params) {
        if (eventType == AROUND_CONSTRUCT) {
            checkSetParameters(params);
            ctorParams = params;
        } else {
            throw new IllegalStateException("not applicable to Callback methods");
        }
    }

    @Override
    public Map<String, Object> getContextData() {
        if (contextData == null) {
            contextData = new HashMap<>();
        }

        return contextData;
    }

    @Override
    public Object proceed() throws Exception {
        try {
            callbackIndex++;
            return callbackChain.invokeNext(callbackIndex, this);
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable th) {
            throw new Exception(th);
        }
    }

    /**
     * Called from Interceptor Chain to create the bean instance.
     */
    public void invokeSpecial() throws Throwable {
        if (eventType == AROUND_CONSTRUCT) {
            if (container == null) {
                targetObjectInstance = targetObjectClass.newInstance();
                interceptorInfo.setTargetObjectInstance(targetObjectInstance);
            } else {
                container.createEjbInstanceForInterceptors(ctorParams, ctx);
                targetObjectInstance = ctx.getEJB();
            }
        } // else do nothing? XXX
    }

    private void checkSetParameters(Object[] params) {
        if (ctor == null) {
            throw new IllegalStateException("Internal Error: Got null constructor");
        }

        if (params == null && ctorParamTypes.length != 0) {
            throw new IllegalArgumentException("Wrong number of parameters for " + " constructor: " + ctor);
        }
        if (params != null && ctorParamTypes.length != params.length) {
            throw new IllegalArgumentException("Wrong number of parameters for " + " constructor: " + ctor);
        }

        int index = 0;
        for (Class<?> type : ctorParamTypes) {
            if (params[index] == null) {
                if (type.isPrimitive()) {
                    throw new IllegalArgumentException("Parameter type mismatch for constructor " + ctor
                            + ".  Attempt to set a null value for Arg[" + index + "]. Expected a value of type: " + type.getName());
                }
            } else if (type.isPrimitive()) {
                if (!hasCompatiblePrimitiveWrapper(type, params[index].getClass())) {
                    throw new IllegalArgumentException("Parameter type mismatch for constructor " + ctor + ".  Arg[" + index + "] type: "
                            + params[index].getClass().getName() + " is not compatible with the expected type: " + type.getName());
                }
            } else if (!type.isAssignableFrom(params[index].getClass())) {
                throw new IllegalArgumentException("Parameter type mismatch for constructor " + ctor + ".  Arg[" + index + "] type: "
                        + params[index].getClass().getName() + " does not match the expected type: " + type.getName());
            }

            index++;
        }

    }
}
