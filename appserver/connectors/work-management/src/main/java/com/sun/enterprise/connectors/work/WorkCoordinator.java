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

package com.sun.enterprise.connectors.work;

import com.sun.corba.ee.spi.threadpool.WorkQueue;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.connectors.work.context.WorkContextHandlerImpl;
import com.sun.enterprise.connectors.work.monitor.WorkManagementProbeProvider;
import com.sun.enterprise.security.SecurityContext;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ResourceAdapterAssociation;
import jakarta.resource.spi.work.*;

import org.glassfish.logging.annotation.LogMessageInfo;

import static jakarta.resource.spi.work.WorkException.UNDEFINED;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.util.logging.Logger;

/**
 * WorkCoordinator : Coordinates one work's execution. Handles all exception conditions and does JTS coordination.
 *
 * @author Binod P.G
 */
public final class WorkCoordinator {

    private static final Logger logger = LogFacade.getLogger();

    @LogMessageInfo(
            message = "Resource adapter association failed.",
            comment = "Failed to associate Resource Adapter bean to Work instance.",
            level = "SEVERE",
            cause = "Resource Adapter throws exception during ManagedConnectionFactory.setResourceAdapter().",
            action = "[1] If you are using third party resource adapter, contact resource adapter vendor." +
                     "[2] If you are a resource adapter developer, please check the resource adapter code.",
            publish = true)
    private static final String RAR_RA_ASSOCIATE_ERROR = "AS-RAR-05005";

    static final int WAIT_UNTIL_START = 1;
    static final int WAIT_UNTIL_FINISH = 2;
    static final int NO_WAIT = 3;

    static final int CREATED = 1;
    static final int STARTED = 2;
    static final int COMPLETED = 3;
    static final int TIMEDOUT = 4;

    private volatile int waitMode;
    private volatile int state = CREATED;

    private final jakarta.resource.spi.work.Work work;
    private final long timeout;
    private long startTime;
    private final ExecutionContext ec;
    private final WorkQueue queue;
    private final WorkListener listener;
    private volatile WorkException exception;
    private final Object lock;
    private static int seed;
    private final int id;

    private WorkManagementProbeProvider probeProvider;

    private ConnectorRuntime runtime;
    private String raName = null;

    private WorkContextHandlerImpl contextHandler;

    /**
     * Constructs a coordinator
     *
     * @param work A work object as submitted by the resource adapter
     * @param timeout timeout for the work instance
     * @param ec ExecutionContext object.
     * @param queue WorkQueue of the threadpool, to which the work will be submitted
     * @param listener WorkListener object from the resource adapter.
     */
    public WorkCoordinator(jakarta.resource.spi.work.Work work, long timeout, ExecutionContext ec, WorkQueue queue, WorkListener listener,
            WorkManagementProbeProvider probeProvider, ConnectorRuntime runtime, String raName, WorkContextHandlerImpl handler) {

        this.work = work;
        this.timeout = timeout;
        this.ec = ec;
        this.queue = queue;
        this.listener = listener;
        this.id = increaseSeed();
        this.runtime = runtime;
        this.lock = new Object();
        this.probeProvider = probeProvider;
        this.raName = raName;
        this.contextHandler = handler;
    }

    public String getRAName() {
        return raName;
    }

    /**
     * Submits the work to the queue and generates a work accepted event.
     */
    public void submitWork(int waitModeValue) {
        this.waitMode = waitModeValue;
        this.startTime = System.currentTimeMillis();
        if (listener != null) {
            listener.workAccepted(new WorkEvent(this, WorkEvent.WORK_ACCEPTED, work, null));
        }
        if (probeProvider != null) {
            probeProvider.workSubmitted(raName);
            probeProvider.workQueued(raName);
        }

        queue.addWork(new OneWork(work, this, Thread.currentThread().getContextClassLoader()));
    }

    /**
     * Pre-invoke operation. This does the following
     *
     * <pre>
     * 1. Notifies the <code> WorkManager.startWork </code> method.
     * 2. Checks whether the wok has already been timed out.
     * 3. Recreates the transaction with JTS.
     * </pre>
     */
    public void preInvoke() {
        // If the work is just scheduled, check whether it has timed out or not.
        if (waitMode == NO_WAIT && timeout > -1) {
            long elapsedTime = System.currentTimeMillis() - startTime;

            if (probeProvider != null) {
                probeProvider.workWaitedFor(raName, elapsedTime);
            }

            if (elapsedTime > timeout) {
                workTimedOut();
            }
        }

        // If the work is timed out then return.
        if (!proceed()) {
            if (probeProvider != null) {
                probeProvider.workDequeued(raName);
            }
            return;
        } else {
            if (probeProvider != null) {
                probeProvider.workProcessingStarted(raName);
                probeProvider.workDequeued(raName);
            }
        }

        // Associate ResourceAdapter if the Work is RAA
        if (work instanceof ResourceAdapterAssociation) {
            try {
                runtime.associateResourceAdapter(raName, (ResourceAdapterAssociation) work);
            } catch (ResourceException re) {
                logger.log(SEVERE, RAR_RA_ASSOCIATE_ERROR, re);
            }
        }

        // Change the status to started.
        setState(STARTED);

        if (waitMode == WAIT_UNTIL_START) {
            unLock();
        }

        // All set to do start the work. So send the event.
        if (listener != null) {
            listener.workStarted(new WorkEvent(this, WorkEvent.WORK_STARTED, work, null));
        }

        // Set the unauthenticated securityContext before executing the work
        SecurityContext.setUnauthenticatedContext();

    }

    public void setupContext(OneWork oneWork) throws WorkException {
        contextHandler.setupContext(getExecutionContext(ec, work), this, oneWork);
    }

    /**
     * Post-invoke operation. This does the following after the work is executed.
     *
     * <pre>
     * 1. Releases the transaction with JTS.
     * 2. Generates work completed event.
     * 3. Clear the thread context.
     * </pre>
     */
    public void postInvoke() {
        boolean txImported = (getExecutionContext(ec, work) != null && getExecutionContext(ec, work).getXid() != null);
        try {
            JavaEETransactionManager eeTransactionManager = getTransactionManager();
            if (txImported) {
                eeTransactionManager.release(getExecutionContext(ec, work).getXid());
            }
        } catch (WorkException ex) {
            setException(ex);
        } finally {
            try {
                if (!isTimedOut()) {
                    if (probeProvider != null) {
                        probeProvider.workProcessingCompleted(raName);
                        probeProvider.workProcessed(raName);
                    }

                    // If exception is not null, the work has already been rejected.
                    if (listener != null) {
                        listener.workCompleted(new WorkEvent(this, WorkEvent.WORK_COMPLETED, work, getException()));
                    }
                }

                // Also release the TX from the record of TX Optimizer
                if (txImported) {
                    getTransactionManager().clearThreadTx();
                }
            } catch (Exception e) {
                logger.log(WARNING, e.getMessage());
            } finally {
                // Reset the securityContext once the work has completed
                SecurityContext.setUnauthenticatedContext();
            }
        }

        setState(COMPLETED);

        if (waitMode == WAIT_UNTIL_FINISH) {
            unLock();
        }
    }

    /**
     * Times out the thread
     */
    private void workTimedOut() {
        setState(TIMEDOUT);
        exception = new WorkRejectedException();
        exception.setErrorCode(WorkException.START_TIMED_OUT);

        if (listener != null) {
            listener.workRejected(new WorkEvent(this, WorkEvent.WORK_REJECTED, work, exception));
        }

        if (probeProvider != null) {
            probeProvider.workTimedOut(raName);
        }
    }

    /**
     * Checks the work is good to proceed with further processing.
     *
     * @return true if the work is good and false if it is bad.
     */
    public boolean proceed() {
        return !isTimedOut() && exception == null;
    }

    public boolean isTimedOut() {
        return getState() == TIMEDOUT;
    }

    /**
     * Retrieves the exception created during the work's execution.
     *
     * @return a <code>WorkException</code> object.
     */
    public WorkException getException() {
        return exception;
    }

    /**
     * Accepts an exception object and converts to a <code>WorkException</code> object.
     *
     * @param throwable Throwable object.
     */
    public void setException(Throwable throwable) {
        if (getState() < STARTED) {
            if (throwable instanceof WorkRejectedException) {
                exception = (WorkException) throwable;
            } else if (throwable instanceof WorkException) {
                WorkException we = (WorkException) throwable;
                exception = new WorkRejectedException(we);
                exception.setErrorCode(we.getErrorCode());
            } else {
                exception = new WorkRejectedException(throwable);
                exception.setErrorCode(UNDEFINED);
            }
        } else {
            if (throwable instanceof WorkCompletedException) {
                exception = (WorkException) throwable;
            } else if (throwable instanceof WorkException) {
                WorkException we = (WorkException) throwable;
                exception = new WorkCompletedException(we);
                exception.setErrorCode(we.getErrorCode());
            } else {
                exception = new WorkCompletedException(throwable);
                exception.setErrorCode(UNDEFINED);
            }
        }
    }

    /**
     * Lock the thread upto the end of execution or start of work execution.
     */
    public void lock() {
        if (!lockRequired()) {
            return;
        }

        try {
            synchronized (lock) {
                while (checkStateBeforeLocking()) {
                    if (timeout != -1) {
                        lock.wait(timeout);
                    } else {
                        lock.wait();
                    }
                }
            }

            if (getState() < STARTED) {
                workTimedOut();
            }

            if (lockRequired()) {
                synchronized (lock) {
                    if (checkStateBeforeLocking()) {
                        lock.wait();
                    }
                }
            }

        } catch (Exception e) {
            setException(e);
        }
    }

    /**
     * Unlocks the thread.
     */
    private void unLock() {
        try {
            synchronized (lock) {
                lock.notifyAll();
            }
        } catch (Exception e) {
            setException(e);
        }
    }

    /**
     * Returns the string representation of WorkCoordinator.
     *
     * @return Unique identification concatenated by work object.
     */
    @Override
    public String toString() {
        return id + ":" + work;
    }

    /**
     * Sets the state of the work coordinator object
     *
     * @param state CREATED or Either STARTED or COMPLETED or TIMEDOUT
     */
    public synchronized void setState(int state) {
        this.state = state;
    }

    /**
     * Retrieves the state of the work coordinator object.
     *
     * @return Integer represnting the state.
     */
    public synchronized int getState() {
        return state;
    }

    private boolean lockRequired() {
        if (!proceed()) {
            return false;
        }

        if (waitMode == NO_WAIT) {
            return false;
        }

        if (waitMode == WAIT_UNTIL_FINISH) {
            return getState() < COMPLETED;
        }

        if (waitMode == WAIT_UNTIL_START) {
            return getState() < STARTED;
        }

        return false;
    }

    /**
     * It is possible that state is modified just before the lock is obtained. So check it again. Access the variable
     * directly to avoid nested locking.
     */
    private boolean checkStateBeforeLocking() {
        if (waitMode == WAIT_UNTIL_FINISH) {
            return state < COMPLETED;
        }

        if (waitMode == WAIT_UNTIL_START) {
            return state < STARTED;
        }

        return false;
    }

    private JavaEETransactionManager getTransactionManager() {
        return runtime.getTransactionManager();
    }

    public static ExecutionContext getExecutionContext(ExecutionContext executionContext, Work work) {
        if (executionContext == null) {
            return WorkContextHandlerImpl.getExecutionContext(work);
        }

        return executionContext;
    }

    public static synchronized int increaseSeed() {
        return ++seed;
    }

}
