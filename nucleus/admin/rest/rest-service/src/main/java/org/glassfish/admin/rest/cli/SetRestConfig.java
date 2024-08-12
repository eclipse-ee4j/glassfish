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

package org.glassfish.admin.rest.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;

import org.glassfish.admin.restconnector.RestConfig;
import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Remote asadmin command: set-rest-config
 *
 * Purpose: Allows the invoker to configure the REST module.
 *
 *
 *
 * @author Ludovic Champenois
 *
 */
@Service(name = "_set-rest-admin-config")
@PerLookup
@ExecuteOn(RuntimeType.DAS)
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({ @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.POST) })
public class SetRestConfig implements AdminCommand {

    @AccessRequired.To("update")
    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    private ServiceLocator habitat;
    @Param(optional = true)
    private String debug;
    @Param(optional = true, defaultValue = "-100")
    private int indentLevel;
    @Param(optional = true)
    private String wadlGeneration;
    @Param(optional = true)
    private String showHiddenCommands;
    @Param(optional = true)
    private String showDeprecatedItems;
    @Param(optional = true)
    private String logOutput;
    @Param(optional = true)
    private String logInput;
    @Param(optional = true)
    private String sessionTokenTimeout;

    @Override
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();

        RestConfig restConfig = config.getExtensionByType(RestConfig.class);

        /**
         * The schedules does not exist in this Config. We will need to add it plus the default schedules.
         */
        if (restConfig == null) {
            try {
                ConfigSupport.apply(new SingleConfigCode<Config>() {

                    @Override
                    public Object run(Config parent) throws TransactionFailure {
                        RestConfig child = parent.createChild(RestConfig.class);
                        parent.getContainers().add(child);
                        return child;
                    }
                }, config);
            } catch (TransactionFailure e) {
                report.setMessage("TransactionFailure failure while creating the REST config");
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setFailureCause(e);
                return;
            }

            restConfig = config.getExtensionByType(RestConfig.class);
            if (restConfig == null) {
                report.setMessage("Rest Config is NULL...");
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<RestConfig>() {

                @Override
                public Object run(RestConfig param) throws TransactionFailure, PropertyVetoException {
                    if (debug != null) {
                        param.setDebug(debug);
                    }
                    if (indentLevel != -100) {
                        param.setIndentLevel("" + indentLevel);
                    }
                    if (showHiddenCommands != null) {
                        param.setShowHiddenCommands(showHiddenCommands);
                    }
                    if (showDeprecatedItems != null) {
                        param.setShowDeprecatedItems(showDeprecatedItems);
                    }
                    if (wadlGeneration != null) {
                        param.setWadlGeneration(wadlGeneration);
                    }
                    if (logOutput != null) {
                        param.setLogOutput(logOutput);
                    }
                    if (logInput != null) {
                        param.setLogInput(logInput);
                    }
                    if (sessionTokenTimeout != null) {
                        param.setSessionTokenTimeout(sessionTokenTimeout);
                    }

                    return param;
                }
            }, restConfig);
        } catch (TransactionFailure e) {
            report.setMessage("TransactionFailure while changing the REST config");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        return;
    }
}
