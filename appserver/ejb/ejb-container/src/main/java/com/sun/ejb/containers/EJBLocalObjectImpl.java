/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBLocalHome;
import jakarta.ejb.EJBLocalObject;
import jakarta.ejb.RemoveException;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.HashMap;

/**
 * Implementation of the EJBLocalObject interface.
 * This is NOT serializable to prevent local references from leaving
 * the JVM.
 * It is extended by the generated concrete type-specific EJBLocalObject
 * implementation (e.g. Hello_EJBLocalObject).
 *
 * @author Mahesh Kannan
 */
public abstract class EJBLocalObjectImpl extends EJBLocalRemoteObject
    implements EJBLocalObject, IndirectlySerializable {

    private static final Method REMOVE_METHOD = getRemoveMethod();

    private static Method getRemoveMethod() {
        try {
            return EJBLocalObject.class.getMethod("remove",  new Class[0]);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Exception retrieving remove method", e);
        }
    }

    // True this local object instance represents the LocalHome view
    // False if this local object instance represents the LocalBusiness view
    private boolean isLocalHomeView;


    // True this local object instance represents the client view of a bean
    // with a no-interface view.
    private boolean isOptionalLocalBusinessView;

    private Object optionalLocalBusinessClientObject;

    private final HashMap<String, Object> clientObjectMap = new HashMap<>();

    /**
     * Get the client object corresponding to an EJBLocalObjectImpl.
     * Users of this class cannot
     * assume they can cast an EJBLocalObjectImpl to the object that
     * the client uses,  and vice-versa.  This is overridden in the
     * InvocationHandler.  Only applicable for local home view.
     */
    public Object getClientObject() {
        return this;
    }

    void mapClientObject(String intfClassName, Object obj) {
        clientObjectMap.put(intfClassName, obj);
        if( isOptionalLocalBusinessView ) {
            optionalLocalBusinessClientObject = obj;
        }
    }

    Object getClientObject(String intfClassName) {
        return clientObjectMap.get(intfClassName);
    }

    void setIsLocalHomeView(boolean flag) {
        isLocalHomeView = flag;
    }

    boolean isLocalHomeView() {
        return isLocalHomeView;
    }

    boolean isOptionalLocalBusinessView() {
        return isOptionalLocalBusinessView;
    }

    void setIsOptionalLocalBusinessView(boolean flag) {
        isOptionalLocalBusinessView = flag;
    }

    Object getOptionalLocalBusinessClientObject() {
        return optionalLocalBusinessClientObject;
    }


    /**
     * Since EJBLocalObject might be a dynamic proxy, the container can't assume
     * it can cast from EJBLocalObject to EJBLocalObjectImpl.  This convenience
     * method is used to hide the logic behind the translation from an
     * client-side EJBLocalObject to the corresponding EJBLocalObjectImpl.
     *
     * In the case of a proxy, the invocation handler is the
     * EJBLocalObjectImpl.  Otherwise, the argument is returned as is.
     * NOTE : To translate in the other direction, use
     *   EJBLocalObjectImpl.getEJBLocalObject()
     *
     */
    public static EJBLocalObjectImpl toEJBLocalObjectImpl(EJBLocalObject localObj) {
        if (localObj instanceof EJBLocalObjectImpl) {
            return (EJBLocalObjectImpl) localObj;
        }
        return (EJBLocalObjectImpl) Proxy.getInvocationHandler(localObj);
    }

    @Override
    public EJBLocalHome getEJBLocalHome() throws EJBException {
        container.authorizeLocalMethod(BaseContainer.EJBLocalObject_getEJBLocalHome);
        container.checkExists(this);
        return container.getEJBLocalHome();
    }

    @Override
    public void remove() throws RemoveException, EJBException {
        // authorization is performed within container
        try {
            container.removeBean(this, REMOVE_METHOD, true);
        } catch (RemoteException re) {
            // This should never be thrown for local invocations, but it's
            // part of the removeBean signature. If for some strange
            // reason it happens, convert to EJBException
            throw new EJBException("unexpected RemoteException", re);
        }
    }

    @Override
    public Object getPrimaryKey() throws EJBException {
        container.authorizeLocalGetPrimaryKey(this);
        return primaryKey;
    }

    @Override
    public boolean isIdentical(EJBLocalObject other) throws EJBException {
        container.authorizeLocalMethod(BaseContainer.EJBLocalObject_isIdentical);
        container.checkExists(this);

        // For all types of beans (entity, stful/stless session),
        // there is exactly one EJBLocalObject instance per bean identity.
        return this == other;
    }

    /**
     * Called from EJBUtils.EJBObjectOutputStream.replaceObject
     */
    @Override
    public SerializableObjectFactory getSerializableObjectFactory() {
        // Note: for stateful SessionBeans, the EJBLocalObjectImpl contains
        // a pointer to the EJBContext. We should not serialize it here.

        return new SerializableLocalObject(container.getEjbDescriptor().getUniqueId(), isLocalHomeView,
            isOptionalLocalBusinessView, primaryKey, getSfsbClientVersion());
    }

    private static final class SerializableLocalObject implements SerializableObjectFactory {

        private static final long serialVersionUID = 1L;
        private final long containerId;
        private final boolean localHomeView;
        private final boolean optionalLocalBusView;
        private final Object primaryKey;
        // Used only for SFSBs
        private final long version;

        SerializableLocalObject(long containerId,
                                boolean localHomeView,
                                boolean optionalLocalBusinessView,
                                Object primaryKey, long version) {
            this.containerId = containerId;
            this.localHomeView = localHomeView;
            this.optionalLocalBusView = optionalLocalBusinessView;
            this.primaryKey = primaryKey;
            this.version = version;
        }

        long getVersion() {
            return version;
        }

        @Override
        public Object createObject() throws IOException {
            BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
            if (localHomeView) {
                EJBLocalObjectImpl ejbLocalObjectImpl = container.getEJBLocalObjectImpl(primaryKey);
                ejbLocalObjectImpl.setSfsbClientVersion(version);
                // Return the client EJBLocalObject.
                return ejbLocalObjectImpl.getClientObject();
            }
            EJBLocalObjectImpl ejbLocalBusinessObjectImpl = optionalLocalBusView
                ? container.getOptionalEJBLocalBusinessObjectImpl(primaryKey)
                : container.getEJBLocalBusinessObjectImpl(primaryKey);
            ejbLocalBusinessObjectImpl.setSfsbClientVersion(version);
            // Return the client EJBLocalObject.
            return ejbLocalBusinessObjectImpl.getClientObject();
        }
    }

}
