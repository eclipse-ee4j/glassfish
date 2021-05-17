/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging.commands;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.common.util.logging.LoggingConfig;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.server.logging.ODLLogFormatter;
import com.sun.enterprise.server.logging.UniformLogFormatter;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

/*
 * Set log file format command.
 * Updates the formatter for the log file to either ODL, ULF or a custom name.
 */
@ExecuteOn( { RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType( { CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
@CommandLock(CommandLock.LockType.NONE)
@Service(name = "set-log-file-format")
@PerLookup
@I18n("set.log.file.format")
@RestEndpoints( { @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST, path = "set-log-file-format", description = "set-log-file-format") })
public class SetLogFileFormat implements AdminCommand {

    private static final String ODL_FORMATTER_NAME = "ODL";

    private static final String ULF_FORMATTER_NAME = "ULF";

    @Param(optional = true)
    @I18n("set.log.file.format.target")
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Param(optional = true, defaultValue=ODL_FORMATTER_NAME, primary=true)
    @I18n("set.log.file.format.formatter")
    String formatter = ODL_FORMATTER_NAME;

    @Inject
    LoggingConfig loggingConfig;

    @Inject
    Domain domain;

    @Inject
    Servers servers;

    @Inject
    Clusters clusters;

    @Inject
    ServerEnvironment env;

    final private static LocalStringManagerImpl LOCAL_STRINGS = new LocalStringManagerImpl(
            SetLogFileFormat.class);

    public void execute(AdminCommandContext context) {

        String formatterClassName = null;
        if (formatter.equalsIgnoreCase(ODL_FORMATTER_NAME)) {
            formatterClassName = ODLLogFormatter.class.getName();
        } else if (formatter.equalsIgnoreCase(ULF_FORMATTER_NAME)) {
            formatterClassName = UniformLogFormatter.class.getName();
        } else {
            formatterClassName = formatter;
        }

        if (formatterClassName == null || formatter.isEmpty()) {
            formatterClassName = ODLLogFormatter.class.getName();
        }

        Map<String, String> loggingProperties = new HashMap<String, String>();
        loggingProperties.put("com.sun.enterprise.server.logging.GFFileHandler.formatter", formatterClassName);

        final ActionReport report = context.getActionReport();
        boolean isCluster = false;
        boolean isDas = false;
        boolean isInstance = false;
        boolean isConfig = false;
        String targetConfigName = "";

        try {
            Config config = domain.getConfigNamed(target);
            if (config != null) {
                targetConfigName = target;
                isConfig = true;
            } else {
                Server targetServer = domain.getServerNamed(target);
                if (targetServer != null) {
                    if (targetServer.isDas()) {
                        isDas = true;
                    } else {
                        isInstance = true;
                        Cluster clusterForInstance = targetServer.getCluster();
                        if (clusterForInstance != null) {
                            targetConfigName = clusterForInstance.getConfigRef();
                        } else {
                            targetConfigName = targetServer.getConfigRef();
                        }
                    }
                } else {
                    com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
                    if (cluster != null) {
                        isCluster = true;
                        targetConfigName = cluster.getConfigRef();
                    }
                }
            }

            if (isDas) {
                loggingConfig.updateLoggingProperties(loggingProperties);
            } else if ((targetConfigName != null && !targetConfigName.isEmpty()) &&
                    (isCluster || isInstance || isConfig)) {
                loggingConfig.updateLoggingProperties(loggingProperties, targetConfigName);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                String msg = LOCAL_STRINGS.getLocalString(
                        "invalid.target.sys.props",
                        "Invalid target: {0}. Valid default target is a server named ''server'' (default) or cluster name.",
                        targetConfigName);
                report.setMessage(msg);
                return;
            }

            String successMsg = LOCAL_STRINGS.getLocalString("set.log.file.format.success",
                    "The log file formatter is set to {0} for {1}.",
                    formatterClassName,
                    env.getInstanceName());
           report.setMessage(successMsg);
           report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        } catch (IOException e) {
            report.setMessage(LOCAL_STRINGS.getLocalString(
                    "set.log.file.format.failed",
                    "Could not set log file formatter for {0}.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

}
