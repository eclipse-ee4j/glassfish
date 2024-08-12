/*
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
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

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
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete Network Listener command
 */
@Service(name = "delete-network-listener")
@PerLookup
@I18n("delete.network.listener")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class DeleteNetworkListener implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "networkListenerName", primary = true)
    String networkListenerName;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;
    NetworkListener listenerToBeRemoved = null;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    ServiceLocator services;
    @Inject
    Domain domain;

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
        ActionReport report = context.getActionReport();
        NetworkListeners networkListeners = config.getNetworkConfig().getNetworkListeners();
        try {
            if (findListener(networkListeners, report)) {
                final Protocol httpProtocol = listenerToBeRemoved.findHttpProtocol();
                final VirtualServer virtualServer = config.getHttpService().getVirtualServerByName(
                        httpProtocol.getHttp().getDefaultVirtualServer());

                ConfigSupport.apply(new ConfigCode() {
                    public Object run(ConfigBeanProxy... params) throws PropertyVetoException {
                        final NetworkListeners listeners = (NetworkListeners) params[0];
                        final VirtualServer server = (VirtualServer) params[1];
                        listeners.getNetworkListener().remove(listenerToBeRemoved);
                        server.removeNetworkListener(listenerToBeRemoved.getName());
                        return listenerToBeRemoved;
                    }
                }, networkListeners, virtualServer);

            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_NETWORK_LISTENER_FAIL), networkListenerName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean findListener(final NetworkListeners networkListeners, ActionReport report) {
        for (NetworkListener listener : networkListeners.getNetworkListener()) {
            if(listener.getName().equals(networkListenerName)) {
                listenerToBeRemoved = listener;
            }
        }
        if (listenerToBeRemoved == null) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_NETWORK_LISTENER_NOT_EXISTS), networkListenerName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }

}
