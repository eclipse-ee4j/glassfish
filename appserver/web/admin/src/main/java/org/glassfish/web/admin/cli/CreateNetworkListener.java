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

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.SystemPropertyConstants;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ProtocolFinder;
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
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Command to create Network Listener
 */
@Service(name = "create-network-listener")
@PerLookup
@I18n("create.network.listener")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class CreateNetworkListener implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "address", optional = true)
    String address;
    @Param(name = "listenerport", optional = false, alias="Port")
    String port;
    @Param(name = "threadpool", optional = true, defaultValue = "http-thread-pool", alias="threadPool")
    String threadPool;
    @Param(name = "protocol", optional = false)
    String protocol;
    @Param(name = "name", primary = true)
    String listenerName;
    @Param(name = "transport", optional = true, defaultValue = "tcp")
    String transport;
    @Param(name = "enabled", optional = true, defaultValue = "true")
    Boolean enabled;
    @Param(name = "jkenabled", optional = true, defaultValue = "false", alias = "jkEnabled")
    Boolean jkEnabled;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
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
        final ActionReport report = context.getActionReport();
        NetworkConfig networkConfig = config.getNetworkConfig();
        NetworkListeners nls = networkConfig.getNetworkListeners();
        // ensure we don't have one of this name already
        for (NetworkListener networkListener : nls.getNetworkListener()) {
            if (networkListener.getName().equals(listenerName)) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_NETWORK_LISTENER_FAIL_DUPLICATE), listenerName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
        if (!verifyUniquePort(networkConfig)) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.PORT_IN_USE), port, address));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        Protocol prot = networkConfig.findProtocol(protocol);
        if (prot == null) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_HTTP_FAIL_PROTOCOL_NOT_FOUND), protocol));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        if (prot.getHttp() == null && prot.getPortUnification() == null) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_NETWORK_LISTENER_FAIL_BAD_PROTOCOL), protocol));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }
        try {
            ConfigSupport.apply(new ConfigCode() {
                public Object run(ConfigBeanProxy... params) throws TransactionFailure, PropertyVetoException {
                    NetworkListeners listeners = (NetworkListeners) params[0];
                    NetworkListener newNetworkListener = listeners.createChild(NetworkListener.class);
                    newNetworkListener.setProtocol(protocol);
                    newNetworkListener.setTransport(transport);
                    newNetworkListener.setEnabled(enabled.toString());
                    newNetworkListener.setJkEnabled(jkEnabled.toString());
                    newNetworkListener.setPort(port);
                    newNetworkListener.setThreadPool(threadPool);
                    newNetworkListener.setName(listenerName);
                    newNetworkListener.setAddress(address);
                    listeners.getNetworkListener().add(newNetworkListener);
                    ((VirtualServer) params[1]).addNetworkListener(listenerName);
                    return newNetworkListener;
                }
            }, nls, findVirtualServer(prot));
        } catch (TransactionFailure e) {
            e.printStackTrace();
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_NETWORK_LISTENER_FAIL), listenerName) +
                    (e.getMessage() == null ? "No reason given" : e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private VirtualServer findVirtualServer(Protocol protocol) {
        String name = null;
        final Http http = protocol.getHttp();
        if (http != null) {
            name = http.getDefaultVirtualServer();
        } else {
            final List<ProtocolFinder> finders = protocol.getPortUnification().getProtocolFinder();
            for (ProtocolFinder finder : finders) {
                if (name == null) {
                    final Protocol p = finder.findProtocol();
                    if (p.getHttp() != null) {
                        name = p.getHttp().getDefaultVirtualServer();
                    }
                }
            }
        }

        return config.getHttpService().getVirtualServerByName(name);
    }

    private boolean verifyUniquePort(NetworkConfig networkConfig) {
        //check port uniqueness, only for same address
        for (NetworkListener listener : networkConfig.getNetworkListeners()
            .getNetworkListener()) {
            if (listener.getPort().trim().equals(port) &&
                listener.getAddress().trim().equals(address)) {
                return false;
            }
        }
        return true;
    }
}
