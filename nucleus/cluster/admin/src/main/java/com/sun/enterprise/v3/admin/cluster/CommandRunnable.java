/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import java.util.concurrent.ArrayBlockingQueue;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandInvocation;

/**
 * This class wraps a CommandInvocation so that it can be run via a
 * thread pool. On construction you pass it the CommandInvocation
 * to execute as well as a response queue and the ActionReport
 * that was set on the CommandInvocation. When the run() method
 * is called the CommandInvocation is executed (which sets its results
 * in the ActionReport) and then it adds itself to the response queue
 * where it can be picked up and the ActionReport inspected for the results.
 *
 * @author dipol
 */
final class CommandRunnable implements Runnable {

    private String name;
    private CommandInvocation ci;
    private ActionReport report;
    private ArrayBlockingQueue<CommandRunnable> responseQueue;

    /**
     * Construct a CommandRunnable. This class wraps a CommandInvocation
     * so that it can be executed via a thread pool.
     *
     * @param name used for toString()
     * @param ci A CommandInvocation containing the command you want to run.
     * @param report The ActionReport you used with the CommandInvocation
     * @param responseQueue A blocking queue that this class will add itself to
     *            when its run method has completed.
     *            After dispatching this class to a thread pool the caller can block
     *            on the response queue where it will dequeue CommandRunnables and then
     *            use the getActionReport() method to retrieve the results.
     */
    public CommandRunnable(String name, CommandInvocation ci, ActionReport report,
        ArrayBlockingQueue<CommandRunnable> responseQueue) {
        this.name = name;
        this.report = report;
        this.ci = ci;
        this.responseQueue = responseQueue;
    }

    @Override
    public void run() {
        ci.execute();
        responseQueue.add(this);
    }

    /**
     * Get the name that was previously set.
     *
     * @return  A name that was previously set or null if no name was set.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the ActionReport that was passed on the constructor.
     *
     * @return  the ActionReport that was passed on the constructor.
     */
    public ActionReport getActionReport() {
        return report;
    }

    @Override
    public String toString() {
        return name;
    }
}
