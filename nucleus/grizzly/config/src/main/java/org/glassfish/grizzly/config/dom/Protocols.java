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

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Contains the description of higher level protocols like:
 * {@code http}, {@code https}, {@code iiop}, etc.
 */
@Configured
public interface Protocols extends ConfigBeanProxy, PropertyBag {

    /**
     * List of Protocol implementations
     */
    @Element
    List<Protocol> getProtocol();

    default Protocol findProtocol(String name) {
        for (Protocol protocol : getProtocol()) {
            if (protocol.getName().equals(name)) {
                return protocol;
            }
        }

        return null;
    }

    default NetworkConfig getParent() {
        return getParent(NetworkConfig.class);
    }
}
