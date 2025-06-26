/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;


import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;

import java.lang.System.Logger;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.embeddable.GlassFishVariable;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;


/*
 * ClusterCommandHelper is a helper class that knows how to execute an
 * asadmin command in the DAS for each instance in a cluster. For example
 * it is used by start-cluster and stop-cluster to execute start-instance
 * or stop-instance for each instance in a cluster. Note this is not the
 * same as cluster replication where general commands are executed on
 * each instances of a cluster.
 *
 * @author Joe Di Pol
 */
class ClusterCommandHelper {
    private static final Logger LOG = System.getLogger(ClusterCommandHelper.class.getName());
    private static final String NL = System.lineSeparator();

    private Domain domain;
    private CommandRunner runner;
    private ProgressStatus progress;

    /**
     * Construct a ClusterCommandHelper
     *
     * @param domain The Domain we are running in
     * @param runner A CommandRunner to use for running commands
     */
    ClusterCommandHelper(Domain domain, CommandRunner runner) {
        this.domain = domain;
        this.runner = runner;
    }

    /**
     * Loop through all instances in a cluster and execute a command for
     * each one.
     *
     * @param command       The string of the command to run. The instance
     *                      name will be used as the operand for the command.
     * @param map           A map of parameters to use for the command. May be
     *                      null if no parameters. When the command is
     *                      executed for a server instance, the instance name
     *                      is set as the DEFAULT parameter (operand)
     * @param clusterName   The name of the cluster containing the instances
     *                      to run the command against.
     * @param context       The AdminCommandContext to use when executing the
     *                      command.
     * @param verbose       true for more verbose output
     * @return              An ActionReport containing the results
     * @throws CommandException
     */
    ActionReport runCommand(
            String  command,
            ParameterMap map,
            String  clusterName,
            AdminCommandContext context,
            boolean debug,
            boolean verbose,
            Duration timeout) throws CommandException {

        ActionReport report = context.getActionReport();

        // Get the cluster specified by clusterName
        Cluster cluster = domain.getClusterNamed(clusterName);
        if (cluster == null) {
            String msg = Strings.get("cluster.command.unknownCluster", clusterName);
            throw new CommandException(msg);
        }

        // Get the list of servers in the cluster.
        List<Server> targetServers = domain.getServersInTarget(clusterName);

        // If the cluster is empty, say so
        if (targetServers == null || targetServers.isEmpty()) {
            report.setActionExitCode(ExitCode.SUCCESS);
            report.setMessage(Strings.get("cluster.command.noInstances", clusterName));
            return report;
        }
        final int nInstances = targetServers.size();

        // We will save the name of the instances that worked and did
        // not work so we can summarize our results.
        StringBuilder failedServerNames = new StringBuilder();
        StringBuilder succeededServerNames = new StringBuilder();
        List<String> waitingForServerNames = new ArrayList<String>();
        ReportResult reportResult = new ReportResult();
        boolean failureOccurred = false;
        progress = context.getProgressStatus();

        // Save command output to return in ActionReport
        StringBuilder output = new StringBuilder();


        // Optimize the oder of server instances to avoid clumping on nodes
        LOG.log(DEBUG, () -> "Instance list " + serverListToString(targetServers));
        if (map == null) {
            map = new ParameterMap();
        }

        final ThreadFactory threadFactory = new ThreadFactory() {

            private final AtomicInteger id = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r,
                    "ClusterCommandHelper-" + command + "-" + clusterName + "-" + id.incrementAndGet());
                t.setDaemon(true);
                return t;
            }
        };
        int threadPoolSize = getAdminThreadPoolSize(LOG);
        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize, threadFactory);

        LOG.log(INFO, String.format(
            "Executing %s on %d instances using a thread pool of size %d: %s",
            command, nInstances, threadPoolSize,
            serverListToString(targetServers)));

        progress.setTotalStepCount(nInstances);
        progress.progress(Strings.get("cluster.command.executing", command, nInstances));

        // Loop through instance names, construct the command for each
        // instance name, and hand it off to the threadpool.
        final ArrayBlockingQueue<CommandRunnable> responseQueue = new ArrayBlockingQueue<>(nInstances);
        for (Server server : targetServers) {
            String iname = server.getName();
            waitingForServerNames.add(iname);

            ParameterMap instanceParameterMap = new ParameterMap(map);
            // Set the instance name as the operand for the commnd
            instanceParameterMap.set("DEFAULT", iname);
            if (debug) {
                instanceParameterMap.set("debug", "true");
            }

            ActionReport instanceReport = runner.getActionReport("plain");
            instanceReport.setActionExitCode(ExitCode.SUCCESS);
            CommandInvocation invocation = runner.getCommandInvocation(command, instanceReport, context.getSubject());
            invocation.parameters(instanceParameterMap);

            String msg = command + " " + iname;
            LOG.log(INFO, msg);
            if (verbose) {
                output.append(msg).append(NL);
            }

            // Wrap the command invocation in a runnable and hand it off
            // to the thread pool
            CommandRunnable cmdRunnable = new CommandRunnable(iname, invocation, instanceReport, responseQueue);
            CompletableFuture.runAsync(cmdRunnable, executor);
        }

        LOG.log(DEBUG, () -> "Started commands in parallel, waiting for responses...");

        // Now go get results from the response queue.
        final long deadline = computeDeadline(timeout);
        for (int i = 0; i < nInstances; i++) {
            final long timeLeft = deadline - System.currentTimeMillis();
            CommandRunnable cmdRunnable = null;
            try {
                cmdRunnable = responseQueue.poll(timeLeft, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                // This thread has been interrupted. Abort
                executor.shutdownNow();
                String msg = Strings.get("cluster.command.interrupted", clusterName, i, Integer.toString(nInstances),
                    command);
                LOG.log(WARNING, msg);
                output.append(msg).append(NL);
                failureOccurred = true;
                // Re-establish interrupted state on thread
                Thread.currentThread().interrupt();
                break;
            }

            if (cmdRunnable == null) {
                // We've timed out.
                break;
            }
            String iname = cmdRunnable.getName();
            waitingForServerNames.remove(iname);
            ActionReport instanceReport = cmdRunnable.getActionReport();
            LOG.log(DEBUG, String.format("Instance %d of %d (%s) has responded with %s", i + 1, nInstances, iname,
                instanceReport.getActionExitCode()));
            if (instanceReport.getActionExitCode() != ExitCode.SUCCESS) {
                // Bummer, the command had an error. Log and save output
                failureOccurred = true;
                failedServerNames.append(iname).append(" ");
                reportResult.failedServerNames.add(iname);
                String msg = iname + ": " + instanceReport.getMessage();
                LOG.log(ERROR, msg);
                output.append(msg).append(NL);
                progress.progress(1, Strings.get("cluster.command.instancesFailed", command, iname));
            } else {
                // Command worked. Note that too.
                succeededServerNames.append(iname).append(" ");
                reportResult.succeededServerNames.add(iname);
                progress.progress(1, iname);
            }
        }

        report.setActionExitCode(ExitCode.SUCCESS);

        if (failureOccurred) {
            report.setResultType(List.class, reportResult.failedServerNames);
        } else {
            report.setResultType(List.class, reportResult.succeededServerNames);
        }


        // Display summary of started servers if in verbose mode or we
        // had one or more failures.
        if (succeededServerNames.length() > 0 && (verbose || failureOccurred)) {
            output.append(NL).append(Strings.get("cluster.command.instancesSucceeded", command, succeededServerNames));
        }

        if (failureOccurred) {
            // Display summary of failed servers if we have any
            output.append(NL).append(Strings.get("cluster.command.instancesFailed", command, failedServerNames));
            if (succeededServerNames.length() > 0) {
                // At least one instance started. Warning.
                report.setActionExitCode(ExitCode.WARNING);
            } else {
                // No instance started. Failure
                report.setActionExitCode(ExitCode.FAILURE);
            }
        }

        // Check for server that did not respond
        if (!waitingForServerNames.isEmpty()) {
            String msg = Strings.get("cluster.command.instancesTimedOut", command, listToString(waitingForServerNames));
            LOG.log(WARNING, msg);
            if (output.length() > 0) {
                output.append(NL);
            }
            output.append(msg);
            report.setActionExitCode(ExitCode.WARNING);
        }

        report.setMessage(output.toString());
        executor.shutdown();
        return report;
    }

    /**
     * @param timeout
     * @return
     */
    private long computeDeadline(Duration timeoutParameter) {
        // Make sure we don't wait longer than the admin read timeout.
        // Set our limit to be 2 seconds less.
        long adminTimeout = RemoteRestAdminCommand.getReadTimeout() - 2000;
        final long timeout;
        if (adminTimeout <= 0) {
            timeout = timeoutParameter.toMillis();
        } else if (timeoutParameter.toMillis() > adminTimeout) {
            LOG.log(WARNING, "Cluster command timeout is greater than admin read timeout."
                + " Using admin read timeout instead to prevent socket read timeouts.");
            timeout = adminTimeout;
        } else {
            timeout = timeoutParameter.toMillis();
        }
        return System.currentTimeMillis() + timeout;
    }

    /**
     * Get the size of the admin threadpool
     */
    private int getAdminThreadPoolSize(Logger logger) {
        Config config = domain.getConfigNamed("server-config");
        if (config == null) {
            return 10;
        }
        AdminEndpointDecider aed = new AdminEndpointDecider(config);
        return aed.getMaxThreadPoolSize();
    }

    private static String serverListToString(List<Server> servers) {
        StringBuilder sb = new StringBuilder();
        for (Server s : servers) {
            sb.append(s.getNodeRef()).append(':').append(s.getName()).append(' ');
        }
        return sb.toString().trim();
    }

    private static String listToString(List<String> slist) {
        StringBuilder sb = new StringBuilder();
        for (String s : slist) {
            sb.append(s).append(' ');
        }
        return sb.toString().trim();
    }

    static Duration getTimeout(Integer timeout, GlassFishVariable envVariable) {
        if (timeout == null) {
            String envValue = System.getenv(envVariable.getEnvName());
            if (envValue == null) {
                return Duration.ofSeconds(60);
            }
            return Duration.ofSeconds(Long.parseLong(envValue));
        }
        return Duration.ofSeconds(timeout);
    }


    static public class ReportResult {
        final public List<String> succeededServerNames = new ArrayList<>();
        final public List<String> failedServerNames = new ArrayList<>();
    }
}
