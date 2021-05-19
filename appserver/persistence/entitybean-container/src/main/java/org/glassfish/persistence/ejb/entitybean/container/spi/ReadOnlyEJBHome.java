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

package org.glassfish.persistence.ejb.entitybean.container.spi;

/**
 * Home interface for all Remote ReadOnly Beans
 *
 * @author Mahesh Kannan
 */
public interface ReadOnlyEJBHome
    extends jakarta.ejb.EJBHome
{

    public void _refresh_com_sun_ejb_containers_read_only_bean_(Object primaryKey)
        throws java.rmi.RemoteException;

    public void _refresh_All() throws java.rmi.RemoteException;
}
