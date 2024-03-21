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

import java.beans.PropertyVetoException;
import java.util.List;

import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Protocols;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.embedded.Port;
import jakarta.inject.Inject;
import jakarta.inject.Named;


import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.*;

/**
 * Abstract to port creation and destruction
 */
@Service
@PerLookup
public class PortImpl implements Port {
    @Inject
    CommandRunner runner = null;
    @Inject @Named("plain")
    ActionReport report = null;
    @Inject
    PortsImpl ports;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    NetworkConfig config;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    HttpService httpService;
    String listenerName;
    int number;
    String defaultVirtualServer = "server";

    public void setPortNumber(final int portNumber) {
        number = portNumber;
    }

    public void close() {
        removeListener();
        ports.remove(this);
    }

    private void removeListener() {
        try {
            ConfigSupport.apply(new ConfigCode() {
                public Object run(ConfigBeanProxy... params) throws PropertyVetoException, TransactionFailure {
                    final NetworkListeners nt = (NetworkListeners) params[0];
                    final VirtualServer vs = (VirtualServer) params[1];
                    final Protocols protocols = (Protocols) params[2];
                    List<Protocol> protos = protocols.getProtocol();
                    for (Protocol proto : protos) {
                        if (proto.getName().equals(listenerName)) {
                            protos.remove(proto);
                            break;
                        }
                    }
                    final List<NetworkListener> list = nt.getNetworkListener();
                    for (NetworkListener listener : list) {
                        if (listener.getName().equals(listenerName)) {
                            list.remove(listener);
                            break;
                        }
                    }
                    String regex = listenerName + ",?";
                    String lss = vs.getNetworkListeners();
                    if (lss != null) {
                        vs.setNetworkListeners(lss.replaceAll(regex, ""));
                    }
                    return null;
                }
            }, config.getNetworkListeners(),
                httpService.getVirtualServerByName(defaultVirtualServer),
                config.getProtocols());
        } catch (TransactionFailure tf) {
            tf.printStackTrace();
            throw new RuntimeException(tf);
        }
    }

    public int getPortNumber() {
        return number;
    }
}
