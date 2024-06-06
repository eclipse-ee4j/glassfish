/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.glassfish.admin.restconnector.RestConfig;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Remote asadmin command: get-rest-config
 *
 * Purpose: Allows the invoker to get values for the REST module.
 *
 *
 *
 * @author Ludovic Champenois
 *
 */
@Service(name = "_get-rest-admin-config")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({ @RestEndpoint(configBean = Domain.class) })
public class GetRestConfig implements AdminCommand {

    @AccessRequired.To("read")
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Override
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();

        RestConfig restConfig = config.getExtensionByType(RestConfig.class);

        if (restConfig == null) {
            report.setMessage(
                    "debug=false, indentLevel=-1, showHiddenCommands=false, wadlGeneration=false, logOutput=false, logInput=false, showDeprecatedItems=false, sessionTokenTimeout=30");

            report.getTopMessagePart().addProperty("debug", "false");
            report.getTopMessagePart().addProperty("indentLevel", "-1");
            report.getTopMessagePart().addProperty("showHiddenCommands", "false");
            report.getTopMessagePart().addProperty("showDeprecatedItems", "false");
            report.getTopMessagePart().addProperty("wadlGeneration", "" + "false");
            report.getTopMessagePart().addProperty("logOutput", "" + "false");
            report.getTopMessagePart().addProperty("logInput", "" + "false");
            report.getTopMessagePart().addProperty("sessionTokenTimeout", "30");

        } else {
            report.setMessage("debug=" + restConfig.getDebug() + ", indentLevel=" + restConfig.getIndentLevel() + ", showHiddenCommands="
                    + restConfig.getShowHiddenCommands() + ", wadlGeneration=" + restConfig.getWadlGeneration() + ", logOutput="
                    + restConfig.getLogOutput() + ", logInput=" + restConfig.getLogInput() + ", sessionTokenTimeout="
                    + restConfig.getSessionTokenTimeout());

            report.getTopMessagePart().addProperty("debug", restConfig.getDebug());
            report.getTopMessagePart().addProperty("indentLevel", restConfig.getIndentLevel());
            report.getTopMessagePart().addProperty("showHiddenCommands", restConfig.getShowHiddenCommands());
            report.getTopMessagePart().addProperty("showDeprecatedItems", restConfig.getShowDeprecatedItems());
            report.getTopMessagePart().addProperty("wadlGeneration", restConfig.getWadlGeneration());
            report.getTopMessagePart().addProperty("logOutput", restConfig.getLogOutput());
            report.getTopMessagePart().addProperty("logInput", restConfig.getLogInput());
            report.getTopMessagePart().addProperty("sessionTokenTimeout", "" + restConfig.getSessionTokenTimeout());
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
