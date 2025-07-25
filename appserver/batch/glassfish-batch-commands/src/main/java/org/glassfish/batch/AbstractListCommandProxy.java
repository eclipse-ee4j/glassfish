/*
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

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;

/**
 * Command to list batch jobs info
 *
 *         1      *             1      *
 * jobName --------> instanceId --------> executionId
 *
 * @author Mahesh Kannan
 */
public abstract class AbstractListCommandProxy
    implements AdminCommand {

    @Inject
    protected ServiceLocator serviceLocator;

    @Inject
    protected Target targetUtil;

    @Inject
    protected Logger logger;

    @Param(name = "terse", optional=true, defaultValue="false", shortName="t")
    protected boolean isTerse = false;

    @Param(name = "output", shortName = "o", optional = true)
    protected String outputHeaderList;

    @Param(name = "header", shortName = "h", optional = true)
    protected boolean header;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    protected String target;

    @Param(name = "long", shortName = "l", optional = true)
    protected boolean useLongFormat;

    protected ActionReport.ExitCode commandsExitCode = ActionReport.ExitCode.SUCCESS;

    @Override
    public final void execute(AdminCommandContext context) {
        ActionReport actionReport = context.getActionReport();
        Properties extraProperties = actionReport.getExtraProperties();
        if (extraProperties == null) {
            extraProperties = new Properties();
            actionReport.setExtraProperties(extraProperties);
        }

        ActionReport subReport = null;
        if (! preInvoke(context, actionReport)) {
            commandsExitCode = ActionReport.ExitCode.FAILURE;
            actionReport.setActionExitCode(commandsExitCode);
            return;
        }

        if (targetUtil.isCluster(target)) {
            for (Server serverInst : targetUtil.getInstances(target)) {
                try {
                    subReport = executeInternalCommand(context, serverInst.getName());
                    break;
                } catch (Throwable ex) {
                    logger.log(Level.INFO, "Got exception: " + ex.toString());
                }
            }
        } else {
            subReport = executeInternalCommand(context, target);
        }

        if (subReport != null) {
            if (subReport.getExtraProperties() != null && subReport.getExtraProperties().size() > 0)
                postInvoke(context, subReport);
            else {
                if (subReport.getSubActionsReport() != null && subReport.getSubActionsReport().size() > 0
                                                            && subReport.getSubActionsReport().get(0).getExtraProperties() != null) {
                    postInvoke(context, subReport.getSubActionsReport().get(0));
                } else {
                    actionReport.setMessage(subReport.getMessage());
                }
            }
            commandsExitCode = subReport.getActionExitCode();
        }
        actionReport.setActionExitCode(commandsExitCode);
    }

    protected boolean preInvoke(AdminCommandContext ctx, ActionReport subReport) {
        return true;
    }

    protected abstract String getCommandName();

    protected abstract void postInvoke(AdminCommandContext context, ActionReport subReport);

    private ActionReport executeInternalCommand(AdminCommandContext context, String targetInstanceName) {
        String commandName = getCommandName();
        ParameterMap params = new ParameterMap();
        params.add("target", targetInstanceName);
        fillParameterMap(params);
        CommandRunner runner = serviceLocator.getService(CommandRunner.class);
        ActionReport subReport = context.getActionReport().addSubActionsReport();
        CommandInvocation inv = runner.getCommandInvocation(commandName, subReport, context.getSubject());
        inv.parameters(params);
        inv.execute();

        return subReport;
    }

    protected void fillParameterMap(ParameterMap parameterMap) {
        if (isTerse)
            parameterMap.add("terse", ""+isTerse);
        if (outputHeaderList != null)
            parameterMap.add("output", outputHeaderList);
        if (header)
            parameterMap.add("header", ""+header);
        if (useLongFormat)
            parameterMap.add("long", ""+useLongFormat);
    }

    protected boolean isLongNumber(String str) {
        try {
            Long.parseLong(str);
        } catch (NumberFormatException nEx) {
            return false;
        }

        return true;
    }
}
