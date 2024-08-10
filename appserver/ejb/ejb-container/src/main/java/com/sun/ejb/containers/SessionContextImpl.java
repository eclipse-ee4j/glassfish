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
import com.sun.ejb.containers.StatefulSessionContainer.EEMRefInfo;
import com.sun.ejb.spi.container.StatefulEJBContext;
import com.sun.enterprise.container.common.impl.PhysicalEntityManagerWrapper;
import com.sun.enterprise.deployment.EjbSessionDescriptor;

import jakarta.ejb.SessionContext;
import jakarta.ejb.TimerService;
import jakarta.persistence.EntityManagerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.glassfish.api.invocation.ComponentInvocation;

/**
 * Implementation of EJBContext for SessionBeans
 *
 * @author Mahesh Kannan
 */

public final class SessionContextImpl
    extends AbstractSessionContextImpl
    implements StatefulEJBContext
{

    private boolean completedTxStatus;
    private boolean afterCompletionDelayed=false;
    private boolean committing=false;
    private boolean inAfterCompletion=false;
    private boolean isStateless = false;
    private boolean isStateful  = false;

    private boolean existsInSessionStore = false;
    private transient int refCount = 0;

    private boolean txCheckpointDelayed;
    private long    lastPersistedAt;

    private long version;

    // Do not call Session Synchronization callbacks when in transactional
    // lifecycle callbacks
    private boolean inLifeCycleCallback = false;

    // Map of entity managers with extended persistence context
    // for this stateful session bean.
    private transient Map<EntityManagerFactory, PhysicalEntityManagerWrapper> extendedEntityManagerMap;

    private transient Set<EntityManagerFactory> emfsRegisteredWithTx;

    //Used during activation to populate entries in the above maps
    //Also, EEMRefInfo implements IndirectlySerializable
    private Collection<EEMRefInfo> eemRefInfos = new HashSet<EEMRefInfo>();

    // Used to provide serialized access to an SFSB instance.
    private transient ReentrantReadWriteLock statefulSerializedAccessLock;

    SessionContextImpl(Object ejb, BaseContainer container) {
        super(ejb, container);
        EjbSessionDescriptor sessionDesc =
            (EjbSessionDescriptor) getContainer().getEjbDescriptor();
        isStateless = sessionDesc.isStateless();
        isStateful  = sessionDesc.isStateful();
        if( isStateful ) {
            initializeStatefulWriteLock();
        }
    }

    public Map<EntityManagerFactory, PhysicalEntityManagerWrapper> getExtendedEntityManagerMap() {
        if( extendedEntityManagerMap == null ) {
            extendedEntityManagerMap = new HashMap<>();
        }
        return extendedEntityManagerMap;
    }


    Collection<EEMRefInfo> getAllEEMRefInfos() {
        return eemRefInfos;
    }

    void setEEMRefInfos(Collection<EEMRefInfo> val) {
        if (val != null) {
            eemRefInfos = val;
    }
    }

    public void addExtendedEntityManagerMapping(EntityManagerFactory emf,
            EEMRefInfo refInfo) {
        getExtendedEntityManagerMap().put(emf, new PhysicalEntityManagerWrapper(refInfo.getEntityManager(),
                refInfo.getSynchronizationType()) );
    }


    public PhysicalEntityManagerWrapper getExtendedEntityManager(EntityManagerFactory emf) {
        return getExtendedEntityManagerMap().get(emf);
    }

    public Collection<PhysicalEntityManagerWrapper> getExtendedEntityManagers() {
        return getExtendedEntityManagerMap().values();
    }

    private Set<EntityManagerFactory> getEmfsRegisteredWithTx() {
        if( emfsRegisteredWithTx == null ) {
            emfsRegisteredWithTx = new HashSet<EntityManagerFactory>();
        }
        return emfsRegisteredWithTx;
    }

    public void setEmfRegisteredWithTx(EntityManagerFactory emf, boolean flag)
    {
        if( flag ) {
            getEmfsRegisteredWithTx().add(emf);
        } else {
            getEmfsRegisteredWithTx().remove(emf);
        }
    }

    public boolean isEmfRegisteredWithTx(EntityManagerFactory emf) {
        return getEmfsRegisteredWithTx().contains(emf);
    }


    public void initializeStatefulWriteLock() {
        statefulSerializedAccessLock = new ReentrantReadWriteLock(true);
    }

    public ReentrantReadWriteLock.WriteLock getStatefulWriteLock() {
        return statefulSerializedAccessLock.writeLock();
    }

    public void setStatefulWriteLock(SessionContextImpl other) {
        statefulSerializedAccessLock = other.statefulSerializedAccessLock;
    }

    @Override
    public TimerService getTimerService() throws IllegalStateException {
        if( isStateful ) {
            throw new IllegalStateException
                ("EJBTimer Service is not accessible to Stateful Session ejbs");
        }

        // Instance key is first set between after setSessionContext and
        // before ejbCreate
        if ( instanceKey == null ) {
            throw new IllegalStateException("Operation not allowed");
        }

        EJBTimerService timerService = EJBTimerService.getValidEJBTimerService();
        return new EJBTimerServiceWrapper(timerService, this);
    }

    @Override
    protected void checkAccessToCallerSecurity()
        throws IllegalStateException
    {

        if( isStateless ) {
            // This covers constructor, setSessionContext, ejbCreate,
            // and ejbRemove. NOTE : For stateless session beans,
            // instances don't move past CREATED until after ejbCreate.
            if( (state == BeanState.CREATED) || inEjbRemove ) {
                throw new IllegalStateException("Operation not allowed");
            }
        } else {
            // This covers constructor and setSessionContext.
            // For stateful session beans, instances move past
            // CREATED after setSessionContext.
            if( state == BeanState.CREATED ) {
                throw new IllegalStateException("Operation not allowed");
            }
        }

    }

    @Override
    public void checkTimerServiceMethodAccess()
        throws IllegalStateException
    {
        // checks that only apply to stateful session beans
        ComponentInvocation compInv = getCurrentComponentInvocation();
        if (isStateful) {
            if (
            inStatefulSessionEjbCreate(compInv) ||
            inActivatePassivate(compInv) ||
            inAfterCompletion ) {
                throw new IllegalStateException
                ("EJB Timer methods for stateful session beans cannot be " +
                " called in this context");
            }
        }

        // checks that apply to both stateful AND stateless
        if ( (state == BeanState.CREATED) || inEjbRemove ) {
            throw new IllegalStateException
            ("EJB Timer method calls cannot be called in this context");
        }
    }

    boolean getCompletedTxStatus() {
        return completedTxStatus;
    }

    void setCompletedTxStatus(boolean s) {
        this.completedTxStatus = s;
    }

    boolean isAfterCompletionDelayed() {
        return afterCompletionDelayed;
    }

    void setAfterCompletionDelayed(boolean s) {
        this.afterCompletionDelayed = s;
    }

    boolean isTxCompleting() {
        return committing;
    }

    void setTxCompleting(boolean s) {
        this.committing = s;
    }

    void setInAfterCompletion(boolean flag) {
        inAfterCompletion = flag;
    }

    void setInLifeCycleCallback(boolean s) {
        inLifeCycleCallback = s;
    }

    boolean getInLifeCycleCallback() {
        return inLifeCycleCallback;
    }

    // Used to check if stateful session bean is in ejbCreate.
    // Since bean goes to READY state before ejbCreate is called by
    // EJBHomeImpl and EJBLocalHomeImpl, we can't rely on getState()
    // being CREATED for operations matrix checks.
    private boolean inStatefulSessionEjbCreate(ComponentInvocation inv) {
        boolean inEjbCreate = false;
        if ( inv instanceof EjbInvocation ) {
            Class clientIntf = ((EjbInvocation)inv).clientInterface;
            // If call came through a home/local-home, this can only be a
            // create call.
            inEjbCreate = ((EjbInvocation)inv).isHome &&
                (jakarta.ejb.EJBHome.class.isAssignableFrom(clientIntf) ||
                 jakarta.ejb.EJBLocalHome.class.isAssignableFrom(clientIntf));
        }
        return inEjbCreate;
    }

    void setTxCheckpointDelayed(boolean val) {
        this.txCheckpointDelayed = val;
    }

    boolean isTxCheckpointDelayed() {
        return this.txCheckpointDelayed;
    }

    long getLastPersistedAt() {
        return lastPersistedAt;
    }

    void setLastPersistedAt(long val) {
        this.lastPersistedAt = val;
    }

    public long getVersion() {
        return version;
    }

    public long incrementAndGetVersion() {
        return ++version;
    }

    public void setVersion(long newVersion) {
        this.version = newVersion;
    }

    /*************************************************************************/
    /************ Implementation of StatefulEJBContext ***********************/
    /*************************************************************************/

    public long getLastAccessTime() {
        return getLastTimeUsed();
    }

    public boolean canBePassivated() {
        return (state == EJBContextImpl.BeanState.READY);
    }

    public boolean hasExtendedPC() {
        return (this.getExtendedEntityManagerMap().size() != 0);
    }

    public SessionContext getSessionContext() {
        return this;
    }

    public boolean existsInStore() {
        return existsInSessionStore ;
    }

    public void setExistsInStore(boolean val) {
        this.existsInSessionStore = val;
    }

    public final void incrementRefCount() {
        refCount++;
    }

    public final void decrementRefCount() {
        refCount--;
    }

    public final int getRefCount() {
        return refCount;
    }

}
