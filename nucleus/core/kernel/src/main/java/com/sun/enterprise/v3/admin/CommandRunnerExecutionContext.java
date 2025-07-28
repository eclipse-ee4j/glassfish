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

import com.sun.enterprise.util.AnnotationUtil;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Objects;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandState.State;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandParameters;
import org.glassfish.api.admin.JobManager.Checkpoint;
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
final class CommandRunnerExecutionContext implements CommandInvocation<AdminCommandJob> {

    private final CommandRunnerImpl commandRunner;

    private final String scope;
    private final String name;
    private final ActionReport report;
    private final Subject subject;
    private final boolean notify;
    private final boolean detach;

    private ParameterMap params;
    private CommandParameters paramObject;
    private Payload.Inbound inbound;
    private Payload.Outbound outbound;
    private ProgressStatus progressStatus;

    CommandRunnerExecutionContext(String scope, String name, ActionReport report, Subject subject, boolean notify,
        boolean detach, final CommandRunnerImpl commandRunner) {
        this.scope = scope;
        this.name = name;
        this.report = report;
        this.subject = evaluateSubject(subject);
        this.notify = notify;
        this.detach = detach;
        this.commandRunner = commandRunner;
    }

    @Override
    public CommandInvocation<AdminCommandJob> parameters(CommandParameters paramObject) {
        this.paramObject = paramObject;
        return this;
    }

    @Override
    public CommandInvocation<AdminCommandJob> parameters(ParameterMap params) {
        this.params = params;
        return this;
    }

    @Override
    public CommandInvocation<AdminCommandJob> inbound(Payload.Inbound inbound) {
        this.inbound = inbound;
        return this;
    }

    @Override
    public CommandInvocation<AdminCommandJob> outbound(Payload.Outbound outbound) {
        this.outbound = outbound;
        return this;
    }

    @Override
    public CommandInvocation<AdminCommandJob> progressStatus(ProgressStatus ps) {
        this.progressStatus = ps;
        return this;
    }

    @Override
    public ParameterMap parameters() {
        return params == null ? new ParameterMap() : params;
    }

    @Override
    public CommandParameters typedParams() {
        return paramObject;
    }

    @Override
    public String getCommandName() {
        return name;
    }

    @Override
    public ActionReport report() {
        return report;
    }

    @Override
    public Payload.Inbound inboundPayload() {
        return inbound;
    }

    @Override
    public Payload.Outbound outboundPayload() {
        return outbound;
    }

    @Override
    public ProgressStatus progressStatus() {
        return progressStatus;
    }

    @Override
    public boolean isDetached() {
        return detach;
    }

    @Override
    public void start(AdminCommand command, AdminCommandJob job) {
        commandRunner.getJobManager().start(() -> execute(command, job));
    }

    @Override
    public AdminCommandJob execute() {
        AdminCommand command = evaluateCommand();
        if (command == null) {
            // No need to throw an exception, the actionReport contains a user error message.
            return null;
        }
        return execute(command);
    }

    @Override
    public AdminCommandJob execute(AdminCommand command) {
        Objects.requireNonNull(command, "AdminCommand");
        final AdminCommandJob job = createJob(command);
        execute(command, job);
        return job;
    }

    @Override
    public AdminCommandJob createJob(AdminCommand command) {
        final boolean managedJob = detach || AnnotationUtil.presentTransitive(ManagedJob.class, command.getClass());
        return commandRunner.getJobManager().createJob(scope, name, subject, managedJob, params, report);
    }

    @Override
    public AdminCommand evaluateCommand() {
        return commandRunner.getCommand(scope, name, report);
    }

    @Override
    public void execute(final AdminCommand command, final AdminCommandJob job) {
        job.setState(State.RUNNING);
        if (job.isManaged()) {
            commandRunner.getJobManager().registerJob(job);
        }
        commandRunner.doCommand(this, command, subject, job);
        job.complete(report, outboundPayload());
        if (progressStatus != null) {
            progressStatus.complete();
        }
        commandRunner.done(command, job, notify);
    }

    public void executeFromCheckpoint(Checkpoint<AdminCommandJob> checkpoint, boolean revert) {
        AdminCommandJob job = checkpoint.getJob();
        parameters(job.getParameters());
        final AdminCommandContext context = checkpoint.getContext();
        this.inbound = context.getInboundPayload();
        this.outbound = context.getOutboundPayload();
        job.setState(revert ? REVERTING : RUNNING_RETRYABLE);
        commandRunner.getJobManager().registerJob(job);

        AdminCommand command = checkpoint.getCommand();
        if (command == null) {
            command = commandRunner.getCommand(job.getScope(), job.getName(), report);
            if (command == null) {
                return;
            }
        }

        commandRunner.doCommand(this, command, subject, job);
        job.complete(report, outboundPayload());
        if (progressStatus != null) {
            progressStatus.complete();
        }
        commandRunner.done(command, job, false);
    }

    /**
     * The caller should have set the subject explicitly.
     * In case it didn't, try setting it from the current access controller context
     * since the command framework will have set that before invoking
     * the original command's execute method.
     */
    private static Subject evaluateSubject(Subject subject) {
        if (subject != null) {
            return subject;
        }
        PrivilegedAction<Subject> action = () -> Subject.getSubject(AccessController.getContext());
        return AccessController.doPrivileged(action);
    }
}
