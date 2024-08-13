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

package com.sun.ejb.containers;

import com.sun.ejb.Container;
import com.sun.ejb.portable.HomeHandleImpl;

import jakarta.ejb.CreateException;
import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBMetaData;
import jakarta.ejb.EJBObject;
import jakarta.ejb.Handle;
import jakarta.ejb.HomeHandle;
import jakarta.ejb.RemoveException;

import java.lang.reflect.Method;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class implements the EJBHome interface.
 * This class is also the base class for all generated concrete EJBHome
 * implementations.
 * At deployment time, one instance of the EJBHome is created
 * for each EJB class in a JAR that has a remote home.
 *
 */

public abstract class EJBHomeImpl
    implements jakarta.ejb.EJBHome
{

    protected static final Logger _logger = EjbContainerUtilImpl.getLogger();

    private BaseContainer container;

    /**
     * This constructor is called from an EJBHome implementation's constructor.
     */
    protected EJBHomeImpl()
        throws RemoteException
    {
    }

    /**
     * Called from EJBHome implementation.
     */
    protected final Container getContainer() {
        return container;
    }


    /**
     * Called from BaseContainer only.
     */
    final void setContainer(BaseContainer c) {
        container = c;
    }

    /**
     * Get the EJBHome corresponding to an EJBHomeImpl.
     * These objects are one and the same when the home is generated,
     * but distinct in the case of dynamic proxies.  Therefore, code can't
     * assume it can cast an EJBHomeImpl to the EJBHome that
     * the client uses,  and vice-versa.  This is overridden in the
     * InvocationHandler.
     */
    protected EJBHome getEJBHome() {
        return this;
    }

    /**
     * Create a new EJBObject and new EJB if necessary.
     * This is called from the generated "HelloEJBHomeImpl" create method.
     * Return the EJBObject for the bean.
     */
    public EJBObjectImpl createEJBObjectImpl()
        throws RemoteException, CreateException
    {
        return container.createEJBObjectImpl();
    }

    public EJBObjectImpl createRemoteBusinessObjectImpl()
        throws RemoteException, CreateException
    {
        return container.createRemoteBusinessObjectImpl();
    }


    /***************************************
***********************************
    The following are implementations of jakarta.ejb.EJBHome methods.
     **************************************************************************/

    /**
     * This is the implementation of the jakarta.ejb.EJBHome remove method.
     * @exception RemoveException on error during removal
     */
    @Override
    public final void remove(Handle handle)
        throws RemoteException, RemoveException
    {
        container.authorizeRemoteMethod(BaseContainer.EJBHome_remove_Handle);

        EJBObject ejbo;
        try {
            ejbo = handle.getEJBObject();
        } catch ( RemoteException ex ) {
            _logger.log(Level.FINE, "Exception in method remove()", ex);
            NoSuchObjectException nsoe =
                new NoSuchObjectException(ex.toString());
            nsoe.initCause(ex);
            throw nsoe;
        }
        ejbo.remove();
    }


    /**
     * This is the implementation of the jakarta.ejb.EJBHome remove method.
     * @exception RemoveException on error during removal
     */
    @Override
    public final void remove(Object primaryKey)
        throws RemoteException, RemoveException
    {
        if (container.getContainerInfo().type != BaseContainer.ContainerType.ENTITY) {
            // Session beans dont have primary keys. EJB2.0 Section 6.6
            throw new RemoveException("Invalid remove operation.");
        }

        container.authorizeRemoteMethod(BaseContainer.EJBHome_remove_Pkey);

        Method method=null;
        try {
            method = EJBHome.class.getMethod("remove",
                        new Class[]{Object.class});
        } catch ( NoSuchMethodException e ) {
            _logger.log(Level.FINE, "Exception in method remove()", e);
        }

        container.doEJBHomeRemove(primaryKey, method, false);
    }


    /**
     * This is the implementation of the jakarta.ejb.EJBHome method.
     */
    @Override
    public final EJBMetaData getEJBMetaData()
        throws RemoteException
    {
        container.authorizeRemoteMethod(BaseContainer.EJBHome_getEJBMetaData);

        return container.getEJBMetaData();
    }

    /**
     * This is the implementation of the jakarta.ejb.EJBHome getHomeHandle
     * method.
     */
    @Override
    public final HomeHandle getHomeHandle()
        throws RemoteException
    {
        container.authorizeRemoteMethod(BaseContainer.EJBHome_getHomeHandle);

        return new HomeHandleImpl(container.getEJBHomeStub());
    }
}
