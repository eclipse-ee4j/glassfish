/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

import java.util.logging.Logger;
import org.glassfish.grizzly.config.dom.NetworkListener;

import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.nio.transport.TCPNIOTransportBuilder;
import org.glassfish.grizzly.strategies.SameThreadIOStrategy;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * This class extends Grizzly's GrizzlyServiceListener class to customize it for GlassFish and enable a single listener
 * do both lazy service initialization as well as init of HTTP and admin listeners
 *
 * @author Vijay Ramachandran
 * @author Alexey Stashok
 */
public class ServiceInitializerListener extends org.glassfish.grizzly.config.GenericGrizzlyListener {
    private final Logger logger;
    private final GrizzlyService grizzlyService;
    private final NetworkListener networkListener;

    public ServiceInitializerListener(final GrizzlyService grizzlyService,
            final NetworkListener networkListener,
            final Logger logger) {
        this.grizzlyService = grizzlyService;
        this.networkListener = networkListener;
        this.logger = logger;
    }

    public NetworkListener getNetworkListener() {
        return networkListener;
    }

    @Override
    protected void configureTransport(final NetworkListener networkListener,
                                      final Transport transportConfig,
                                      final FilterChainBuilder filterChainBuilder) {

        transport = configureDefaultThreadPoolConfigs(
                TCPNIOTransportBuilder.newInstance().build());

        final int acceptorThreads = transportConfig != null
                ? Integer.parseInt(transportConfig.getAcceptorThreads())
                : Transport.ACCEPTOR_THREADS;

        transport.setSelectorRunnersCount(acceptorThreads);
        transport.getKernelThreadPoolConfig().setPoolName(networkListener.getName() + "-kernel");

        if (acceptorThreads > 0) {
            transport.getKernelThreadPoolConfig()
                    .setCorePoolSize(acceptorThreads)
                    .setMaxPoolSize(acceptorThreads);
        }

        rootFilterChain = FilterChainBuilder.stateless().build();

        transport.setProcessor(rootFilterChain);
        transport.setIOStrategy(SameThreadIOStrategy.getInstance());
    }


    @Override
    protected void configureProtocol(final ServiceLocator habitat,
            final NetworkListener networkListener,
            final Protocol protocol, final FilterChainBuilder filterChainBuilder) {
        filterChainBuilder.add(new ServiceInitializerFilter(this,
                grizzlyService.getServiceLocator(), logger));
    }

    @Override
    protected void configureThreadPool(final ServiceLocator habitat,
            final NetworkListener networkListener,
            final ThreadPool threadPool) {

        // we don't need worker thread pool
        transport.setWorkerThreadPoolConfig(null);
    }
}
