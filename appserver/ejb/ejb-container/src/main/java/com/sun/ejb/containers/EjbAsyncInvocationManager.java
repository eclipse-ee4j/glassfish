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

import com.sun.ejb.Container;
import com.sun.ejb.EjbInvocation;
import com.sun.logging.LogDomains;

import jakarta.ejb.EJBException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;

/**
 * @author Mahesh Kannan
 */
@Service
public class EjbAsyncInvocationManager {
    private static final Logger _logger = LogDomains.getLogger(EjbAsyncInvocationManager.class, LogDomains.EJB_LOGGER);

    private AtomicLong invCounter = new AtomicLong();

    // Map of Remote Future<> tasks.
    private ConcurrentHashMap<Long, EjbFutureTask> remoteTaskMap =
            new ConcurrentHashMap<Long, EjbFutureTask>();

    public Future createLocalFuture(EjbInvocation inv) {
        return createFuture(inv);
    }

    public Future createRemoteFuture(EjbInvocation inv, Container container, GenericEJBHome ejbHome) {

        // Create future but only use the local task to register in the
        // remote task map. We'll be replacing the result value with a
        // remote future task.
        EjbFutureTask localFutureTask = (EjbFutureTask) createFuture(inv);

        EjbRemoteFutureTask returnFuture = new EjbRemoteFutureTask(inv.getInvId(), ejbHome);

        // If this is a future task for a remote invocation
        // and the method has Future<T> return type, add the
        // corresponding local task to the async map.
        // TODO Need to work on cleanup logic.  Maybe we should store
        // this in a per-container data structure so cleanup is automatic
        // on container shutdown / undeployment.  Otherwise, we need a way to easily
        // identify all tasks for a given container for cleanup.
        Method m = inv.getMethod();
        if( !(m.getReturnType().equals(Void.TYPE))) {
            remoteTaskMap.put(inv.getInvId(), localFutureTask);
        }

        return returnFuture;
    }

    private Future createFuture(EjbInvocation inv) {
        // Always create a Local future task that is associated with the
        // invocation.
        EjbFutureTask futureTask = new EjbFutureTask(new EjbAsyncTask(), this);

        // Assign a unique id to this async task
        long invId = invCounter.incrementAndGet();
        inv.setInvId(invId);
        inv.setEjbFutureTask(futureTask);

        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Creating new async future task " + inv);
        }

        return futureTask;
    }


    public Future submit(EjbInvocation inv) {

        //We need to clone this invocation as submitting
        //so that the inv is *NOT* shared between the
        //current thread and the executor service thread
        EjbInvocation asyncInv = inv.clone();

        //EjbInvocation.clone clears the txOpsManager field so getTransactionOperationsManager()
        // returns null *after* EjbInvocation.clone(). However, in this case, we do want the original
        // TransactionOperationsManager so we explicitly set it after calling clone.
        //
        //Note: EjbInvocation implements TransactionOperationsManager so we can use asyncInv to
        //  be the TransactionOperationsManager for the new cloned invocation
        asyncInv.setTransactionOperationsManager(asyncInv);

        //In most of the cases we don't want registry entries from being reused in the cloned
        //  invocation, in which case, this method must be called. I am not sure if async
        //  ejb invocation must call this (It never did and someone in ejb team must investigate
        //  if clearRegistry() must be called from here)


        inv.clearYetToSubmitStatus();
        asyncInv.clearYetToSubmitStatus();

        EjbFutureTask futureTask = asyncInv.getEjbFutureTask();

        // EjbAsyncTask.initialize captures calling thread's
        // CallerPrincipal and sets it on the dispatch thread
        // before authorization.
        futureTask.getEjbAsyncTask().initialize(asyncInv);

        EjbContainerUtil ejbContainerUtil = EjbContainerUtilImpl.getInstance();
        return ejbContainerUtil.getThreadPoolExecutor(null).submit(futureTask.getEjbAsyncTask());
    }

    public void cleanupContainerTasks(Container container) {

        Set<Map.Entry<Long, EjbFutureTask>> entrySet = remoteTaskMap.entrySet();
        Iterator<Map.Entry<Long, EjbFutureTask>> iterator = entrySet.iterator();

        List<Long> removedTasks = new ArrayList<Long>();

        while(iterator.hasNext()) {

            Map.Entry<Long, EjbFutureTask> next = iterator.next();

            EjbAsyncTask task = next.getValue().getEjbAsyncTask();
            if( task.getEjbInvocation().container == container ) {

                removedTasks.add(task.getInvId());

                if( _logger.isLoggable(Level.FINE)) {
                    _logger.log(Level.FINE, "Cleaning up async task " + task.getFutureTask());
                }

                // TODO Add some additional checking here for in-progress
                // tasks.
                iterator.remove();
            }
        }

        _logger.log(Level.FINE, "Cleaning up " + removedTasks.size() + "async tasks for " +
                   "EJB " + container.getEjbDescriptor().getName() + " .  Total of " +
                   remoteTaskMap.size() + " remaining");

        return;
    }

    RemoteAsyncResult remoteCancel(Long asyncTaskID) {
        EjbFutureTask task = getLocalTaskForID(asyncTaskID);

        if( _logger.isLoggable(Level.FINE) ) {
            EjbAsyncTask asyncTask = task.getEjbAsyncTask();
            _logger.log(Level.FINE, "Enter remoteCancel for async task " + asyncTaskID +
                    " : " + asyncTask.getEjbInvocation());
        }

        RemoteAsyncResult result = null;

        if( task.isDone() ) {

            // Since the task is done just return the result on this
            // internal remote request.
            result = new RemoteAsyncResult();

            result.resultException = task.getResultException();
            result.resultValue = task.getResultValue();
            result.asyncID = asyncTaskID;

            // The client object won't make another request once it
            // has the result so we can remove it from the container map.
            remoteTaskMap.remove(asyncTaskID);

        } else {

            // Set flag on invocation so bean method has visibility to
            // the fact that client called cancel()
            EjbInvocation inv = task.getEjbAsyncTask().getEjbInvocation();
            inv.setWasCancelCalled(true);

        }

        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Exit remoteCancel for async task " + asyncTaskID +
                    " : " + task);
        }

        return result;
    }

    RemoteAsyncResult remoteIsDone(Long asyncTaskID) {

        EjbFutureTask task = getLocalTaskForID(asyncTaskID);

        if( _logger.isLoggable(Level.FINE) ) {
            EjbAsyncTask asyncTask = task.getEjbAsyncTask();
            _logger.log(Level.FINE, "Enter remoteisDone for async task " + asyncTaskID +
                    " : " + asyncTask.getEjbInvocation());
        }

        // If not done, just return null.
        RemoteAsyncResult result = null;

        if( task.isDone() ) {

            // Since the task is done just return the result on this
            // internal remote request.
            result = new RemoteAsyncResult();

            result.resultException = task.getResultException();
            result.resultValue = task.getResultValue();
            result.asyncID = asyncTaskID;

            // The client object won't make another request once it
            // has the result so we can remove it from the container map.
            remoteTaskMap.remove(asyncTaskID);

        }


        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Exit remoteIsDone for async task " + asyncTaskID +
                    " : " + task);
        }

        return result;
    }

    RemoteAsyncResult remoteGet(Long asyncTaskID) {
        EjbFutureTask task = getLocalTaskForID(asyncTaskID);

        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Enter remoteGet for async task " + asyncTaskID +
                    " : " + task.getEjbAsyncTask().getEjbInvocation());
        }

        RemoteAsyncResult result = new RemoteAsyncResult();
        result.asyncID = asyncTaskID;

        try {

            result.resultValue = task.get();

        } catch(Throwable t) {

            result.resultException = t;
        }

        remoteTaskMap.remove(asyncTaskID);

        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Exit remoteGet for async task " + asyncTaskID +
                    " : " + task);
        }

        return result;
    }

    RemoteAsyncResult remoteGetWithTimeout(Long asyncTaskID, Long timeout, TimeUnit unit)
            throws TimeoutException {

        EjbFutureTask task = getLocalTaskForID(asyncTaskID);

        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Enter remoteGetWithTimeout for async task " + asyncTaskID +
                    " timeout=" + timeout + " , unit=" + unit + " : " +
                    task.getEjbAsyncTask().getEjbInvocation());
        }

        RemoteAsyncResult result = new RemoteAsyncResult();
        result.asyncID = asyncTaskID;

        try {

            result.resultValue = task.get(timeout, unit);

        } catch(TimeoutException to) {

            if( _logger.isLoggable(Level.FINE) ) {
                _logger.log(Level.FINE, "TimeoutException for async task " + asyncTaskID +
                    " : " + task);
            }

            throw to;

        } catch(Throwable t) {

            result.resultException = t;
        }

        // As long as we're not throwing a TimeoutException, just remove the task
        // from the map.
        remoteTaskMap.remove(asyncTaskID);

        if( _logger.isLoggable(Level.FINE) ) {
            _logger.log(Level.FINE, "Exit remoteGetWithTimeout for async task " + asyncTaskID +
                    " : " + task);
        }

        return result;
    }


    private EjbFutureTask getLocalTaskForID(Long asyncTaskID) {
        EjbFutureTask task = remoteTaskMap.get(asyncTaskID);

        if( task == null ) {
            _logger.log(Level.FINE, "Could not find async task for ID " + asyncTaskID);

            throw new EJBException("Could not find Local Async task corresponding to ID " +
                asyncTaskID);
        }

        return task;
    }
}
