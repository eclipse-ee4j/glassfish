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

package org.glassfish.ejb.spi;

import org.glassfish.ejb.api.MessageBeanProtocolManager;

/*
 * MessageBeanClient is an interface implemented by clients
 * of the MessageBeanContainer.  It contains lifecycle methods
 * that allow the container to bootstrap the MessageBeanClient.
 * A MessageBeanClient is some part of the container that needs
 * to deliver messages to a message-driven bean.
 *
 * @author Kenneth Saks
 */
public interface MessageBeanClient {

    /**
     * First method called by MessageBeanContainer during bootstrapping.
     * Allow the MessageBeanClient to initialize itself.  Message delivery
     * should not begin until start is called.
     *
     * @param pm MessageBeanProtocolManager allows the MessageBeanClient to
     * access the services provided by the MessageBeanContainer.
     */
    void setup(MessageBeanProtocolManager pm) throws Exception;

    /**
     * MessageBeanContainer calls this when it is ready handle message delivery.
     */
    void start() throws Exception;

    /**
     * MessageBeanContainer calls this to shutdown MessageBeanClient.
     */
    void close();

}
