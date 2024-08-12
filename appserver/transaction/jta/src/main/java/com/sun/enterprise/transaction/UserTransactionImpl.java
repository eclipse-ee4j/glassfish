/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.TransactionOperationsManager;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.UserTransaction;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.SEVERE;

/**
 * This class implements jakarta.transaction.UserTransaction . Its methods are called from TX_BEAN_MANAGED EJB code.
 * Most of its methods just delegate to the TransactionManager after doing some EJB Container-related steps.
 *
 * Note: EJB1.1 Section 6.4.1 requires that the Container must be able to preserve an object reference of the
 * UserTransaction interface across passivation, so we make this Serializable.
 *
 * @author Tony Ng
 * @author Marina Vatkina
 */
@Service
@ContractsProvided({ UserTransactionImpl.class, UserTransaction.class }) // Needed because we can't change spec provided class
@PerLookup
public class UserTransactionImpl implements UserTransaction, Serializable {

    private static final long serialVersionUID = -9058595590726479777L;

    static Logger _logger = LogDomains.getLogger(UserTransactionImpl.class, LogDomains.JTA_LOGGER);

    // Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(UserTransactionImpl.class);

    @Inject
    private transient JavaEETransactionManager transactionManager;

    @Inject
    private transient InvocationManager invocationManager;

    private transient boolean initialized;

    // for non-J2EE clients usage
    // currently is never set
    private transient UserTransaction userTransaction;

    // private int transactionTimeout;

    // true if ejb access checks should be performed. Default is
    // true. All instances of UserTransaction exposed to applications
    // will have checking turned on.
    private boolean checkEjbAccess;

    /**
     * Default constructor.
     */
    public UserTransactionImpl() {
        this(true);
    }

    /**
     * Alternate version of constructor that allows control over whether ejb access checks are performed.
     */
    public UserTransactionImpl(boolean doEjbAccessChecks) {
        init();
        checkEjbAccess = doEjbAccessChecks;
    }

    /**
     * Could be called after passivation and reactivation
     */
    private void init() {
        initialized = true;
    }

    private void checkUserTransactionMethodAccess(ComponentInvocation inv) throws IllegalStateException, SystemException {
        TransactionOperationsManager transactionOperationsManager = (TransactionOperationsManager) inv.getTransactionOperationsManager();

        if (transactionOperationsManager != null && checkEjbAccess) {
            if (!transactionOperationsManager.userTransactionMethodsAllowed()) {
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.operation_not_allowed"));
            }
        }
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        if (!initialized) {
            init();
        }

        if (userTransaction != null) {
            userTransaction.begin();
            return;
        }

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
        if (componentInvocation != null) {
            checkUserTransactionMethodAccess(componentInvocation);
        }

        transactionManager.begin();

        try {
            if (componentInvocation != null) {
                TransactionOperationsManager toMgr = (TransactionOperationsManager) componentInvocation.getTransactionOperationsManager();
                if (toMgr != null)
                    toMgr.doAfterUtxBegin();

                componentInvocation.setTransaction(transactionManager.getTransaction());
                transactionManager.enlistComponentResources();
            }
        } catch (RemoteException ex) {
            _logger.log(SEVERE, "enterprise_distributedtx.excep_in_utx_begin", ex);
            SystemException sysEx = new SystemException(ex.getMessage());
            sysEx.initCause(ex);
            throw sysEx;
        }
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        if (!initialized) {
            init();
        }

        if (userTransaction != null) {
            userTransaction.commit();
            return;
        }

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
        if (componentInvocation != null) {
            checkUserTransactionMethodAccess(componentInvocation);
        }

        try {
            transactionManager.delistComponentResources(false); // TMSUCCESS
            transactionManager.commit();
        } catch (RemoteException ex) {
            _logger.log(SEVERE, "enterprise_distributedtx.excep_in_utx_commit", ex);
            throw new SystemException();
        } finally {
            if (componentInvocation != null) {
                componentInvocation.setTransaction(null);
            }
        }
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        if (!initialized) {
            init();
        }

        if (userTransaction != null) {
            userTransaction.rollback();
            return;
        }

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
        if (componentInvocation != null) {
            checkUserTransactionMethodAccess(componentInvocation);
        }

        try {
            transactionManager.delistComponentResources(false); // TMSUCCESS
            transactionManager.rollback();
        } catch (RemoteException ex) {
            _logger.log(SEVERE, "enterprise_distributedtx.excep_in_utx_rollback", ex);
            throw new SystemException();
        } finally {
            if (componentInvocation != null) {
                componentInvocation.setTransaction(null);
            }
        }
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (!initialized) {
            init();
        }

        if (userTransaction != null) {
            userTransaction.setRollbackOnly();
            return;
        }

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
        if (componentInvocation != null) {
            checkUserTransactionMethodAccess(componentInvocation);
        }

        transactionManager.setRollbackOnly();
    }

    @Override
    public int getStatus() throws SystemException {
        if (!initialized) {
            init();
        }

        if (userTransaction != null) {
            return userTransaction.getStatus();
        }

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
        if (componentInvocation != null) {
            checkUserTransactionMethodAccess(componentInvocation);
        }

        return transactionManager.getStatus();
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
        if (!initialized) {
            init();
        }

        if (userTransaction != null) {
            userTransaction.setTransactionTimeout(seconds);
            return;
        }

        ComponentInvocation componentInvocation = invocationManager.getCurrentInvocation();
        if (componentInvocation != null) {
            checkUserTransactionMethodAccess(componentInvocation);
        }

        transactionManager.setTransactionTimeout(seconds);
    }

    public void setForTesting(JavaEETransactionManager tm, InvocationManager im) {
        transactionManager = tm;
        invocationManager = im;
        ((JavaEETransactionManagerSimplified) transactionManager).invMgr = im;
    }

    /**
     * Return instance with all injected values from deserialization if possible
     */
    Object readResolve() throws ObjectStreamException {
        ServiceLocator serviceLocator = Globals.getDefaultHabitat();
        if (serviceLocator != null) {
            return serviceLocator.getService(UserTransactionImpl.class);
        }

        return this;
    }
}
