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

import java.lang.reflect.Method;

import org.glassfish.persistence.ejb.entitybean.container.spi.ReadOnlyEJBHome;
import com.sun.ejb.containers.util.MethodMap;
import com.sun.enterprise.deployment.EjbDescriptor;

/**
 * Implementation of the EJBHome interface for ReadOnly Entity Beans.
 * This class is also the base class for all generated concrete ReadOnly
 * EJBHome implementations.
 * At deployment time, one instance of ReadOnlyEJBHomeImpl is created
 * for each EJB class in a JAR that has a remote home.
 *
 * @author Mahesh Kannan
 */

public final class ReadOnlyEJBHomeImpl
    extends EntityBeanHomeImpl
    implements ReadOnlyEJBHome
{
    // robContainer initialized in ReadOnlyBeanContainer.initializeHome()
    private ReadOnlyBeanContainer robContainer;

    ReadOnlyEJBHomeImpl(EjbDescriptor ejbDescriptor,
                             Class homeIntfClass)
            throws Exception {
        super(ejbDescriptor, homeIntfClass);
    }

    /**
     * Called from ReadOnlyBeanContainer only.
     */
    final void setReadOnlyBeanContainer(ReadOnlyBeanContainer container) {
        this.robContainer = container;
    }


    /***********************************************/
    /** Implementation of ReadOnlyEJBHome methods **/
    /***********************************************/

    public void _refresh_com_sun_ejb_containers_read_only_bean_(Object primaryKey)
        throws java.rmi.RemoteException
    {
        if (robContainer != null) {
            robContainer.setRefreshFlag(primaryKey);
        }
    }

    public void _refresh_All() throws java.rmi.RemoteException
    {
        if (robContainer != null) {
            robContainer.refreshAll();
        }
    }

    protected boolean invokeSpecialEJBHomeMethod(Method method, Class methodClass,
            Object[] args) throws Exception {
        if( methodClass == ReadOnlyEJBHome.class ) {
            if( method.getName().equals("_refresh_All") ) {
                _refresh_All();
            } else {
                _refresh_com_sun_ejb_containers_read_only_bean_
                    (args[0]);
            }

            return true;
        }
        return false;
    }
}

