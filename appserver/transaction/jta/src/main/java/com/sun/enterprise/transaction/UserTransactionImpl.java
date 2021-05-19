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

package com.sun.enterprise.transaction;

import java.rmi.RemoteException;
import java.io.Serializable;
import java.io.ObjectStreamException;
import java.util.logging.*;

import jakarta.transaction.*;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.TransactionOperationsManager;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.internal.api.Globals;

import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * This class implements jakarta.transaction.UserTransaction .
 * Its methods are called from TX_BEAN_MANAGED EJB code.
 * Most of its methods just delegate to the TransactionManager
 * after doing some EJB Container-related steps.
 *
 * Note: EJB1.1 Section 6.4.1 requires that the Container must be able to
 * preserve an object reference of the UserTransaction interface across
 * passivation, so we make this Serializable.
 *
 * @author Tony Ng
 * @author Marina Vatkina
 */
@Service
@ContractsProvided({UserTransactionImpl.class, UserTransaction.class}) // Needed because we can't change spec provided class
@PerLookup
public class UserTransactionImpl implements UserTransaction, Serializable
{

    static Logger _logger = LogDomains.getLogger(UserTransactionImpl.class, LogDomains.JTA_LOGGER);

    // Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(UserTransactionImpl.class);

    @Inject
    private transient JavaEETransactionManager transactionManager;

    @Inject
    private transient InvocationManager invocationManager;

    private static final boolean debug = false;
    private transient boolean initialized;

    // for non-J2EE clients usage
    // currently is never set
    private transient UserTransaction userTx;

    // private int transactionTimeout;

    // true if ejb access checks should be performed.  Default is
    // true.  All instances of UserTransaction exposed to applications
    // will have checking turned on.
    private boolean checkEjbAccess;


    /**
     * Default constructor.
     */
    public UserTransactionImpl()
    {
        this(true);
    }

    /**
     * Alternate version of constructor that allows control over whether
     * ejb access checks are performed.
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

        // non J2EE client, set up UserTransaction from JTS
        //  userTx = new com.sun.jts.jta.UserTransactionImpl();
    }

    private void checkUserTransactionMethodAccess(ComponentInvocation inv)
        throws IllegalStateException, SystemException {
        TransactionOperationsManager toMgr =
                (TransactionOperationsManager)inv.getTransactionOperationsManager();

        if ( toMgr != null && checkEjbAccess ) {
            if( !toMgr.userTransactionMethodsAllowed() ) {
                throw new IllegalStateException(sm.getString("enterprise_distributedtx.operation_not_allowed"));
            }
        }
    }

    public void begin() throws NotSupportedException, SystemException
    {
        if (!initialized) init();

        if (userTx != null) {
            userTx.begin();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
            checkUserTransactionMethodAccess(inv);
        }

        transactionManager.begin();
            /** V2 **
        if ( transactionTimeout > 0 )
            transactionManager.begin(transactionTimeout);
        else
            transactionManager.begin();
            **/

        try {
            if (inv != null) {
                TransactionOperationsManager toMgr =
                        (TransactionOperationsManager)inv.getTransactionOperationsManager();
                if ( toMgr != null)
                    toMgr.doAfterUtxBegin();

                inv.setTransaction(transactionManager.getTransaction());
                transactionManager.enlistComponentResources();
            }
        } catch ( RemoteException ex ) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_utx_begin", ex);
            SystemException sysEx = new SystemException(ex.getMessage());
            sysEx.initCause(ex);
            throw sysEx;
        }
    }

    public void commit() throws RollbackException,
            HeuristicMixedException, HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {
        if (!initialized) init();

        if (userTx != null) {
            userTx.commit();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
            checkUserTransactionMethodAccess(inv);
        }

        try {
            transactionManager.delistComponentResources(false);  // TMSUCCESS
            transactionManager.commit();
        } catch ( RemoteException ex ) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_utx_commit", ex);
            throw new SystemException();
        } finally {
            if (inv != null)
                inv.setTransaction(null);
        }
    }

    public void rollback() throws IllegalStateException, SecurityException,
            SystemException {
        if (!initialized) init();

        if (userTx != null) {
            userTx.rollback();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
            checkUserTransactionMethodAccess(inv);
        }

        try {
            transactionManager.delistComponentResources(false); // TMSUCCESS
            transactionManager.rollback();
        } catch ( RemoteException ex ) {
            _logger.log(Level.SEVERE,"enterprise_distributedtx.excep_in_utx_rollback", ex);
            throw new SystemException();
        } finally {
            if (inv !=  null)
                inv.setTransaction(null);
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {
        if (!initialized) init();

        if (userTx != null) {
            userTx.setRollbackOnly();
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
            checkUserTransactionMethodAccess(inv);
        }

        transactionManager.setRollbackOnly();
    }

    public int getStatus() throws SystemException {
        if (!initialized) init();

        if (userTx != null) {
            return userTx.getStatus();
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
                checkUserTransactionMethodAccess(inv);
        }

        return transactionManager.getStatus();
    }

    public void setTransactionTimeout(int seconds) throws SystemException {
        if (!initialized) init();

        if (userTx != null) {
            userTx.setTransactionTimeout(seconds);
            return;
        }

        ComponentInvocation inv = invocationManager.getCurrentInvocation();
        if (inv != null) {
            checkUserTransactionMethodAccess(inv);
        }

        transactionManager.setTransactionTimeout(seconds);
    }

    public void setForTesting(JavaEETransactionManager tm, InvocationManager im) {
        transactionManager = tm;
        invocationManager = im;
        ((JavaEETransactionManagerSimplified)transactionManager).invMgr = im;
    }

    /**
     * Return instance with all injected values from deserialization if possible
     */
    Object readResolve() throws ObjectStreamException {
        ServiceLocator h = Globals.getDefaultHabitat();
        if (h != null) {
            return h.getService(UserTransactionImpl.class);
        }

        return this;
    }
}
