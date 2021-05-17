/*
 * Copyright (c) 2009, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.validation.constraints.Pattern;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Binds protocol to a specific endpoint to listen on
 */
@Configured
public interface NetworkListener extends ConfigBeanProxy, PropertyBag {
    boolean ENABLED = true;
    boolean JK_ENABLED = false;
    String DEFAULT_ADDRESS = "0.0.0.0";
    String DEFAULT_CONFIGURATION_FILE = "${com.sun.aas.instanceRoot}/config/glassfish-jk.properties";
    String TYPE_PATTERN = "(standard|proxy)";
    String DEFAULT_TYPE = "standard";


    /**
     * IP address to listen on
     */
    @Attribute(defaultValue = DEFAULT_ADDRESS)
    @NetworkAddress
    String getAddress();

    void setAddress(String value);

    /**
     * If false, a configured listener, is disabled
     */
    @Attribute(defaultValue = "" + ENABLED, dataType = Boolean.class)
    String getEnabled();

    void setEnabled(String enabled);

    @Attribute(defaultValue = DEFAULT_CONFIGURATION_FILE)
    String getJkConfigurationFile();

    void setJkConfigurationFile(String file);

    /**
     * If true, a jk listener is enabled
     */
    @Attribute(defaultValue = "" + JK_ENABLED, dataType = Boolean.class)
    String getJkEnabled();

    void setJkEnabled(String enabled);

    /**
     * Network-listener name, which could be used as reference
     */
    @Attribute(required = true, key = true)
    String getName();

    void setName(String value);

    /**
     * Network-listener name, which could be used as reference
     */
    @Attribute(required = true, dataType = String.class, defaultValue = DEFAULT_TYPE)
    @Pattern(regexp = TYPE_PATTERN)
    String getType();

    void setType(String type);

    /**
     * Port to listen on
     */
    @Attribute(required = true, dataType = Integer.class)
    @Range(min=0, max=65535)
    String getPort();

    void setPort(String value);

    /**
     * Reference to a protocol
     */
    @Attribute(required = true)
    String getProtocol();

    void setProtocol(String value);

    /**
     * Reference to a thread-pool, defined earlier in the document.
     */
    @Attribute
    String getThreadPool();

    void setThreadPool(String value);

    /**
     * Reference to a low-level transport
     */
    @Attribute(required = true)
    String getTransport();

    void setTransport(String value);

    @DuckTyped
    Protocol findHttpProtocol();

    @DuckTyped
    String findHttpProtocolName();

    @DuckTyped
    Protocol findProtocol();

    @DuckTyped
    ThreadPool findThreadPool();

    @DuckTyped
    Transport findTransport();

    @DuckTyped
    NetworkListeners getParent();

    class Duck {

        public static Protocol findHttpProtocol(NetworkListener listener) {
            return findHttpProtocol(new HashSet<String>(), findProtocol(listener));
        }

        private static Protocol findHttpProtocol(Set<String> tray, Protocol protocol) {
            if (protocol == null) {
                return null;
            }

            final String protocolName = protocol.getName();
            if (tray.contains(protocolName)) {
                throw new IllegalStateException(
                    "Loop found in Protocol definition. Protocol name: " + protocol.getName());
            }

            if (protocol.getHttp() != null) {
                return protocol;
            } else if (protocol.getPortUnification() != null) {
                final List<ProtocolFinder> finders = protocol.getPortUnification().getProtocolFinder();
                tray.add(protocolName);

                try {
                    Protocol foundHttpProtocol = null;
                    for (ProtocolFinder finder : finders) {
                        final Protocol subProtocol = finder.findProtocol();
                        if (subProtocol != null) {
                            final Protocol httpProtocol = findHttpProtocol(tray, subProtocol);
                            if (httpProtocol != null) {
                                foundHttpProtocol = httpProtocol;
                            }
                        }
                    }
                    return foundHttpProtocol;
                } finally {
                    tray.remove(protocolName);
                }
            }

            return null;
        }

        public static String findHttpProtocolName(NetworkListener listener) {
            final Protocol httpProtocol = findHttpProtocol(listener);
            if (httpProtocol != null) {
                return httpProtocol.getName();
            }

            return null;
        }

        public static Protocol findProtocol(NetworkListener listener) {
            return listener.getParent().getParent().findProtocol(listener.getProtocol());
        }

        public static ThreadPool findThreadPool(NetworkListener listener) {
            final NetworkListeners listeners = listener.getParent();
            List<ThreadPool> list = listeners.getThreadPool();
            if (list == null || list.isEmpty()) {
                final ConfigBeanProxy parent = listener.getParent().getParent().getParent();
                final Dom proxy = Dom.unwrap(parent).element("thread-pools");
                final List<Dom> domList = proxy.nodeElements("thread-pool");
                list = new ArrayList<ThreadPool>(domList.size());
                for (Dom dom : domList) {
                    list.add(dom.<ThreadPool>createProxy());
                }
            }
            for (ThreadPool pool : list) {
                if (listener.getThreadPool().equals(pool.getName())) {
                    return pool;
                }
            }
            return null;
        }

        public static Transport findTransport(NetworkListener listener) {
            List<Transport> list = listener.getParent().getParent().getTransports().getTransport();
            for (Transport transport : list) {
                if (listener.getTransport().equals(transport.getName())) {
                    return transport;
                }
            }
            return null;
        }

        public static NetworkListeners getParent(NetworkListener listener) {
            return listener.getParent(NetworkListeners.class);
        }
    }
}
