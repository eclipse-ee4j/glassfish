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

package org.glassfish.batch;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.batch.spi.impl.BatchRuntimeConfiguration;
import org.glassfish.batch.spi.impl.GlassFishBatchValidationException;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * @author Mahesh Kannan
 *
 */
@Service(name = "set-batch-runtime-configuration")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("set.batch.runtime.configuration")
@ExecuteOn(value = {RuntimeType.DAS})
@TargetType(value = {CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class,
                opType = RestEndpoint.OpType.POST,
                path = "set-batch-runtime-configuration",
                description = "Set Batch Runtime Configuration")
})
public class SetBatchRuntimeConfiguration
    implements AdminCommand {

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    protected Logger logger;

    @Param(name = "target", optional = true, defaultValue = "server")
    protected String target;

    @Inject
    protected Target targetUtil;

    @Param(name = "dataSourceLookupName", shortName = "d", optional = true)
    private String dataSourceLookupName;

    @Param(name = "executorServiceLookupName", shortName = "x", optional = true)
    private String executorServiceLookupName;

    @Override
    public void execute(final AdminCommandContext context) {
        final ActionReport actionReport = context.getActionReport();
        Properties extraProperties = actionReport.getExtraProperties();
        if (extraProperties == null) {
            extraProperties = new Properties();
            actionReport.setExtraProperties(extraProperties);
        }

        if (dataSourceLookupName == null && executorServiceLookupName == null) {
            actionReport.setMessage("Either dataSourceLookupName or executorServiceLookupName must be specified.");
            actionReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        try {
            Config config = targetUtil.getConfig(target);
            System.out.println("** EXECUTING -d " + dataSourceLookupName + "  -x " + executorServiceLookupName);

            BatchRuntimeConfiguration batchRuntimeConfiguration = config.getExtensionByType(BatchRuntimeConfiguration.class);
            if (batchRuntimeConfiguration != null) {
                ConfigSupport.apply(new SingleConfigCode<BatchRuntimeConfiguration>() {
                    @Override
                    public Object run(final BatchRuntimeConfiguration batchRuntimeConfigurationProxy)
                            throws PropertyVetoException, TransactionFailure {
                        boolean encounteredError = false;
                        if (dataSourceLookupName != null) {
                            try {
                                validateDataSourceLookupName(context, target, dataSourceLookupName);
                                batchRuntimeConfigurationProxy.setDataSourceLookupName(dataSourceLookupName);
                                actionReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                            } catch (GlassFishBatchValidationException ex) {
                                logger.log(Level.WARNING, ex.getMessage());
                                actionReport.setMessage(dataSourceLookupName + " is not mapped to a DataSource");
                                actionReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
                                throw new GlassFishBatchValidationException(dataSourceLookupName + " is not mapped to a DataSource");
                            }
                        }
                        if (executorServiceLookupName != null && !encounteredError) {
                            try {
                                validateExecutorServiceLookupName(context, target, executorServiceLookupName);
                                batchRuntimeConfigurationProxy.setExecutorServiceLookupName(executorServiceLookupName);
                                actionReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
                            } catch (GlassFishBatchValidationException ex) {
                                logger.log(Level.WARNING, ex.getMessage());
                                actionReport.setMessage("No executor service bound to name = " + executorServiceLookupName);
                                actionReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
                                throw new GlassFishBatchValidationException("No executor service bound to name = " + executorServiceLookupName);
                            }
                        }

                        return null;
                    }
                }, batchRuntimeConfiguration);
            }

        } catch (TransactionFailure txfEx) {
            logger.log(Level.WARNING, "Exception during command ", txfEx);
            actionReport.setMessage(txfEx.getCause().getMessage());
            actionReport.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
    }

    public void validateDataSourceLookupName(AdminCommandContext context, String targetName, String dsLookupName) {
        try {
            CommandRunner<?> runner = serviceLocator.getService(CommandRunner.class);
            ActionReport subReport = context.getActionReport().addSubActionsReport();
            CommandInvocation<?> inv = runner.getCommandInvocation("list-jdbc-resources", subReport, context.getSubject());

            ParameterMap params = new ParameterMap();
            params.add("target", targetName);
            inv.parameters(params);
            inv.execute();

            Properties props = subReport.getExtraProperties();
            if (props != null) {
                if (props.get("jdbcResources") != null) {
                    List<HashMap<String, String>> map = (List<HashMap<String, String>>) props.get("jdbcResources");
                    for (HashMap<String, String> e : map) {
                        if (e.get("name").equals(dsLookupName)) {
                            return;
                        }
                    }
                }
            }
            throw new GlassFishBatchValidationException("No DataSource mapped to " + dsLookupName);
        } catch (Exception ex) {
            throw new GlassFishBatchValidationException("Exception during validation: ", ex);
        }
    }

    public void validateExecutorServiceLookupName(AdminCommandContext context, String targetName, String exeLookupName) {
        if ("concurrent/__defaultManagedExecutorService".equals(exeLookupName)) {
            return;
        }
        try {
            CommandRunner<?> runner = serviceLocator.getService(CommandRunner.class);
            ActionReport subReport = context.getActionReport().addSubActionsReport();
            CommandInvocation<?> inv = runner.getCommandInvocation("list-managed-executor-services", subReport, context.getSubject());

            ParameterMap params = new ParameterMap();
            params.add("target", targetName);
            inv.parameters(params);
            inv.execute();

            Properties props = subReport.getExtraProperties();
            if (props != null) {
                if (props.get("managedExecutorServices") != null) {
                    List<HashMap<String, String>> map = (List<HashMap<String, String>>) props.get("managedExecutorServices");
                    for (HashMap<String, String> e : map) {
                        if (e.get("name").equals(exeLookupName)) {
                            return;
                        }
                    }
                }
            }
            throw new GlassFishBatchValidationException("No ExecutorService mapped to " + exeLookupName);
        } catch (Exception ex) {
            throw new GlassFishBatchValidationException("Exception during validation: ", ex);
        }
    }

}
