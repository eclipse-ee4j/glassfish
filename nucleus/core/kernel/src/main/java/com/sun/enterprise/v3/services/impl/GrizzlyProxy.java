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

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.deployment.ApplicationContainer;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.enterprise.util.Result;
import java.util.concurrent.Callable;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.config.GenericGrizzlyListener;
import org.glassfish.grizzly.config.GrizzlyListener;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.utils.Futures;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * This class is responsible for configuring Grizzly.
 *
 * @author Jerome Dochez
 * @author Jeanfrancois Arcand
 */
public class GrizzlyProxy implements NetworkProxy {
    final Logger logger;
    final NetworkListener networkListener;

    protected GrizzlyListener grizzlyListener;
    private int portNumber;

    public final static String LEADER_FOLLOWER
            = "org.glassfish.grizzly.useLeaderFollower";

    public final static String AUTO_CONFIGURE
            = "org.glassfish.grizzly.autoConfigure";

    // <http-listener> 'address' attribute
    private InetAddress address;

    private GrizzlyService grizzlyService;

    //private VirtualServer vs;


    public GrizzlyProxy(GrizzlyService service, NetworkListener listener) {
        grizzlyService = service;
        logger = service.getLogger();
        networkListener = listener;
    }

    /**
     * Create a <code>GrizzlyServiceListener</code> based on a NetworkListener
     * configuration object.
     */
    public void initialize() throws IOException {
        portNumber = initPort(networkListener);
        address = initAddress(networkListener);
        grizzlyListener = createGrizzlyListener(networkListener);

        grizzlyListener.configure(grizzlyService.getServiceLocator(), networkListener);
    }

    int initPort(final NetworkListener networkListener) {
        String port = networkListener.getPort();
        if (port == null) {
            logger.severe(KernelLoggerInfo.noPort);
            throw new RuntimeException("Cannot find port information from domain configuration");
        }
        try {
            return Integer.parseInt(port);
        } catch (final NumberFormatException e) {
            logger.log(Level.SEVERE, KernelLoggerInfo.badPort, port);
            return 8080;
        }
    }

    InetAddress initAddress(final NetworkListener networkListener) {
        String addressAsString = networkListener.getAddress();
        try {
            return InetAddress.getByName(addressAsString);
        } catch (final UnknownHostException e) {
            LogHelper.log(logger, Level.SEVERE, KernelLoggerInfo.badAddress, e, addressAsString);
            return null;
        }
    }

    protected GrizzlyListener createGrizzlyListener(
            final NetworkListener networkListener) {
        if (GrizzlyService.isLightWeightListener(networkListener)) {
            return createServiceInitializerListener(networkListener);
        } else {
            return createGlassfishListener(networkListener);
        }
    }

    protected GrizzlyListener createGlassfishListener(
            final NetworkListener networkListener) {
        return new GlassfishNetworkListener(grizzlyService,
                networkListener, logger);
    }

    protected GrizzlyListener createServiceInitializerListener(
            final NetworkListener networkListener) {
        return new ServiceInitializerListener(grizzlyService,
                networkListener, logger);
    }

    static ArrayList<String> toArray(String list, String token){
        return new ArrayList<String>(Arrays.asList(list.split(token)));
    }

    /**
     * Stops the Grizzly service.
     */
    @Override
    public void stop() throws IOException {
        grizzlyListener.stop();
    }

    @Override
    public void destroy() {
        grizzlyListener.destroy();
    }

    @Override
    public String toString() {
        return "GrizzlyProxy{" +
                //"virtual server=" + vs +
                "address=" + address +
                ", portNumber=" + portNumber +
                '}';
    }


    /*
    * Registers a new endpoint (adapter implementation) for a particular
    * context-root. All request coming with the context root will be dispatched
    * to the adapter instance passed in.
    * @param contextRoot for the adapter
    * @param endpointAdapter servicing requests.
    */
    @Override
    public void registerEndpoint(String contextRoot, Collection<String> vsServers,
            HttpHandler endpointService,
            ApplicationContainer container) throws EndpointRegistrationException {

        // e.g., there is no admin service in an instance
        if (contextRoot == null) {
            return;
        }

        if (endpointService == null) {
            throw new EndpointRegistrationException(
                "The endpoint adapter is null");
        }

        final HttpAdapter httpAdapter = grizzlyListener.getAdapter(HttpAdapter.class);
        if (httpAdapter != null) {
            httpAdapter.getMapper().register(contextRoot, vsServers, endpointService, container);
        }
    }

    /**
     * Removes the context-root from our list of endpoints.
     */
    @Override
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException {
        final HttpAdapter httpAdapter = grizzlyListener.getAdapter(HttpAdapter.class);
        if (httpAdapter != null) {
            httpAdapter.getMapper().unregister(contextRoot);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerEndpoint(final Endpoint endpoint) {
        final HttpAdapter httpAdapter = grizzlyListener.getAdapter(HttpAdapter.class);
        if (httpAdapter != null) {
            httpAdapter.getMapper().register(endpoint);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterEndpoint(final Endpoint endpoint) throws EndpointRegistrationException {
        unregisterEndpoint(endpoint.getContextRoot(), endpoint.getContainer());
    }


    @Override
    public Future<Result<Thread>> start() throws IOException {
        final FutureImpl<Result<Thread>> future =
                Futures.<Result<Thread>>createUnsafeFuture();

        if (!isAjpEnabled(grizzlyListener)) {
            // If this is not AJP listener - initiate startup right now
            start0();
        } else {
            // For AJP listener we have to wait until server is up and ready
            // to process incoming requests
            // Related to the GLASSFISH-18267
            grizzlyService.addServerReadyListener(new Callable<Void>() {

                @Override
                public Void call() throws Exception {
                    start0();
                    return null;
                }
            });
        }

        future.result(new Result<Thread>(Thread.currentThread()));
        return future;
    }

    /**
     * Start internal Grizzly listener.
     * @throws IOException
     */
    protected void start0() throws IOException {
        final long t1 = System.currentTimeMillis();

        grizzlyListener.start();

        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, KernelLoggerInfo.grizzlyStarted,
                    new Object[]{Grizzly.getDotedVersion(),
                    System.currentTimeMillis() - t1,
                    grizzlyListener.getAddress() + ":" + grizzlyListener.getPort()});
        }
    }

    @Override
    public int getPort() {
        return portNumber;
    }

    @Override
    public InetAddress getAddress() {
        return address;
    }

    public GrizzlyListener getUnderlyingListener() {
        return grizzlyListener;
    }

    private static boolean isAjpEnabled(final GrizzlyListener grizzlyListener) {
        return (grizzlyListener instanceof GenericGrizzlyListener) &&
                ((GenericGrizzlyListener) grizzlyListener).isAjpEnabled();
    }
}
