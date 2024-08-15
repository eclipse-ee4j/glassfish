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

package org.glassfish.ejb.api;

import com.sun.appserv.connectors.internal.api.ResourceHandle;
import com.sun.enterprise.deployment.runtime.BeanPoolDescriptor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * MessageBeanProtocolManager is implemented by the MessageBeanContainer
 * and allows MessageBeanClients to create message bean listeners
 * capable of receiving messages.  Each MessageBeanListener logically
 * represents a single message-driven bean instance, although there is
 * no guarantee as to exactly when the container creates that instance.
 * MessageBeanListeners are single-threaded.  Each MessageBeanListener is
 * held exclusively by a MessageBeanClient.
 *
 * @author Kenneth Saks
 */
public interface MessageBeanProtocolManager {

    /**
     * Create a MessageBeanListener.
     *
     * @param resourceHandle handle associated with this listener.  can be null.
     *
     * @throws Exception if the MessageBeanContainer was not able to create
     * the MessageBeanListener
     */
    MessageBeanListener createMessageBeanListener(ResourceHandle resourceHandle)
      throws ResourcesExceededException;

    /**
     * Return the MessageBeanListener to the container.  Since a
     * MessageBeanListener is typically associated with active resources
     * in the MessageBeanContainer, it is the responsibility of the
     * MessageBeanClient to manage them judiciously.
     */
    void destroyMessageBeanListener(MessageBeanListener listener);

    Object createMessageBeanProxy(InvocationHandler handler) throws Exception;

    /**
     * This is used by the message provider to find out whether message
     * deliveries will be transacted or not. The message delivery preferences
     * must not change during the lifetime of a message endpoint. This
     * information is only a hint and may be useful to perform optimizations
     * on message delivery.
     *
     * @param method One of the methods used to deliver messages, e.g.
     *               onMessage method for jakarta.jms.MessageListener.
     *               Note that if the <code>method</code> is not one
     *               of the methods for message delivery, the behavior
     *               of this method is not defined.
     */
    boolean isDeliveryTransacted (Method method) ;


    /**
     * Returns the message-bean container's pool properties.
     */
    BeanPoolDescriptor getPoolDescriptor();

}
