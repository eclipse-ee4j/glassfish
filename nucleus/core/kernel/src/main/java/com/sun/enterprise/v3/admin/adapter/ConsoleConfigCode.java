/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.admin.adapter;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Engine;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemApplications;

import java.beans.PropertyVetoException;
import java.util.Arrays;
import java.util.List;

import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Create the Administration Console web application entry in {@code domain.xml}.
 */
class ConsoleConfigCode implements ConfigCode {

    private final List<String> virtualServers;
    private final String contextRoot;

    ConsoleConfigCode(List<String> virtualServers, String contextRoot) {
        this.virtualServers = virtualServers;
        this.contextRoot = contextRoot;
    }

    @Override
    public Object run(ConfigBeanProxy... proxies) throws PropertyVetoException, TransactionFailure {
        SystemApplications systemApplications = (SystemApplications) proxies[0];

        Application application = systemApplications.createChild(Application.class);
        systemApplications.getModules().add(application);

        application.setName(ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
        application.setEnabled(Boolean.TRUE.toString());
        application.setObjectType(DeploymentProperties.SYSTEM_ADMIN);
        application.setDirectoryDeployed("true");
        application.setContextRoot(contextRoot);
        try {
            application.setLocation("${com.sun.aas.installRootURI}/lib/install/applications/"
                    + ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
        } catch (Exception e) {
            // Can't do anything
            throw new RuntimeException(e);
        }

        Module consoleModule = application.createChild(Module.class);
        application.getModule().add(consoleModule);
        consoleModule.setName(application.getName());

        Engine webEngine = consoleModule.createChild(Engine.class);
        webEngine.setSniffer("web");

        Engine weldEngine = consoleModule.createChild(Engine.class);
        weldEngine.setSniffer("weld");

        Engine securityEngine = consoleModule.createChild(Engine.class);
        securityEngine.setSniffer("security");

        consoleModule.getEngines().add(webEngine);
        consoleModule.getEngines().add(weldEngine);
        consoleModule.getEngines().add(securityEngine);

        Server server = (Server) proxies[1];
        ApplicationRef applicationRef = server.createChild(ApplicationRef.class);
        applicationRef.setRef(application.getName());
        applicationRef.setEnabled(Boolean.TRUE.toString());
        applicationRef.setVirtualServers(getVirtualServerList());
        server.getApplicationRef().add(applicationRef);

        return true;
    }

    private String getVirtualServerList() {
        if (virtualServers == null) {
            return "";
        }

        String servers = Arrays.toString(virtualServers.toArray(String[]::new));
        // Remove [] if present
        if (servers.startsWith("[") && servers.endsWith("]")) {
            servers = servers.substring(1, servers.length() - 1);
        }
        return servers;
    }
}
