/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Contains complete Grizzly configuration.
 */
@Configured
public interface NetworkConfig extends ConfigBeanProxy, PropertyBag {
    /**
     * Describes low level transports configuration.  Like tcp, udp, ssl
     * transports configuration
     */
    @Element(required = true)
    Transports getTransports();

    void setTransports(Transports value);

    /**
     * Describes higher level protocols like: http, https, iiop
     */
    @Element(required = true)
    Protocols getProtocols();

    void setProtocols(Protocols value);

    /**
     * Binds protocols with lower level transports
     */
    @Element(required = true)
    NetworkListeners getNetworkListeners();

    void setNetworkListeners(NetworkListeners value);

    @DuckTyped
    NetworkListener getNetworkListener(String name);

    @DuckTyped
    Protocol findProtocol(String name);

    class Duck {
        public static Protocol findProtocol(NetworkConfig config, String name) {
            for (final Protocol protocol : config.getProtocols().getProtocol()) {
                if (protocol.getName().equals(name)) {
                    return protocol;
                }
            }
            return null;
        }

        public static NetworkListener getNetworkListener(NetworkConfig config, String name) {
            if (name != null) {
                for (final NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
                    if (listener.getName().equals(name)) {
                        return listener;
                    }
                }
            }
            return null;
        }
    }
}
