/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.utils;

import com.sun.enterprise.v3.admin.AsyncAdminCommandInvoker;
import com.sun.enterprise.v3.admin.JobManagerService;

import java.lang.System.Logger;
import java.util.concurrent.CountDownLatch;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ProgressEvent;
import org.glassfish.internal.api.Globals;

import static java.lang.System.Logger.Level.WARNING;

/**
 *
 * @author jdlee
 */
public final class JobIdAsyncAdminCommandInvoker extends AsyncAdminCommandInvoker {
    private static final Logger LOG = System.getLogger(JobIdAsyncAdminCommandInvoker.class.getName());

    private final CountDownLatch waitForJobId = new CountDownLatch(1);
    private volatile String jobId;
    private volatile ActionReport report;


    public JobIdAsyncAdminCommandInvoker(final CommandInvocation commandInvocation) {
        super(commandInvocation);
    }

    @Override
    protected void onStateChangeEvent(final String eventName, final AdminCommandState state) {
        jobId = state.getId();
        if (jobId != null) {
            getBroker().unregisterListener(this);
        }
        report = state.getActionReport();
        waitForJobId.countDown();
    }

    @Override
    protected void onStateChangeEvent(String eventName, ProgressEvent state) {
        // We ignore these events.
    }

    /**
     * Starts the job and waits until the job reports its job id.
     *
     * @return jobId
     * @throws IllegalStateException when instead of jobId we received an exception.
     */
    public String start() throws IllegalStateException {
        Globals.getDefaultHabitat().getService(JobManagerService.class).start(this);
        try {
            waitForJobId.await();
        } catch (InterruptedException e) {
            LOG.log(WARNING, "Async command interrupted!", e);
            Thread.currentThread().interrupt();
        }
        if (report == null) {
            return jobId;
        }
        throw new IllegalStateException("Failed to schedule async command: " + report.getMessage(),
            report.getFailureCause());
    }
}
