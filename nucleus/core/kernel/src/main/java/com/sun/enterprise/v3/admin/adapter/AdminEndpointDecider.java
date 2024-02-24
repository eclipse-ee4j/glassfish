/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.AdminService;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.v3.admin.AdminAdapter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.kernel.KernelLoggerInfo;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.config.types.Property;

/**
 * Makes various decisions about the admin adapters.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @since GlassFish V3 (March 2008)
 */
public final class AdminEndpointDecider {

    private static final Logger logger = KernelLoggerInfo.getLogger();

    public static final int ADMIN_PORT = 4848;

    private final Config config;
    private String asadminContextRoot;
    private String guiContextRoot;
    private List<String> asadminHosts; // List of virtual servers for asadmin
    private List<String> guiHosts;     // List of virtual servers for admin GUI
    private InetAddress address;
    private int port;  // Both asadmin and admin GUI are on same port
    private int maxThreadPoolSize = 5;

    public AdminEndpointDecider(Config config) {
        if (config == null || logger == null) {
            throw new IllegalArgumentException("config or logger can't be null");
        }
        this.config = config;
        setValues();
    }

    public int getListenPort() {
        return port;
    }

    public InetAddress getListenAddress() {
        return address;
    }

    public int getMaxThreadPoolSize() {
        return maxThreadPoolSize;
    }

    public List<String> getAsadminHosts() {
        return asadminHosts;
    }

    public List<String> getGuiHosts() {
        return guiHosts;
    }

    public String getAsadminContextRoot() {
        return asadminContextRoot;
    }

    public String getGuiContextRoot() {
        return guiContextRoot;
    }

    private void setValues() {
        asadminContextRoot = AdminAdapter.PREFIX_URI;  // Can't change

        NetworkListener adminListener = config.getAdminListener();
        ThreadPool threadPool = adminListener.findThreadPool();
        if (threadPool != null) {
            try {
                maxThreadPoolSize = Integer.parseInt(threadPool.getMaxThreadPoolSize());
            } catch (NumberFormatException ne) {
                logger.log(Level.WARNING, "Invalid maxThreadPoolSize value: {0}", threadPool.getMaxThreadPoolSize());
            }
        }
        String defaultVirtualServer = adminListener.findHttpProtocol().getHttp().getDefaultVirtualServer();
        guiHosts = List.of(defaultVirtualServer);
        asadminHosts = guiHosts;  // Same for now
        try {
            address = InetAddress.getByName(adminListener.getAddress());
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }

        if (ServerTags.ADMIN_LISTENER_ID.equals(adminListener.getName())) {
            // At the root context for separate admin-listener
            guiContextRoot = "";
            try {
                port = Integer.parseInt(adminListener.getPort());
            } catch(NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid admin port: {0}", adminListener.getPort());
                port = ADMIN_PORT;
            }
        } else {
            try {
                port = Integer.parseInt(adminListener.getPort());
            } catch (NumberFormatException e) {
                logger.log(Level.WARNING, "Invalid admin port: {0}", adminListener.getPort());
                port = 8080;   // This is the last resort
            }

            // Get the context root from admin-service
            AdminService adminService = config.getAdminService();
            if (adminService == null) {
                guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            } else {
                setGuiContextRootFromAdminService(adminService);
            }
        }
    }

    private void setGuiContextRootFromAdminService(AdminService adminService) {
        for (Property property : adminService.getProperty()) {
            setGuiContextRoot(property);
        }
    }

    private void setGuiContextRoot(Property property) {
        if (property == null) {
            guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            return;
        }

        if (ServerTags.ADMIN_CONSOLE_CONTEXT_ROOT.equals(property.getName())) {
            if (property.getValue() != null && property.getValue().startsWith("/")) {
                guiContextRoot = property.getValue();
                logger.log(Level.INFO, KernelLoggerInfo.contextRoot, guiContextRoot);
            } else {
                logger.log(Level.INFO, KernelLoggerInfo.invalidContextRoot,
                        ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT);
                guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            }
        }
    }
}
