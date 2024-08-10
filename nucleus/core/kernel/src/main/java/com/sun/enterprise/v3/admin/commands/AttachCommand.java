/*
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

import com.sun.enterprise.admin.remote.AdminCommandStateImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.v3.admin.JobManagerService;

import jakarta.inject.Inject;

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

import static org.glassfish.api.admin.AdminCommandState.State.COMPLETED;
import static org.glassfish.api.admin.AdminCommandState.State.PREPARED;
import static org.glassfish.api.admin.AdminCommandState.State.REVERTED;
import static org.glassfish.api.admin.AdminCommandState.State.RUNNING;
import static org.glassfish.api.admin.AdminCommandState.State.RUNNING_RETRYABLE;


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


    public static final String COMMAND_NAME = "attach";
    protected final static LocalStringManagerImpl strings = new LocalStringManagerImpl(AttachCommand.class);

    protected AdminCommandEventBroker eventBroker;
    protected Job attached;

    @Inject
    JobManagerService registry;

    @Param(primary=true, optional=false, multiple=false)
    protected String jobID;

    @Override
    public void execute(AdminCommandContext context) {
        eventBroker = context.getEventBroker();

        attached = registry.get(jobID);
        JobInfo jobInfo = null;
        String jobName = null;

        if (attached == null) {
            //try for completed jobs
            if (registry.getCompletedJobs(registry.getJobsFile()) != null) {
                jobInfo = (JobInfo) registry.getCompletedJobForId(jobID);
            }
            if (jobInfo != null) {
                jobName = jobInfo.jobName;
            }

        }

        attach(attached,jobInfo,context,jobName);

    }

    @Override
    public void onAdminCommandEvent(String name, Object event) {
        if (name == null || name.startsWith("client.")) { //Skip nonsence or own events
            return;
        }
        if (AdminCommandStateImpl.EVENT_STATE_CHANGED.equals(name) &&
                (((Job) event).getState().equals(COMPLETED) || ((Job) event).getState().equals(REVERTED))) {
            synchronized (attached) {
                attached.notifyAll();
            }
        } else {
            eventBroker.fireEvent(name, event); //Forward
        }
    }


    protected void purgeJob(String jobid) {
        try {
            registry.purgeJob(jobid);
            registry.purgeCompletedJobForId(jobid);
        } catch (Exception ex) {
        }
    }

    public void attach(Job attached, JobInfo jobInfo, AdminCommandContext context,String jobName) {
        ActionReport ar = context.getActionReport();
        String attachedUser = SubjectUtil.getUsernamesFromSubject(context.getSubject()).get(0);
        if ((attached == null && jobInfo == null) || (attached != null && attached.getName().startsWith("_"))
                || (attached != null && AttachCommand.COMMAND_NAME.equals(attached.getName()))) {
            ar.setActionExitCode(ActionReport.ExitCode.FAILURE);
            ar.setMessage(strings.getLocalString("attach.wrong.commandinstance.id", "Job with id {0} does not exist.", jobID));
            return;
        }

        if (attached != null) {
            String jobInitiator = attached.getSubjectUsernames().get(0);
            if (!attachedUser.equals( jobInitiator)) {
                ar.setActionExitCode(ActionReport.ExitCode.FAILURE);
                ar.setMessage(strings.getLocalString("user.not.authorized",
                        "User {0} not authorized to attach to job {1}", attachedUser, jobID));
                return;
            }
        }
        if (attached != null) {
            //Very sensitive locking part
            AdminCommandEventBroker attachedBroker = attached.getEventBroker();
            CommandProgress commandProgress = attached.getCommandProgress();
            if (commandProgress == null) {
                synchronized (attachedBroker) {
                    onAdminCommandEvent(AdminCommandStateImpl.EVENT_STATE_CHANGED, attached);
                    attachedBroker.registerListener(".*", this);
                }
            } else {
                synchronized (commandProgress) {
                    onAdminCommandEvent(AdminCommandStateImpl.EVENT_STATE_CHANGED, attached);
                    onAdminCommandEvent(CommandProgress.EVENT_PROGRESSSTATUS_STATE, attached.getCommandProgress());
                    attachedBroker.registerListener(".*", this);
                }
            }
            synchronized (attached) {
                while(attached.getState().equals(PREPARED) ||
                        attached.getState().equals(RUNNING) ||
                        attached.getState().equals(RUNNING_RETRYABLE)) {
                    try {
                        attached.wait(1000*60*5); //5000L just to be sure
                    } catch (InterruptedException ex) {}
                }
                if (attached.getState().equals(COMPLETED) || attached.getState().equals(REVERTED)) {
                    String commandUser = attached.getSubjectUsernames().get(0);
                    //In most cases if the user who attaches to the command is the same
                    //as one who started it then purge the job once it is completed
                    if ((commandUser != null && commandUser.equals(attachedUser)) && attached.isOutboundPayloadEmpty())  {
                        purgeJob(attached.getId());

                    }
                    ar.setActionExitCode(attached.getActionReport().getActionExitCode());
                    ar.appendMessage(strings.getLocalString("attach.finished", "Command {0} executed with status {1}",attached.getName(),attached.getActionReport().getActionExitCode()));
                }
            }
        } else {

            if (jobInfo != null && (jobInfo.state.equals(COMPLETED.toString()) || jobInfo.state.equals(REVERTED.toString()))) {

                //In most cases if the user who attaches to the command is the same
                //as one who started it then purge the job once it is completed
                if (attachedUser!= null && attachedUser.equals( jobInfo.user)) {
                    purgeJob(jobInfo.jobId);

                }
                ar.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                ar.appendMessage(strings.getLocalString("attach.finished", "Command {0} executed{1}",jobName,jobInfo.exitCode));
            }
        }
    }

}
