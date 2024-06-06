/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.kernel.KernelLoggerInfo.snifferAdapterContainerStarted;

import java.util.Collection;
import java.util.logging.Logger;

import org.glassfish.api.container.Sniffer;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.util.MappingData;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ContainerRegistry;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.v3.server.ContainerStarter;

import jakarta.inject.Inject;

/**
 * These adapters are temporarily registered to the mapper to handle static pages request that a container would like to
 * process rather than serving them statically unchanged. This is useful for things like .jsp or .php files saved in the
 * context root of the application server.
 *
 * @author Jerome Dochez
 * @author Jeanfrancois Arcand
 */
@Service
@PerLookup
public class SnifferAdapter extends HttpHandler {

    private static final Logger LOGGER = KernelLoggerInfo.getLogger();

    @Inject
    private ContainerRegistry containerRegistry;

    @Inject
    private ContainerStarter containerStarter;

    @Inject
    private ModulesRegistry modulesRegistry;

    private Sniffer sniffer;
    private ContainerMapper mapper;
    private HttpHandler adapter;

    public void initialize(Sniffer sniffer, ContainerMapper mapper) {
        this.sniffer = sniffer;
        this.mapper = mapper;
    }

    // I could synchronize this method since I only start one container and do it
    // synchronously but that seems like an overkill and I would still need to handle
    // pending requests.
    @Override
    public void service(Request req, Response resp) throws Exception {
        if (adapter != null) {
            // This is not supposed to happen, however due to multiple requests coming in, I would
            // not be surprised...
            adapter.service(req, resp);
            return;
        }

        // We found a sniffer that wants to handle this requested
        // page, let's get to the container or start it.
        // start all the containers associated with sniffers.

        // Need to synchronize on the registry to not end up starting the same container from
        // different threads.
        synchronized (containerRegistry) {
            if (adapter != null) {
                // Got started in the meantime
                adapter.service(req, resp);
                return;
            }

            if (containerRegistry.getContainer(sniffer.getContainersNames()[0]) != null) {
                LOGGER.fine("Container is claimed to be started...");
                containerRegistry.getContainer(sniffer.getContainersNames()[0]).getContainer();
            } else {
                final long startTime = System.currentTimeMillis();
                LOGGER.log(INFO, KernelLoggerInfo.snifferAdapterStartingContainer, sniffer.getModuleType());
                try {
                    Collection<EngineInfo<?, ?>> containersInfo = containerStarter.startContainer(sniffer);
                    if (containersInfo != null && !containersInfo.isEmpty()) {

                        // Force the start on each container
                        for (EngineInfo<?, ?> info : containersInfo) {
                            LOGGER.log(FINE, "Got container, deployer is {0}", info.getDeployer());
                            info.getContainer();
                            LOGGER.log(INFO, snifferAdapterContainerStarted, new Object[] { sniffer.getModuleType(), System.currentTimeMillis() - startTime });
                        }
                    } else {
                        LOGGER.severe(KernelLoggerInfo.snifferAdapterNoContainer);
                    }
                } catch (Exception e) {
                    LOGGER.log(SEVERE, KernelLoggerInfo.snifferAdapterExceptionStarting, new Object[] { sniffer.getContainersNames()[0], e });
                }
            }

            // At this point the post construct should have been called.
            // seems like there is some possibility that the container is not synchronously started
            // preventing the calls below to succeed...
            DataChunk decodedURI = req.getRequest().getRequestURIRef().getDecodedRequestURIBC();
            try {
                // Clear the previous mapped information.
                MappingData mappingData = ContainerMapper.getNote(req);
                mappingData.recycle();

                adapter = mapper.mapUriWithSemicolon(req, decodedURI, 0, null);
                // If a SnifferAdapter doesn't do it's job, avoid recursion
                // and throw a Runtime exception.
                if (adapter.equals(this)) {
                    adapter = null;
                    throw new RuntimeException("SnifferAdapter cannot map themself.");
                }
            } catch (Exception e) {
                LOGGER.log(SEVERE, KernelLoggerInfo.snifferAdapterExceptionMapping, e);
                throw e;
            }

            // pass on,,,
            if (adapter != null) {
                adapter.service(req, resp);
            } else {
                throw new RuntimeException("No Adapter found.");
            }
        }
    }
}
