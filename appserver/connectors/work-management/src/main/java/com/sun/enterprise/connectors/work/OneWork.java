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

import jakarta.resource.spi.work.Work;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * Represents one piece of work that will be submitted to the workqueue.
 *
 * @author Binod P.G
 */
public final class OneWork implements com.sun.corba.ee.spi.threadpool.Work {

    @LogMessageInfo(
            message = "The Work named [ {0} ], progress [ {1} ].",
            comment = "Print Work status",
            level = "INFO",
            publish = false)
    private static final String RAR_WORK_PROGRESS_INFO = "AS-RAR-05004";

    private final Work work;
    private final WorkCoordinator coordinator;
    private long nqTime;
    private static final Logger logger = LogFacade.getLogger();

    private String name = "Resource adapter work";
    private boolean nameSet = false;

    // Store the client's TCC so that when the work is executed,
    // TCC is set appropriately.
    private ClassLoader tcc;

    /**
     * Creates a work object that can be submitted to a workqueue.
     *
     * @param work Actual work submitted by Resource adapter.
     * @param coordinator <code>WorkCoordinator</code> object.
     */
    OneWork(Work work, WorkCoordinator coordinator, ClassLoader tcc) {
        this.work = work;
        this.coordinator = coordinator;
        this.tcc = tcc;
    }

    /**
     * This method is executed by thread pool as the basic work operation.
     */
    @Override
    public void doWork() {
        ClassLoader callerCL = Thread.currentThread().getContextClassLoader();
        if (tcc != null && tcc != callerCL) {
            Thread.currentThread().setContextClassLoader(tcc);
        }

        try {
            coordinator.preInvoke(); // pre-invoke will set work state to "started",
            boolean timedOut = coordinator.isTimedOut();

            // validation of work context should be after this
            // so as to throw WorkCompletedException in case of error.
            if (coordinator.proceed()) {
                try {
                    coordinator.setupContext(this);
                    // work-name will be set (if specified via HintsContext "javax.resources.spi.HintsContext.NAME_HINT")
                    log("Start of Work");
                } catch (Throwable e) {
                    coordinator.setException(e);
                }
            }

            // there may be failure in context setup
            if (coordinator.proceed()) {
                try {
                    work.run();
                    log("Work Executed");
                } catch (Throwable t) {
                    log("Execution has thrown exception " + t.getMessage());
                    coordinator.setException(t);
                }
            }

            if (!timedOut) {
                coordinator.postInvoke();
            }
            log("End of Work");
        } finally {
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            if (cl != callerCL) {
                Thread.currentThread().setContextClassLoader(callerCL);
            }
            tcc = null;
        }
    }

    public void log(String message) {
        if (nameSet) {
            Object args[] = new Object[] { name, message };
            logger.log(Level.INFO, RAR_WORK_PROGRESS_INFO, args);
        }
    }

    /**
     * Time at which this work is enqueued.
     *
     * @param tme Time in milliseconds.
     */
    @Override
    public void setEnqueueTime(long tme) {
        this.nqTime = tme;
    }

    /**
     * Retrieves the time at which this work is enqueued
     *
     * @return Time in milliseconds.
     */
    @Override
    public long getEnqueueTime() {
        return nqTime;
    }

    /**
     * Retrieves the name of the work.
     *
     * @return Name of the work.
     */
    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        nameSet = true;
    }

    /**
     * Retrieves the string representation of work.
     *
     * @return String representation of work.
     */
    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();
        if (nameSet) {
            result.append("[Work : " + name + "] ");
        }
        result.append(work.toString());
        return result.toString();
    }
}
