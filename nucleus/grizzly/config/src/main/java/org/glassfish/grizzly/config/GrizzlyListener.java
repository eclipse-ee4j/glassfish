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

package org.glassfish.grizzly.config;

import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.net.InetAddress;

import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * The GrizzlyServiceListener is responsible of mapping incoming requests to the proper Container or
 * Grizzly extensions. Registered Containers can be notified by Grizzly using three mode:
 * <ul>
 * <li>At the transport level: Containers can be notified when TCP, TLS or UDP requests are mapped to them.
 * <li>At the protocol level: Containers can be notified when protocols (ex: SIP, HTTP) requests are
 * mapped to them.
 * <li>At the requests level: Containers can be notified when specific patterns requests are mapped to them.
 * </ul>
 *
 * @author Jeanfrancois Arcand
 * @author Justin Lee
 */
public interface GrizzlyListener {

    void start() throws IOException;

    void stop() throws IOException;

    void destroy();

    String getName();

    InetAddress getAddress();

    int getPort();

    /**
     * Configures the given grizzlyListener.
     *
     * @param networkListener The NetworkListener to configure
     */
    void configure(ServiceLocator locator, NetworkListener networkListener) throws IOException;

    void processDynamicConfigurationChange(ServiceLocator locator, PropertyChangeEvent[] events);

    <T> T getAdapter(Class<T> adapterClass);
}
