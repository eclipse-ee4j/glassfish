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

package com.sun.ejb.containers;

import com.sun.enterprise.container.common.spi.util.IndirectlySerializable;
import com.sun.enterprise.container.common.spi.util.SerializableObjectFactory;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * This class is used as a "proxy" or adapter between the business interface
 *  proxy and the EJBLocalObjectInvocationHandler. An instance of this class
 *  is created for each business interface of a bean. All java.lang.Object
 *  methods and mthods of IndirectlySerializable are handled by this
 *  InvocationHandler itself while the business interface methods are delegated
 *  to the delegate (which is the EJBLocalObjectInvocaionHandler).
 *
 * @author Mahesh Kannan
 *
 */
public class EJBLocalObjectInvocationHandlerDelegate
    implements InvocationHandler {

    private Class intfClass;
    private long containerId;
    private EJBLocalObjectInvocationHandler delegate;
    private boolean isOptionalLocalBusinessView;

    EJBLocalObjectInvocationHandlerDelegate(Class intfClass, long containerId,
            EJBLocalObjectInvocationHandler delegate) {
        this.intfClass = intfClass;
        this.containerId = containerId;
        this.delegate = delegate;
        this.isOptionalLocalBusinessView = delegate.isOptionalLocalBusinessView();
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {

        Class methodClass = method.getDeclaringClass();
        Object result = null;
        if( methodClass == java.lang.Object.class ) {
            result = InvocationHandlerUtil.invokeJavaObjectMethod
                (this, method, args);
        } else if( methodClass == IndirectlySerializable.class ) {
            result = this.getSerializableObjectFactory();
        }else {
            result = delegate.invoke(intfClass, method, args);
        }

        return result;
    }

    EJBLocalObjectInvocationHandler getDelegate() {
        return delegate;
    }

    public int hashCode() {
        return (int) containerId;
    }

    public boolean equals(Object other) {
        boolean result = false;

        if ((other != null)
        && (other instanceof EJBLocalObjectInvocationHandlerDelegate)) {
            EJBLocalObjectInvocationHandlerDelegate otherDelegate
                    = (EJBLocalObjectInvocationHandlerDelegate) other;
            if ((containerId == otherDelegate.containerId)
            && (intfClass == otherDelegate.intfClass)) {
                EJBLocalObjectInvocationHandler otherHandler
                    = otherDelegate.delegate;
                result = (delegate.getKey() != null)
                    ? delegate.getKey().equals(otherHandler.getKey())
                    : (otherHandler.getKey() == null);
            }
        }

        return result;
    }

    public String toString() {
        return intfClass.getName() + "_" + System.identityHashCode(this);
    }

    public SerializableObjectFactory getSerializableObjectFactory() {
        // Note: for stateful SessionBeans, the EJBLocalObjectImpl contains
        // a pointer to the EJBContext. We should not serialize it here.

        return new SerializableLocalObjectDelegate(
            containerId, intfClass.getName(), delegate.getKey(),
            isOptionalLocalBusinessView,
            delegate.getSfsbClientVersion());
    }

    private static final class SerializableLocalObjectDelegate
        implements SerializableObjectFactory
    {
        private long containerId;
        private String intfClassName;
        private Object primaryKey;
        private boolean isOptionalLocalBusinessView;
        private long version = 0L; //Used only for SFSBs

        SerializableLocalObjectDelegate(long containerId,
                String intfClassName, Object primaryKey, boolean isOptionalLocalBusView, long version) {
            this.containerId = containerId;
            this.intfClassName = intfClassName;
            this.primaryKey = primaryKey;
            this.isOptionalLocalBusinessView = isOptionalLocalBusView;
            this.version = version;
        }

        public Object createObject()
            throws IOException
        {
            BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
            EJBLocalObjectImpl ejbLocalBusinessObjectImpl = isOptionalLocalBusinessView ?
                container.getOptionalEJBLocalBusinessObjectImpl(primaryKey) :
                container.getEJBLocalBusinessObjectImpl(primaryKey);
            ejbLocalBusinessObjectImpl.setSfsbClientVersion(version);
            // Return the client EJBLocalObject.

            return isOptionalLocalBusinessView ?
                ejbLocalBusinessObjectImpl.getOptionalLocalBusinessClientObject() :
                ejbLocalBusinessObjectImpl.getClientObject(intfClassName);
        }
    }
}
