/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.admin;

import java.lang.System.Logger;
import java.util.Objects;

import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.CommandInvocation;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Asynchronous admin command lister running in a separate thread.
 */
public abstract class AsyncAdminCommandInvoker<T> {
    private static final Logger LOG = System.getLogger(AsyncAdminCommandInvoker.class.getName());

    private final CommandInvocation<AdminCommandJob> commandInvocation;
    private final AdminCommand command;
    private final AdminCommandJob job;


    /**
     * @param commandInvocation must not be null
     */
    protected AsyncAdminCommandInvoker(CommandInvocation<AdminCommandJob> commandInvocation) {
        this.commandInvocation = Objects.requireNonNull(commandInvocation, "commandInvocation");
        this.command = Objects.requireNonNull(this.commandInvocation.evaluateCommand(), "command");
        this.job = this.commandInvocation.createJob(command);
    }


    /**
     * Prepares the invocation and job, starts it, then returns desired product.
     *
     * @return Product of this class.
     */
    public abstract T start();

    /**
     * @return job managed by this invoker.
     */
    protected AdminCommandJob getJob() {
        return job;
    }

    /**
     * Asynchronously starts this instance.
     */
    protected void startJob() {
        LOG.log(TRACE, () -> "Starting the job by " + this);
        commandInvocation.start(command, job);
    }

    @Override
    public String toString() {
        return super.toString() + "[job=" + job + "]";
    }
}
