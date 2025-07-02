/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.event.AdminCommandEventBrokerImpl;
import com.sun.enterprise.v3.admin.CheckpointHelper.CheckpointFilename;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.System.Logger;
import java.util.List;
import java.util.Objects;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.JobManager;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.services.common.SubjectUtil;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Represents running (or finished) command instance.
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */
public final class AdminCommandJob implements Job, Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = System.getLogger(AdminCommandJob.class.getName());

    private CommandProgress commandProgress;

    private final String id;
    private final String commandName;
    private final String scope;
    private final ActionReport actionReport;
    private final long executionDate;
    private final boolean managedJob;
    private final List<String> subjectUsernames;
    private final ParameterMap parameters;

    private volatile State state;

    private File jobsFile;
    private long completionDate;
    private boolean failToRetryable;

    private transient Payload.Outbound payload;
    private transient AdminCommandEventBroker broker;

    protected AdminCommandJob(String name, String scope, Subject sub, boolean managedJob, ParameterMap parameters, ActionReport actionReport) {
        this(null, name, scope, sub, managedJob, parameters, actionReport);
    }

    protected AdminCommandJob(String id, String name, String commandScope, Subject sub, boolean managedJob, ParameterMap parameters, ActionReport actionReport) {
        this.id = id;
        this.actionReport = Objects.requireNonNull(actionReport, "actionReport");
        this.state = State.PREPARED;
        this.broker = new AdminCommandEventBrokerImpl();
        this.executionDate = System.currentTimeMillis();
        this.commandName = name;
        this.scope= commandScope;
        this.managedJob = managedJob;
        this.subjectUsernames = SubjectUtil.getUsernamesFromSubject(sub);
        this.parameters = parameters;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    @Override
    public final ActionReport getActionReport() {
        return this.actionReport;
    }

    @Override
    public final State getState() {
        return this.state;
    }

    @Override
    public CommandProgress getCommandProgress() {
        return commandProgress;
    }

    @Override
    public void setCommandProgress(CommandProgress commandProgress) {
        this.commandProgress = commandProgress;
        this.commandProgress.setEventBroker(broker);
    }

    @Override
    public final AdminCommandEventBroker getEventBroker() {
        return this.broker;
    }

    @Override
    public File getJobsFile() {
        return jobsFile;
    }

    @Override
    public void setJobsFile(File jobsFile) {
        this.jobsFile = jobsFile;
    }

    @Override
    public List<String> getSubjectUsernames() {
        return subjectUsernames;
    }

    @Override
    public String getName() {
        return commandName;
    }

    /**
     * Sets the state and fires the event - state change which runs listeners.
     *
     * @param state must not be null.
     */
    @Override
    public final void setState(State state) {
        Objects.requireNonNull(state, "state");
        if (!State.isAllowedTransition(this.state, state)) {
            throw new IllegalStateException("Illegal state transition: " + this.state + " -> " + state);
        }
        LOG.log(DEBUG, "Job state changed: {0} -> {1}, original this: {2}", this.state, state, this);
        this.state = state;
        getEventBroker().fireEvent(EVENT_STATE_CHANGED, this);
    }

    @Override
    public boolean isOutboundPayloadEmpty() {
        return payload == null || payload.size() == 0;
    }

    @Override
    public void complete(ActionReport report, Payload.Outbound outbound) {
        LOG.log(DEBUG, "complete(report={0}, outbound={1})", report, outbound);
        this.payload = outbound;
        this.completionDate = System.currentTimeMillis();
        if (!managedJob) {
            setState(State.COMPLETED);
            if (commandProgress != null && report.getActionExitCode() == ExitCode.SUCCESS) {
                commandProgress.complete();
            }
            return;
        }
        final ServiceLocator serviceLocator = Globals.getDefaultHabitat();
        final State originalState = getState();
        final JobManagerService jobManager = serviceLocator.getService(JobManagerService.class);
        if (originalState.equals(State.RUNNING_RETRYABLE) && failToRetryable) {
            LOG.log(WARNING, "Failed to retry: {0}", this);
            jobManager.getRetryableJobsInfo().put(getId(), CheckpointFilename.createBasic(this));
            jobManager.purgeJob(getId());
            setState(State.FAILED_RETRYABLE);
            return;
        }
        final State finalState = originalState.equals(State.REVERTING) ? State.REVERTED : State.COMPLETED;
        final String user = subjectUsernames.isEmpty() ? null : subjectUsernames.get(0);
        final JobInfo jobInfo = new JobInfo(getId(), commandName, executionDate, report.getActionExitCode().name(),
            user, report.getMessage(), getJobsFile(), finalState.name(), completionDate);
        jobManager.moveToCompletedJobs(jobInfo);
        if (originalState.equals(State.RUNNING_RETRYABLE) || originalState.equals(State.REVERTING)) {
            File jobFile = getJobsFile();
            if (jobFile == null) {
                jobFile = serviceLocator.getService(DefaultJobManagerFile.class).getFile();
            }
            jobManager.deleteCheckpoint(jobFile.getParentFile(), getId());
        }
        setState(finalState);
        if (commandProgress != null && report.getActionExitCode() == ExitCode.SUCCESS) {
            commandProgress.complete();
        }
        LOG.log(INFO, "Completed: {0}", this);
    }

    @Override
    public void revert() {
        setState(State.REVERTING);
    }

    @Override
    public long getCommandExecutionDate ()  {
        return executionDate;
    }

    @Override
    public Payload.Outbound getPayload() {
        return payload;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public long getCommandCompletionDate() {
        return completionDate;
    }

    @Override
    public void setFailToRetryable(boolean value) {
        this.failToRetryable = value;
    }

    @Override
    public ParameterMap getParameters() {
        return parameters;
    }


    /**
     * @return managed jobs are managed by the {@link JobManager}. That means that they have unique
     *         id (used, unused ids may be recycled) and usually are executed in own thread.
     */
    public boolean isManaged() {
        return managedJob;
    }

    @Override
    public String toString() {
        return super.toString() + "[id=" + id + ", name=" + commandName + ", state=" + state + ", report="
            + actionReport + "]";
    }


    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Lazy loaded fields
        this.payload = null;
        this.broker = null;
    }
}
