/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
 * proxy and the {@link EJBLocalObjectInvocationHandler}.
 * An instance of this class is created for each business interface of a bean.
 * All java.lang.Object methods and methods of {@link IndirectlySerializable} are handled by this
 * {@link InvocationHandler} itself while the business interface methods are delegated
 * to the delegate (which is the {@link EJBLocalObjectInvocationHandler}).
 *
 * @author Mahesh Kannan
 */
public class EJBLocalObjectInvocationHandlerDelegate implements InvocationHandler, IndirectlySerializable {

    private final Class<?> intfClass;
    private final long containerId;
    private final EJBLocalObjectInvocationHandler delegate;
    private final boolean isOptionalLocalBusinessView;

    EJBLocalObjectInvocationHandlerDelegate(Class<?> intfClass, long containerId,
        EJBLocalObjectInvocationHandler delegate) {
        this.intfClass = intfClass;
        this.containerId = containerId;
        this.delegate = delegate;
        this.isOptionalLocalBusinessView = delegate.isOptionalLocalBusinessView();
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        final Class<?> methodClass = method.getDeclaringClass();
        if (methodClass == java.lang.Object.class) {
            return InvocationHandlerUtil.invokeJavaObjectMethod(this, method, args);
        } else if (methodClass == IndirectlySerializable.class) {
            return this.getSerializableObjectFactory();
        } else {
            return delegate.invoke(intfClass, method, args);
        }
    }

    EJBLocalObjectInvocationHandler getDelegate() {
        return delegate;
    }

    @Override
    public int hashCode() {
        return (int) containerId;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof EJBLocalObjectInvocationHandlerDelegate) {
            EJBLocalObjectInvocationHandlerDelegate otherDelegate = (EJBLocalObjectInvocationHandlerDelegate) other;
            if (containerId == otherDelegate.containerId && intfClass == otherDelegate.intfClass) {
                EJBLocalObjectInvocationHandler otherHandler = otherDelegate.delegate;
                return delegate.getKey() == null ? otherHandler.getKey() == null : delegate.getKey().equals(otherHandler.getKey());
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return intfClass.getName() + "_" + System.identityHashCode(this);
    }

    @Override
    public SerializableObjectFactory getSerializableObjectFactory() {
        // Note: for stateful SessionBeans, the EJBLocalObjectImpl contains
        // a pointer to the EJBContext. We should not serialize it here.

        return new SerializableLocalObjectDelegate(
            containerId, intfClass.getName(), delegate.getKey(),
            isOptionalLocalBusinessView,
            delegate.getSfsbClientVersion());
    }

    private static final class SerializableLocalObjectDelegate implements SerializableObjectFactory {
        private static final long serialVersionUID = 1L;
        private final long containerId;
        private final String intfClassName;
        private final Object primaryKey;
        private final boolean isOptionalLocalBusinessView;
        // Used only for SFSBs
        private long version = 0L;

        SerializableLocalObjectDelegate(long containerId,
                String intfClassName, Object primaryKey, boolean isOptionalLocalBusView, long version) {
            this.containerId = containerId;
            this.intfClassName = intfClassName;
            this.primaryKey = primaryKey;
            this.isOptionalLocalBusinessView = isOptionalLocalBusView;
            this.version = version;
        }

        @Override
        public Object createObject() throws IOException {
            final BaseContainer container = EjbContainerUtilImpl.getInstance().getContainer(containerId);
            final EJBLocalObjectImpl ejbLocalBusinessObjectImpl = isOptionalLocalBusinessView
                ? container.getOptionalEJBLocalBusinessObjectImpl(primaryKey)
                : container.getEJBLocalBusinessObjectImpl(primaryKey);
            ejbLocalBusinessObjectImpl.setSfsbClientVersion(version);

            // Return the client EJBLocalObject.
            return isOptionalLocalBusinessView
                ? ejbLocalBusinessObjectImpl.getOptionalLocalBusinessClientObject()
                : ejbLocalBusinessObjectImpl.getClientObject(intfClassName);
        }
    }
}
