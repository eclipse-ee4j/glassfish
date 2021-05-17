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

package org.glassfish.persistence.ejb.entitybean.container.distributed;

/**
 * DistributedReadOnlyBeanService defines the methods that can be used to
 *  implement a distributed ReadOnly beans. An instance of
 *  ReadOnlyBeanRefreshEventHandler is used to handle requests received from
 *  other server instances. An instance of  DistributedReadOnlyBeanNotifier is used to
 *  notify other server instances.
 *
 * @author Mahesh Kannan
 * @see ReadOnlyBeanRefreshEventHandler
 */
public interface DistributedReadOnlyBeanService {

    /**
     * This is typically done during appserver startup time. One of the LifeCycle
     *  listeners will create an instance of DistributedReadOnlyBeanNotifier and
     *  register that instance with DistributedReadOnlyBeanService
     *
     * @param notifier the notifier who is responsible for notifying refresh
     *  event(s) to other instances
     */
    public void setDistributedReadOnlyBeanNotifier(
            DistributedReadOnlyBeanNotifier notifier);

    /**
     * Called from ReadOnlyBeanContainer to register itself as a
     *  ReadOnlyBeanRefreshEventHandler.
     *
     * @param ejbID the ejbID that uniquely identifies the container
     * @param loader the class loader that will be used to serialize/de-serialize
     *  the primary key
     * @param handler The handler that is responsible for
     *  correctly refresing the state of a RO bean
     */
    public void addReadOnlyBeanRefreshEventHandler(
            long ejbID, ClassLoader loader,
            ReadOnlyBeanRefreshEventHandler handler);

    /**
     * Called from ReadOnlyBeanContainer to unregister itself as a
     *  ReadOnlyBeanRefreshEventHandler. Typically called during undeployment.
     *
     * @param ejbID
     */
    public void removeReadOnlyBeanRefreshEventHandler(long ejbID);

    /**
     * Called by the container after it has refreshed the RO bean
     *
     * @param ejbID the ejbID that uniquely identifies the container
     * @param pk the primary key to be refreshed
     */
    public void notifyRefresh(long ejbID, Object pk);

    /**
     * Called by the container after it has refreshed all RO beans
     *
     * @param ejbID the ejbID that uniquely identifies the container
     */
    public void notifyRefreshAll(long ejbID);

    /**
     * Called from the DistributedReadOnlyBeanNotifier when it receives a (remote)
     *  request to refresh a RO bean
     *
     * @param ejbID the ejbID that uniquely identifies the container
     * @param pk the primary key to be refreshed
     */
    public void handleRefreshRequest(long ejbID, byte[] pkData);

    /**
     * Called from the DistributedReadOnlyBeanNotifier when it receives a (remote)
     *  request to refresh all RO beans
     *
     * @param ejbID the ejbID that uniquely identifies the container
     */
    public void handleRefreshAllRequest(long ejbID);

}
