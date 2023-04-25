/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import java.util.ArrayList;
import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Defines one single high-level protocol like:
 * {@code http}, {@code https}, {@code iiop}, etc.
 */
@Configured
public interface Protocol extends ConfigBeanProxy, PropertyBag {

    boolean SECURITY_ENABLED = false;

    /**
     * Defines any {@code HTTP} settings for this {@code Protocol}.
     */
    @Element
    Http getHttp();

    void setHttp(Http http);

    /**
     * Protocol {@code name} which could be used as reference.
     */
    @Attribute(required = true, key = true)
    String getName();

    void setName(String name);

    /**
     * Defines port-unification logic.  If it is required to handle more than one
     * high level protocol on a single network-listener.
     */
    @Element
    PortUnification getPortUnification();

    void setPortUnification(PortUnification portUnification);

    /**
     * Defines {@code http-redirect} logic.
     */
    @Element
    HttpRedirect getHttpRedirect();

    void setHttpRedirect(HttpRedirect httpRedirect);

    /**
     * Protocol chain instance handler logic.
     */
    @Element
    ProtocolChainInstanceHandler getProtocolChainInstanceHandler();

    void setProtocolChainInstanceHandler(ProtocolChainInstanceHandler protocolChainHandler);

    /**
     * True means the protocol is secured and ssl element will be used to initialize security
     * settings. {@code False} means that protocol is not secured and {@code ssl} element,
     * if present, will be ignored.
     */
    @Attribute(defaultValue = "" + SECURITY_ENABLED, dataType = Boolean.class)
    String getSecurityEnabled();

    void setSecurityEnabled(String securityEnabled);

    /**
     * Protocol security (ssl) configuration.
     */
    @Element
    Ssl getSsl();

    void setSsl(Ssl ssl);

    default List<NetworkListener> findNetworkListeners() {
        final List<NetworkListener> listeners = getParent().getParent().getNetworkListeners().getNetworkListener();
        List<NetworkListener> networkListeners = new ArrayList<>();
        for (NetworkListener listener : listeners) {
            if (listener.getProtocol().equals(getName())) {
                networkListeners.add(listener);
            }
        }
        return networkListeners;
    }

    default Protocols getParent() {
        return getParent(Protocols.class);
    }
}
