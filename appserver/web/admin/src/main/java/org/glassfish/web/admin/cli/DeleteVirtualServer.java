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

import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.beans.PropertyVetoException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

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
 * Delete virtual server command
 *
 */
@Service(name="delete-virtual-server")
@PerLookup
@I18n("delete.virtual.server")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
public class DeleteVirtualServer implements AdminCommand {

    private static final ResourceBundle rb = LogFacade.getLogger().getResourceBundle();

    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Param(name="virtual_server_id", primary=true)
    String vsid;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    Domain domain;

    @Inject
    ServiceLocator services;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Server server;

    private HttpService httpService;
    private NetworkConfig networkConfig;

    /**
     * Executes the command with the command parameters passed as Properties
     * where the keys are the parameter names and the values the parameter values
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
        httpService = config.getHttpService();
        networkConfig = config.getNetworkConfig();

        if(!exists()) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_VIRTUAL_SERVER_NOT_EXISTS), vsid));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }

        // reference check
        String referencedBy = getReferencingListener();
        if(referencedBy != null && referencedBy.length() != 0) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_VIRTUAL_SERVER_REFERENCED), vsid, referencedBy));
            report.setActionExitCode(ExitCode.FAILURE);
            return;
        }

        try {

            // we need to determine which deployed applications reference this virtual-server
            List<ApplicationRef> appRefs = new ArrayList<ApplicationRef>();
            for (ApplicationRef appRef : server.getApplicationRef()) {
                if (appRef.getVirtualServers()!=null && appRef.getVirtualServers().contains(vsid)) {
                    appRefs.add(appRef);
                }
            }
            // transfer into the array of arguments
            ConfigBeanProxy[] proxies = new ConfigBeanProxy[appRefs.size()+1];
            proxies[0] = httpService;
            for (int i=0;i<appRefs.size();i++) {
                proxies[i+1] = appRefs.get(i);
            }

            ConfigSupport.apply(new ConfigUpdate(vsid), proxies);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        } catch(TransactionFailure e) {
            report.setMessage(MessageFormat.format(rb.getString(LogFacade.DELETE_VIRTUAL_SERVER_FAIL), vsid));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }

    private boolean exists() {
        if ((vsid == null) || (httpService == null))
            return false;

        List<VirtualServer> list = httpService.getVirtualServer();

        for(VirtualServer vs : list) {
            String currId = vs.getId();

            if(currId != null && currId.equals(vsid))
                return true;
        }
        return false;
    }

    private String getReferencingListener() {
        if (networkConfig != null) {
            List<NetworkListener> list = networkConfig.getNetworkListeners().getNetworkListener();

            for(NetworkListener listener: list) {
                String virtualServer = listener.findHttpProtocol().getHttp().getDefaultVirtualServer();

                if(virtualServer != null && virtualServer.equals(vsid)) {
                    return listener.getName();
                }
            }
        }
        return null;
    }

    private static class ConfigUpdate implements ConfigCode {
        private ConfigUpdate(String vsid) {
            this.vsid = vsid;
        }
        public Object run(ConfigBeanProxy... proxies) throws PropertyVetoException, TransactionFailure {
            List<VirtualServer> list = ((HttpService) proxies[0]).getVirtualServer();
            for(VirtualServer item : list) {
                String currId = item.getId();
                if (currId != null && currId.equals(vsid)) {
                    list.remove(item);
                    break;
                }
            }
            // we now need to remove the virtual server id from all application-ref passed.
            if (proxies.length>1) {
                // we have some appRefs to clean.
                for (int i=1;i<proxies.length;i++) {
                    ApplicationRef appRef = (ApplicationRef) proxies[i];
                    StringBuilder newList = new StringBuilder();
                    StringTokenizer st = new StringTokenizer(appRef.getVirtualServers(), ",");
                    while (st.hasMoreTokens()) {
                        final String id = st.nextToken();
                        if (!id.equals(vsid)) {
                            if (newList.length()>0) {
                                newList.append(",");
                            }
                            newList.append(id);
                        }
                    }
                    appRef.setVirtualServers(newList.toString());
                }
            }
            return list;
        }
        private String vsid;
    }
}
