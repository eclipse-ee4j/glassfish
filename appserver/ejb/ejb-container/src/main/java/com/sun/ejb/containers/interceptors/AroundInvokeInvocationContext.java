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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;


/**
 * Concrete InvocationContext implementation passed to callback methods
 * defined in interceptor classes.
 */
public class AroundInvokeInvocationContext extends CallbackInvocationContext
    implements InterceptorManager.AroundInvokeContext {

    private Method method;
    private int interceptorIndex = 0;
    private InterceptorManager.InterceptorChain chain;
    private Object[] parameters;


    public AroundInvokeInvocationContext(Object targetObjectInstance,
                                     Object[] interceptorInstances,
                                     InterceptorManager.InterceptorChain chain,
                                     Method m,
                                     Object[] params
                                     ) {
        super(targetObjectInstance, interceptorInstances, null);
        method = m;
        this.chain = chain;
        parameters = params;
    }

    @Override
    public Constructor<?> getConstructor() {
        return null;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Object proceed()
        throws Exception
    {
        try {
            interceptorIndex++;
            return chain.invokeNext(interceptorIndex, this);
        } catch (Exception ex) {
            throw ex;
        } catch (Throwable th) {
            throw new Exception(th);
        } finally {
            interceptorIndex--;
        }
    }

    @Override
    public Object[] getParameters() {
        return parameters;
    }

    @Override
    public void setParameters(Object[] params) {
        InterceptorUtil.checkSetParameters(params, getMethod());
        parameters = params;

    }


    /**
      * Called from Interceptor Chain to invoke the actual bean method.
      * This method must throw any exception from the bean method *as is*,
      * without being wrapped in an InvocationTargetException.  The exception
      * thrown from this method will be propagated through the application's
      * interceptor code, so it must not be changed in order for any exception
      * handling logic in that code to function properly.
      */
    public  Object invokeBeanMethod() throws Throwable {

        try {

            return method.invoke(getTarget(), parameters);

        } catch(InvocationTargetException ite) {
            throw ite.getCause();
        }

    }



}

