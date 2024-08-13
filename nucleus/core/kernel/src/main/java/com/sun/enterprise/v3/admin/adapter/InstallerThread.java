/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.v3.server.ApplicationLoaderService;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.ConfigSupport;

/**
 * Install and load console application.
 *
 * @author kedar
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
final class InstallerThread extends Thread {

    private final Logger logger = KernelLoggerInfo.getLogger();

    private final Domain domain;
    private final ServerEnvironmentImpl serverEnvironment;
    private final String contextRoot;
    private final AdminConsoleAdapter adapter;
    private final ServiceLocator serviceLocator;
    private final List<String> virtualServers;


    /**
     * Constructor.
     */
    InstallerThread(AdminConsoleAdapter adapter, ServiceLocator serviceLocator, Domain domain,
            ServerEnvironmentImpl serverEnvironment, String contextRoot, List<String> virtualServers) {
        this.adapter = adapter;
        this.serviceLocator = serviceLocator;
        this.domain = domain;
        this.serverEnvironment = serverEnvironment;
        this.contextRoot = contextRoot;
        // Defensive copying is not required here
        this.virtualServers = virtualServers;
    }

    /**
     *
     */
    @Override
    public void run() {
        try {
            // The following are the basic steps which are required to get the
            // Admin Console web application running.  Each step ensures that
            // it has not already been completed and adjusts the state message
            // accordingly.
            install();
            load();

            // From within this Thread mark the installation process complete
            adapter.setInstalling(false);
        } catch (Exception e) {
            adapter.setInstalling(false);
            adapter.setStateMsg(AdapterState.APPLICATION_NOT_INSTALLED);
            logger.log(Level.INFO, KernelLoggerInfo.adminGuiInstallProblem, e);
        }
    }


    /**
     * Install the Admin Console web application.
     */
    private void install() throws Exception {
        if (domain.getSystemApplicationReferencedFrom(serverEnvironment.getInstanceName(),
                ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME) != null) {
            // Application is already installed
            adapter.setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);
            return;
        }

        // Set the adapter state
        adapter.setStateMsg(AdapterState.INSTALLING);

        logger.log(Level.FINE, "Installing the Admin Console Application...");

        ConsoleConfigCode code = new ConsoleConfigCode(virtualServers, contextRoot);
        Server instance = domain.getServerNamed(serverEnvironment.getInstanceName());

        ConfigSupport.apply(code, domain.getSystemApplications(), instance);

        // Set the adapter state
        adapter.setStateMsg(AdapterState.APPLICATION_INSTALLED_BUT_NOT_LOADED);

        logger.log(Level.FINE, "Admin Console Application Installed.");
    }

    /**
     * Load the Admin Console web application.
     */
    private void load() {
        ApplicationRegistry applicationRegistry = serviceLocator.getService(ApplicationRegistry.class);
        ApplicationInfo applicationInfo = applicationRegistry.get(ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
        if (applicationInfo != null && applicationInfo.isLoaded()) {
            // Application is already loaded
            adapter.setStateMsg(AdapterState.APPLICATION_LOADED);
            return;
        }

        Application config = adapter.getConfig();
        if (config == null) {
            throw new IllegalStateException("Admin Console application has no system app entry!");
        }

        // Set adapter state
        adapter.setStateMsg(AdapterState.APPLICATION_LOADING);

        // Load the Admin Console Application
        String instance = serverEnvironment.getInstanceName();

        ApplicationRef applicationRef = domain.getApplicationRefInServer(instance,
                ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_APP_NAME);
        serviceLocator.getService(ApplicationLoaderService.class).processApplication(config, applicationRef);

        // Set adapter state
        adapter.setStateMsg(AdapterState.APPLICATION_LOADED);
    }
}
