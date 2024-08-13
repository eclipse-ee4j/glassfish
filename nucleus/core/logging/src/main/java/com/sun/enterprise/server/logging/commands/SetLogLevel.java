/*
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
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.io.IOException;
import java.util.HashMap;
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
 * Date: Jul 8, 2009
 * Time: 11:48:20 AM
 * To change this template use File | Settings | File Templates.
 */

/*
* Set Logger Level Command
*
* Updates one or more loggers' level
*
* Usage: set-log-level [-?|--help=false]
* (logger_name=logging_value)[:logger_name=logging_value]*
*
*/
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@Service(name = "set-log-levels")
@CommandLock(CommandLock.LockType.NONE)
@PerLookup
@I18n("set.log.level")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="set-log-levels",
        description="set-log-levels")
})
public class SetLogLevel implements AdminCommand {

    @Param(name = "name_value", primary = true, separator = ':')
    Properties properties;

    @Param(optional = true)
    String target = SystemPropertyConstants.DAS_SERVER_NAME;

    @Inject
    LoggingConfigImpl loggingConfig;

    @Inject
    Domain domain;

    String[] validLevels = {"ALL", "OFF", "EMERGENCY", "ALERT", "SEVERE", "WARNING", "INFO", "CONFIG", "FINE", "FINER", "FINEST"};

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SetLogLevel.class);


    public void execute(AdminCommandContext context) {


        final ActionReport report = context.getActionReport();
        StringBuffer successMsg = new StringBuffer();
        boolean success = false;
        boolean invalidLogLevels = false;

        Map<String, String> m = new HashMap<String, String>();
        try {
            for (final Object key : properties.keySet()) {
                final String logger_name = (String) key;
                final String level = (String) properties.get(logger_name);
                // that is is a valid level
                boolean vlvl = false;
                for (String s : validLevels) {
                    if (s.equals(level)) {
                        m.put(logger_name + ".level", level);
                        vlvl = true;
                        successMsg.append(localStrings.getLocalString(
                                "set.log.level.properties", "{0} package set with log level {1}.\n", logger_name, level));
                    }
                }
                if (!vlvl) {
                    report.setMessage(localStrings.getLocalString("set.log.level.invalid",
                            "Invalid logger level found {0}.  Valid levels are: SEVERE, WARNING, INFO, FINE, FINER, FINEST", level));
                    invalidLogLevels = true;
                    break;
                }
            }

            if (invalidLogLevels) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            TargetInfo targetInfo = new TargetInfo(domain, target);
            String targetConfigName = targetInfo.getConfigName();
            boolean isDas = targetInfo.isDas();

            if (targetConfigName != null && !targetConfigName.isEmpty()) {
                loggingConfig.updateLoggingProperties(m, targetConfigName);
                success = true;
            } else if (isDas) {
                loggingConfig.updateLoggingProperties(m);
                success = true;
            }

            if (success) {
                successMsg.append(localStrings.getLocalString(
                        "set.log.level.success", "These logging levels are set for {0}.", target));
                report.setMessage(successMsg.toString());
                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }

        } catch (IOException e) {
            report.setMessage(localStrings.getLocalString("set.log.level.failed",
                    "Could not set logger levels for {0}.", target));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }
}
