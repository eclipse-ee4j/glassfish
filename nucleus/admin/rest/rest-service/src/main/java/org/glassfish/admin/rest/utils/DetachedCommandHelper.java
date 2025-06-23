/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.v3.admin.JobManagerService;
import com.sun.enterprise.v3.admin.RunnableAdminCommandListener;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.internal.api.Globals;

import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;

/**
 *
 * @author jdlee
 */
public class DetachedCommandHelper extends RunnableAdminCommandListener {

    private volatile String jobId;
    private volatile ActionReport report;

    private DetachedCommandHelper(final CommandInvocation commandInvocation) {
        super(commandInvocation);
    }

    @Override
    public void processCommandEvent(final String name, Object event) {
        if (!EVENT_STATE_CHANGED.equals(name)) {
            return;
        }
        AdminCommandState acs = (AdminCommandState) event;
        jobId = acs.getId();
        report = acs.getActionReport();
    }

    @Override
    protected void finalizeRun() {
        // Nothing to do here.
    }

    public static String invokeAsync(CommandInvocation commandInvocation) {
        if (commandInvocation == null) {
            throw new IllegalArgumentException("commandInvocation");
        }
        DetachedCommandHelper helper = new DetachedCommandHelper(commandInvocation);
        JobManagerService jobManagerService = Globals.getDefaultHabitat().getService(JobManagerService.class);
        jobManagerService.startAsyncListener(helper);
        helper.awaitFinish();
        if (helper.report == null) {
            return helper.jobId;
        }
        throw new IllegalStateException("Failed to schedule detached job: " + helper.report.getMessage(),
            helper.report.getFailureCause());
    }
}
