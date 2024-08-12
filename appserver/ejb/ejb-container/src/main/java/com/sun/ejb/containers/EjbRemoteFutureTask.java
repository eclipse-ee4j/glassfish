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

import jakarta.ejb.EJBException;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ken Saks
 */
public class EjbRemoteFutureTask<V>
    implements Future<V>, Serializable {


    private Long asyncId;

    private GenericEJBHome server;

    // Used to remember if cancel() was called already
    private boolean cancelCalled = false;


    private boolean complete = false;
    private V resultValue;
    private Throwable resultException;


    public EjbRemoteFutureTask(Long id, GenericEJBHome home) {

       asyncId = id;
       server = home;

    }

    public boolean cancel(boolean mayInterruptIfRunning) {

        if( !cancelCalled ) {

            cancelCalled = true;

            // mayInterruptIfRunning only determines whether the bean method
            // has visibility to the fact that the caller called Future.cancel().
            if( mayInterruptIfRunning ) {

                try {
                    //GenericEJBHome server2 = (GenericEJBHome)
                       //     javax.rmi.PortableRemoteObject.narrow(server, GenericEJBHome.class);
                    RemoteAsyncResult result = server.cancel(asyncId);
                    if( result != null ) {
                        if( result.resultException != null ) {
                            setResultException(result.resultException);
                        } else {
                            setResultValue((V) result.resultValue);
                        }
                    }

                } catch(RemoteException re) {

                    throw new EJBException("Exception during cancel operation", re);

                }

            }
        }

        // For now we don't even try checking to see if the task has started running.
        // Just return false so the caller knows the task could not be cancelled.
        return false;
    }


    public V get() throws ExecutionException {

        // If get() has already been called, produce the same behavior
        // as initial call, except if get(timeout, unit) resulted in a
        // TimeoutException

        if( !complete ) {

            try {
                //GenericEJBHome server2 = (GenericEJBHome)
                            //javax.rmi.PortableRemoteObject.narrow(server, GenericEJBHome.class);
                RemoteAsyncResult result = server.get(asyncId);
                if( result != null ) {
                    if( result.resultException != null ) {
                        setResultException(result.resultException);
                    } else {
                        setResultValue((V) result.resultValue);
                    }
                }

            } catch(RemoteException re) {
                setResultException(re);
            }
        }

        if( resultException != null ) {
            if( resultException instanceof ExecutionException ) {
                throw (ExecutionException) resultException;
            } else {
                throw new ExecutionException(resultException);
            }
        }

        return resultValue;
    }

    public V get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {

        // If get() has already been called, produce the same behavior
        // as initial call, except if get(timeout, unit) resulted in a
        // TimeoutException

        if( !complete ) {

            try {

                RemoteAsyncResult result = server.getWithTimeout(asyncId, timeout, unit.toString());
                if( result != null ) {
                    if( result.resultException != null ) {
                        setResultException(result.resultException);
                    } else {
                        setResultValue((V) result.resultValue);
                    }
                }

            } catch(TimeoutException te) {
                throw te;
            } catch(RemoteException re) {
                setResultException(re);
            }
        }

        if( resultException != null ) {
            if( resultException instanceof ExecutionException ) {
                throw (ExecutionException) resultException;
            } else {
                throw new ExecutionException(resultException);
            }
        }

        return resultValue;
    }


    public boolean isCancelled() {
        // For now, we don't ever actually forcibly cancel a task
        // that hasn't executed.
        return false;
    }


    public boolean isDone() {

        // Per the Future javadoc.  It's a little odd that isDone()
        // is required to return true even if cancel() was called but
        // returned false.  However, that's the behavior.  There's nothing
        // stopping the caller from still calling get() though.
        boolean isDone = cancelCalled || complete;

        if( !isDone ) {
            // Ask server.
            try {
                RemoteAsyncResult result = server.isDone(asyncId);
                if( result != null ) {
                    isDone = true;
                    if( result.resultException != null ) {
                        setResultException(result.resultException);
                    } else {
                        setResultValue((V) result.resultValue);
                    }
                }
            } catch(RemoteException re) {
                throw new EJBException(re);
            }
        }

        return isDone;
    }


    private void setResultValue(V v) {
        resultValue = v;
        complete = true;
    }

    private void setResultException(Throwable t) {
        resultException = t;
        complete = true;
    }


}
