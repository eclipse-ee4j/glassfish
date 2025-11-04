/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Clusters;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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
 * Created by IntelliJ IDEA.
 * User: cmott, naman mehta
 * Date: Aug 26, 2009
 * Time: 5:32:17 PM
 * To change this template use File | Settings | File Templates.
 */
@ExecuteOn({RuntimeType.DAS})
@Service(name = "list-log-levels")
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.CONFIG})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.log.levels")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-log-levels",
        description="list-log-levels")
})
public class ListLoggerLevels implements AdminCommand {

    @Inject
    LoggingConfigImpl loggingConfig;

    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Inject
    Domain domain;

    @Inject
    Servers servers;

    @Inject
    Clusters clusters;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListLoggerLevels.class);

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();
        boolean isCluster = false;
        boolean isDas = false;
        boolean isInstance = false;
        boolean isConfig = false;
        String targetConfigName = "";

        try {
            HashMap<String, String> props = null;

            Config config = domain.getConfigNamed(target);
            if (config != null) {
                targetConfigName = target;
                isConfig = true;

                Server targetServer = domain.getServerNamed(SystemPropertyConstants.DAS_SERVER_NAME);
                if (targetServer.getConfigRef().equals(target)) {
                    isDas = true;
                }
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
                props = (HashMap<String, String>) loggingConfig.getLoggingProperties(targetConfigName);
            } else if (isDas) {
                props = (HashMap<String, String>) loggingConfig.getLoggingProperties();
            } else if (isConfig) {
                // This loop is for the config which is not part of any target
                props = (HashMap<String, String>) loggingConfig.getLoggingProperties(targetConfigName);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                String msg = localStrings.getLocalString("invalid.target.sys.props",
                        "Invalid target: {0}. Valid default target is a server named ''server'' (default) or cluster name.", target);
                report.setMessage(msg);
                return;
            }

            List<String> keys = new ArrayList<String>();
            keys.addAll(props.keySet());
            Collections.sort(keys);
            Iterator<String> it2 = keys.iterator();
            // The following Map & List are used to hold the REST data
            Map<String, String> logLevelMap = new HashMap<String, String>();
            List<String> loggerList = new ArrayList<String>();
            while (it2.hasNext()) {
                String name = it2.next();
                if (name.endsWith(".level") && !name.equals(".level")) {
                    final ActionReport.MessagePart part = report.getTopMessagePart()
                            .addChild();
                    String n = name.substring(0, name.lastIndexOf(".level"));
                   // GLASSFISH-21560: removing the condition which filter out logger ending with "Handler"
                   // above if condition takes care of filtering out everything except log levels from logging.properties
                  //  Format of logger used is <logger_name>.level=<log_level>
                    part.setMessage(n + "\t" + "<" + (String) props.get(name) + ">");
                    logLevelMap.put(n, props.get(name)); //Needed for REST xml and JSON output
                    loggerList.add(n); //Needed for REST xml and JSON output
                }
            }
            // Populate the extraProperties data structure for REST...
            Properties restData = new Properties();
            restData.put("logLevels", logLevelMap);
            restData.put("loggers", loggerList);
            report.setExtraProperties(restData);

        } catch (IOException ex) {
            report.setMessage(localStrings.getLocalString("get.log.level.failed",
                    "Could not get logging levels for {0}.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);


    }
}
