/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.util.AnnotationUtil;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.AdminCommandEventBroker.AdminCommandListener;
import org.glassfish.api.admin.AdminCommandState.State;
import org.glassfish.api.admin.CommandParameters;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.CommandSupport;
import org.glassfish.api.admin.JobCreator;
import org.glassfish.api.admin.JobManager;
import org.glassfish.api.admin.ManagedJob;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.ProgressStatus;

import static org.glassfish.api.admin.AdminCommandState.State.REVERTING;
import static org.glassfish.api.admin.AdminCommandState.State.RUNNING_RETRYABLE;

/*
 * Some private classes used in the implementation of CommandRunner.
 */
/**
 * ExecutionContext is a CommandInvocation, which
 * defines a command excecution context like the requested
 * name of the command to execute, the parameters of the command, etc.
 */
final class CommandRunnerExecutionContext implements CommandInvocation {

    private final CommandRunnerImpl commandRunner;

    private String scope;
    private String name;
    private ActionReport report;
    private ParameterMap params;
    private CommandParameters paramObject;
    private Payload.Inbound inbound;
    private Payload.Outbound outbound;
    private Subject subject;
    private ProgressStatus progressStatus;
    private boolean isManagedJob;
    private boolean isNotify;
    private final List<MatchedListener> listeners = new ArrayList<>();

    CommandRunnerExecutionContext(String scope, String name, ActionReport report, Subject subject, boolean isNotify, final CommandRunnerImpl commandRunner) {
        this.commandRunner = commandRunner;
        this.scope = scope;
        this.name = name;
        this.report = report;
        this.subject = subject;
        this.isNotify = isNotify;
    }

    @Override
    public CommandInvocation parameters(CommandParameters paramObject) {
        this.paramObject = paramObject;
        return this;
    }

    @Override
    public CommandInvocation parameters(ParameterMap params) {
        this.params = params;
        return this;
    }

    @Override
    public CommandInvocation inbound(Payload.Inbound inbound) {
        this.inbound = inbound;
        return this;
    }

    @Override
    public CommandInvocation outbound(Payload.Outbound outbound) {
        this.outbound = outbound;
        return this;
    }

    @Override
    public CommandInvocation listener(String nameRegexp, AdminCommandListener listener) {
        listeners.add(new MatchedListener(nameRegexp, listener));
        return this;
    }

    @Override
    public CommandInvocation progressStatusChild(ProgressStatus ps) {
        this.progressStatus = ps;
        return this;
    }

    @Override
    public CommandInvocation managedJob() {
        this.isManagedJob = true;
        return this;
    }

    @Override
    public void execute() {
        execute(null);
    }

    ParameterMap parameters() {
        return params;
    }

    CommandParameters typedParams() {
        return paramObject;
    }

    String name() {
        return name;
    }

    private String scope() {
        return scope;
    }

    @Override
    public ActionReport report() {
        return report;
    }

    void setReport(ActionReport report) {
        this.report = report;
    }

    Payload.Inbound inboundPayload() {
        return inbound;
    }

    Payload.Outbound outboundPayload() {
        return outbound;
    }

    ProgressStatus progressStatus() {
        return progressStatus;
    }

    void executeFromCheckpoint(JobManager.Checkpoint checkpoint, boolean revert, AdminCommandEventBroker eventBroker) {
        if (subject == null) {
            subject = checkpoint.getContext().getSubject();
        }
        AdminCommandJob job = (AdminCommandJob) checkpoint.getJob();
        parameters(job.getParameters());
        AdminCommandContext context = checkpoint.getContext();
        this.report = context.getActionReport();
        this.inbound = context.getInboundPayload();
        this.outbound = context.getOutboundPayload();
        this.scope = job.getScope();
        this.name = job.getName();
        if (eventBroker == null) {
            eventBroker = job.getEventBroker() == null ? new AdminCommandEventBrokerImpl() : job.getEventBroker();
        }
        job.setEventBroker(eventBroker);
        job.setState(revert ? REVERTING : RUNNING_RETRYABLE);
        JobManager jobManager = commandRunner.serviceLocator.getService(JobManagerService.class);
        jobManager.registerJob(job);

        AdminCommand command = checkpoint.getCommand();
        if (command == null) {
            command = commandRunner.getCommand(job.getScope(), job.getName(), report());
            if (command == null) {
                return;
            }
        }

        commandRunner.doCommand(this, command, subject, job);
        job.complete(report(), outboundPayload());
        if (progressStatus != null) {
            progressStatus.complete();
        }
        CommandSupport.done(commandRunner.serviceLocator, command, job);
    }

    @Override
    public void execute(AdminCommand command) {
        if (command == null) {
            command = commandRunner.getCommand(scope(), name(), report());
            if (command == null) {
                return;
            }
        }
        /*
         * The caller should have set the subject explicitly.  In case
         * it didn't, try setting it from the current access controller context
         * since the command framework will have set that before invoking
         * the original command's execute method.
         */
        if (subject == null) {
            subject = AccessController.doPrivileged(new PrivilegedAction<Subject>() {
                @Override
                public Subject run() {
                    return Subject.getSubject(AccessController.getContext());
                }
            });
        }
        if (!isManagedJob) {
            isManagedJob = AnnotationUtil.presentTransitive(ManagedJob.class, command.getClass());
        }
        JobCreator jobCreator = commandRunner.serviceLocator.getService(JobCreator.class, scope + "job-creator");
        JobManager jobManager = commandRunner.serviceLocator.getService(JobManagerService.class);
        if (jobCreator == null) {
            jobCreator = commandRunner.serviceLocator.getService(JobCreatorService.class);
        }
        final AdminCommandJob job;
        if (isManagedJob) {
            job = (AdminCommandJob) jobCreator.createJob(jobManager.getNewId(), scope(), name(), subject,
                isManagedJob, parameters());
        } else {
            job = (AdminCommandJob) jobCreator.createJob(null, scope(), name(), subject, isManagedJob,
                parameters());
        }
        // Register the listeners else the detach functionality will not work
        for (MatchedListener listener : listeners) {
            job.getEventBroker().registerListener(listener.nameRegexp, listener.listener);
        }
        job.setState(State.RUNNING);
        if (isManagedJob) {
            jobManager.registerJob(job);
        }
        commandRunner.doCommand(this, command, subject, job);
        job.complete(report(), outboundPayload());
        if (progressStatus != null) {
            progressStatus.complete();
        }
        CommandSupport.done(commandRunner.serviceLocator, command, job, isNotify);
    }

    private class MatchedListener {

        private final String nameRegexp;
        private final AdminCommandListener listener;

        private MatchedListener(String nameRegexp, AdminCommandListener listener) {
            this.nameRegexp = nameRegexp;
            this.listener = listener;
        }
    }
}
