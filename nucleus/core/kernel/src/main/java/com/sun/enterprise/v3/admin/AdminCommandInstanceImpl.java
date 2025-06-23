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
import com.sun.enterprise.admin.remote.AdminCommandStateImpl;
import com.sun.enterprise.v3.admin.CheckpointHelper.CheckpointFilename;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.System.Logger;
import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.progress.JobInfo;
import org.glassfish.api.admin.progress.JobPersistence;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.services.common.SubjectUtil;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Represents running (or finished) command instance.
 *
 * @author Martin Mares
 * @author Bhakti Mehta
 */
public class AdminCommandInstanceImpl extends AdminCommandStateImpl implements Job {

    private static final long serialVersionUID = 1L;
    private static final Logger LOG = System.getLogger(AdminCommandInstanceImpl.class.getName());

    private CommandProgress commandProgress;
    private transient Payload.Outbound payload;
    private transient AdminCommandEventBroker broker;

    private final long executionDate;

    private final String commandName;

    private List<String> subjectUsernames;

    private final String scope;

    private boolean isManagedJob;

    private File jobsFile;

    private long completionDate;

    private ParameterMap parameters;

    private boolean failToRetryable;

    protected AdminCommandInstanceImpl(String id, String name, String commandScope, Subject sub, boolean managedJob, ParameterMap parameters) {
        super(id);
        this.broker = new AdminCommandEventBrokerImpl();
        this.executionDate = System.currentTimeMillis();
        this.commandName = name;
        this.scope= commandScope;
        this.isManagedJob = managedJob;
        this.subjectUsernames = SubjectUtil.getUsernamesFromSubject(sub);
        this.parameters = parameters;
    }

    protected AdminCommandInstanceImpl(String name, String scope, Subject sub, boolean managedJob, ParameterMap parameters) {
        this(null, name, scope, sub, managedJob, parameters);
    }

    @Override
    public CommandProgress getCommandProgress() {
        return commandProgress;
    }

    @Override
    public void setCommandProgress(CommandProgress commandProgress) {
        this.commandProgress = commandProgress;
        commandProgress.setEventBroker(broker);
    }

    @Override
    public AdminCommandEventBroker getEventBroker() {
        return this.broker;
    }

    public void setEventBroker(AdminCommandEventBroker eventBroker) {
        this.broker = eventBroker;
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

    @Override
    protected void setState(State state) {
        if (state != getState()) {
            super.setState(state);
            getEventBroker().fireEvent(EVENT_STATE_CHANGED, this);
        }
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
        if (!isManagedJob) {
            setState(State.COMPLETED, report);
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
            setState(State.FAILED_RETRYABLE, report);
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
        LOG.log(TRACE, "Completed: {0}", this);
        setState(finalState, report);
        if (commandProgress != null && report.getActionExitCode() == ExitCode.SUCCESS) {
            commandProgress.complete();
        }
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

    private void readObject(ObjectInputStream in) throws IOException,ClassNotFoundException {
        in.defaultReadObject();
        // Lazy loaded fields
        this.payload = null;
        this.broker = null;
    }

    private JobPersistence getJobPersistenceService(final ServiceLocator serviceLocator) {
        if (scope == null) {
            return serviceLocator.getService(JobPersistenceService.class);
        }
        return serviceLocator.getService(JobPersistence.class, scope + "job-persistence");
    }
}
