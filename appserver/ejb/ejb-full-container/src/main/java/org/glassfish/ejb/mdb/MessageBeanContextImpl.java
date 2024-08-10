/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.ejb.mdb;

import com.sun.ejb.EjbInvocation;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.ejb.containers.EJBObjectImpl;
import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EJBTimerServiceWrapper;
import com.sun.ejb.containers.EjbContainerUtilImpl;

import jakarta.ejb.EJBHome;
import jakarta.ejb.EJBObject;
import jakarta.ejb.MessageDrivenContext;
import jakarta.ejb.TimerService;
import jakarta.transaction.UserTransaction;

import org.glassfish.api.invocation.ComponentInvocation;

/**
 * Implementation of EJBContext for message-driven beans
 *
 * @author Kenneth Saks
 */
public final class MessageBeanContextImpl extends EJBContextImpl implements MessageDrivenContext {
    private static final long serialVersionUID = 1L;
    private boolean afterSetContext;

    MessageBeanContextImpl(Object ejb, BaseContainer container) {
        super(ejb, container);
    }

    void setEJBStub(EJBObject ejbStub) {
        throw new RuntimeException("No stubs for Message-driven beans");
    }

    void setEJBObjectImpl(EJBObjectImpl ejbo) {
        throw new RuntimeException("No EJB Object for Message-driven beans");
    }

    // FIXME later
    EJBObjectImpl getEJBObjectImpl() {
        throw new RuntimeException("No EJB Object for Message-driven beans");
    }

    public void setContextCalled() {
        this.afterSetContext = true;
    }

    /*****************************************************************
     * The following are implementations of EJBContext methods.
     ******************************************************************/

    /**
     *
     */
    @Override
    public UserTransaction getUserTransaction() throws java.lang.IllegalStateException {
        // The state check ensures that an exception is thrown if this
        // was called from the constructor or setMessageDrivenContext.
        // The remaining checks are performed by the container.
        if (!this.afterSetContext) {
            throw new java.lang.IllegalStateException("Operation not allowed");
        }

        return ((BaseContainer) getContainer()).getUserTransaction();
    }

    /**
     * Doesn't make any sense to get EJBHome object for a message-driven ejb.
     */
    @Override
    public EJBHome getEJBHome() {
        RuntimeException exception = new java.lang.IllegalStateException("getEJBHome not allowed for message-driven beans");
        throw exception;
    }

    @Override
    protected void checkAccessToCallerSecurity() throws java.lang.IllegalStateException {
        // A message-driven ejb's state transitions past UNINITIALIZED
        // AFTER ejbCreate
        if (!operationsAllowed()) {
            throw new java.lang.IllegalStateException("Operation not allowed");
        }

    }

    @Override
    public boolean isCallerInRole(String roleRef) {
        if (roleRef == null) {
            throw new IllegalStateException("Argument is null");
        }

        checkAccessToCallerSecurity();

        ComponentInvocation inv = EjbContainerUtilImpl.getInstance().getCurrentInvocation();
        if (inv instanceof EjbInvocation) {
            EjbInvocation ejbInv = (EjbInvocation) inv;
            if (ejbInv.isTimerCallback) {
                throw new IllegalStateException("isCallerInRole not allowed from timer callback");
            }

        } else {
            throw new IllegalStateException("not invoked from within a message-bean context");
        }

        com.sun.enterprise.security.SecurityManager sm = container.getSecurityManager();
        return sm.isCallerInRole(roleRef);
    }

    @Override
    public TimerService getTimerService() throws java.lang.IllegalStateException {
        if (!afterSetContext) {
            throw new java.lang.IllegalStateException("Operation not allowed");
        }

        EJBTimerService timerService = EJBTimerService.getValidEJBTimerService();
        return new EJBTimerServiceWrapper(timerService, this);
    }

    @Override
    public void checkTimerServiceMethodAccess() throws java.lang.IllegalStateException {
        // A message-driven ejb's state transitions past UNINITIALIZED
        // AFTER ejbCreate
        if (!operationsAllowed()) {
            throw new java.lang.IllegalStateException("EJB Timer Service method calls cannot be called in " + " this context");
        }
    }

    boolean isInState(BeanState value) {
        return getState() == value;
    }

    void setState(BeanState s) {
        state = s;
    }

    void setInEjbRemove(boolean beingRemoved) {
        inEjbRemove = beingRemoved;
    }

    boolean operationsAllowed() {
        return !(isUnitialized() || inEjbRemove);
    }

    /**
     * Returns true if this context has NOT progressed past its initial state.
     */
    private boolean isUnitialized() {
        return (state == EJBContextImpl.BeanState.CREATED);
    }

}
