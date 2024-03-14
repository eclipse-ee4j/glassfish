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

package org.glassfish.admin.restconnector;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.v3.admin.adapter.AdminEndpointDecider;
import org.glassfish.api.container.Adapter;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.hk2.api.ServiceLocator;

import java.net.InetAddress;
import java.util.List;

/**
 * Base class for our implementation of Adapter proxies. To avoid early loading of adapter implentations, use a
 * handle-body idiom here. Only operations related to metadata is handled by this class. The rest of the operations are
 * delegated to a delegate which is looked up in the service registry on demand.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public abstract class AbstractProxyRestAdapter implements Adapter {
    // TODO(Sahoo): This class can be moved to kernel and be used as proxy for other Adapter implementations.

    /**
     * Our delegate which depends on a lot of things.
     */
    private ProxiedRestAdapter delegate;

    private boolean registered;

    private AdminEndpointDecider aed;

    /*
     * This is not a component itself, so it can not use injection facility.
     * All injection capable fields are implemented as abstract getters.
     */
    protected abstract ServiceLocator getServices();

    protected abstract Config getConfig();

    protected abstract String getName();

    private synchronized AdminEndpointDecider getEpd() {
        if (aed == null) {
            aed = new AdminEndpointDecider(getConfig());
        }
        return aed;
    }

    /**
     * @return the real adapter - looked up in service registry using {@link #getName}
     */
    private synchronized ProxiedRestAdapter getDelegate() {
        if (delegate == null) {
            delegate = getServices().getService(ProxiedRestAdapter.class, getName());
            if (delegate == null) {
                throw new RuntimeException(
                        "Unable to locate a service of type = " + ProxiedRestAdapter.class + " with name = " + getName());
            }
        }
        return delegate;
    }

    @Override
    public HttpHandler getHttpService() {
        return getDelegate().getHttpService();
    }

    /**
     * Context root this adapter is responsible for handling.
     */
    @Override
    public abstract String getContextRoot();

    @Override
    public int getListenPort() {
        return getEpd().getListenPort();
    }

    @Override
    public InetAddress getListenAddress() {
        return getEpd().getListenAddress();
    }

    @Override
    public List<String> getVirtualServers() {
        return getEpd().getAsadminHosts();
    }

    @Override
    public synchronized boolean isRegistered() {
        return registered;
    }

    @Override
    public synchronized void setRegistered(boolean registered) {
        this.registered = registered;
    }

}
