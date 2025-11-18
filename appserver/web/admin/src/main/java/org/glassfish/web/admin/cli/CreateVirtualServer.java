/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.config.ModelBinding;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

/**
 * Command to create virtual server
 */
@Service(name = "create-virtual-server")
@PerLookup
@I18n("create.virtual.server")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class CreateVirtualServer implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "hosts", defaultValue = "${com.sun.aas.hostName}")
    String hosts;
    @Param(name = "httplisteners", optional = true)
    String httpListeners;
    @Param(name = "networklisteners", optional = true)
    String networkListeners;
    @Param(name = "defaultwebmodule", optional = true)
    String defaultWebModule;
    @Param(name = "state", defaultValue = "on", acceptableValues = "on, off, disabled", optional = true)
    String state;
    @Param(name = "logfile", optional = true)
    @ModelBinding(type=VirtualServer.class, getterMethodName ="getLogFile")
    String logFile;
    @Param(name = "property", optional = true, separator = ':')
    Properties properties;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
    @Param(name = "virtual_server_id", primary = true)
    String virtualServerId;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        final ActionReport report = context.getActionReport();
        if (networkListeners != null && httpListeners != null) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_VIRTUAL_SERVER_BOTH_HTTP_NETWORK), virtualServerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        //use the listener parameter provided by the user.
        if (networkListeners == null) {
            networkListeners = httpListeners;
        }
        HttpService httpService = config.getHttpService();
        // ensure we don't already have one of this name
        for (VirtualServer virtualServer : httpService.getVirtualServer()) {
            if (virtualServer.getId().equals(virtualServerId)) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_VIRTUAL_SERVER_DUPLICATE), virtualServerId));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        try {
            ConfigSupport.apply(new SingleConfigCode<HttpService>() {
                public Object run(HttpService param) throws PropertyVetoException, TransactionFailure {
                    String docroot = "${com.sun.aas.instanceRoot}/docroot";    // default
                    String accessLog = "${com.sun.aas.instanceRoot}/logs/access";    // default

                    VirtualServer newVirtualServer = param.createChild(VirtualServer.class);
                    newVirtualServer.setId(virtualServerId);
                    newVirtualServer.setHosts(hosts);
                    newVirtualServer.setNetworkListeners(networkListeners);
                    newVirtualServer.setDefaultWebModule(defaultWebModule);
                    newVirtualServer.setState(state);
                    newVirtualServer.setLogFile(logFile);
                    // 1. add properties
                    // 2. check if the access-log and docroot properties have
                    //    been specified. We will use with default
                    //    values if the properties have not been specified.
                    if (properties != null) {
                        for (Map.Entry entry : properties.entrySet()) {
                            String pn = (String) entry.getKey();
                            String pv = (String)entry.getValue();

                            if ("docroot".equals(pn)) {
                                docroot = pv;
                            } else if ("accesslog".equals(pn)) {
                                accessLog = pv;
                            } else {
                                Property property = newVirtualServer.createChild(Property.class);
                                property.setName(pn);
                                property.setValue(pv);
                                newVirtualServer.getProperty().add(property);
                            }
                        }
                    }

                    newVirtualServer.setDocroot(docroot);
                    newVirtualServer.setAccessLog(accessLog);

                    param.getVirtualServer().add(newVirtualServer);
                    return newVirtualServer;
                }
            }, httpService);

        } catch (TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_VIRTUAL_SERVER_FAIL), virtualServerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
