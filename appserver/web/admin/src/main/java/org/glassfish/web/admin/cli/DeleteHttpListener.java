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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.glassfish.web.admin.LogFacade;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete http listener command
 */
@Service(name = "delete-http-listener")
@PerLookup
@I18n("delete.http.listener")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class DeleteHttpListener implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "listener_id", primary = true)
    String listenerId;
    @Param(name = "secure", optional = true)
    String secure;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DEFAULT_SERVER_INSTANCE_NAME)
    String target;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;
    private NetworkConfig networkConfig;

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
        networkConfig = config.getNetworkConfig();
        if (!exists()) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_HTTP_LISTENER_NOT_EXISTS), listenerId));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }
        try {
            NetworkListener ls = networkConfig.getNetworkListener(listenerId);
            final String name = ls.getProtocol();
            VirtualServer vs = config.getHttpService()
                .getVirtualServerByName(ls.findHttpProtocol().getHttp().getDefaultVirtualServer());
            ConfigSupport.apply(new DeleteNetworkListener(), networkConfig.getNetworkListeners());
            ConfigSupport.apply(new UpdateVirtualServer(), vs);
            cleanUp(name);
            report.setActionExitCode(ExitCode.SUCCESS);
        } catch (TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_HTTP_LISTENER_FAIL), listenerId));
            report.setActionExitCode(ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean exists() {
        if (networkConfig != null) {
            return networkConfig.getNetworkListener(listenerId) != null;
        } else {
            return false;
        }
    }

    private void cleanUp(String name) throws TransactionFailure {
        boolean found = false;
        if (networkConfig != null) {
            for (NetworkListener candidate : networkConfig.getNetworkListeners().getNetworkListener()) {
                found |= candidate.getProtocol().equals(name);
            }
            if (!found) {
                ConfigSupport.apply(new DeleteProtocol(name), networkConfig.getProtocols());
            }
        }
    }

    private class DeleteNetworkListener implements SingleConfigCode<NetworkListeners> {
        public Object run(NetworkListeners param) throws PropertyVetoException, TransactionFailure {
            final List<NetworkListener> list = param.getNetworkListener();
            for (NetworkListener listener : list) {
                if (listener.getName().equals(listenerId)) {
                    list.remove(listener);
                    break;
                }
            }
            return list;
        }
    }

    private class UpdateVirtualServer implements SingleConfigCode<VirtualServer> {
        public Object run(VirtualServer avs) throws PropertyVetoException {
            String lss = avs.getNetworkListeners();
            if (lss != null && lss.contains(listenerId)) { //change only if needed
                Pattern p = Pattern.compile(",");
                String[] names = p.split(lss);
                List<String> nl = new ArrayList<String>();
                for (String rawName : names) {
                    final String name = rawName.trim();
                    if (!listenerId.equals(name)) {
                        nl.add(name);
                    }
                }
                //we removed the listenerId from lss and is captured in nl by now
                lss = nl.toString();
                lss = lss.substring(1, lss.length() - 1);
                avs.setNetworkListeners(lss);
            }
            return avs;
        }
    }

    private static class DeleteProtocol implements SingleConfigCode<Protocols> {
        private final String name;

        public DeleteProtocol(String name) {
            this.name = name;
        }

        public Object run(Protocols param) throws PropertyVetoException, TransactionFailure {
            List<Protocol> list = new ArrayList<Protocol>(param.getProtocol());
            for (Protocol old : list) {
                if (name.equals(old.getName())) {
                    param.getProtocol().remove(old);
                    break;
                }
            }
            return param;
        }
    }
}
