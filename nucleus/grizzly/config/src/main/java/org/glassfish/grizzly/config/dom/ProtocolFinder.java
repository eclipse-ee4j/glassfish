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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Describes a protocol finder/recognizer, which is able to recognize whether incoming request
 * belongs to the specific protocol or not. If yes - protocol-finder forwards request processing to a
 * specific protocol.
 */
@Configured
public interface ProtocolFinder extends ConfigBeanProxy, PropertyBag {
    /**
     * Finder name, which could be used as reference
     */
    @Attribute(key = true)
    String getName();

    void setName(String value);

    /**
     * Reference to a protocol, which was defined before.
     */
    @Attribute
    String getProtocol();

    void setProtocol(String value);

    /**
     * Finder logic implementation class
     */
    @Attribute(required = true)
    String getClassname();

    void setClassname(String value);

    @DuckTyped
    Protocol findProtocol();

    @DuckTyped
    PortUnification getParent();

    class Duck {
        public static Protocol findProtocol(ProtocolFinder finder) {
            String name = finder.getProtocol();
            final NetworkConfig networkConfig = finder.getParent().getParent().getParent().getParent();
            return networkConfig.findProtocol(name);
        }

        public static PortUnification getParent(ProtocolFinder finder) {
            return finder.getParent(PortUnification.class);
        }
    }
}
