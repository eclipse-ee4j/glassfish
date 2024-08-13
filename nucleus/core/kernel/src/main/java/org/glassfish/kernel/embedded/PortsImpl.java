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

package org.glassfish.kernel.embedded;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.embedded.Port;
import org.glassfish.internal.embedded.Ports;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Jerome Dochez
 */
@Service
public class PortsImpl implements Ports {


    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    NetworkConfig network;

    @Inject
    ServiceLocator habitat;

    final Map<Integer, Port> ports = new HashMap<Integer, Port>();

    public Port createPort(int number) throws IOException {
        return createPort(Integer.valueOf(number));
    }

    private Port createPort(Integer portNumber) throws IOException {

        for (NetworkListener nl : network.getNetworkListeners().getNetworkListener()) {
            if (nl.getPort().equals(portNumber.toString())) {
                throw new IOException("Port " + portNumber + " is already configured");
            }
        }
        for (Integer pn : ports.keySet()) {
            if (pn.equals(portNumber)) {
                throw new IOException("Port " + portNumber + " is alredy open");
            }
        }
        PortImpl port = habitat.getService(PortImpl.class);
        port.setPortNumber(portNumber);
        ports.put(portNumber, port);
        return port;    }

    public Collection<Port> getPorts() {
        return ports.values();
    }

    public void remove(Port port) {
        ports.remove(port.getPortNumber());
    }
}
