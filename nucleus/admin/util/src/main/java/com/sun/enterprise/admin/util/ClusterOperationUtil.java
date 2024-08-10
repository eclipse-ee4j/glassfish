/*
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.InstanceCommandResult;
import org.glassfish.api.admin.InstanceState;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;

/**
 *
 */
public class ClusterOperationUtil {
    private static final Logger logger = AdminLoggerInfo.getLogger();

    private static final LocalStringManagerImpl strings = new LocalStringManagerImpl(ClusterOperationUtil.class);

    //TODO : Begin temp fix for undoable commands
    private static List<Server> completedInstances = new ArrayList<Server>();

    public static List<Server> getCompletedInstances() {
        return completedInstances;
    }

    public static void clearInstanceList() {
        completedInstances.clear();
    }
    //TODO : End temp fix for undoable commands

    //TODO: Remove after one replication method will be choosen
    private static boolean useRest() {
        //        String useRestStr = System.getenv("AS_ADMIN_USE_REST");
        //        return Boolean.valueOf(useRestStr);
        return true;
    }

    public static ActionReport.ExitCode replicateCommand(String commandName, FailurePolicy failPolicy, FailurePolicy offlinePolicy,
            FailurePolicy neverStartedPolicy, List<Server> instancesForReplication, AdminCommandContext context, ParameterMap parameters,
            ServiceLocator habitat) {
        return replicateCommand(commandName, failPolicy, offlinePolicy, neverStartedPolicy, instancesForReplication, context, parameters,
                habitat, null);
    }

    /**
     * Replicates a given command on the given list of targets, optionally gathering downloaded result payloads from the
     * instance commands into a directory.
     * <p>
     * If intermediateDownloadDir is non-null, then any files returned from the instances in the payload of the HTTP
     * response will be stored in a directory tree like this:
     *
     * <pre>
     * ${intermediateDownloadDir}/
     *     ${instanceA}/
     *         file(s) returned from instance A
     *     ${instanceB}/
     *         file(s) returned from instance B
     *     ...
     * </pre>
     *
     * where ${instanceA}, ${instanceB}, etc. are the names of the instances to which the command was replicated. This
     * method does no further processing on the downloaded files but leaves that to the calling command.
     */
    public static ActionReport.ExitCode replicateCommand(String commandName, FailurePolicy failPolicy, FailurePolicy offlinePolicy,
            FailurePolicy neverStartedPolicy, List<Server> instancesForReplication, AdminCommandContext context, ParameterMap parameters,
            ServiceLocator habitat, final File intermediateDownloadDir) {

        ActionReport.ExitCode returnValue = ActionReport.ExitCode.SUCCESS;
        InstanceStateService instanceState = habitat.getService(InstanceStateService.class);
        validateIntermediateDownloadDir(intermediateDownloadDir);
        RemoteInstanceCommandHelper rich = new RemoteInstanceCommandHelper(habitat);
        Map<String, Future<InstanceCommandResult>> futures = new HashMap<String, Future<InstanceCommandResult>>();
        try {
            for (Server svr : instancesForReplication) {
                if (instanceState.getState(svr.getName()) == InstanceState.StateType.NEVER_STARTED) {
                    // Do not replicate commands to instances that have never been started.
                    // For certain commands, warn about the failure to replicate even if
                    // the instance has never been started.
                    ActionReport.ExitCode finalResult = FailurePolicy.applyFailurePolicy(neverStartedPolicy, ActionReport.ExitCode.FAILURE);
                    if (!finalResult.equals(ActionReport.ExitCode.SUCCESS)) {
                        ActionReport aReport = context.getActionReport().addSubActionsReport();
                        if (finalResult.equals(ActionReport.ExitCode.FAILURE)) {
                            aReport.setMessage(strings.getLocalString("clusterutil.failneverstarted",
                                    "FAILURE: Instance {0} has never been started; command {1} was not replicated to that instance",
                                    svr.getName(), commandName));
                        } else {
                            aReport.setMessage(strings.getLocalString("clusterutil.warnneverstarted",
                                    "WARNING: Instance {0} has never been started; command {1} was not replicated to that instance",
                                    svr.getName(), commandName));
                        }
                        aReport.setActionExitCode(finalResult);
                        if (returnValue == ActionReport.ExitCode.SUCCESS)
                            returnValue = finalResult;
                    }
                    continue;
                }
                Config scfg = svr.getConfig();
                if (!Boolean.valueOf(scfg.getDynamicReconfigurationEnabled())) {
                    // Do not replicate to servers for which dynamic configuration is disabled
                    ActionReport aReport = context.getActionReport().addSubActionsReport();
                    aReport.setActionExitCode(ActionReport.ExitCode.WARNING);
                    aReport.setMessage(strings.getLocalString("clusterutil.dynrecfgdisabled",
                            "WARNING: The command {0} was not replicated to instance {1} because the "
                                    + "dynamic-reconfiguration-enabled flag is set to false for config {2}",
                            new Object[] { commandName, svr.getName(), scfg.getName() }));
                    instanceState.setState(svr.getName(), InstanceState.StateType.RESTART_REQUIRED, false);
                    instanceState.addFailedCommandToInstance(svr.getName(), commandName, parameters);
                    returnValue = ActionReport.ExitCode.WARNING;
                    continue;
                }
                String host = svr.getAdminHost();
                int port = rich.getAdminPort(svr);
                ActionReport aReport = context.getActionReport().addSubActionsReport();
                InstanceCommandResult aResult = new InstanceCommandResult();
                //                InstanceCommandExecutor ice =
                //                        new InstanceCommandExecutor(habitat, commandName, failPolicy, offlinePolicy,
                //                                svr, host, port, logger, parameters, aReport, aResult);
                //                if (CommandTarget.DAS.isValid(habitat, ice.getServer().getName()))
                //                    continue;
                //                if (intermediateDownloadDir != null) {
                //                    ice.setFileOutputDirectory(
                //                        subdirectoryForInstance(intermediateDownloadDir, ice));
                //                }
                //                Future<InstanceCommandResult> f = instanceState.submitJob(svr, ice, aResult);
                //TODO: Remove this if after only one remote admin call method will be choosen
                Future<InstanceCommandResult> f;
                if (useRest()) {
                    InstanceRestCommandExecutor irce = new InstanceRestCommandExecutor(habitat, commandName, failPolicy, offlinePolicy, svr,
                            host, port, logger, parameters, aReport, aResult);
                    if (CommandTarget.DAS.isValid(habitat, irce.getServer().getName())) {
                        continue;
                    }
                    if (intermediateDownloadDir != null) {
                        irce.setFileOutputDirectory(new File(intermediateDownloadDir, irce.getServer().getName()));
                    }
                    setConnectionTimeout(irce::setConnectTimeout, parameters);
                    f = instanceState.submitJob(svr, irce, aResult);
                } else {
                    logger.log(Level.FINEST, "replicateCommand(): Use traditional way for replication - {0}", commandName);
                    InstanceCommandExecutor ice = new InstanceCommandExecutor(habitat, commandName, failPolicy, offlinePolicy, svr, host,
                            port, logger, parameters, aReport, aResult);
                    if (CommandTarget.DAS.isValid(habitat, ice.getServer().getName())) {
                        continue;
                    }
                    if (intermediateDownloadDir != null) {
                        ice.setFileOutputDirectory(new File(intermediateDownloadDir, ice.getServer().getName()));
                    }
                    setConnectionTimeout(ice::setConnectTimeout, parameters);
                    f = instanceState.submitJob(svr, ice, aResult);
                }
                if (f == null) {
                    logger.severe(AdminLoggerInfo.stateNotFound);
                    continue;
                }
                futures.put(svr.getName(), f);
                logger.fine(strings.getLocalString("dynamicreconfiguration.diagnostics.jobsubmitted",
                        "Successfully submitted command {0} for execution at instance {1}", commandName, svr.getName()));
            }
        } catch (Exception ex) {
            ActionReport aReport = context.getActionReport().addSubActionsReport();
            ActionReport.ExitCode finalResult = FailurePolicy.applyFailurePolicy(failPolicy, ActionReport.ExitCode.FAILURE);
            aReport.setActionExitCode(finalResult);
            aReport.setMessage(strings.getLocalString("clusterutil.replicationfailed", "Error during command replication: {0}",
                    ex.getLocalizedMessage()));
            logger.log(Level.SEVERE, AdminLoggerInfo.replicationError, ex.getLocalizedMessage());
            if (returnValue == ActionReport.ExitCode.SUCCESS) {
                returnValue = finalResult;
            }
        }

        boolean gotFirstResponse = false;
        long maxWaitTime = RemoteRestAdminCommand.getReadTimeout();
        long timeBeforeAsadminTimeout = maxWaitTime;
        long waitStart = System.currentTimeMillis();
        for (Map.Entry<String, Future<InstanceCommandResult>> fe : futures.entrySet()) {
            String s = fe.getKey();
            ActionReport.ExitCode finalResult;
            try {
                logger.fine(strings.getLocalString("dynamicreconfiguration.diagnostics.waitingonjob",
                        "Waiting for command {0} to be completed at instance {1}", commandName, s));
                Future<InstanceCommandResult> aFuture = fe.getValue();
                InstanceCommandResult aResult = aFuture.get(maxWaitTime, TimeUnit.MILLISECONDS);
                long elapsedTime = System.currentTimeMillis() - waitStart;
                timeBeforeAsadminTimeout -= elapsedTime;
                if (!gotFirstResponse) {
                    maxWaitTime = elapsedTime * 4;
                    gotFirstResponse = true;
                }
                if ((maxWaitTime > timeBeforeAsadminTimeout) || (maxWaitTime < 60000)) {
                    maxWaitTime = timeBeforeAsadminTimeout;
                }
                ActionReport iReport;
                Server iServer;
                if (useRest()) {
                    InstanceRestCommandExecutor ice = (InstanceRestCommandExecutor) aResult.getInstanceCommand();
                    iReport = ice.getReport();
                    iServer = ice.getServer();
                } else {
                    InstanceCommandExecutor ice = (InstanceCommandExecutor) aResult.getInstanceCommand();
                    iReport = ice.getReport();
                    iServer = ice.getServer();
                }
                if (iReport.getActionExitCode() != ActionReport.ExitCode.FAILURE) {
                    completedInstances.add(iServer);
                }
                finalResult = FailurePolicy.applyFailurePolicy(failPolicy, iReport.getActionExitCode());
                if (returnValue == ActionReport.ExitCode.SUCCESS) {
                    returnValue = finalResult;
                }
                if (finalResult != ActionReport.ExitCode.SUCCESS) {
                    instanceState.setState(s, InstanceState.StateType.RESTART_REQUIRED, false);
                    instanceState.addFailedCommandToInstance(s, commandName, parameters);
                }
            } catch (Exception ex) {
                ActionReport aReport = context.getActionReport().addSubActionsReport();
                finalResult = FailurePolicy.applyFailurePolicy(failPolicy, ActionReport.ExitCode.FAILURE);
                if (finalResult == ActionReport.ExitCode.FAILURE) {
                    if (ex instanceof TimeoutException)
                        aReport.setMessage(strings.getLocalString("clusterutil.timeoutwhilewaiting",
                                "Timed out while waiting for result from instance {0}", s));
                    else
                        aReport.setMessage(strings.getLocalString("clusterutil.exceptionwhilewaiting",
                                "Exception while waiting for result from instance {0} : {1}", s, ex.getLocalizedMessage()));
                }
                aReport.setActionExitCode(finalResult);
                if (returnValue == ActionReport.ExitCode.SUCCESS)
                    returnValue = finalResult;
                instanceState.setState(s, InstanceState.StateType.RESTART_REQUIRED, false);
                instanceState.addFailedCommandToInstance(s, commandName, parameters);
            }
        }
        return returnValue;
    }

    private static void setConnectionTimeout(Consumer<Integer> applyTimeout, ParameterMap parameters) {
        applyTimeout.accept(SystemPropertyConstants.getDefaultAdminTimeout());
        final String timeoutKey = "timeoutmsec";
        if (parameters.containsKey(timeoutKey) && !parameters.get(timeoutKey).isEmpty()) {
            try {
                applyTimeout.accept(Integer.parseInt(parameters.get(timeoutKey).get(0)));
            } catch (Exception e) {
                logger.fine(() -> "Failed to set timeout from the " + timeoutKey
                        + " parameter. The value " + parameters.get(timeoutKey)
                        + " raised an exception: " + e.getMessage());
                // ignore and keep the default
            }
        }
    }

    public static ActionReport.ExitCode replicateCommand(String commandName, FailurePolicy failPolicy, FailurePolicy offlinePolicy,
            FailurePolicy neverStartedPolicy, Collection<String> targetNames, AdminCommandContext context, ParameterMap parameters,
            ServiceLocator habitat) {
        return replicateCommand(commandName, failPolicy, offlinePolicy, neverStartedPolicy, targetNames, context, parameters, habitat,
                null);
    }

    /**
     * Replicates a given command on the given list of targets, optionally gathering downloaded result payloads from the
     * instance commands into a directory.
     * <p>
     * If intermediateDownloadDir is non-null, then any files returned from the instances in the payload of the HTTP
     * response will be stored in a directory tree like this:
     *
     * <pre>
     * ${intermediateDownloadDir}/
     *     ${instanceA}/
     *         file(s) returned from instance A
     *     ${instanceB}/
     *         file(s) returned from instance B
     *     ...
     * </pre>
     *
     * where ${instanceA}, ${instanceB}, etc. are the names of the instances to which the command was replicated. This
     * method does no further processing on the downloaded files but leaves that to the calling command.
     */
    public static ActionReport.ExitCode replicateCommand(String commandName, FailurePolicy failPolicy, FailurePolicy offlinePolicy,
            FailurePolicy neverStartedPolicy, Collection<String> targetNames, AdminCommandContext context, ParameterMap parameters,
            ServiceLocator habitat, File intermediateDownloadDir) {

        ActionReport.ExitCode result = ActionReport.ExitCode.SUCCESS;
        Target targetService = habitat.getService(Target.class);
        for (String t : targetNames) {
            if (CommandTarget.DAS.isValid(habitat, t) || CommandTarget.DOMAIN.isValid(habitat, t))
                continue;
            parameters.set("target", t);
            ActionReport.ExitCode returnValue = replicateCommand(commandName, failPolicy, offlinePolicy, neverStartedPolicy,
                    targetService.getInstances(t), context, parameters, habitat, intermediateDownloadDir);
            if (!returnValue.equals(ActionReport.ExitCode.SUCCESS)) {
                result = returnValue;
            }
        }
        return result;
    }

    /**
     * Makes sure the intermediate download directory is null (meaning the calling command does not care about any
     * downloaded content from the instances) or that the specified file is a valid place to store any downloaded files.
     * Create the directory if it does not already exist.
     *
     * @param dir the caller-specified File to check
     */
    private static void validateIntermediateDownloadDir(final File dir) {
        if (dir == null) {
            return;
        }
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw new RuntimeException(strings.getLocalString("clusterutil.errCreDir",
                        "Could not create directory {0}; no further information available", dir.getAbsolutePath()));
            }
        } else {
            if (!dir.isDirectory() || !dir.canWrite()) {
                throw new IllegalArgumentException(dir.getAbsolutePath());
            }
        }
    }
}
