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
 * An instance of DistributedReadOnlyBeanNotifier is used to notify other server
 *  instances to refresh a ReadOnly Bean. An instance of
 *  ReadOnlyBeanRefreshEventHandler is used to handle requests received from
 *  other server instances.
 *
 *  @author Mahesh Kannan
 *  @see ReadOnlyBeanRefreshEventHandler
 */
public interface DistributedReadOnlyBeanNotifier {

    /**
     * This is called by the container after it has called refresh
     *
     * @param ejbID the ejbID that uniquely identifies the container
     * @param pk The primary key of the bean(s) that is to be refreshed
     */
    public void notifyRefresh(long ejbID, byte[] pk);

    /**
     * This is called by the container after it has called refresh
     *
     * @param ejbID the ejbID that uniquely identifies the container
     * @param pk The primary key of the bean(s) that is to be refreshed
     */
    public void notifyRefreshAll(long ejbID);

}
