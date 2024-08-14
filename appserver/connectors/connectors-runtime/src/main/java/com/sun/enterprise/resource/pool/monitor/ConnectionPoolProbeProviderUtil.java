/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool.monitor;

import com.sun.enterprise.connectors.ConnectionPoolMonitoringExtension;
import com.sun.enterprise.connectors.ConnectorRuntime;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.Collection;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Utility class to create providers for monitoring purposes.
 *
 * @author Shalini M
 */
@Service
public class ConnectionPoolProbeProviderUtil {

    private ConnectionPoolProbeProvider jcaProbeProvider = null;
    private ConnectionPoolProbeProvider jdbcProbeProvider = null;

    @Inject
    private Provider<ConnectionPoolStatsProviderBootstrap> connectionPoolStatsProviderBootstrapProvider;

    @Inject
    private ServiceLocator habitat;

    public void registerProbeProvider() {
        if(ConnectorRuntime.getRuntime().isServer()) {
            getConnPoolBootstrap().registerProvider();
        }
    }

    /**
     * Create probe provider for jcaPool related events.
     *
     * The generated jcaPool probe providers are shared by all
     * jca connection pools. Each jca connection pool will qualify a
     * probe event with its pool name.
     *
     */
    public void createJcaProbeProvider() {
        jcaProbeProvider = new ConnectorConnPoolProbeProvider();
    }

    /**
     * Create probe provider for jdbcPool related events.
     *
     * The generated jdbcPool probe providers are shared by all
     * jdbc connection pools. Each jdbc connection pool will qualify a
     * probe event with its pool name.
     *
     */
    public void createJdbcProbeProvider() {
        Collection<ConnectionPoolMonitoringExtension> extensions =
                habitat.getAllServices(ConnectionPoolMonitoringExtension.class);
        for(ConnectionPoolMonitoringExtension extension : extensions) {
            jdbcProbeProvider = extension.createProbeProvider();
        }
    }

    public ConnectionPoolStatsProviderBootstrap getConnPoolBootstrap() {
        return connectionPoolStatsProviderBootstrapProvider.get();
    }
    /**
     * Get probe provider for connector connection pool related events
     * @return ConnectorConnPoolProbeProvider
     */
    public ConnectionPoolProbeProvider getJcaProbeProvider() {
        return jcaProbeProvider;
    }

    /**
     * Get probe provider for jdbc connection pool related events
     * @return JdbcConnPoolProbeProvider
     */
    public ConnectionPoolProbeProvider getJdbcProbeProvider() {
        return jdbcProbeProvider;
    }

}
