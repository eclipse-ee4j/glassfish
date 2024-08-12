/*
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
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;

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
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoint.OpType;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@ExecuteOn({RuntimeType.DAS})
@Service(name = "_load-default-log-levels")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("load.default.log.levels")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class, opType=OpType.POST, path="load-default-log-levels")
})
public class LoadDefaultLogLevels implements AdminCommand {

    @Inject
    LoggingConfigImpl loggingConfig;

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(LoadDefaultLogLevels.class);

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final String target = "default template";

        try {
            HashMap<String, String> props = null;
            props = (HashMap<String, String>) loggingConfig.getDefaultLoggingProperties();

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
