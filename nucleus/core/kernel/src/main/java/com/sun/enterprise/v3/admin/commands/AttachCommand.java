/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.admin.DefaultJobManagerFile;
import com.sun.enterprise.v3.admin.JobManagerService;

import jakarta.inject.Inject;

import java.lang.System.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.AdminCommandEventBroker.AdminCommandListener;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.security.services.common.SubjectUtil;
import org.jvnet.hk2.annotations.Service;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;
import static org.glassfish.api.admin.AdminCommandState.State.COMPLETED;
import static org.glassfish.api.admin.AdminCommandState.State.PREPARED;
import static org.glassfish.api.admin.AdminCommandState.State.REVERTED;
import static org.glassfish.api.admin.AdminCommandState.State.RUNNING;
import static org.glassfish.api.admin.AdminCommandState.State.RUNNING_RETRYABLE;
import static org.glassfish.api.admin.CommandProgress.EVENT_PROGRESSSTATUS_STATE;


/**
 * Gets CommandInstance from registry based on given id and forwards all events.
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */
@Service(name = AttachCommand.COMMAND_NAME)
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n(AttachCommand.COMMAND_NAME)
@ManagedJob
@AccessRequired(resource="jobs/job/$jobID", action="attach")
public class AttachCommand implements AdminCommand, AdminCommandListener {
    /** Command name: attach */
    // Must be public to be used in annotations
    public static final String COMMAND_NAME = "attach";
    private static final LocalStringManagerImpl strings = new LocalStringManagerImpl(AttachCommand.class);
    private static final Logger LOG = System.getLogger(AttachCommand.class.getName());

    @Inject
    private JobManagerService registry;
    @Inject
    private DefaultJobManagerFile defaultJobManagerFile;

    @Param(primary = true, optional = false, multiple = false)
    private String jobID;
    @Param(optional = true)
    private Integer timeout;

    private AdminCommandEventBroker<?> eventBroker;
    private Job job;

    @Override
    public void execute(AdminCommandContext context) {
        eventBroker = context.getEventBroker();
        job = registry.get(jobID);
        final String attachedUser = SubjectUtil.getUsernamesFromSubject(context.getSubject()).get(0);
        final ActionReport report = context.getActionReport();
        if (job == null) {
            LOG.log(TRACE, "Trying to find completed job id: {0}", jobID);
            JobInfo jobInfo = registry.getCompletedJobForId(jobID, defaultJobManagerFile.getFile());
            attachCompleted(jobInfo, attachedUser, report);
        } else {
            attachRunning(attachedUser, report);
        }
    }

    private void attachCompleted(JobInfo jobInfo, String attachedUser, ActionReport report) {
        if (jobInfo == null || isInvisibleJob(jobInfo.jobName)) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(
                strings.getLocalString("attach.wrong.commandinstance.id", "Job with id {0} does not exist.", jobID));
            return;
        }
        if (!jobInfo.user.equals(attachedUser)) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(strings.getLocalString("user.not.authorized",
                "User {0} not authorized to attach to job {1}", attachedUser, jobID));
            return;
        }
        if (jobInfo.state.equals(COMPLETED.toString()) || jobInfo.state.equals(REVERTED.toString())) {
            // In most cases if the user who attaches to the command is the same
            // as one who started it then purge the job once it is completed
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            report.appendMessage(strings.getLocalString("attach.finished", "Command {0} executed with status {1}",
                jobInfo.jobName, jobInfo.exitCode));
        }
    }

    private void attachRunning(String attachedUser, ActionReport report) {
        if (job == null || isInvisibleJob(job.getName())) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(
                strings.getLocalString("attach.wrong.commandinstance.id", "Job with id {0} does not exist.", jobID));
            return;
        }
        final String jobInitiator = job.getSubjectUsernames().get(0);
        if (!attachedUser.equals(jobInitiator)) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(strings.getLocalString("user.not.authorized",
                "User {0} not authorized to attach to job {1}", attachedUser, jobID));
            return;
        }
        AdminCommandEventBroker<?> attachedBroker = job.getEventBroker();
        CommandProgress commandProgress = job.getCommandProgress();
        onAdminCommandEvent(EVENT_STATE_CHANGED, job);
        attachedBroker.registerListener(".*", this);
        if (commandProgress != null) {
            onAdminCommandEvent(EVENT_PROGRESSSTATUS_STATE, commandProgress);
        }
        LOG.log(TRACE, "Waiting until job {0} is finished.", job);
        synchronized (job) {
            while (isJobStillActive()) {
                try {
                    if (timeout == null) {
                        job.wait();
                    } else {
                        job.wait(timeout * 1000L);
                        if (isJobStillActive()) {
                            LOG.log(DEBUG, "Job {0} is still in state {1} after timeout {1} seconds.",
                                job.getName(), job.getState(), timeout);
                            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                            report.setMessage(strings.getLocalString("attach.timeout",
                                "Waiting for job {0} timed out after {1} seconds.", job.getName(), timeout));
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            LOG.log(TRACE, "Finished waiting for job {0}", job);
            if (COMPLETED.equals(job.getState()) || REVERTED.equals(job.getState())) {
                report.setActionExitCode(job.getActionReport().getActionExitCode());
                report.appendMessage(strings.getLocalString("attach.finished", "Command {0} executed with status {1}",
                    job.getName(), job.getActionReport().getActionExitCode()));
            }
        }
    }

    private boolean isJobStillActive() {
        return PREPARED.equals(job.getState())
            || RUNNING.equals(job.getState())
            || RUNNING_RETRYABLE.equals(job.getState());
    }

    @Override
    public void onAdminCommandEvent(String name, Object event) {
        LOG.log(TRACE, "onAdminCommandEvent(name={0}, event={1})", name, event);
        // Skip nonsense or own events
        if (name == null || name.startsWith("client.")) {
            return;
        }
        if (EVENT_STATE_CHANGED.equals(name)
            && (((Job) event).getState().equals(COMPLETED) || ((Job) event).getState().equals(REVERTED))) {
            synchronized (job) {
                LOG.log(DEBUG, "Notifying attached: {0}", job);
                job.notifyAll();
            }
        } else {
            // Forward
            eventBroker.fireEvent(name, event);
        }
    }


    private boolean isInvisibleJob(String name) {
        return name.startsWith("_") || COMMAND_NAME.equals(name);
    }
}
