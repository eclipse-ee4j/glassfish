/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.common.util.logging.LoggingConfigImpl;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author naman
 */
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@Service(name = "delete-log-levels")
@CommandLock(CommandLock.LockType.NONE)
@PerLookup
@I18n("delete.log.levels")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-log-levels",
        description="delete-log-levels")
})
public class DeleteLogLevel implements AdminCommand {
    @Param(name = "logger_name", primary = true, separator = ':')
    String properties;

    @Param(optional = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    LoggingConfigImpl loggingConfig;

    @Inject
    Domain domain;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteLogLevel.class);

    @Override
    public void execute(AdminCommandContext context) {


        final ActionReport report = context.getActionReport();
        boolean isCluster = false;
        boolean isDas = false;
        boolean isInstance = false;
        StringBuffer successMsg = new StringBuffer();
        boolean isConfig = false;
        boolean success = false;
        String targetConfigName = "";

        Map<String, String> m = new HashMap<>();
        try {

            String loggerNames[] = properties.split(":");

            for (final Object key : loggerNames) {
                final String logger_name = (String) key;
                m.put(logger_name + ".level", null);
            }


            Config config = domain.getConfigNamed(target);
            if (config != null) {
                targetConfigName = target;
                isConfig = true;

                Server targetServer = domain.getServerNamed(SystemPropertyConstants.DAS_SERVER_NAME);
                if (targetServer != null && targetServer.getConfigRef().equals(target)) {
                    isDas = true;
                }
                targetServer = null;
            } else {
                Server targetServer = domain.getServerNamed(target);

                if (targetServer != null && targetServer.isDas()) {
                    isDas = true;
                } else {
                    com.sun.enterprise.config.serverbeans.Cluster cluster = domain.getClusterNamed(target);
                    if (cluster != null) {
                        isCluster = true;
                        targetConfigName = cluster.getConfigRef();
                    } else if (targetServer != null) {
                        isInstance = true;
                        targetConfigName = targetServer.getConfigRef();
                    }
                }

                if (isInstance) {
                    Cluster clusterForInstance = targetServer.getCluster();
                    if (clusterForInstance != null) {
                        targetConfigName = clusterForInstance.getConfigRef();
                    }
                }
            }

            if (isCluster || isInstance) {
                loggingConfig.deleteLoggingProperties(m.keySet(), targetConfigName);
                success = true;
            } else if (isDas) {
                loggingConfig.deleteLoggingProperties(m.keySet());
                success = true;
            } else if (isConfig) {
                // This loop is for the config which is not part of any target
                loggingConfig.deleteLoggingProperties(m.keySet(), targetConfigName);
                success = true;
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                String msg = localStrings.getLocalString("invalid.target.sys.props",
                        "Invalid target: {0}. Valid default target is a server named ''server'' (default) or cluster name.", target);
                report.setMessage(msg);
                return;
            }

            if (success) {
                successMsg.append(localStrings.getLocalString(
                        "delete.log.level.success", "These logging levels are deleted for {0}.", target));
                report.setMessage(successMsg.toString());
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }


        } catch (IOException e) {
            report.setMessage(localStrings.getLocalString("delete.log.level.failed",
                    "Could not delete logger levels for {0}.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }
}
