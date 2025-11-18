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

package com.sun.enterprise.admin.commands;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JmxConnector;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Ssl command
 *
 * Usage: delete-ssl --type [http-listener|iiop-listener|iiop-service|protocol] [--terse=false] [--echo=false]
 * [--interactive=true] [--host localhost] [--port 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile
 * file_name] [--target target(Default server)] [listener_id|protocol_id]
 *
 * @author Nandini Ektare
 */
@Service(name = "delete-ssl")
@PerLookup
@I18n("delete.ssl")
@ExecuteOn({ RuntimeType.DAS, RuntimeType.INSTANCE })
@TargetType({ CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG })
public class DeleteSsl implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteSsl.class);

    @Param(name = "type", acceptableValues = "network-listener, http-listener, iiop-listener, iiop-service, jmx-connector, protocol")
    public String type;

    @Param(name = "listener_id", primary = true, optional = true)
    public String listenerId;

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    public String target;

    @Inject
    NetworkListeners networkListeners;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    public Config config;

    @Inject
    Domain domain;

    @Inject
    ServiceLocator habitat;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the parameter names and the
     * values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Target targetUtil = habitat.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig != null) {
            config = newConfig;
        }

        if (!type.equals("iiop-service")) {
            if (listenerId == null) {
                report.setMessage(localStrings.getLocalString("create.ssl.listenerid.missing", "Listener id needs to be specified"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        try {
            SslConfigHandler sslConfigHandler = habitat.getService(SslConfigHandler.class, type);
            if (sslConfigHandler != null) {
                sslConfigHandler.delete(this, report);
            } else if ("jmx-connector".equals(type)) {
                JmxConnector jmxConnector = null;
                for (JmxConnector listener : config.getAdminService().getJmxConnector()) {
                    if (listener.getName().equals(listenerId)) {
                        jmxConnector = listener;
                    }
                }

                if (jmxConnector == null) {
                    report.setMessage(localStrings.getLocalString("delete.ssl.jmx.connector.notfound", "Iiop Listener named {0} not found",
                            listenerId));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }

                if (jmxConnector.getSsl() == null) {
                    report.setMessage(localStrings.getLocalString("delete.ssl.element.doesnotexist",
                            "Ssl element does " + "not exist for Listener named {0}", listenerId));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }

                ConfigSupport.apply(new SingleConfigCode<JmxConnector>() {
                    public Object run(JmxConnector param) throws PropertyVetoException {
                        param.setSsl(null);
                        return null;
                    }
                }, jmxConnector);

                report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            }
        } catch (TransactionFailure e) {
            reportError(report, e);
        }
    }

    public void reportError(ActionReport report, Exception e) {
        report.setMessage(localStrings.getLocalString("delete.ssl.fail", "Deletion of Ssl in {0} failed", listenerId));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        report.setFailureCause(e);
    }
}
