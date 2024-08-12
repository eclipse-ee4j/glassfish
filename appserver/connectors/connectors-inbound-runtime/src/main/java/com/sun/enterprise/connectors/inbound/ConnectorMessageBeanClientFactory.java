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

package com.sun.enterprise.connectors.inbound;

import com.sun.appserv.connectors.internal.api.ConnectorRuntime;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;

import jakarta.inject.Inject;

import org.glassfish.ejb.spi.MessageBeanClient;
import org.glassfish.ejb.spi.MessageBeanClientFactory;
import org.jvnet.hk2.annotations.Service;

/**
 * MessageBeanClientFactory for connector message bean clients.
 *
 * @author Qingqing Ouyang
 */
@Service(name="ConnectorMessageBeanClientFactory") //name by which MDB container will refer connectors impl.
public final class ConnectorMessageBeanClientFactory
        implements MessageBeanClientFactory {

    @Inject
    private ConnectorRuntime runtime; //initialize connector-runtime if already not done

    /**
     * Creates a <code>ConnectorMessageBeanClient</code>
     *
     * @param descriptor <code>EjbMessageBeanDescriptor.
     * @return <code>ConnectorMessageBeanClient<code>
     */
    public MessageBeanClient createMessageBeanClient(EjbMessageBeanDescriptor descriptor) {
        return new ConnectorMessageBeanClient(descriptor);
    }
}
