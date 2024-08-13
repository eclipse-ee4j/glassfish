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

package com.sun.ejb.containers;

import com.sun.ejb.EjbInvocation;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


/**
 * @author Mahesh Kannan
 */
public class EjbFutureTask<V>
    extends FutureTask<V> {

    private EjbAsyncTask ejbAsyncTask;

    // Used to remember if cancel() was called already
    private boolean cancelCalled = false;

    // State which could be set from both the caller's thread and
    // the thread on which the task is executing.
    private volatile boolean complete = false;
    private volatile V resultValue;
    private volatile Throwable resultException;


    public EjbFutureTask(EjbAsyncTask<V> callable, EjbAsyncInvocationManager mgr) {
        super(callable);
        this.ejbAsyncTask = callable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        if( !cancelCalled ) {

            cancelCalled = true;

            // mayInterruptIfRunning only determines whether the bean method
            // has visibility to the fact that the caller called Future.cancel().
            if( mayInterruptIfRunning ) {
                EjbInvocation inv = ejbAsyncTask.getEjbInvocation();
                inv.setWasCancelCalled(true);
            }
        }

        // For now we don't even try checking to see if the task has started running.
        // Just return false so the caller knows the task could not be cancelled.
        return false;
    }

    @Override
    public V get() throws InterruptedException, ExecutionException {

        // If get() has already been called, produce the same behavior
        // as initial call, except if get(timeout, unit) resulted in a
        // TimeoutException

        if( !complete ) {
            try {
                super.get();
                // result value is set directly by AsyncTask
            } catch(ExecutionException ee) {
                // already set directly by AsyncTask
            } catch(InterruptedException ie) {
                setResultException(ie);
            } catch(RuntimeException re) {
                setResultException(re);
            }
        }

        // We really shouldn't get CancellationException or
        // InterruptedException, but throw whatever kind we get.
        if( resultException != null ) {
           if( resultException instanceof ExecutionException ) {
               throw (ExecutionException) resultException;
           } else if( resultException instanceof InterruptedException) {
               throw (InterruptedException) resultException;
           } else {
               throw (RuntimeException) resultException;
           }
        }

        return resultValue;
    }

    @Override
    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        // If get() has already been called, produce the same behavior
        // as initial call, except if get(timeout, unit) resulted in a
        // TimeoutException

        if( !complete ) {
            try {
                super.get(timeout, unit);
            } catch(ExecutionException ee) {
                // already set directly by AsyncTask
            } catch(TimeoutException t) {
                // If it's a TimeoutException, complete will not have been set.
                // In that case just rethrow the TimeoutException without
                // remembering it in resultException.  That way, the caller
                // can call get() or get(timeout, unit) to try again.
                throw t;
            } catch(InterruptedException ie) {
                setResultException(ie);
            } catch(RuntimeException re) {
                setResultException(re);
            }
        }

        // We really shouldn't get CancellationException or
        // InterruptedException, but throw whatever kind we get.
        if( resultException != null ) {
           if( resultException instanceof ExecutionException ) {
               throw (ExecutionException) resultException;
           } else if( resultException instanceof InterruptedException) {
               throw (InterruptedException) resultException;
           } else {
               throw (RuntimeException) resultException;
           }
        }

        return resultValue;

    }

    @Override
    public boolean isCancelled() {
        // For now, we don't ever actually forcibly cancel a task
        // that hasn't executed.
        return false;
    }

    @Override
    public boolean isDone() {
        // Per the Future javadoc.  It's a little odd that isDone()
        // is required to return true even if cancel() was called but
        // returned false.  However, that's the behavior.  There's nothing
        // stopping the caller from still calling get() though.
        return (cancelCalled || complete);
    }

    EjbAsyncTask getEjbAsyncTask() {
        return ejbAsyncTask;
    }

    long getInvId() {
        return ejbAsyncTask.getInvId();
    }

    void setResultValue(V v) {
        // EjbAsyncTask calls this directly.  That way
        // we can return true from isDone() after completion of
        // the task, even if get() was not called.
        resultValue = v;
        complete = true;

    }

    void setResultException(Throwable t) {
        // EjbAsyncTask calls this directly.  That way
        // we can return true from isDone() after completion of
        // the task, even if get() was not called.
        resultException = t;
        complete = true;
    }

    // Internal method to retrieve any result value
    V getResultValue() {
        return resultValue;
    }

    Throwable getResultException() {
        return resultException;
    }

    public String toString() {

        StringBuffer sbuf = new StringBuffer();

        sbuf.append("EjbFutureTask  ");
        sbuf.append("taskId="+ejbAsyncTask.getInvId());
        sbuf.append(",cancelCalled="+cancelCalled);
        sbuf.append(",complete="+complete);
        if( complete ) {
            if( resultException == null ) {
                sbuf.append(",resultValue="+resultValue);
            } else {
                sbuf.append(",resultException="+resultException);
            }

        }

        return sbuf.toString();
    }
}
