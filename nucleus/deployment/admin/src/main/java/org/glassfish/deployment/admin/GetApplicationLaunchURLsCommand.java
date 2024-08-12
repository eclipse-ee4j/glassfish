/*
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

package org.glassfish.deployment.admin;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.StringUtils;

import jakarta.inject.Inject;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name="_get-application-launch-urls")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
    @RestEndpoint(configBean=Applications.class, opType= RestEndpoint.OpType.GET, path="_get-application-launch-urls", description="Get Urls for launch the application")
})
public class GetApplicationLaunchURLsCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(primary=true)
    private String appname = null;

    @Inject
    Domain domain;

    @Inject
    CommandRunner commandRunner;

    private List<Server> servers;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        List<String> targets = domain.getAllReferencedTargetsForApplication(appname);
        for (String target : targets) {
            if (domain.isAppEnabledInTarget(appname, target)) {
                servers = new ArrayList<Server>();
                Cluster cluster = domain.getClusterNamed(target);
                if (cluster != null) {
                    servers = cluster.getInstances();
                }
                Server server = domain.getServerNamed(target);
                if (server != null) {
                    servers.add(server);
                }
                for (Server svr : servers) {
                    accessChecks.add(new AccessCheck(DeploymentCommandUtils.getTargetResourceNameForExistingAppRef(domain, svr.getName(), appname), "read"));
                }
            }
        }
        return accessChecks;
    }


    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        Logger logger = context.getLogger();
        getLBLaunchURLInformation(appname, report, context.getSubject());
        List<URL> launchURLs = getLaunchURLInformation(appname, logger);
        if(launchURLs == null || launchURLs.isEmpty()){
            return;
        }
        ActionReport.MessagePart part = report.getTopMessagePart();
        //Adding a new part for adding instance(s) urls
        part = part.addChild();
        part.setMessage("Instances");
        int j = 0;
        for (URL url : launchURLs) {
            ActionReport.MessagePart childPart = part.addChild();
            childPart.setMessage(Integer.toString(j++));
            childPart.addProperty(DeploymentProperties.PROTOCOL,
                url.getProtocol());
            childPart.addProperty(DeploymentProperties.HOST,
                url.getHost());
            childPart.addProperty(DeploymentProperties.PORT,
                String.valueOf(url.getPort()));
            childPart.addProperty(DeploymentProperties.CONTEXT_PATH,
                url.getPath());
        }
    }

    private void getLBLaunchURLInformation(String appName, ActionReport report, final Subject subject){
        CommandRunner.CommandInvocation invocation =
                commandRunner.getCommandInvocation("_get-lb-launch-urls", report, subject);
        if(invocation != null){
            ParameterMap map = new ParameterMap();
            map.add("appname", appName);
            invocation.parameters(map).execute();
        }
    }

    private List<URL> getLaunchURLInformation(String appName, Logger logger) {
        List<URL> launchURLs = new ArrayList<URL>();
        String contextRoot = getContextRoot(appName);

        for (Server svr : servers) {
            launchURLs.addAll(getURLsForServer(svr, appName, contextRoot, logger));
        }
        return launchURLs;
    }

    private String getContextRoot(String appName) {
        Application application = domain.getApplications().getApplication(appName);
        if(application == null){
            return "";
        }
        String contextRoot = application.getContextRoot();
        // non standalone war cases
        if (contextRoot == null) {
            contextRoot = "";
        }
        return contextRoot;
    }

    private List<URL> getURLsForServer(Server server, String appName, String contextRoot, Logger logger) {
        List<URL> serverURLs = new ArrayList<URL>();

        String virtualServers = server.getApplicationRef(appName).getVirtualServers();
        if (virtualServers == null || virtualServers.trim().equals("")) {
            return serverURLs;
        }

        String nodeName = server.getNodeRef();
        String host = null;
        if (nodeName != null) {
            Node node = domain.getNodeNamed(nodeName);
            host = node.getNodeHost();
        }
        if (host == null || host.trim().equals("") || host.trim().equalsIgnoreCase("localhost")) {
            host = DeploymentCommandUtils.getLocalHostName();
        }

        List<String> vsList = StringUtils.parseStringList(virtualServers, " ,");
        Config config =  domain.getConfigNamed(server.getConfigRef());
        HttpService httpService = config.getHttpService();
        for (String vsName : vsList) {
            VirtualServer vs = httpService.getVirtualServerByName(vsName);
            String vsHttpListeners = vs.getNetworkListeners();
            if (vsHttpListeners == null || vsHttpListeners.trim().equals("")) {
                continue;
            }
            List<String> vsHttpListenerList =
                StringUtils.parseStringList(vsHttpListeners, " ,");
            List<NetworkListener> httpListenerList = config.getNetworkConfig().getNetworkListeners().getNetworkListener();

            for (String vsHttpListener : vsHttpListenerList) {
                for (NetworkListener httpListener : httpListenerList) {
                    if (!httpListener.getName().equals(vsHttpListener)) {
                        continue;
                    }
                    if (!Boolean.valueOf(httpListener.getEnabled())) {
                        continue;
                    }
                    Protocol protocol = httpListener.findHttpProtocol();
                    //Do not include jk enabled listeners
                    if(Boolean.valueOf(protocol.getHttp().getJkEnabled())){
                        continue;
                    }
                    boolean securityEnabled = Boolean.valueOf(protocol.getSecurityEnabled());
                    String proto = (securityEnabled ? "https" : "http");
                    String portStr = httpListener.getPort();
                    String redirPort = protocol.getHttp().getRedirectPort();
                    if (redirPort != null && !redirPort.trim().equals("")) {
                        portStr = redirPort;
                    }
                    // we need to resolve port for non-DAS instances
                    if (!DeploymentUtils.isDASTarget(server.getName())) {
                        PropertyResolver resolver = new PropertyResolver(domain, server.getName());
                        portStr = resolver.getPropertyValue(portStr);
                    }
                    try {
                        int port = Integer.parseInt(portStr);
                        URL url = new URL(proto, host, port, contextRoot);
                        serverURLs.add(url);
                    } catch (Exception ee) {
                        logger.log(Level.WARNING, ee.getMessage(), ee);
                    }
                }
            }
        }
        return serverURLs;
    }
}
