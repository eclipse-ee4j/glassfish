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

import com.sun.enterprise.container.common.spi.util.IndirectlySerializable;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;

import java.lang.reflect.Method;


import jakarta.ejb.*;

import java.util.logging.*;

import java.io.IOException;

/**
 * Implementation of the EJBLocalHome interface.
 * This class is also the base class for all generated concrete EJBLocalHome
 * implementations.
 * At deployment time, one instance of the EJBLocalHome is created
 * for each EJB class in a JAR that has a local home.
 *
 * @author Mahesh Kannan
 */
public abstract class EJBLocalHomeImpl
    implements EJBLocalHome, IndirectlySerializable
{
    protected BaseContainer container;

    private static final Logger _logger = EjbContainerUtilImpl.getLogger();

    /**
     * Called from BaseContainer only.
     */
    final void setContainer(BaseContainer c) {
        container = c;
    }

    /**
     * Called from concrete EJBLocalHome implementation.
     */
    protected final BaseContainer getContainer() {
        return container;
    }

    /**
     * Get the EJBLocalHome corresponding to an EJBLocalHomeImpl.
     * These objects are one and the same when the local home is generated,
     * but distinct in the case of dynamic proxies.  Therefore, code can't
     * assume it can cast an EJBLocalHomeImpl to the EJBLocalHome that
     * the client uses,  and vice-versa.  This is overridden in the
     * InvocationHandler.
     */
    protected EJBLocalHome getEJBLocalHome() {
        return this;
    }

    /**
     * Create a new EJBLocalObjectImpl and new EJB if necessary.
     * This is called from the concrete "HelloEJBHomeImpl" create method.
     * Return the EJBObjectImpl for the bean.
     */
    protected EJBLocalObjectImpl createEJBLocalObjectImpl()
        throws CreateException
    {
        return container.createEJBLocalObjectImpl();
    }

    /**
     * Create a new EJBLocalBusinessObjectImpl and new EJB if necessary.
     * @param intfName
     */
    protected final EJBLocalObjectImpl createEJBLocalBusinessObjectImpl(String intfName)
        throws CreateException
    {
        // intfName is the Generated interface name in the case of the no-interface view
        return container.createEJBLocalBusinessObjectImpl(intfName);
    }

    /**
     * This is the implementation of the jakarta.ejb.EJBLocalHome remove method.
     */
    public final void remove(Object primaryKey)
        throws RemoveException, EJBException
    {
        if (container.getContainerType() != BaseContainer.ContainerType.ENTITY) {
            // Session beans dont have primary keys. EJB2.0 Section 6.6.
            throw new RemoveException("Attempt to call remove(Object primaryKey) on a session bean.");
        }

        container.authorizeLocalMethod(BaseContainer.EJBLocalHome_remove_Pkey);

        Method method=null;
        try {
            method = EJBLocalHome.class.getMethod("remove",
                                                  new Class[]{Object.class});
        } catch ( NoSuchMethodException e ) {
            _logger.log(Level.FINE, "Exception in method remove()", e);
        }

        try {
            container.doEJBHomeRemove(primaryKey, method, true);
        } catch(java.rmi.RemoteException re) {
            // This should never be thrown for local invocations, but it's
            // part of the removeBean signature.  If for some strange
            // reason it happens, convert to EJBException
            EJBException ejbEx =new EJBException("unexpected RemoteException");
            ejbEx.initCause(re);
            throw ejbEx;
        }
    }

    public SerializableObjectFactory getSerializableObjectFactory() {
        return new SerializableLocalHome(
            container.getEjbDescriptor().getUniqueId());
    }

    public static final class SerializableLocalHome
        implements SerializableObjectFactory
    {
        private long ejbId;

        public SerializableLocalHome(long uniqueId) {
            this.ejbId = uniqueId;
        }

        public Object createObject()
            throws IOException
        {
            // Return the LocalHome by getting the target container based
            // on the ejb id.  Note that we can assume this is the
            // LocalHome rather than a LocalBusinessHome since the
            // LocalBusinessHome is never visible to the application and
            // would never be stored in SFSB state.
            BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(ejbId);
            return container.getEJBLocalHome();
        }
    }
}

