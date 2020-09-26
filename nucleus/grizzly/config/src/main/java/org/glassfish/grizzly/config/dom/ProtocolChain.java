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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.PropertyBag;

import jakarta.validation.constraints.Pattern;
import java.util.List;

/**
 * Defines the type of protocol chain and describes protocol filters, which will participate in request processing
 */
@Configured
public interface ProtocolChain extends ConfigBeanProxy, PropertyBag {
    String TYPE = "STATELESS";
    String TYPE_PATTERN = "STATELESS|STATEFUL";

    /**
     * Protocol chain instance handler implementation class
     */
    @Attribute
    String getClassname();

    void setClassname(String value);

    /**
     * Protocol chain type. Could be STATEFUL or STATELESS
     */
    @Attribute(defaultValue = TYPE)
    @Pattern(regexp = TYPE_PATTERN)
    String getType();

    void setType(String value);

    /**
     * Defines protocol filter sequence, which will process a request.
     */
    @Element
    List<ProtocolFilter> getProtocolFilter();

    void setProtocolFilter(List<ProtocolFilter> list);

    @DuckTyped
    Protocol getParent();

    class Duck {
        public static Protocol getParent(ProtocolChain chain) {
            return chain.getParent(Protocol.class);
        }
    }
}
