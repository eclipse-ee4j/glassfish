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

import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Defines logic of hosting several protocol on a single tcp port.
 */
@Configured
public interface PortUnification extends ConfigBeanProxy, PropertyBag {

    boolean STICKY_ENABLED = true;

    /**
     * Port unification logic implementation class.
     */
    @Attribute
    String getClassname();

    void setClassname(String classname);

    /**
     * Set of protocol finders, which will be responsible for protocol recognition.
     */
    @Element
    List<ProtocolFinder> getProtocolFinder();

    void setProtocolFinder(List<ProtocolFinder> protocolFinders);

    /**
     * If the data came on a network connection is recognized as HTTP packet
     * and if it is passed to a default Web protocol - then, if Web protocol
     * {@code sticky} flag is enabled, the network connection gets associated with
     * the Web protocol forever, and port unification finder will never be called again
     * for this network connection. If the web protocol {@code sticky} flag is
     * {@code false} - then this time HTTP packet will be passed to a Web protocol,
     * but next time for a next data on this connection - protocol finders will
     * be called again to recognize the target protocol.
     */
    @Attribute(defaultValue = "" + STICKY_ENABLED, dataType = Boolean.class)
    String getWebProtocolStickyEnabled();

    /**
     * If the data came on a network connection is recognized as HTTP packet
     * and if it is passed to a default Web protocol - then, if Web protocol
     * {@code sticky} flag is enabled, the network connection gets associated with
     * the Web protocol forever, and port unification finder will never be called again
     * for this network connection. If the web protocol {@code sticky} flag is
     * {@code false} - then this time HTTP packet will be passed to a Web protocol,
     * but next time for a next data on this connection - protocol finders will
     * be called again to recognize the target protocol.
     */
    void setStickyProtocolEnabled(final String stickyProtocolEnabled);

    default Protocol getParent() {
        return getParent(Protocol.class);
    }
}
