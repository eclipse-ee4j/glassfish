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
 * ReadOnlyBeanRefreshEventHandler defines the methods that can be used to
 *  implement a distributed ReadOnly beans. An instance of
 *  ReadOnlyBeanRefreshEventHandler is used to handle requests received from
 *  other server instances. An instance of  DistributedReadOnlyBeanNotifier is used to
 *  notify other server instances.
 *
 * @author Mahesh Kannan
 * @see DistributedReadOnlyBeanService
 */
public interface ReadOnlyBeanRefreshEventHandler {

    /**
     * Called from DistributedReadOnlyBeanServiceImpl before de-serializing
     *  the bean's pk
     * @return the application class loader
     */
    public ClassLoader getClassLoader();

    /**
     * Called from DistributedReadOnlyBeanServiceImpl when a refresh message
     *  arrives at this instance
     * @param pk the primary key that needs to be refreshed
     */
    public void handleRefreshRequest(Object pk);

    /**
     * Called from DistributedReadOnlyBeanServiceImpl when a refreshAll message
     *  arrives at this instance
     */
    public void handleRefreshAllRequest();

}
