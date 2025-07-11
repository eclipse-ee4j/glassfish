/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transactions;

/**
 * Create Http Listener Command
 */
@Service(name = "create-http-listener")
@PerLookup
@I18n("create.http.listener")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class CreateHttpListener implements AdminCommand {

    @Param(name = "listeneraddress")
    String listenerAddress;
    @Param(name = "listenerport")
    String listenerPort;
    @Param(name = "defaultvs", optional = true)
    String defaultVS;
    @Param(name = "default-virtual-server", optional = true)
    String defaultVirtualServer;
    @Param(name = "servername", optional = true)
    String serverName;
    @Param(name = "xpowered", optional = true, defaultValue = "true")
    Boolean xPoweredBy;
    @Param(name = "acceptorthreads", optional = true)
    String acceptorThreads;
    @Param(name = "redirectport", optional = true)
    String redirectPort;
    @Param(name = "securityenabled", optional = true, defaultValue = "false")
    Boolean securityEnabled;
    @Param(optional = true, defaultValue = "true")
    Boolean enabled;
    @Param(optional = true, defaultValue = "false")
    Boolean secure; //FIXME
    @Param(name = "listener_id", primary = true)
    String listenerId;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    ServiceLocator services;
    @Inject
    CommandRunner runner;
    @Inject
    Domain domain;
    private static final String DEFAULT_TRANSPORT = "tcp";
    private NetworkConfig networkConfig = null;

    private static final Logger logger = LogFacade.getLogger();

    private static final ResourceBundle rb = logger.getResourceBundle();

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and
     * the values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        if(!validateInputs(report)) {
            return;
        }
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig!=null) {
            config = newConfig;
        }
        networkConfig = config.getNetworkConfig();
        HttpService httpService = config.getHttpService();
        if (!(verifyUniqueName(report, networkConfig) && verifyUniquePort(report, networkConfig)
            && verifyDefaultVirtualServer(report))) {
            return;
        }
        VirtualServer vs = httpService.getVirtualServerByName(defaultVirtualServer);
        boolean listener = false;
        boolean protocol = false;
        boolean transport = false;
        try {
            transport = createOrGetTransport(null);
            protocol = createProtocol(context);
            createHttp(context);
            final ThreadPool threadPool = getThreadPool(networkConfig);
            listener = createNetworkListener(networkConfig, transport, threadPool);
            updateVirtualServer(vs);
        } catch (TransactionFailure e) {
            try {
                if (listener) {
                    deleteListener(context);
                }
                if (protocol) {
                    deleteProtocol(context);
                }
                if (transport) {
                    deleteTransport(context);
                }
            } catch (Exception e1) {
                if (logger.isLoggable(Level.INFO)) {
                    logger.log(Level.INFO, e.getMessage(), e);
                }
                throw new RuntimeException(e.getMessage());
            }
            if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, e.getMessage(), e);
            }
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_HTTP_LISTENER_FAIL), listenerId, e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }

    private void updateVirtualServer(VirtualServer vs) throws TransactionFailure {
        //now change the associated virtual server
        ConfigSupport.apply(new SingleConfigCode<VirtualServer>() {
            @Override
            public Object run(VirtualServer avs) throws PropertyVetoException {
                String DELIM = ",";
                String lss = avs.getNetworkListeners();
                boolean listenerShouldBeAdded = true;
                if (lss == null || lss.length() == 0) {
                    lss = listenerId; //the only listener in the list
                } else if (!lss.contains(listenerId)) { //listener does not already exist
                    if (!lss.endsWith(DELIM)) {
                        lss += DELIM;
                    }
                    lss += listenerId;
                } else { //listener already exists in the list, do nothing
                    listenerShouldBeAdded = false;
                }
                if (listenerShouldBeAdded) {
                    avs.setNetworkListeners(lss);
                }
                return avs;
            }
        }, vs);
    }

    private boolean createNetworkListener(NetworkConfig networkConfig, final boolean newTransport,
        final ThreadPool threadPool) throws TransactionFailure {
        ConfigSupport.apply(new SingleConfigCode<NetworkListeners>() {
            @Override
            public Object run(NetworkListeners listenersParam)
                throws TransactionFailure {
                final NetworkListener newListener = listenersParam.createChild(NetworkListener.class);
                newListener.setName(listenerId);
                newListener.setAddress(listenerAddress);
                newListener.setPort(listenerPort);
                newListener.setTransport(newTransport ? listenerId : DEFAULT_TRANSPORT);
                newListener.setProtocol(listenerId);
                newListener.setThreadPool(threadPool.getName());
                newListener.setEnabled(enabled.toString());
                listenersParam.getNetworkListener().add(newListener);
                return newListener;
            }
        }, networkConfig.getNetworkListeners());
        services.<Transactions>getService(Transactions.class).waitForDrain();
        return true;
    }

    private boolean verifyDefaultVirtualServer(ActionReport report) {
        if (defaultVS == null && defaultVirtualServer == null) {
            report.setMessage(rb.getString(LogFacade.CREATE_HTTP_LISTENER_VS_BLANK));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        if (defaultVS != null && defaultVirtualServer != null && !defaultVS.equals(defaultVirtualServer)) {
            report.setMessage(rb.getString(LogFacade.CREATE_HTTP_LISTENER_VS_BOTH_PARAMS));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        } else if (defaultVirtualServer == null && defaultVS != null) {
            defaultVirtualServer = defaultVS;
        }
        //no need to check the other things (e.g. id) for uniqueness
        // ensure that the specified default virtual server exists
        if (!defaultVirtualServerExists()) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_HTTP_LISTENER_VS_NOTEXISTS), defaultVirtualServer));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }

    private boolean verifyUniquePort(ActionReport report, NetworkConfig networkConfig) {
        //check port uniqueness, only for same address
        for (NetworkListener listener : networkConfig.getNetworkListeners()
            .getNetworkListener()) {
            if (listener.getPort().trim().equals(listenerPort) &&
                listener.getAddress().trim().equals(listenerAddress)) {
                String def = "Port is already taken by another listener, choose another port.";
                String msg = MessageFormat.format(rb.getString(LogFacade.PORT_IN_USE), listenerPort, listenerAddress);
                report.setMessage(msg);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }
        return true;
    }

    private boolean validateInputs(final ActionReport report) {
        if(acceptorThreads != null && Integer.parseInt(acceptorThreads) < 1) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.ACCEPTOR_THREADS_TOO_LOW), listenerId));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return false;
        }
        return true;
    }
    private boolean verifyUniqueName(ActionReport report, NetworkConfig networkConfig) {
        // ensure we don't already have one of this name
        for (NetworkListener listener : networkConfig.getNetworkListeners().getNetworkListener()) {
            if (listener.getName().equals(listenerId)) {
                report.setMessage(MessageFormat.format(rb.getString(LogFacade.CREATE_HTTP_LISTENER_DUPLICATE), listenerId));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }
        return true;
    }

    private ThreadPool getThreadPool(NetworkConfig config) {
        final List<ThreadPool> pools = config.getParent(Config.class).getThreadPools().getThreadPool();
        ThreadPool target = null;
        for (ThreadPool pool : pools) {
            if ("http-thread-pool".equals(pool.getName())) {
                target = pool;
            }
        }
        if (target == null && !pools.isEmpty()) {
            target = pools.get(0);
        }
        return target;
    }

    private boolean createOrGetTransport(final AdminCommandContext context) throws TransactionFailure {
        boolean newTransport = false;
        for (Transport t : networkConfig.getTransports().getTransport()) {
            if (!t.getName().equals(DEFAULT_TRANSPORT)) {
                 newTransport = true;
            }
        }
        if (newTransport) {
            final CreateTransport command = (CreateTransport) runner
                .getCommand("create-transport", context.getActionReport());
            command.transportName = listenerId;
            command.acceptorThreads = acceptorThreads;
            command.target = target;
            command.execute(context);
            checkProgress(context);
            newTransport = true;
        }
        return newTransport;
    }

    private boolean createProtocol(final AdminCommandContext context) throws TransactionFailure {
        final CreateProtocol command = (CreateProtocol) runner
            .getCommand("create-protocol", context.getActionReport());
        command.protocolName = listenerId;
        command.securityEnabled = securityEnabled;
        command.target = target;
        command.execute(context);
        checkProgress(context);
        return true;
    }

    private boolean createHttp(final AdminCommandContext context) throws TransactionFailure {
        final CreateHttp command = (CreateHttp) runner
            .getCommand("create-http", context.getActionReport());
        command.protocolName = listenerId;
        command.defaultVirtualServer = defaultVirtualServer;
        command.xPoweredBy = xPoweredBy;
        command.serverName = serverName;
        command.target = target;
        command.execute(context);
        checkProgress(context);
        return true;
    }

    private void checkProgress(final AdminCommandContext context) throws TransactionFailure {
        if(context.getActionReport().getActionExitCode() != ExitCode.SUCCESS) {
            throw new TransactionFailure(context.getActionReport().getMessage());
        }
    }

    private boolean deleteProtocol(final AdminCommandContext context) {
        final DeleteProtocol command = (DeleteProtocol) runner
            .getCommand("delete-protocol", context.getActionReport());
        command.protocolName = listenerId;
        command.target = target;
        command.execute(context);
        return true;
    }

    private boolean deleteTransport(final AdminCommandContext context) {
        final DeleteTransport command = (DeleteTransport) runner
            .getCommand("delete-transport", context.getActionReport());
        command.transportName = listenerId;
        command.target = target;
        command.execute(context);
        return true;
    }

    private boolean deleteListener(final AdminCommandContext context) {
        final DeleteNetworkListener command = (DeleteNetworkListener) runner
            .getCommand("delete-network-listener", context.getActionReport());
        command.networkListenerName = listenerId;
        command.target = target;
        command.execute(context);
        return true;
    }

    private boolean defaultVirtualServerExists() {
        return (defaultVirtualServer != null) && (
                config.getHttpService().getVirtualServerByName(defaultVirtualServer) != null);
    }
}
