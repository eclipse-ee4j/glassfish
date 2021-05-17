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

import java.io.IOException;
import java.nio.channels.SelectableChannel;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.nio.NIOConnection;
import org.glassfish.grizzly.nio.NIOTransport;
import org.glassfish.grizzly.nio.SelectorHandler;
import org.glassfish.grizzly.nio.SelectorRunner;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.internal.grizzly.LazyServiceInitializer;

/**
 * The {@link org.glassfish.grizzly.filterchain.Filter} implementation,
 * which lazily initializes custom service on the first accepted connection
 * and passes connection there.
 *
 * @author Vijay Ramachandran
 */
public class ServiceInitializerFilter extends BaseFilter {
    private final ServiceLocator locator;
    private volatile LazyServiceInitializer targetInitializer = null;
    private final List<ActiveDescriptor<?>> initializerImplList;

    protected final Logger logger;

    private final ServiceInitializerListener listener;

    private final Object LOCK_OBJ = new Object();
//    private long timeout = 60000;

    public ServiceInitializerFilter(final ServiceInitializerListener listener,
            final ServiceLocator habitat, final Logger logger) {
        this.locator = habitat;

        initializerImplList =
                habitat.getDescriptors(BuilderHelper.createContractFilter(LazyServiceInitializer.class.getName()));

        if (initializerImplList.isEmpty()) {
            throw new IllegalStateException("NO Lazy Initializer was found for port = " +
                    listener.getPort());
        }

        this.logger = logger;
        this.listener = listener;
    }

    @Override
    public NextAction handleAccept(final FilterChainContext ctx) throws IOException {
        final NIOConnection nioConnection = (NIOConnection) ctx.getConnection();
        final SelectableChannel channel = nioConnection.getChannel();

        // The LazyServiceInitializer's name we're looking for should be equal
        // to either listener or protocol name
        final String listenerName = listener.getName();
        final String protocolName = listener.getNetworkListener().getProtocol();

        if (targetInitializer == null) {
            synchronized (LOCK_OBJ) {
                if (targetInitializer == null) {
                    LazyServiceInitializer targetInitializerLocal = null;
                    for (final ActiveDescriptor<?> initializer : initializerImplList) {
                        String serviceName = initializer.getName();


                        if (serviceName != null &&
                                (listenerName.equalsIgnoreCase(serviceName) ||
                                protocolName.equalsIgnoreCase(serviceName))) {
                            targetInitializerLocal = (LazyServiceInitializer) locator.getServiceHandle(initializer).getService();
                            break;
                        }
                    }

                    if (targetInitializerLocal == null) {
                        logger.log(Level.SEVERE, "NO Lazy Initialiser implementation was found for port = {0}",
                                String.valueOf(listener.getPort()));
                        nioConnection.close();

                        return ctx.getStopAction();
                    }
                    if (!targetInitializerLocal.initializeService()) {
                        logger.log(Level.SEVERE, "Lazy Service initialization failed for port = {0}",
                                String.valueOf(listener.getPort()));

                        nioConnection.close();

                        return ctx.getStopAction();
                    }

                    targetInitializer = targetInitializerLocal;
                }
            }
        }

        final NextAction nextAction = ctx.getSuspendAction();
        ctx.completeAndRecycle();

        // Deregister channel
        final SelectorRunner runner = nioConnection.getSelectorRunner();
        final SelectorHandler selectorHandler =
                ((NIOTransport) nioConnection.getTransport()).getSelectorHandler();

        selectorHandler.deregisterChannel(runner, channel);

        // Underlying service rely the channel is blocking
        channel.configureBlocking(true);
        targetInitializer.handleRequest(channel);

        return nextAction;
    }
}
