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

package org.glassfish.api.container;

import java.net.InetAddress;
import java.util.List;

import org.glassfish.grizzly.http.server.HttpHandler;
import org.jvnet.hk2.annotations.Contract;

/**
 * Contract interface for registering adapters to a port. Each adapter listens to a particular context root. Context
 * root can be / which makes this adapter the default web application
 *
 * @author Jerome Dochez
 *
 */
@Contract
public interface Adapter {

    /**
     * Get the underlying Grizzly {@link HttpHandler}.
     *
     * @return the underlying Grizzly {@link HttpHandler}.
     */
    HttpHandler getHttpService();

    /**
     * Returns the context root for this adapter
     *
     * @return context root
     */
    String getContextRoot();

    /**
     * Returns the listener port for this adapter
     *
     * @return listener port
     */
    int getListenPort();

    /**
     * @return the {@link InetAddress} on which this adapter is listening
     */
    InetAddress getListenAddress();

    /**
     * Returns the virtual servers supported by this adapter
     *
     * @return List&lt;String&gt; the virtual server list supported by the adapter
     */
    List<String> getVirtualServers();

    /**
     * Checks whether this adapter has been registered as a network endpoint.
     */
    boolean isRegistered();

    /**
     * Marks this adapter as having been registered or unregistered as a network endpoint
     */
    void setRegistered(boolean isRegistered);
}
