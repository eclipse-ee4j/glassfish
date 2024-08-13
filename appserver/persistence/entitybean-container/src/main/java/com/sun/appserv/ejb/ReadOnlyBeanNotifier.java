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

package com.sun.appserv.ejb;

import java.rmi.RemoteException;

import org.glassfish.persistence.ejb.entitybean.container.spi.ReadOnlyEJBHome;
import org.jvnet.hk2.annotations.Contract;

/**
 * ReadOnlyBeanNotifier is used to force refresh of ReadOnly beans
 *
 * @author Mahesh Kannan
 */

@Contract
public interface ReadOnlyBeanNotifier
    extends java.rmi.Remote
{

    /**
     * This method would be used by the user to manually force the
     *  refresh of read only beans for an application. After this method
     *  the next access to the bean, identified by the primary key, will
     *  cause a ejbLoad() to be called on the bean
     *
     * @param The primary of the bean to be refreshed
     */
    public void refresh(Object primaryKey)
        throws RemoteException;

    /**
     * This method forces *all* primary keys for a read-only bean
     * to be marked as needing a refresh.
     */
    public void refreshAll()
        throws RemoteException;

    /**
     * This is an init method for the helper class not to depend on the impl
     */
    public void setHome(ReadOnlyEJBHome home) throws RemoteException;
}
