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

package org.glassfish.persistence.ejb.entitybean.container;

import java.lang.reflect.Method;
import jakarta.ejb.*;

import org.glassfish.api.invocation.ComponentInvocation;

import com.sun.ejb.EjbInvocation;
import com.sun.ejb.containers.EJBContextImpl;
import com.sun.ejb.containers.BaseContainer;
import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EJBTimerServiceWrapper;
import org.glassfish.persistence.ejb.entitybean.container.spi.CascadeDeleteNotifier;

/**
 * Implementation of EJBContext for EntityBeans
 *
 */

public class EntityContextImpl
    extends EJBContextImpl
    implements EntityContext, CascadeDeleteNotifier
{
    private int lastTxStatus=-1;
    private boolean newlyActivated = false;
    private int nCallsInProgress = 0;
    private boolean dirty = false;
    private boolean inUnsetEntityContext = false;
    private boolean inEjbLoad = false;
    private boolean inEjbStore = false;

    private boolean cascadeDeleteBeforeEJBRemove = false;
    private boolean cascadeDeleteAfterSuperEJBRemove = false;

    //The following member variables are used to directly cache
    //    the EntityContext instead of enclosing it within a wrapper class.
    private Object            _primaryKey;
    private int                _pkHashCode;
    private EntityContextImpl        _next;

    transient private EntityContainer _container = null;

    EntityContextImpl(Object ejb, BaseContainer container) {
        super(ejb, container);
        _container = (EntityContainer) container;
    }

    int getLastTransactionStatus() {
        return lastTxStatus;
    }

    void setLastTransactionStatus(int status) {
        lastTxStatus = status;
    }

    void setInUnsetEntityContext(boolean flag) {
        inUnsetEntityContext = flag;
    }

    void setInEjbLoad(boolean flag) {
        inEjbLoad = flag;
    }

    void setInEjbStore(boolean flag) {
        inEjbStore = flag;
    }

    boolean isDirty() {
        return dirty;
    }

    void setDirty(boolean b) {
        dirty = b;
    }

    // overrides EJBContextImpl.setState
    void setState(BeanState s) {
        state = s;
        if ( state == BeanState.POOLED ||
            state == BeanState.DESTROYED )
        {
            dirty = false;
        }
    }


    boolean isNewlyActivated() {
        return newlyActivated;
    }

    void setNewlyActivated(boolean b) {
        newlyActivated = b;
    }

    boolean hasReentrantCall() {
        return (nCallsInProgress > 1);
    }

    synchronized void decrementCalls() {
        nCallsInProgress--;
    }

    synchronized void incrementCalls() {
        nCallsInProgress++;
    }

    boolean hasIdentity() {
        return( (ejbObjectImpl != null) || (ejbLocalObjectImpl != null) );
    }

    /**
     * Implementation of EntityContext method.
     */
    public Object getPrimaryKey() throws IllegalStateException {
        if ( ejbObjectImpl == null && ejbLocalObjectImpl == null ) {
            // There is no ejbObjectImpl/localObject in ejbCreate, ejbFind,
            // setEntityCtx etc
            throw new IllegalStateException("Primary key not available");
        }

        return getKey();
    }

    /**
     * Implementation of EntityContext method, overrides EJBContextImpl method.
     */
    public EJBObject getEJBObject()
        throws IllegalStateException
    {
        if (! isRemoteInterfaceSupported) {
            throw new IllegalStateException("EJBObject not available");
        }

        if ( ejbStub == null ) {
            Object pkey = getPrimaryKey(); // throws IllegalStateException
            ejbStub = _container.getEJBObjectStub(pkey, null);
        }

        return ejbStub;
    }

    public TimerService getTimerService() throws IllegalStateException {
        if( state == BeanState.CREATED || inUnsetEntityContext || inFinder() ) {
            throw new IllegalStateException("Operation not allowed");
        }

        EJBTimerService timerService = EJBTimerService.getValidEJBTimerService();
        return new EJBTimerServiceWrapper(timerService, (EntityContext) this);
    }

    protected void checkAccessToCallerSecurity()
        throws IllegalStateException
    {
        if( state == BeanState.CREATED || inUnsetEntityContext ) {
            throw new IllegalStateException("Operation not allowed");
        }
        checkActivatePassivate();

        if (inEjbLoad || inEjbStore) {
            // Security access is allowed from these two methods.  In the
            // case that they are invoked as part of an ejbTimeout call,
            // getCallerPrincipal will return null and isCallerInRole will
            // be false
            return;
        }
    }

    public void checkTimerServiceMethodAccess()
        throws IllegalStateException
    {

        // Prohibit access from constructor, setEntityContext, ejbCreate,
        // ejbActivate, ejbPassivate, unsetEntityContext, ejbFind
        if( (state == BeanState.CREATED) ||
        inUnsetEntityContext ||
        inFinder() ||
        inActivatePassivate() ||
        !hasIdentity() ) {
            throw new IllegalStateException("Operation not allowed");
        }

    }

    public final boolean isCascadeDeleteAfterSuperEJBRemove() {
        return cascadeDeleteAfterSuperEJBRemove;
    }

    public final void setCascadeDeleteAfterSuperEJBRemove(boolean value) {
        this.cascadeDeleteAfterSuperEJBRemove = value;
    }

    public final boolean isCascadeDeleteBeforeEJBRemove() {
        return cascadeDeleteBeforeEJBRemove;
    }

    public final void setCascadeDeleteBeforeEJBRemove(boolean value) {
        this.cascadeDeleteBeforeEJBRemove = value;
    }

    private boolean inFinder() {
        boolean inFinder = false;
        ComponentInvocation ci = _container.getCurrentInvocation();
        if ( ci instanceof EjbInvocation ) {
            EjbInvocation inv = (EjbInvocation) ci;
            Method currentMethod = inv.method;
            inFinder = ( (currentMethod != null) && inv.isHome &&
                         currentMethod.getName().startsWith("find") );
        }
        return inFinder;
    }

    //Called from EntityContainer after an ejb is obtained from the pool.
    final void cachePrimaryKey() {
    Object pk = getPrimaryKey();
    this._primaryKey = pk;
    this._pkHashCode = pk.hashCode();
    }

    final void clearCachedPrimaryKey() {
    this._primaryKey = null;
    }

    //Called from IncompleteTxCache to get an already cached context
    final boolean doesMatch(BaseContainer baseContainer, int pkHashCode, Object pk) {
    return (
        (container == baseContainer)
        && (_pkHashCode == pkHashCode)
        && (_primaryKey.equals(pk))
    );
    }

    final void _setNext(EntityContextImpl val) {
    this._next = val;
    }

    final EntityContextImpl _getNext() {
    return _next;
    }

    final int _getPKHashCode() {
    return this._pkHashCode;
    }

    final boolean isInState(BeanState value) {
    return getState() == value;
    }

}
