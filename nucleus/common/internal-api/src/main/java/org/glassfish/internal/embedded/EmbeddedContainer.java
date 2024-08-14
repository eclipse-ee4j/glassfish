/*
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

package org.glassfish.internal.embedded;

import java.util.List;

import org.glassfish.api.container.Sniffer;
import org.jvnet.hk2.annotations.Contract;

/**
 * Embedded container definition, although most containers will be bound
 * to a {@link Port} instance, it's not automatic, for instance JPA and
 * other non network based containers might not.
 *
 * @author Jerome Dochez
 */
@Contract
public interface EmbeddedContainer {

    /**
     * Binds a port using a specific protocol to this container.
     * @param port the port instance to bind
     * @param protocol the protocol the port should be used for, can
     * be null and the container can use the port for any protocol(s)
     * it needs to.
     */
    public void bind(Port port, String protocol);

    /**
     * Returns the list of sniffers associated with this container.
     *
     * @return a list of sniffers that will be used when application are
     * deployed to the embedded server.
     */
    public List<Sniffer> getSniffers();

    /**
     * Starts the embedded container instance
     *
     * @throws LifecycleException if the container cannot started
     */
    public void start() throws LifecycleException;

    /**
     * Stops the embedded container instance
     *
     * @throws LifecycleException if the container cannot be stopped
     */
    public void stop() throws LifecycleException;

}
