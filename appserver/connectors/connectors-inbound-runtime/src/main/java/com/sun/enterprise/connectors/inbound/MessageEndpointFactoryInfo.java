/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors.inbound;

import jakarta.resource.spi.ActivationSpec;
import jakarta.resource.spi.endpoint.MessageEndpointFactory;

/**
 * An instance of this class keeps <code> ActivationSpec <code>
 * and <code>MessageEndPointFactory</code> related to a endpoint
 * activation.
 *
 * @author Qingqing Ouyang
 */
public final class MessageEndpointFactoryInfo {
    //@@@ should we include name of the MDB here?

    private final MessageEndpointFactory factory_;
    private final ActivationSpec as_;

    /**
     * @param fac <code>MessageEndpointFactory</code>
     * @param as  <code>ActivationSpec</code>
     */
    MessageEndpointFactoryInfo(MessageEndpointFactory fac, ActivationSpec as) {
        factory_ = fac;
        as_ = as;
    }

    /**
     * @return <code> MessageEndpointFactory</code> object.
     */
    public MessageEndpointFactory getEndpointFactory() {
        return this.factory_;
    }

    /**
     * @return <code>ActivationSpec</code> object.
     */
    public ActivationSpec getActivationSpec() {
        return this.as_;
    }
}
