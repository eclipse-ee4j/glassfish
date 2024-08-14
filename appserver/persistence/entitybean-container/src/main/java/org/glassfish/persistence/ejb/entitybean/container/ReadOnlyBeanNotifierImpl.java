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

package org.glassfish.persistence.ejb.entitybean.container;


import org.glassfish.persistence.ejb.entitybean.container.spi.ReadOnlyEJBHome;
import org.jvnet.hk2.annotations.Service;

/**
 * An instance of ReadOnlyBeanNotifier is used to refresh ReadOnlyBeans
 *
 * @author Mahesh Kannan
 */

@Service
public final class ReadOnlyBeanNotifierImpl
    implements java.io.Serializable,
        com.sun.appserv.ejb.ReadOnlyBeanNotifier
{
    transient private ReadOnlyEJBHome readOnlyBeanHome = null;

    public ReadOnlyBeanNotifierImpl()
        throws java.rmi.RemoteException
    {
        super();
    }

    public void setHome(ReadOnlyEJBHome home) throws java.rmi.RemoteException {
        this.readOnlyBeanHome = home;
    }

    public void refresh (Object primaryKey)
        throws java.rmi.RemoteException
    {
        readOnlyBeanHome._refresh_com_sun_ejb_containers_read_only_bean_(primaryKey);
    }

    public void refreshAll() throws java.rmi.RemoteException
    {
        readOnlyBeanHome._refresh_All();
    }

}

