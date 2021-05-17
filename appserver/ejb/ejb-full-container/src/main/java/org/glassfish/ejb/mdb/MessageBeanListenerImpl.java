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

package org.glassfish.ejb.mdb;

import java.lang.reflect.Method;
import com.sun.appserv.connectors.internal.api.ResourceHandle;
import org.glassfish.ejb.api.MessageBeanListener;
import org.glassfish.ejb.mdb.MessageBeanContainer.MessageDeliveryType;


/**
 *
 *
 * @author Kenneth Saks
 */
public class MessageBeanListenerImpl implements MessageBeanListener {

    private MessageBeanContainer container_;
    private ResourceHandle resourceHandle_;

    MessageBeanListenerImpl(MessageBeanContainer container,
                            ResourceHandle handle) {
        container_ = container;

        // can be null
        resourceHandle_ = handle;
    }

    public void setResourceHandle(ResourceHandle handle) {
        resourceHandle_ = handle;
    }

    public ResourceHandle getResourceHandle() {
        return resourceHandle_;
    }

    public void beforeMessageDelivery(Method method, boolean txImported) {
        container_.onEnteringContainer();   //Notify Callflow Agent
        container_.beforeMessageDelivery(method, MessageDeliveryType.Message, txImported, resourceHandle_);
    }

    public Object deliverMessage(Object[] params) throws Throwable {
        return container_.deliverMessage(params);
    }

    public void afterMessageDelivery() {
        try {
            container_.afterMessageDelivery(resourceHandle_);
        } finally {
            container_.onLeavingContainer();    //Notify Callflow Agent
        }
    }

}
