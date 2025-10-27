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

import com.sun.enterprise.util.Result;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.Future;

import org.glassfish.grizzly.http.server.HttpHandler;

/**
 * Generic interface used by the GrizzlyService to start the tcp/udp/tcl stack.
 * By default, we are starting Grizzly, but we might allow other framework to
 * hook in and drive hk2/v3.
 *
 * TODO: Allow addition of other types of Container, not only Adapter but
 *       also any extension.
 *
 * @author Jeanfrancois Arcand
 */
public interface NetworkProxy extends EndpointMapper<HttpHandler>{


    /**
     * Stop the proxy.
     */
    void stop() throws IOException;

    /**
     * Stop the proxy and remove all shared resources if it's the last proxy.
     * @param lastOne Whether it's the last proxy.
     *   {@code false} if other proxies still exist and depend on the shared resources
     */
    default void stop(boolean lastOne) throws IOException {
        stop();
    }

    /**
     * Start the proxy.
     */
    Future<Result<Thread>> start() throws IOException;


    /**
     * @return the network port upon which this <code>NetworkProxy</code> is
     *  listening on
     */
    int getPort();


    /**
     * @return the {@link InetAddress} of this <code>NetworkProxy</code>
     */
    InetAddress getAddress();


    /**
     * Destroy the proxy.
     */
    void destroy();
}
