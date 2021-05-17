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

import com.sun.ejb.EjbInvocation;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;

import jakarta.ejb.SessionContext;
import jakarta.ejb.TimerService;
import jakarta.transaction.UserTransaction;
import com.sun.ejb.EJBUtils;

/**
 * Implementation of EJBContext for SessionBeans
 *
 * @author Mahesh Kannan
 */

public abstract class AbstractSessionContextImpl
        extends EJBContextImpl
        implements SessionContext {

    protected Object instanceKey;

    protected String ejbName;


    protected AbstractSessionContextImpl(Object ejb, BaseContainer container) {
        super(ejb, container);
        EjbSessionDescriptor sessionDesc =
                (EjbSessionDescriptor) getContainer().getEjbDescriptor();

        this.ejbName = sessionDesc.getName();
    }

    public Object getInstanceKey() {
        return instanceKey;
    }

    public void setInstanceKey(Object instanceKey) {
        this.instanceKey = instanceKey;
    }

    public String toString() {
        return ejbName + "; id: " + instanceKey;
    }

    public TimerService getTimerService() throws IllegalStateException {

        // Instance key is first set between after setSessionContext and
        // before ejbCreate
        if (instanceKey == null) {
            throw new IllegalStateException("Operation not allowed");
        }

        EJBTimerService timerService = EJBTimerService.getValidEJBTimerService();
        return new EJBTimerServiceWrapper(timerService, this);
    }

    public UserTransaction getUserTransaction()
            throws IllegalStateException {
        // The state check ensures that an exception is thrown if this
        // was called from setSession/EntityContext. The instance key check
        // ensures that an exception is not thrown if this was called
        // from a stateless SessionBean's ejbCreate.
        if ((state == BeanState.CREATED) && (instanceKey == null))
            throw new IllegalStateException("Operation not allowed");

        return ((BaseContainer) getContainer()).getUserTransaction();
    }

    public <T> T getBusinessObject(Class<T> businessInterface)
            throws IllegalStateException {

        // businessInterface param can also be a class in the case of the
        // no-interface view

        // getBusinessObject not allowed for Stateless/Stateful beans
        // until after dependency injection
        if (instanceKey == null) {
            throw new IllegalStateException("Operation not allowed");
        }

        T businessObject = null;

        EjbDescriptor ejbDesc = container.getEjbDescriptor();

        if (businessInterface != null) {
            String intfName = businessInterface.getName();

            if ((ejbLocalBusinessObjectImpl != null) &&
                    ejbDesc.getLocalBusinessClassNames().contains(intfName)) {

                // Get proxy corresponding to this business interface.
                businessObject = (T) ejbLocalBusinessObjectImpl
                        .getClientObject(intfName);

            } else if ((ejbRemoteBusinessObjectImpl != null) &&
                    ejbDesc.getRemoteBusinessClassNames().contains(intfName)) {

                // Create a new client object from the stub for this
                // business interface.
                String generatedIntf = EJBUtils.getGeneratedRemoteIntfName(intfName);

                java.rmi.Remote stub =
                    ejbRemoteBusinessObjectImpl.getStub(generatedIntf);

                try {
                    businessObject = (T) EJBUtils.createRemoteBusinessObject
                        (container.getClassLoader(), intfName, stub);
                } catch(Exception e) {

                    IllegalStateException ise = new IllegalStateException
                        ("Error creating remote business object for " +
                         intfName);
                    ise.initCause(e);
                    throw ise;
                }

            } else if( ejbDesc.isLocalBean() && intfName.equals( ejbDesc.getEjbClassName() ) ) {

                businessObject = (T) optionalEjbLocalBusinessObjectImpl.
                        getClientObject(ejbDesc.getEjbClassName());

            }
        }

        if (businessObject == null) {
            throw new IllegalStateException("Invalid business interface : " +
                    businessInterface + " for ejb " + ejbDesc.getName());
        }

        return businessObject;
    }

    public Class getInvokedBusinessInterface()
            throws IllegalStateException {

        Class businessInterface = null;

        try {
            ComponentInvocation inv = EjbContainerUtilImpl.getInstance().getCurrentInvocation();

            if ((inv != null) && (inv instanceof EjbInvocation)) {
                EjbInvocation invocation = (EjbInvocation) inv;
                if (invocation.isBusinessInterface) {
                    businessInterface = invocation.clientInterface;
                    if( container.isLocalBeanClass(invocation.clientInterface.getName()) ) {
                        businessInterface = container.getEJBClass();
                    }

                }
            }
        } catch (Exception e) {
            IllegalStateException ise = new IllegalStateException();
            ise.initCause(e);
            throw ise;
        }

        if (businessInterface == null) {
            throw new IllegalStateException("Attempt to call " +
                    "getInvokedBusinessInterface outside the scope of a business " +
                    "method");
        }

        return businessInterface;
    }

    public boolean wasCancelCalled() {

        try {
            ComponentInvocation inv = EjbContainerUtilImpl.getInstance().getCurrentInvocation();

            if ((inv != null) && (inv instanceof EjbInvocation)) {
                EjbInvocation invocation = (EjbInvocation) inv;
                EjbFutureTask task = invocation.getEjbFutureTask();
                if (task == null) {
                    throw new IllegalStateException("Must be invoked from an async method");
                }
                if( (invocation.method.getReturnType() == Void.TYPE) ) {
                    throw new IllegalStateException("Must be invoked from a method with a Future<V> " +
                                                    "return type");
                }
                return invocation.getWasCancelCalled();
            }
        } catch (Exception e) {
            IllegalStateException ise = new IllegalStateException(e.getMessage());
            ise.initCause(e);
            throw ise;
        }

        throw new IllegalStateException("Attempt to invoke wasCancelCalled from " +
                                        "outside an ejb invocation");
    }

    protected void checkAccessToCallerSecurity()
            throws IllegalStateException {
        if (state == BeanState.CREATED) {
            throw new IllegalStateException("Operation not allowed");
        }

    }

    public void checkTimerServiceMethodAccess()
            throws IllegalStateException {
        // checks that apply to both stateful AND stateless
        if ((state == BeanState.CREATED) || inEjbRemove) {
            throw new IllegalStateException
                    ("EJB Timer method calls cannot be called in this context");
        }
    }

    protected ComponentInvocation getCurrentComponentInvocation() {
        BaseContainer container = (BaseContainer) getContainer();
        return container.invocationManager.getCurrentInvocation();
    }

    private boolean isWebServiceInvocation(ComponentInvocation inv) {
        return (inv instanceof EjbInvocation) && ((EjbInvocation) inv).isWebService;
    }

}
