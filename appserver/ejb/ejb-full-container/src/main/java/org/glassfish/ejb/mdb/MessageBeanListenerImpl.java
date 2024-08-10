/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.mdb;

import com.sun.appserv.connectors.internal.api.ResourceHandle;

import java.lang.reflect.Method;

import org.glassfish.ejb.api.MessageBeanListener;

import static org.glassfish.ejb.mdb.MessageBeanContainer.MessageDeliveryType.Message;

/**
 *
 *
 * @author Kenneth Saks
 */
public class MessageBeanListenerImpl implements MessageBeanListener {

    private MessageBeanContainer messageBeanContainer;
    private ResourceHandle resourceHandle;

    MessageBeanListenerImpl(MessageBeanContainer container, ResourceHandle handle) {
        messageBeanContainer = container;

        // can be null
        resourceHandle = handle;
    }

    @Override
    public void setResourceHandle(ResourceHandle handle) {
        resourceHandle = handle;
    }

    @Override
    public ResourceHandle getResourceHandle() {
        return resourceHandle;
    }

    @Override
    public void beforeMessageDelivery(Method method, boolean txImported) {
        messageBeanContainer.onEnteringContainer(); // Notify Callflow Agent
        messageBeanContainer.beforeMessageDelivery(method, Message, txImported, resourceHandle);
    }

    @Override
    public Object deliverMessage(Object[] params) throws Throwable {
        return messageBeanContainer.deliverMessage(params);
    }

    @Override
    public void afterMessageDelivery() {
        try {
            messageBeanContainer.afterMessageDelivery(resourceHandle);
        } finally {
            messageBeanContainer.onLeavingContainer(); // Notify Callflow Agent
        }
    }

}
