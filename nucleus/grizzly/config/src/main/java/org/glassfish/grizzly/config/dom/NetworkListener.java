/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import jakarta.validation.constraints.Pattern;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
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

    String LISTENER_TYPES = "(standard|proxy)";

    String DEFAULT_TYPE = "standard";

    /**
     * IP address to listen on.
     */
    @Attribute(defaultValue = DEFAULT_ADDRESS)
    @NetworkAddress
    String getAddress();

    void setAddress(String address);

    /**
     * If {@code false}, a configured listener is disabled.
     */
    @Attribute(defaultValue = "" + ENABLED, dataType = Boolean.class)
    String getEnabled();

    void setEnabled(String enabled);

    @Attribute(defaultValue = DEFAULT_CONFIGURATION_FILE)
    String getJkConfigurationFile();

    void setJkConfigurationFile(String configFile);

    /**
     * If {@code true}, a jk listener is enabled.
     */
    @Attribute(defaultValue = "" + JK_ENABLED, dataType = Boolean.class)
    String getJkEnabled();

    void setJkEnabled(String jkEnabled);

    /**
     * Network-listener {@code name}, which could be used as reference.
     */
    @Attribute(required = true, key = true)
    String getName();

    void setName(String name);

    /**
     * Network-listener {@code type}, which could be used as reference.
     */
    @Attribute(required = true, defaultValue = DEFAULT_TYPE)
    @Pattern(regexp = LISTENER_TYPES, message = "Valid values: " + LISTENER_TYPES)
    String getType();

    void setType(String type);

    /**
     * A {@code port} to listen on.
     */
    @Attribute(required = true, dataType = Integer.class)
    @Range(max = 65535)
    String getPort();

    void setPort(String port);

    /**
     * Reference to a {@code protocol}.
     */
    @Attribute(required = true)
    String getProtocol();

    void setProtocol(String protocol);

    /**
     * Reference to a {@code thread-pool}, defined earlier in the document.
     */
    @Attribute
    String getThreadPool();

    void setThreadPool(String threadPool);

    /**
     * Reference to a low-level {@code transport}.
     */
    @Attribute(required = true)
    String getTransport();

    void setTransport(String transport);

    default Protocol findHttpProtocol() {
        return findHttpProtocol(new HashSet<>(), findProtocol());
    }

    default String findHttpProtocolName() {
        final Protocol httpProtocol = findHttpProtocol();
        if (httpProtocol != null) {
            return httpProtocol.getName();
        }
        return null;
    }

    default Protocol findProtocol() {
        return getParent().getParent().findProtocol(getProtocol());
    }

    default ThreadPool findThreadPool() {
        final NetworkListeners listeners = getParent();
        List<ThreadPool> threadPools = listeners.getThreadPool();
        if (threadPools == null || threadPools.isEmpty()) {
            final ConfigBeanProxy parent = listeners.getParent().getParent();
            final Dom proxy = Objects.requireNonNull(Dom.unwrap(parent)).element("thread-pools");
            final List<Dom> nodeElements = proxy.nodeElements("thread-pool");
            threadPools = new ArrayList<>(nodeElements.size());
            for (Dom node : nodeElements) {
                threadPools.add(node.createProxy());
            }
        }
        for (ThreadPool pool : threadPools) {
            if (getThreadPool().equals(pool.getName())) {
                return pool;
            }
        }
        return null;
    }

    default Transport findTransport() {
        List<Transport> transports = getParent().getParent().getTransports().getTransport();
        String transportName = getTransport();
        for (Transport transport : transports) {
            if (transportName.equals(transport.getName())) {
                return transport;
            }
        }
        return null;
    }

    @Override
    default NetworkListeners getParent() {
        return getParent(NetworkListeners.class);
    }

    private Protocol findHttpProtocol(Set<String> tray, Protocol protocol) {
        if (protocol == null) {
            return null;
        }

        final String protocolName = protocol.getName();
        if (tray.contains(protocolName)) {
            throw new IllegalStateException("Loop found in Protocol definition. Protocol name: " + protocolName);
        }

        if (protocol.getHttp() != null) {
            return protocol;
        }

        PortUnification portUnification = protocol.getPortUnification();
        if (portUnification != null) {
            final List<ProtocolFinder> finders = portUnification.getProtocolFinder();
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
}
