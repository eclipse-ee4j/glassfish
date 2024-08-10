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

package com.sun.ejb.containers;

import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.security.SecurityContext;
import com.sun.enterprise.util.Utility;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author Mahesh Kannan
 */
public class EjbAsyncTask<V>
        implements Callable<V> {

    private EjbInvocation inv;

    private EjbFutureTask ejbFutureTask;

    private SecurityContext callerSecurityContext;

    public void initialize(EjbInvocation inv) {
        this.inv = inv;
        this.ejbFutureTask = inv.getEjbFutureTask();

        // Capture calling thread's security context and set
        // it on dispatch thread.
        callerSecurityContext = SecurityContext.getCurrent();
    }

    public long getInvId() {
        return inv.getInvId();
    }

    FutureTask getFutureTask() {
        return ejbFutureTask;
    }

    EjbInvocation getEjbInvocation() {
        return inv;
    }

    public V call()
            throws Exception {
        V returnValue = null;
        BaseContainer container = (BaseContainer) inv.container;
        ClassLoader prevCL = Thread.currentThread().getContextClassLoader();
        try {
            Utility.setContextClassLoader(container.getClassLoader());

            // Must be set before preinvoke so it happens before authorization.
            SecurityContext.setCurrent(callerSecurityContext);

            container.preInvoke(inv);

            returnValue = (V) container.intercept(inv);

            if (returnValue instanceof Future) {
                returnValue = (V) ((Future) returnValue).get();
            }
        } catch (InvocationTargetException ite) {
            inv.exception = ite.getCause();
            inv.exceptionFromBeanMethod = inv.exception;
        } catch (Throwable t) {
            inv.exception = t;
        } finally {
            try {
                container.postInvoke(inv, inv.getDoTxProcessingInPostInvoke());

                // Use the same exception handling logic here as is used in the
                // various invocation handlers.  This ensures that the same
                // exception that would be received in the synchronous case will
                // be set as the cause of the ExecutionException returned from
                // Future.get().

                if (inv.exception != null) {
                    if (inv.isLocal) {
                        InvocationHandlerUtil.throwLocalException(
                                inv.exception, inv.method.getExceptionTypes());
                    } else {
                        InvocationHandlerUtil.throwRemoteException(
                                inv.exception, inv.method.getExceptionTypes());
                    }
                }
            } catch (Throwable th) {
                ExecutionException ee = new ExecutionException(th);
                ejbFutureTask.setResultException(ee);
                throw ee;
            } finally {
                SecurityContext.setCurrent(null);
                Utility.setContextClassLoader(prevCL);
            }
        }

        ejbFutureTask.setResultValue(returnValue);
        return returnValue;
    }
}
