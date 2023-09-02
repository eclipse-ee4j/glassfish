/*
 * Copyright (c) 2021, 2023 Contributors to the Eclipse Foundation
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
import com.sun.ejb.spi.container.OptionalLocalInterfaceProvider;
import com.sun.enterprise.deployment.EjbInterceptor;

import jakarta.ejb.EJBException;
import jakarta.ejb.NoSuchEJBException;

import java.lang.reflect.Field;
import java.lang.reflect.InaccessibleObjectException;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.glassfish.ejb.LogFacade;
import org.glassfish.ejb.api.EjbContainerServices;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.jvnet.hk2.annotations.Service;

import static com.sun.ejb.codegen.AsmSerializableBeanGenerator.getGeneratedSerializableClassName;

/**
 *
 */
@Service
public class EjbContainerServicesImpl implements EjbContainerServices {



    public <S> S  getBusinessObject(Object ejbRef, java.lang.Class<S> businessInterface) {

        EJBLocalObjectImpl localObjectImpl = getEJBLocalObject(ejbRef);

        if( localObjectImpl == null ) {
            throw new IllegalStateException("Invalid ejb ref");
        }

        Container container = localObjectImpl.getContainer();
        EjbDescriptor ejbDesc = container.getEjbDescriptor();

        S businessObject = null;

        if (businessInterface != null) {
            String intfName = businessInterface.getName();
            if (ejbDesc.getLocalBusinessClassNames().contains(intfName)) {

                // Get proxy corresponding to this business interface.
                businessObject = (S) localObjectImpl.getClientObject(intfName);

            } else if( ejbDesc.isLocalBean()) {
                //If this is a no-interface view session bean, the bean
                //can be accessed through interfaces in its superclass as well
                boolean isValidBusinessInterface =
                    ejbDesc.getNoInterfaceLocalBeanClasses().contains(intfName);
                if ((intfName.equals(ejbDesc.getEjbClassName()))
                        || isValidBusinessInterface) {
                    businessObject = (S) localObjectImpl.getClientObject(ejbDesc.getEjbClassName());
                }

            }
        }

        if( businessObject == null ) {
            throw new IllegalStateException("Unable to convert ejbRef for ejb " +
            ejbDesc.getName() + " to a business object of type " + businessInterface);
        }

        return businessObject;

    }

    public void remove(Object ejbRef) {

        EJBLocalObjectImpl localObjectImpl = getEJBLocalObject(ejbRef);

        if( localObjectImpl == null ) {
            throw new UnsupportedOperationException("Invalid ejb ref");
        }

        Container container = localObjectImpl.getContainer();
        EjbDescriptor ejbDesc = container.getEjbDescriptor();
        boolean isStatefulBean = false;

        if( ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) ) {

            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            isStatefulBean = sessionDesc.isStateful();

        }

        if( !isStatefulBean ) {

             // TODO CDI impl may incorrectly call this for stateless/singleton
            // beans.  Until it's fixed just treat it as a no-op. Otherwise, any app acquiring
            // stateless/singleton references via CDI could fail until bug is fixed.
            return;

            // TODO reenable this after bug is fixed
            //throw new UnsupportedOperationException("ejbRef for ejb " +
             //       ejbDesc.getName() + " is not a stateful bean ");
        }

        try {
            localObjectImpl.remove();
        } catch(EJBException e) {
            LogFacade.getLogger().log(Level.FINE, "EJBException during remove. ", e);
        } catch(jakarta.ejb.RemoveException re) {
            throw new NoSuchEJBException(re.getMessage(), re);
        }

    }


    public boolean isRemoved(Object ejbRef) {

        EJBLocalObjectImpl localObjectImpl = getEJBLocalObject(ejbRef);

        if( localObjectImpl == null ) {
            throw new UnsupportedOperationException("Invalid ejb ref");
        }

        Container container = localObjectImpl.getContainer();
        EjbDescriptor ejbDesc = container.getEjbDescriptor();
        boolean isStatefulBean = false;

        if( ejbDesc.getType().equals(EjbSessionDescriptor.TYPE) ) {

            EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) ejbDesc;
            isStatefulBean = sessionDesc.isStateful();

        }

        if( !isStatefulBean ) {

            // TODO CDI impl is incorrectly calling isRemoved for stateless/singleton
            // beans.  Until it's fixed just return false. Otherwise, any app acquiring
            // stateless/singleton references via CDI will fail until bug is fixed.
            return false;

            // TODO reenable this per SessionObjectReference.isRemoved SPI
            //throw new UnsupportedOperationException("ejbRef for ejb " +
             //   ejbDesc.getName() + " is not a stateful bean ");
        }

        boolean removed = false;

        try {
            ((BaseContainer)container).checkExists(localObjectImpl);
        } catch(Exception e) {
            removed = true;
        }

        return removed;

    }

    private EJBLocalObjectImpl getEJBLocalObject(Object ejbRef) {

        // ejbRef is assumed to be either a local business view or
        // no-interface view

        EJBLocalObjectInvocationHandlerDelegate localObj = null;

        // First try to convert it as a local or remote business interface object
        try {

            localObj = (EJBLocalObjectInvocationHandlerDelegate) Proxy.getInvocationHandler(ejbRef);

        } catch(IllegalArgumentException iae) {

            Proxy proxy;

            if( ejbRef instanceof OptionalLocalInterfaceProvider ) {

                try {

                     Field proxyField = ejbRef.getClass().getDeclaredField("__ejb31_delegate");

                     final Field finalF = proxyField;
                        java.security.AccessController.doPrivileged(
                        new java.security.PrivilegedExceptionAction() {
                            public java.lang.Object run() throws Exception {
                                if (!finalF.trySetAccessible()) {
                                    throw new InaccessibleObjectException("Unable to make accessible: " + finalF);
                                }
                                return null;
                            }
                        });

                      proxy = (Proxy) proxyField.get(ejbRef);

                } catch(Exception e) {

                    throw new IllegalArgumentException("Invalid ejb ref", e);
                }


                try {

                    localObj = (EJBLocalObjectInvocationHandlerDelegate)
                            Proxy.getInvocationHandler(proxy);

                } catch(IllegalArgumentException i) {}

            }
        }

        return (localObj != null) ?  localObj.getDelegate() : null;
    }

    public boolean isEjbManagedObject(Object desc, Class c) {

        String className = c.getName();

        EjbDescriptor ejbDesc = (EjbDescriptor) desc;

        Set<String> ejbManagedObjectClassNames = new HashSet<String>();
        ejbManagedObjectClassNames.add(ejbDesc.getEjbClassName());

        for(EjbInterceptor next : ejbDesc.getInterceptorClasses()) {
            if( !next.isCDIInterceptor() ) {
                ejbManagedObjectClassNames.add(next.getInterceptorClassName());
            }
        }

        Set<String> serializableClassNames = new HashSet<String>();

        for(String next : ejbManagedObjectClassNames) {
            // Add the serializable sub-class version of each name as well
            serializableClassNames.add(getGeneratedSerializableClassName(next));
        }

        boolean isEjbManagedObject = ejbManagedObjectClassNames.contains(className) ||
                serializableClassNames.contains(className);

        return isEjbManagedObject;

    }

}
