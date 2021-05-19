/*
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
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.jvnet.hk2.config.types.Property;
import org.glassfish.server.ServerEnvironmentImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.kernel.KernelLoggerInfo;

/** Makes various decisions about the admin adapters.
 *
 * @author &#2325;&#2375;&#2342;&#2366;&#2352; (km@dev.java.net)
 * @since GlassFish V3 (March 2008)
 */
public final class AdminEndpointDecider {

    private String asadminContextRoot;
    private String guiContextRoot;
    private List<String> asadminHosts; //list of virtual servers for asadmin
    private List<String> guiHosts;     //list of virtual servers for admin GUI

    private int port;  // both asadmin and admin GUI are on same port
    private InetAddress address;
    private int maxThreadPoolSize = 5;
    private Config cfg;
    private Logger log = KernelLoggerInfo.getLogger();

    public static final int ADMIN_PORT           = 4848;

    public AdminEndpointDecider(Config cfg) {
        if (cfg == null || log == null)
            throw new IllegalArgumentException("config or logger can't be null");
        this.cfg = cfg;
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
        asadminContextRoot = AdminAdapter.PREFIX_URI;  //can't change
        //asadminHosts       = Collections.emptyList();  //asadmin is handled completely by the adapter, no VS needed
        NetworkListener nl = cfg.getAdminListener();
        ThreadPool tp = nl.findThreadPool();
        if (tp != null) {
            try {
                maxThreadPoolSize = Integer.parseInt(tp.getMaxThreadPoolSize());
            } catch (NumberFormatException ne) {
            }
        }
        String dvs     = nl.findHttpProtocol().getHttp().getDefaultVirtualServer();
        guiHosts       = Collections.unmodifiableList(Arrays.asList(dvs));
        asadminHosts   = guiHosts;  //same for now
        try {
            address = InetAddress.getByName(nl.getAddress());
        } catch (UnknownHostException e) {
            throw new IllegalStateException(e);
        }
        if (ServerTags.ADMIN_LISTENER_ID.equals(nl.getName())) {
            guiContextRoot = "";  //at the root context for separate admin-listener
            try {
                port = Integer.parseInt(nl.getPort());
            } catch(NumberFormatException ne) {
                port = ADMIN_PORT;
            }
        }
        else {
            try {
                port = Integer.parseInt(nl.getPort());
            } catch(NumberFormatException ne) {
                port = 8080;   // this is the last resort
            }
            //get the context root from admin-service
            AdminService as = cfg.getAdminService();
            if (as == null)
                guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            else
                setGuiContextRootFromAdminService(as);
        }
    }

    private void setGuiContextRootFromAdminService(AdminService as) {
        for (Property p : as.getProperty()) {
            setGuiContextRoot(p);
        }
    }
    private void setGuiContextRoot(Property prop) {
        if (prop == null) {
            guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            return;
        }
        if (ServerTags.ADMIN_CONSOLE_CONTEXT_ROOT.equals(prop.getName())) {
            if (prop.getValue() != null && prop.getValue().startsWith("/")) {
                guiContextRoot = prop.getValue();
                log.log(Level.INFO, KernelLoggerInfo.contextRoot, guiContextRoot);
            } else {
                log.log(Level.INFO, KernelLoggerInfo.invalidContextRoot, ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT);
                guiContextRoot = ServerEnvironmentImpl.DEFAULT_ADMIN_CONSOLE_CONTEXT_ROOT;
            }
        }
    }
}
