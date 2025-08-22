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
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;
import static org.glassfish.api.admin.AdminCommandState.State.COMPLETED;
import static org.glassfish.api.admin.AdminCommandState.State.REVERTED;
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
@AccessRequired(resource = "jobs/job/$jobID", action = "attach")
public class AttachCommand implements AdminCommand, AdminCommandListener<Object> {
    /** Command name: attach */
    // Must be public to be used in annotations
    public static final String COMMAND_NAME = "attach";
    private static final LocalStringManagerImpl strings = new LocalStringManagerImpl(AttachCommand.class);
    private static final Logger LOG = System.getLogger(AttachCommand.class.getName());

    @Inject
    private JobManagerService jobManagerService;
    @Inject
    private DefaultJobManagerFile defaultJobManagerFile;

    @Param(primary = true, optional = false, multiple = false)
    private String jobID;
    @Param(optional = true)
    private Integer timeout;

    private AdminCommandEventBroker<?> eventBroker;
    private Job detachedJob;

    @Override
    public void execute(AdminCommandContext context) {
        eventBroker = context.getEventBroker();
        detachedJob = jobManagerService.get(jobID);
        final String attachedUser = SubjectUtil.getUsernamesFromSubject(context.getSubject()).get(0);
        final ActionReport report = context.getActionReport();
        if (detachedJob == null) {
            LOG.log(TRACE, "Trying to find completed job id: {0}", jobID);
            JobInfo jobInfo = jobManagerService.getCompletedJobForId(jobID, defaultJobManagerFile.getFile());
            attachCompleted(jobInfo, attachedUser, report);
        } else {
            attachRunning(attachedUser, report);
        }
    }

    @Override
    public void onAdminCommandEvent(String eventName, Object event) {
        LOG.log(TRACE, "onAdminCommandEvent(eventName={0}, event={1})", eventName, event);
        // Skip nonsense or own events
        if (eventName == null || eventName.startsWith("client.")) {
            return;
        }
        // Distribute to listeners of the attach job too.
        eventBroker.fireEvent(eventName, event);
        // on any update check the state of the detached job.
        if (!detachedJob.isJobStillActive()) {
            synchronized (detachedJob) {
                detachedJob.notifyAll();
            }
        }
    }

    private void attachCompleted(JobInfo jobInfo, String attachedUser, ActionReport report) {
        LOG.log(DEBUG, "attachCompleted(jobInfo={0}, attachedUser={1}, report={2})", jobInfo, attachedUser, report);
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
        LOG.log(DEBUG, "attachRunning(attachedUser={0}, report={1})", attachedUser, report);
        if (detachedJob == null || isInvisibleJob(detachedJob.getName())) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(
                strings.getLocalString("attach.wrong.commandinstance.id", "Job with id {0} does not exist.", jobID));
            return;
        }
        final String jobInitiator = detachedJob.getSubjectUsernames().get(0);
        if (!attachedUser.equals(jobInitiator)) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(strings.getLocalString("user.not.authorized",
                "User {0} not authorized to attach to job {1}", attachedUser, jobID));
            return;
        }
        // Send current state of the job
        eventBroker.fireEvent(EVENT_STATE_CHANGED, detachedJob);
        final CommandProgress commandProgress = detachedJob.getCommandProgress();
        if (commandProgress != null) {
            eventBroker.fireEvent(EVENT_PROGRESSSTATUS_STATE, commandProgress);
        }
        // Tell job's broker that we are watching it
        detachedJob.getEventBroker().registerListener(".*", this);
        LOG.log(TRACE, "Waiting until job {0} is finished.", detachedJob);
        synchronized (detachedJob) {
            if (detachedJob.isJobStillActive()) {
                try {
                    if (timeout == null) {
                        detachedJob.wait();
                    } else {
                        detachedJob.wait(timeout * 1000L);
                        if (detachedJob.isJobStillActive()) {
                            LOG.log(WARNING, "Job {0} is still in state {1} after timeout {1} seconds.",
                                detachedJob.getName(), detachedJob.getState(), timeout);
                            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                            report.setMessage(strings.getLocalString("attach.timeout",
                                "Waiting for job {0} timed out after {1} seconds.", detachedJob.getName(), timeout));
                            return;
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            LOG.log(DEBUG, "Finished waiting for job {0}", detachedJob);
            if (COMPLETED.equals(detachedJob.getState()) || REVERTED.equals(detachedJob.getState())) {
                report.setActionExitCode(detachedJob.getActionReport().getActionExitCode());
                report.appendMessage(strings.getLocalString("attach.finished", "Command {0} executed with status {1}",
                    detachedJob.getName(), detachedJob.getActionReport().getActionExitCode()));
            }
        }
    }

    private boolean isInvisibleJob(String name) {
        return name.startsWith("_") || COMMAND_NAME.equals(name);
    }
}
