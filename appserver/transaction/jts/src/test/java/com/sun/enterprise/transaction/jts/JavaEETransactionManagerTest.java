/*
 * Copyright (c) 2021, 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.jts;

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;
import com.sun.enterprise.resource.pool.PoolManagerImpl;
import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.enterprise.transaction.JavaEETransactionManagerSimplifiedDelegate;
import com.sun.enterprise.transaction.TransactionServiceConfigListener;
import com.sun.enterprise.transaction.TransactionSynchronizationRegistryImpl;
import com.sun.enterprise.transaction.UserTransactionImpl;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;

import jakarta.resource.spi.ManagedConnection;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.TransactionSynchronizationRegistry;
import jakarta.transaction.UserTransaction;

import java.beans.PropertyChangeEvent;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.api.naming.SimpleJndiName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class JavaEETransactionManagerTest {

    private JavaEETransactionManagerSimplified txManager;

    @BeforeEach
    public void setUp() {
        txManager = new JavaEETransactionManagerSimplified();
        JavaEETransactionManagerDelegate d = new JavaEETransactionManagerJTSDelegate();
        txManager.setDelegate(d);
        d.setTransactionManager(txManager);
    }

    /**
     * Test that you can'txManager replace delegate with a lower order.
     */
    @Test
    public void testReplaceDelegate() {
        JavaEETransactionManagerDelegate d = new JavaEETransactionManagerSimplifiedDelegate();
        txManager.setDelegate(d);
        assertFalse(txManager.isDelegate(d));
    }

    /**
     * Can'txManager test more than null (but no NPE)
     */
    @Test
    public void testXAResourceWrapper() {
        assertNull(txManager.getXAResourceWrapper("xxx"));
        assertNull(txManager.getXAResourceWrapper("oracle.jdbc.xa.client.OracleXADataSource"));
    }

    /**
     * Test ConfigListener call
     */
    @Test
    public void testTransactionServiceConfigListener() {
        PropertyChangeEvent e1 = new PropertyChangeEvent("", ServerTags.KEYPOINT_INTERVAL, "1", "10");
        PropertyChangeEvent e2 = new PropertyChangeEvent("", ServerTags.RETRY_TIMEOUT_IN_SECONDS, "1", "10");
        TransactionServiceConfigListener listener = new TransactionServiceConfigListener();
        listener.setTM(txManager);
        listener.changed(new PropertyChangeEvent[] {e1, e2});
    }

    @Test
    public void testWrongTMCommit() {
        assertThrows(IllegalStateException.class, txManager::commit);
    }

    @Test
    public void testWrongTMRollback() {
        assertThrows(IllegalStateException.class, txManager::rollback);
    }

    @Test
    public void testWrongUTXCommit() throws Exception {
        UserTransaction utx = createUtx();
        assertThrows(IllegalStateException.class, utx::commit);
    }

    @Test
    @Disabled("No exception throws. This test was commented out for years, but ... I would expect the exception too")
    public void testUTXTimeout() {
        final TestSync sync = new TestSync(false);
        final long start = System.currentTimeMillis();
        assertThrows(RollbackException.class, () -> {
            UserTransaction utx = createUtx();
            System.out.println("**Calling UTX setTransactionTimeout ===>");
            utx.setTransactionTimeout(1);
            utx.begin();

            Transaction tx = txManager.getTransaction();
            System.out.println("**Registering Synchronization ....");
            tx.registerSynchronization(sync);

            TestResource theResource = new TestResource(tx);
            txManager.enlistResource(tx, new TestResourceHandle(theResource));

            Thread.sleep(1100);
            txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);

            utx.commit();
            System.out.println("**UTX commit successful after <===" + (System.currentTimeMillis() - start) + "millis");
        });
        assertFalse(sync.called_beforeCompletion, "beforeCompletion was called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }


    @Test
    public void testWrongTMOperationsAfterCommit() throws Exception {
        txManager.begin();
        txManager.commit();
        assertThrows(IllegalStateException.class, txManager::commit);
        assertThrows(IllegalStateException.class, txManager::rollback);
        assertThrows(IllegalStateException.class, txManager::setRollbackOnly);
    }

    @Test
    public void testResourceStatus() throws Exception {
        {
            txManager.begin();
            Transaction tx = txManager.getTransaction();
            System.out.println("**Testing Resource Status in 2PC ===>");
            TestResource theResource = new TestResource(tx, 1L);
            TestResource theResource2 = new TestResource(tx, 1L);
            TestResource theResource1 = new TestResource(tx);
            txManager.enlistResource(tx, new TestResourceHandle(theResource));
            txManager.enlistResource(tx, new TestResourceHandle(theResource2));
            txManager.enlistResource(tx, new TestResourceHandle(theResource1));
            txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
            txManager.delistResource(tx, new TestResourceHandle(theResource2), XAResource.TMSUCCESS);
            txManager.delistResource(tx, new TestResourceHandle(theResource1), XAResource.TMSUCCESS);
            txManager.commit();

            assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
            assertTrue(theResource.prepareStatusOK());
            assertTrue(theResource1.prepareStatusOK());
            assertTrue(theResource.commitStatusOK());
            assertTrue(theResource1.commitStatusOK());
        }
        {
            txManager.begin();
            Transaction tx = txManager.getTransaction();
            System.out.println("**Testing resource status in rollback ===>");
            TestResource theResource = new TestResource(tx, 1L);
            TestResource theResource2 = new TestResource(tx, 1L);
            TestResource theResource1 = new TestResource(tx);
            txManager.enlistResource(tx, new TestResourceHandle(theResource));
            txManager.enlistResource(tx, new TestResourceHandle(theResource2));
            txManager.enlistResource(tx, new TestResourceHandle(theResource1));
            txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
            txManager.delistResource(tx, new TestResourceHandle(theResource2), XAResource.TMSUCCESS);
            txManager.delistResource(tx, new TestResourceHandle(theResource1), XAResource.TMSUCCESS);

            txManager.rollback();

            assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
            assertTrue(theResource.rollbackStatusOK());
            assertTrue(theResource1.rollbackStatusOK());
            assertFalse(theResource2.isAssociated());
        }
        {
            txManager.begin();
            Transaction tx = txManager.getTransaction();
            System.out.println("**Testing resource status in 1PC ===>");
            TestResource theResource = new TestResource(tx);
            txManager.enlistResource(tx, new TestResourceHandle(theResource));
            txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
            txManager.commit();

            String status = JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus());
            assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
            assertTrue(theResource.commitStatusOK());
        }
    }

    @Test
    public void testSetRollbackOnlyBeforeEnlist() throws Exception {
        try {
            txManager.begin();
            Transaction tx = txManager.getTransaction();
            tx.setRollbackOnly();
            assertEquals("MarkedRollback", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));

            TestResource theResource = new TestResource(tx);
            assertThrows(RollbackException.class,() ->  txManager.enlistResource(tx, new TestResourceHandle(theResource)));
            txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
            assertEquals("MarkedRollback", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        } finally {
            txManager.rollback();
            assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        }
    }

    @Test
    public void testWrongTXOperationsAfterCommit() throws Exception {
        txManager.begin();
        Transaction tx = txManager.getTransaction();
        TestResource theResource = new TestResource();
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        txManager.commit();

        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertThrows(IllegalStateException.class, tx::commit);
        assertThrows(IllegalStateException.class, tx::rollback);
        assertThrows(IllegalStateException.class, tx::setRollbackOnly);
        assertThrows(IllegalStateException.class, () -> tx.enlistResource(new TestResource()));
        assertThrows(IllegalStateException.class, () -> tx.delistResource(new TestResource(), XAResource.TMSUCCESS));
        assertThrows(IllegalStateException.class, () -> tx.registerSynchronization(new TestSync(false)));
    }

    @Test
    public void testWrongResume() throws Exception {
        assertThrows(InvalidTransactionException.class, () -> txManager.resume(null));
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        assertThrows(IllegalStateException.class, () -> txManager.resume(null));
        txManager.rollback();
        assertThrows(InvalidTransactionException.class, () -> txManager.resume(tx));
    }

    @Test
    public void testWrongUTXOperationsAfterCommit() throws Exception {
        txManager.begin();
        UserTransaction utx = createUtx();
        txManager.commit();
        assertThrows(IllegalStateException.class, utx::commit);
        assertThrows(IllegalStateException.class, utx::rollback);
        assertThrows(IllegalStateException.class, utx::setRollbackOnly);
    }

    @Test
    public void testWrongUTXBegin() throws Exception {
        UserTransaction utx = createUtx();
        utx.begin();
        assertThrows(NotSupportedException.class, utx::begin);
    }

    @Test
    public void testBegin() throws Exception {
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
    }

    @Test
    public void testCommit() throws Exception {
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        txManager.commit();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
    }

    @Test
    public void testRollback() throws Exception {
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        txManager.rollback();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
    }

    @Test
    public void testTxCommit() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();

        TestSync sync = new TestSync(false);
        tx.registerSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        tx.commit();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testTxSuspendResume() throws Exception {
        assertNull(txManager.suspend());
        txManager.begin();
        final Transaction tx = txManager.suspend();
        assertNotNull(tx);
        assertNull(txManager.suspend());

        txManager.resume(tx);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        tx.commit();
        assertEquals("Committed", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
    }

    @Test
    public void testTxRollback() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(false);
        tx.registerSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        tx.rollback();
        assertEquals("RolledBack", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertFalse(sync.called_beforeCompletion, "beforeCompletion was called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testUTxCommit() throws Exception {
        final UserTransaction utx = createUtx();
        utx.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus()));

        utx.commit();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus()));
    }

    @Test
    public void testUTxRollback() throws Exception {
        final UserTransaction utx = createUtx();
        utx.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus()));

        utx.rollback();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus()));
    }

    @Test
    public void testTxCommitFailBC2PC() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(true);
        tx.registerSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final TestResource theResource = new TestResource();
        final TestResource theResource1 = new TestResource();
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        txManager.enlistResource(tx, new TestResourceHandle(theResource1));
        theResource.setCommitErrorCode(9999);
        theResource1.setCommitErrorCode(9999);
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        txManager.delistResource(tx, new TestResourceHandle(theResource1), XAResource.TMSUCCESS);

        final RollbackException e = assertThrows(RollbackException.class, tx::commit);
        assertThat("e.cause", e.getCause(), instanceOf(MyRuntimeException.class));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testTMCommitFailBC2PC() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(true);
        tx.registerSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final TestResource theResource = new TestResource();
        final TestResource theResource1 = new TestResource();
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        txManager.enlistResource(tx, new TestResourceHandle(theResource1));
        theResource.setCommitErrorCode(9999);
        theResource1.setCommitErrorCode(9999);
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        txManager.delistResource(tx, new TestResourceHandle(theResource1), XAResource.TMSUCCESS);

        final RollbackException e = assertThrows(RollbackException.class, txManager::commit);
        assertThat("e.cause", e.getCause(), instanceOf(MyRuntimeException.class));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testTxCommitFailBC1PC() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(true);
        tx.registerSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final TestResource theResource = new TestResource();
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        theResource.setCommitErrorCode(9999);
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);

        final RollbackException e = assertThrows(RollbackException.class, tx::commit);
        assertThat("e.cause", e.getCause(), instanceOf(MyRuntimeException.class));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testTxCommitFailBC2PCInterposedSynchronization() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(true);
        final TransactionSynchronizationRegistry ts = new TransactionSynchronizationRegistryImpl(txManager);
        ts.registerInterposedSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final TestResource theResource = new TestResource();
        final TestResource theResource1 = new TestResource();
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        txManager.enlistResource(tx, new TestResourceHandle(theResource1));
        theResource.setCommitErrorCode(9999);
        theResource1.setCommitErrorCode(9999);
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        txManager.delistResource(tx, new TestResourceHandle(theResource1), XAResource.TMSUCCESS);

        final RollbackException e = assertThrows(RollbackException.class, tx::commit);
        assertThat("e.cause", e.getCause(), instanceOf(MyRuntimeException.class));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testTxCommitFailBC1PCInterposedSynchronization() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(true);
        final TransactionSynchronizationRegistry ts = new TransactionSynchronizationRegistryImpl(txManager);
        ts.registerInterposedSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final TestResource theResource = new TestResource();
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        theResource.setCommitErrorCode(9999);
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        final RollbackException e = assertThrows(RollbackException.class, tx::commit);
        assertThat("e.cause", e.getCause(), instanceOf(MyRuntimeException.class));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testTxCommitRollbackBC() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync sync = new TestSync(txManager);
        tx.registerSynchronization(sync);
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final RollbackException e = assertThrows(RollbackException.class, tx::commit);
        assertNull(e.getCause(), "e.cause");
        assertEquals("RolledBack", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
        assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(sync.called_afterCompletion, "afterCompletion was not called");
    }

    @Test
    public void testCommit2PCWithRollbackExc1() throws Exception {
        verifyCommit2PCWithRollbackException(XAException.XA_RBROLLBACK, XAException.XA_HEURRB);
    }

    @Test
    public void testCommit2PCWithRollbackExc2() throws Exception {
        verifyCommit2PCWithRollbackException(XAException.XA_RBROLLBACK, XAException.XA_HEURRB, XAException.XA_HEURRB);
    }

    @Test
    public void testCommit2PCWithRollbackExc3() throws Exception {
        verifyCommit2PCWithRollbackException(XAException.XA_RDONLY, XAException.XA_HEURRB, 9999);
    }

    private void verifyCommit2PCWithRollbackException(int preapareErrCode, int... rollbackErrorCode) throws Exception {
        final TestResource theResourceP = new TestResource();
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        assertNotNull(createUtx(), "createUtx()");
        final Transaction tx = txManager.getTransaction();
        txManager.enlistResource(tx, new TestResourceHandle(theResourceP));
        theResourceP.setPrepareErrorCode(preapareErrCode);
        final TestResource[] theResourceR = enlistForRollback(tx, rollbackErrorCode);
        txManager.delistResource(tx, new TestResourceHandle(theResourceP), XAResource.TMSUCCESS);
        final RollbackException e = assertThrows(RollbackException.class, txManager::commit);
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        final boolean status = theResourceP.forgetCalled();
        assertTrue(status, "result of theResourceP.forgetCalled()");

        for (TestResource element : theResourceR) {
            boolean rStatus = element.forgetCalled();
            assertTrue(rStatus, "result of theResourceR.forgetCalled()");
        }
    }

    @Test
    public void testCommitOnePhaseWithHeuristicRlbExc1() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XA_HEURRB, HeuristicRollbackException.class, false);
    }

    @Test
    public void testCommitOnePhaseWithHeuristicMixedExc2() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XA_HEURMIX, HeuristicMixedException.class, false);
    }

    @Test
    public void testCommitOnePhaseWithRlbExc1() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XAER_NOTA, RollbackException.class, false);
    }

    @Test
    public void testCommitOnePhaseWithRlbExc2() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XAER_RMERR, RollbackException.class, false);
    }

    @Test
    public void testCommitOnePhaseWithRlbExc3() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XA_RBROLLBACK, RollbackException.class, true);
    }

    @Test
    public void testCommitOnePhaseWithRlbExc4() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XAER_RMERR, RollbackException.class, true);
    }

    @Test
    public void testCommitOnePhaseWithRlbExc5() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XAER_RMFAIL, RollbackException.class, true);
    }

    @Test
    public void testCommitOnePhaseWithRlbExc6() throws Exception {
        final int[] falures = {
            XAException.XAER_RMFAIL,
            XAException.XAER_RMERR,
            XAException.XAER_NOTA,
            XAException.XAER_INVAL,
            XAException.XAER_PROTO,
            XAException.XAER_DUPID
        };

        for (int errorCode : falures) {
            final TestResource theResource = new TestResource();
            txManager.begin();
            assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
            assertNotNull(createUtx());
            final Transaction tx = txManager.getTransaction();
            theResource.setStartErrorCode(errorCode);
            assertThrows(SystemException.class, () -> txManager.enlistResource(tx, new TestResourceHandle(theResource)));
            txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
            txManager.commit();
            assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        }
    }

    @Test
    public void testCommitOnePhaseWithXAExc1() throws Exception {
        verifyCommitOnePhaseWithException(XAException.XAER_RMFAIL, SystemException.class, false);
    }

    @Test
    public void testCommitOnePhaseWithXAExc2() throws Exception {
        final TestResource theResource = new TestResource();
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        final Transaction tx = txManager.getTransaction();
        theResource.setCommitErrorCode(XAException.XA_HEURCOM);
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        txManager.commit();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(tx.getStatus()));
    }

    @Test
    public void testRollbackWithErrorNoExc1() throws Exception {
        verifyXARollback(XAException.XA_RBROLLBACK);
    }

    @Test
    public void testRollbackWithErrorNoExc2() throws Exception {
        verifyXARollback(XAException.XAER_RMERR);
    }

    @Test
    public void testRollbackWithErrorNoExc3() throws Exception {
        verifyXARollback(XAException.XAER_NOTA);
    }

    @Test
    public void testRollbackWithErrorNoExc4() throws Exception {
        verifyXARollback(XAException.XAER_RMFAIL);
    }

    @Test
    public void testRollbackWithErrorNoExc5() throws Exception {
        verifyXARollback(XAException.XA_HEURRB);
    }

    @Test
    public void testRollbackWithErrorNoExc6() throws Exception {
        verifyXARollback(XAException.XA_HEURRB, XAException.XA_HEURRB);
    }

    private void verifyCommitOnePhaseWithException(int errorCode, Class<? extends Exception> exType, boolean setRollbackOnly) throws Exception {
        final TestResource theResource = new TestResource();
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        final UserTransaction utx = createUtx();
        final Transaction tx = txManager.getTransaction();
        if (setRollbackOnly) {
            theResource.setRollbackErrorCode(errorCode);
        } else {
            theResource.setCommitErrorCode(errorCode);
        }
        txManager.enlistResource(tx, new TestResourceHandle(theResource));
        txManager.delistResource(tx, new TestResourceHandle(theResource), XAResource.TMSUCCESS);
        if (setRollbackOnly) {
            txManager.setRollbackOnly();
        }
        assertThrows(exType, txManager::commit);
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(utx.getStatus()));
        assertTrue(theResource.forgetCalled(), "theResource.forgetCalled()");
    }

    private void verifyXARollback(int... errorCode) throws Exception {
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        createUtx();
        Transaction tx = txManager.getTransaction();
        enlistForRollback(tx, errorCode);
        txManager.rollback();
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
    }

    @Test
    public void testCommit2PCWithXAExc1() throws Exception {
        verifyCommit2PCWithXAException(XAException.XAER_RMFAIL, 9999, true, SystemException.class);
    }

    @Test
    public void testCommit2PCWithXAExc2() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURRB, XAException.XA_HEURRB, HeuristicRollbackException.class);
    }

    @Test
    public void testCommit2PCWithXAExc3() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURRB, XAException.XA_HEURMIX, HeuristicMixedException.class);
    }

    @Test
    public void testCommit2PCWithXAExc4() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURRB, XAException.XA_HEURCOM, HeuristicMixedException.class);
    }

    @Test
    public void testCommit2PCWithXAExc5() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURCOM, XAException.XA_HEURRB, HeuristicMixedException.class);
    }

    @Test
    public void testCommit2PCWithXAExc6() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURCOM, XAException.XA_HEURCOM, true, null);
    }

    @Test
    public void testCommit2PCWithXAExc7() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURCOM, 9999, true, null);
    }

    @Test
    public void testCommit2PCWithXAExc8() throws Exception {
        verifyCommit2PCWithXAException(XAException.XA_HEURRB, 9999, HeuristicMixedException.class);
    }

    @Test
    public void testCommit2PCWithXAExc9() throws Exception {
        verifyCommit2PCWithXAException(9999, XAException.XAER_PROTO, false, SystemException.class);
    }

    @Test
    public void testCommit2PCWithXAExc10() throws Exception {
        verifyCommit2PCWithXAException(XAException.XAER_PROTO, 9999, false, SystemException.class);
    }

    @Test
    public void testCommit2PCWithXAExc11() throws Exception {
        verifyCommit2PCWithXAException(9999, XAException.XAER_INVAL, false, SystemException.class);
    }

    @Test
    public void testCommit2PCWithXAExc12() throws Exception {
        verifyCommit2PCWithXAException(XAException.XAER_INVAL, 9999, false, SystemException.class);
    }

    private void verifyCommit2PCWithXAException(int errorCode1, int errorCode2, Class<? extends Exception> exType) throws Exception {
        verifyCommit2PCWithXAException(errorCode1, errorCode2, true, exType);
    }

    private void verifyCommit2PCWithXAException(int errorCode1, int errorCode2, boolean failOnCommit, Class<? extends Exception> exType) throws Exception {
        final TestResource theResource1 = new TestResource();
        final TestResource theResource2 = new TestResource();
        final TestSync s = new TestSync(false);
        txManager.begin();
        assertEquals("Active", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));

        createUtx();
        final Transaction tx = txManager.getTransaction();
        tx.registerSynchronization(s);
        txManager.enlistResource(tx, new TestResourceHandle(theResource1));
        txManager.enlistResource(tx, new TestResourceHandle(theResource2));
        if (failOnCommit) {
            theResource1.setCommitErrorCode(errorCode1);
            theResource2.setCommitErrorCode(errorCode2);
        } else {
            theResource1.setPrepareErrorCode(errorCode1);
            theResource2.setPrepareErrorCode(errorCode2);
        }
        txManager.delistResource(tx, new TestResourceHandle(theResource1), XAResource.TMSUCCESS);
        txManager.delistResource(tx, new TestResourceHandle(theResource2), XAResource.TMSUCCESS);
        if (exType == null) {
            txManager.commit();
        } else {
            assertThrows(exType, txManager::commit);
        }
        assertEquals("NoTransaction", JavaEETransactionManagerSimplified.getStatusAsString(txManager.getStatus()));
        assertTrue(s.called_beforeCompletion, "beforeCompletion was not called");
        assertTrue(s.called_afterCompletion, "afterCompletion was not called");
        assertTrue(theResource1.forgetCalled());
        assertTrue(theResource2.forgetCalled());
    }

    private UserTransaction createUtx() throws javax.naming.NamingException {
        UserTransaction utx = new UserTransactionImpl();
        InvocationManager im = new org.glassfish.api.invocation.InvocationManagerImpl();
        ((UserTransactionImpl)utx).setForTesting(txManager, im);
        return utx;
    }

    private TestResource[] enlistForRollback(Transaction tx, int... errorCode) throws Exception {
        TestResource[] theResources = new TestResource[errorCode.length];
        for (int i = 0; i < errorCode.length; i++) {
            theResources[i] = new TestResource();
            txManager.enlistResource(tx, new TestResourceHandle(theResources[i]));
            theResources[i].setRollbackErrorCode(errorCode[i]);
        }

        for (int i = 0; i < errorCode.length; i++) {
            txManager.delistResource(tx, new TestResourceHandle(theResources[i]), XAResource.TMSUCCESS);
        }

        return theResources;
    }


    static class TestSync implements Synchronization {

        // Used to validate the calls
        private boolean fail = false;
        private TransactionManager t = null;

        protected boolean called_beforeCompletion = false;
        protected boolean called_afterCompletion = false;

        public TestSync(boolean fail) {
            this.fail = fail;
        }

        public TestSync(TransactionManager t) {
            fail = true;
            this.t = t;
        }

        @Override
        public void beforeCompletion() {
            System.out.println("**Called beforeCompletion  **");
            called_beforeCompletion = true;
            if (fail) {
                System.out.println("**Failing in beforeCompletion  **");
                if (t != null) {
                    try {
                        t.setRollbackOnly();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("**Throwing MyRuntimeException... **");
                    throw new MyRuntimeException("test");
                }
            }
        }

        @Override
        public void afterCompletion(int status) {
            System.out.println("**Called afterCompletion with status:  "
                    + JavaEETransactionManagerSimplified.getStatusAsString(status));
            called_afterCompletion = true;
        }
    }

    static class MyRuntimeException extends RuntimeException {
        public MyRuntimeException(String msg) {
            super(msg);
        }
    }

    static class TestResource implements XAResource {

        // allow only one resource in use at a time
        private boolean inUse;
        private boolean _forgetCalled = false;
        private boolean _isHeuristic = false;

        private int commitErrorCode = 9999;
        private int rollbackErrorCode = 9999;
        private int prepareErrorCode = 9999;
        private int startErrorCode = 9999;

        private Transaction tx;
        private int commit_status = -1;
        private int rollback_status = -1;
        private int prepare_status = -1;

        private static long id0 = System.currentTimeMillis();
        private long id = ++id0;

        TestResource() {}

        TestResource(Transaction tx) {
            this.tx = tx;

        }

        TestResource(Transaction tx, long id) {
            this.tx = tx;
            this.id = id;
        }

        // to test different xaexception error codes
        public void setCommitErrorCode(int errorCode) {
            this.commitErrorCode = errorCode;
            setHeuristic(errorCode);
        }

        public void setStartErrorCode(int errorCode) {
            this.startErrorCode = errorCode;
        }

        public void setRollbackErrorCode(int errorCode) {
            this.rollbackErrorCode = errorCode;
            setHeuristic(errorCode);
        }

        public void setPrepareErrorCode(int errorCode) {
            this.prepareErrorCode = errorCode;
        }

        private void setHeuristic(int errorCode) {
            if (errorCode == XAException.XA_HEURCOM ||
                errorCode == XAException.XA_HEURHAZ ||
                errorCode == XAException.XA_HEURMIX ||
                errorCode == XAException.XA_HEURRB) {
                _isHeuristic = true;
            }
        }

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException{
            // test goes here
            System.out.println("in XA commit");
            commit_status = getStatus("COMMIT");
            if (commitErrorCode != 9999) {
                System.out.println("throwing XAException." + commitErrorCode + " during commit of " + (onePhase? "1" : "2") + "pc");
                throw new XAException(commitErrorCode);
            }
        }

        @Override
        public boolean isSameRM(XAResource xaresource)
            throws XAException {
            return xaresource == this || this.id == ((TestResource)xaresource).id;
        }


        @Override
        public void rollback(Xid xid)
            throws XAException {
            System.out.println("in XA rollback");
            rollback_status = getStatus("ROLLBACK");
            if (rollbackErrorCode != 9999) {
                System.out.println("throwing XAException." + rollbackErrorCode + " during rollback" );
                throw new XAException(rollbackErrorCode);
            }
        }

        @Override
        public int prepare(Xid xid)
            throws XAException {
            System.out.println("in XA prepare");
            prepare_status = getStatus("PREPARE");
            if (prepareErrorCode != 9999) {
                System.out.println("throwing XAException." + prepareErrorCode + " during prepare" );
                throw new XAException(prepareErrorCode);
            }
            return XAResource.XA_OK;
        }

        @Override
        public boolean setTransactionTimeout(int i)
            throws XAException {
            return true;
        }

        @Override
        public int getTransactionTimeout()
            throws XAException {
            return 0;
        }
        @Override
        public void forget(Xid xid)
            throws XAException {
            _forgetCalled = true;
            inUse = false;
        }

        @Override
        public void start(Xid xid, int flags)
            throws XAException {
            if (inUse) {
                throw new XAException(XAException.XAER_NOTA);
            }
            inUse = true;
            if (startErrorCode != 9999) {
                System.out.println("throwing XAException." + startErrorCode + " during start" );
                throw new XAException(startErrorCode);
            }
        }


        @Override
        public void end(Xid xid, int flags)
            throws XAException {
            inUse = false;
        }


        @Override
        public Xid[] recover(int flags)
            throws XAException {
            return null;
        }

        public boolean forgetCalled() {
            return !_isHeuristic || _forgetCalled;
        }

        public boolean commitStatusOK() {
            return commit_status==Status.STATUS_COMMITTING;
        }

        public boolean rollbackStatusOK() {
            return rollback_status==Status.STATUS_ROLLING_BACK;
        }

        public boolean prepareStatusOK() {
            return prepare_status==Status.STATUS_PREPARING;
        }

        public boolean isAssociated() {
            return inUse;
        }

        private int getStatus(String name) {
            int status = -1;
            try {
                if (tx != null) {
                    status = tx.getStatus();
                    System.out.println("Status in " + name + ": " + JavaEETransactionManagerSimplified.getStatusAsString(status));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return status;
        }

    }

    static class TestResourceHandle extends ResourceHandle {
        private final XAResource xaResource;
        private static PoolManagerImpl poolMgr = new PoolManagerImpl();

        public TestResourceHandle(XAResource xaResource) {
            super(null, new ResourceSpec(new SimpleJndiName("testResource"), 0), null);
            this.xaResource = xaResource;
        }

        @Override
        public boolean isTransactional() {
            return true;
        }

        @Override
        public boolean isShareable() {
            return true;
        }

        @Override
        public boolean supportsXA() {
            return true;
        }

        @Override
        public ResourceAllocator getResourceAllocator() {
            return null;
        }

        @Override
        public ManagedConnection getResource() {
            return null;
        }

        @Override
        public XAResource getXAResource() {
            return xaResource;
        }

        @Override
        public Object getUserConnection() {
            return null;
        }

        @Override
        public void fillInResourceObjects(Object userConnection, XAResource xares) {
        }

        @Override
        public void enlistedInTransaction(Transaction tran) throws IllegalStateException {
            poolMgr.resourceEnlisted(tran, this);
        }
    }
}
