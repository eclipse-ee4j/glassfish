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

import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import jakarta.ejb.SessionContext;
import jakarta.ejb.TimerService;
import jakarta.transaction.UserTransaction;

import java.rmi.Remote;

import org.glassfish.api.invocation.ComponentInvocation;

import static com.sun.ejb.EJBUtils.createRemoteBusinessObject;
import static com.sun.ejb.codegen.RemoteGenerator.getGeneratedRemoteIntfName;

/**
 * Implementation of EJBContext for SessionBeans
 *
 * @author Mahesh Kannan
 */
public abstract class AbstractSessionContextImpl extends EJBContextImpl implements SessionContext {

    protected Object instanceKey;

    protected String ejbName;

    protected AbstractSessionContextImpl(Object ejb, BaseContainer container) {
        super(ejb, container);
        EjbSessionDescriptor sessionDesc = (EjbSessionDescriptor) getContainer().getEjbDescriptor();
        this.ejbName = sessionDesc.getName();
    }


    public Object getInstanceKey() {
        return instanceKey;
    }


    public void setInstanceKey(Object instanceKey) {
        this.instanceKey = instanceKey;
    }


    @Override
    public String toString() {
        return ejbName + "; id: " + instanceKey;
    }


    @Override
    public TimerService getTimerService() throws IllegalStateException {
        // Instance key is first set between after setSessionContext and
        // before ejbCreate
        if (instanceKey == null) {
            throw new IllegalStateException("Operation not allowed");
        }
        EJBTimerService timerService = EJBTimerService.getValidEJBTimerService();
        return new EJBTimerServiceWrapper(timerService, this);
    }


    @Override
    public UserTransaction getUserTransaction() throws IllegalStateException {
        // The state check ensures that an exception is thrown if this
        // was called from setSession/EntityContext. The instance key check
        // ensures that an exception is not thrown if this was called
        // from a stateless SessionBean's ejbCreate.
        if ((state == BeanState.CREATED) && (instanceKey == null)) {
            throw new IllegalStateException("Operation not allowed");
        }

        return ((BaseContainer) getContainer()).getUserTransaction();
    }


    @Override
    public <T> T getBusinessObject(Class<T> businessInterface) throws IllegalStateException {
        // businessInterface param can also be a class in the case of the
        // no-interface view

        // getBusinessObject not allowed for Stateless/Stateful beans
        // until after dependency injection
        if (instanceKey == null) {
            throw new IllegalStateException("Operation not allowed");
        }

        final EjbDescriptor ejbDesc = container.getEjbDescriptor();
        @SuppressWarnings("unchecked")
        T businessObject = (T) getBusinessObject(businessInterface, ejbDesc);
        if (businessObject == null) {
            throw new IllegalStateException(
                "Invalid business interface : " + businessInterface + " for ejb " + ejbDesc.getName());
        }
        return businessObject;
    }


    @Override
    public Class<?> getInvokedBusinessInterface() throws IllegalStateException {
        final Class<?> businessInterface;
        try {
            final ComponentInvocation inv = EjbContainerUtilImpl.getInstance().getCurrentInvocation();
            if (inv instanceof EjbInvocation) {
                final EjbInvocation invocation = (EjbInvocation) inv;
                if (invocation.isBusinessInterface) {
                    if (container.isLocalBeanClass(invocation.clientInterface.getName())) {
                        businessInterface = container.getEJBClass();
                    } else {
                        businessInterface = invocation.clientInterface;
                    }
                } else {
                    businessInterface = null;
                }
            } else {
                businessInterface = null;
            }
        } catch (Exception e) {
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }
        if (businessInterface == null) {
            throw new IllegalStateException(
                "Attempt to call getInvokedBusinessInterface outside the scope of a business method");
        }
        return businessInterface;
    }


    @Override
    public boolean wasCancelCalled() {
        try {
            ComponentInvocation inv = EjbContainerUtilImpl.getInstance().getCurrentInvocation();
            if (inv != null && inv instanceof EjbInvocation) {
                EjbInvocation invocation = (EjbInvocation) inv;
                EjbFutureTask<?> task = invocation.getEjbFutureTask();
                if (task == null) {
                    throw new IllegalStateException("Must be invoked from an async method");
                }
                if (invocation.method.getReturnType() == Void.TYPE) {
                    throw new IllegalStateException("Must be invoked from a method with a Future<V> " + "return type");
                }
                return invocation.getWasCancelCalled();
            }
        } catch (Exception e) {
            IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        }
        throw new IllegalStateException("Attempt to invoke wasCancelCalled from outside an ejb invocation");
    }


    @Override
    protected void checkAccessToCallerSecurity() throws IllegalStateException {
        if (state == BeanState.CREATED) {
            throw new IllegalStateException("Operation not allowed");
        }
    }


    @Override
    public void checkTimerServiceMethodAccess() throws IllegalStateException {
        // checks that apply to both stateful AND stateless
        if (state == BeanState.CREATED || inEjbRemove) {
            throw new IllegalStateException("EJB Timer method calls cannot be called in this context");
        }
    }


    protected ComponentInvocation getCurrentComponentInvocation() {
        return container.invocationManager.getCurrentInvocation();
    }


    private boolean isWebServiceInvocation(ComponentInvocation inv) {
        return (inv instanceof EjbInvocation) && ((EjbInvocation) inv).isWebService;
    }


    private Object getBusinessObject(final Class<?> businessInterface, final EjbDescriptor ejbDesc) {
        if (businessInterface == null) {
            return null;
        }
        final String intfName = businessInterface.getName();
        if (ejbLocalBusinessObjectImpl != null && ejbDesc.getLocalBusinessClassNames().contains(intfName)) {
            // Get proxy corresponding to this business interface.
            return ejbLocalBusinessObjectImpl.getClientObject(intfName);
        } else if (ejbRemoteBusinessObjectImpl != null && ejbDesc.getRemoteBusinessClassNames().contains(intfName)) {
            // Create a new client object from the stub for this business interface.
            final String generatedIntf = getGeneratedRemoteIntfName(intfName);
            final Remote stub = ejbRemoteBusinessObjectImpl.getStub(generatedIntf);
            try {
                return createRemoteBusinessObject(container.getClassLoader(), intfName, stub);
            } catch (final Exception e) {
                throw new IllegalStateException("Error creating remote business object for " + intfName, e);
            }
        } else if (ejbDesc.isLocalBean() && intfName.equals(ejbDesc.getEjbClassName())) {
            return optionalEjbLocalBusinessObjectImpl.getClientObject(ejbDesc.getEjbClassName());
        }
        return null;
    }

}
