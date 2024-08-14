/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;

import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;
import jakarta.transaction.UserTransaction;

import java.beans.PropertyChangeEvent;
import java.util.logging.Level;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.api.invocation.InvocationManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import static com.sun.enterprise.transaction.JavaEETransactionManagerSimplified.getStatusAsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for simple App.
 */
public class JavaEETransactionManagerSimplifiedTest {

    private JavaEETransactionManager txManager;

    @BeforeEach
    public void setUp() {
        txManager = new JavaEETransactionManagerSimplified();
        JavaEETransactionManagerDelegate delegate = new JavaEETransactionManagerSimplifiedDelegate();
        txManager.setDelegate(delegate);
        delegate.setTransactionManager(txManager);
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
        UnprocessedChangeEvents events = listener.changed(new PropertyChangeEvent[] {e1, e2});
        assertNull(events);
    }


    @Test
    public void testWrongResume() throws Exception {
        assertThrows(InvalidTransactionException.class, () -> txManager.resume(null),
            "WRONG: TM resume null successful");

        txManager.begin();
        Transaction tx = txManager.getTransaction();
        assertThrows(IllegalStateException.class, () -> txManager.resume(null),
            "WRONG: TM resume null on active tx successful");

        txManager.rollback();
        assertThrows(InvalidTransactionException.class, () -> txManager.resume(tx), "WRONG: TM resume successful");
    }


    @Test
    public void testWrongTMOperationsAfterCommit() throws Exception {
        txManager.begin();
        assertDoesNotThrow(() -> txManager.commit(), "Commit right after begin.");

        assertThrows(IllegalStateException.class, () -> txManager.commit(), "Duplicit commit");
        assertThrows(IllegalStateException.class, () -> txManager.rollback(), "Rollback after broken commit");
        assertThrows(IllegalStateException.class, () -> txManager.setRollbackOnly(),
            "WRONG: TM setRollbackOnly successful");
    }


    @Test
    public void testWrongTXOperationsAfterCommit() throws Exception {
        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        txManager.commit();

        assertThrows(IllegalStateException.class, () -> tx.commit(), "WRONG: Tx commit successful");
        assertThrows(IllegalStateException.class, () -> tx.rollback(), "WRONG: Tx rollback successful");
        assertThrows(IllegalStateException.class, () -> tx.setRollbackOnly(), "WRONG: Tx setRollbackOnly successful");

        assertThrows(IllegalStateException.class, () -> tx.enlistResource(new TestResource()),
            "WRONG: Tx enlistResource successful");

        assertThrows(IllegalStateException.class, () -> tx.delistResource(new TestResource(), XAResource.TMSUCCESS),
            "WRONG: Tx delistResource successful");

        final TestSync s = new TestSync(false);
        assertThrows(IllegalStateException.class, () -> tx.registerSynchronization(s),
            "WRONG: Tx registerSynchronization successful");
    }


    @Test
    public void testWrongTMCommit() {
        assertThrows(IllegalStateException.class, () -> txManager.commit(), "commit without transaction");
    }


    @Test
    public void testWrongTMRollback() {
        assertThrows(IllegalStateException.class, () -> txManager.rollback(), "rollback without transaction");
    }


    @Test
    public void testWrongTMTimeout() {
        assertThrows(SystemException.class, () -> txManager.setTransactionTimeout(-1),
            "WRONG: TM setTransactionTimeout successful with negative value");
    }


    @Test
    public void testWrongUTXTimeout() {
        final UserTransaction utx = createUtx();
        assertThrows(SystemException.class, () -> utx.setTransactionTimeout(-1),
            "WRONG: UTX setTransactionTimeout successful with negative value");
    }


    @Test
    public void testWrongUTXCommit() {
        final UserTransaction utx = createUtx();
        assertThrows(IllegalStateException.class, () -> utx.commit(), "WRONG: UTX commit successful");
    }


    @Test
    public void testWrongUTXBegin() throws Exception {
        final UserTransaction utx = createUtx();
        utx.begin();
        assertThrows(NotSupportedException.class, () -> utx.begin(), "WRONG: TWICE UTX begin successful");
    }


    @Test
    public void testWrongUTXOperationsAfterCommit() throws Exception {
        txManager.begin();
        UserTransaction utx = createUtx();
        txManager.commit();

        assertThrows(IllegalStateException.class, () -> utx.commit(), "WRONG: UTx commit successful");
        assertThrows(IllegalStateException.class, () -> utx.rollback(), "WRONG: UTx rollback successful");
        assertThrows(IllegalStateException.class, () -> utx.setRollbackOnly(), "WRONG: UTx setRollbackOnly successful");
    }


    @Test
    public void testBegin() throws Exception {
        assertEquals("NoTransaction", getStatusAsString(txManager.getStatus()));
        txManager.begin();
        assertEquals("Active", getStatusAsString(txManager.getStatus()));
    }


    @Test
    public void testCommit() throws Exception {
        txManager.begin();
        assertEquals("Active", getStatusAsString(txManager.getStatus()));
        txManager.commit();
        assertEquals("NoTransaction", getStatusAsString(txManager.getStatus()));
    }


    @Test
    public void testRollback() throws Exception {
        txManager.begin();
        assertEquals("Active", getStatusAsString(txManager.getStatus()));
        txManager.rollback();
        assertEquals("NoTransaction", getStatusAsString(txManager.getStatus()));
    }


    @Test
    public void testTxCommit() throws Exception {
        txManager.begin();
        Transaction tx = txManager.getTransaction();
        TestSync sync = new TestSync(false);
        tx.registerSynchronization(sync);
        assertEquals("Active", getStatusAsString(tx.getStatus()));

        tx.commit();
        assertAll(
            () -> assertEquals("Committed", getStatusAsString(tx.getStatus())),
            () -> assertEquals("NoTransaction", getStatusAsString(txManager.getStatus())),
            () -> assertTrue(sync.called_beforeCompletion, "beforeCompletion was not called"),
            () -> assertTrue(sync.called_afterCompletion, "afterCompletion was not called")
        );
    }


    @Test
    public void testTxSuspendResume() throws Exception {
        assertNull(txManager.suspend());

        txManager.begin();

        Transaction tx = txManager.suspend();
        assertNotNull(tx);
        assertNull(txManager.suspend());

        txManager.resume(tx);
        assertEquals("Active", getStatusAsString(tx.getStatus()));

        tx.commit();
        assertEquals("Committed", getStatusAsString(tx.getStatus()));
        assertEquals("NoTransaction", getStatusAsString(txManager.getStatus()));
    }


    @Test
    public void testTxRollback() throws Exception {
        txManager.begin();
        Transaction tx = txManager.getTransaction();

        TestSync sync = new TestSync(false);
        tx.registerSynchronization(sync);
        assertEquals("Active", getStatusAsString(tx.getStatus()));

        tx.rollback();
        assertAll(
            () -> assertEquals("RolledBack", getStatusAsString(tx.getStatus())),
            () -> assertEquals("NoTransaction", getStatusAsString(txManager.getStatus())),
            () -> assertFalse(sync.called_beforeCompletion, "beforeCompletion was called"),
            () -> assertTrue(sync.called_afterCompletion, "afterCompletion was not called")
        );
    }


    @Test
    public void testUTxCommit() throws Exception {
        UserTransaction utx = createUtx();
        utx.begin();
        assertEquals("Active", getStatusAsString(txManager.getStatus()));

        utx.commit();
        assertEquals("NoTransaction", getStatusAsString(txManager.getStatus()));
    }


    @Test
    public void testUTxRollback() throws Exception {
        UserTransaction utx = createUtx();
        utx.begin();
        assertEquals("Active", getStatusAsString(utx.getStatus()));

        utx.rollback();
        assertEquals("NoTransaction", getStatusAsString(utx.getStatus()));
    }


    @Test
    public void testTxCommitFailBeforeCompletion() throws Exception {
        // Suppress warnings from beforeCompletion() logging
        ((JavaEETransactionManagerSimplified) txManager).getLogger().setLevel(Level.SEVERE);

        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync s = new TestSync(true);
        tx.registerSynchronization(s);
        assertEquals("Active", getStatusAsString(txManager.getStatus()));

        final RollbackException rollbackException = assertThrows(RollbackException.class, () -> tx.commit());
        assertAll(
            () -> assertThat("rollbackException cause", rollbackException.getCause(), instanceOf(MyRuntimeException.class)),
            () -> assertEquals("RolledBack", getStatusAsString(tx.getStatus())),
            () -> assertTrue(s.called_beforeCompletion, "beforeCompletion was not called"),
            () -> assertTrue(s.called_afterCompletion, "afterCompletion was not called")
        );
    }


    @Test
    public void testTMCommitFailBeforeCompletion() throws Exception {
        // Suppress warnings from beforeCompletion() logging
        ((JavaEETransactionManagerSimplified) txManager).getLogger().setLevel(Level.SEVERE);

        txManager.begin();
        final Transaction tx = txManager.getTransaction();
        final TestSync s = new TestSync(true);
        tx.registerSynchronization(s);
        assertEquals("Active", getStatusAsString(txManager.getStatus()));

        final RollbackException rollbackException = assertThrows(RollbackException.class, () -> txManager.commit());
        assertAll(
            () -> assertThat("rollbackException cause", rollbackException.getCause(), instanceOf(MyRuntimeException.class)),
            () -> assertEquals("RolledBack", getStatusAsString(tx.getStatus())),
            () -> assertTrue(s.called_beforeCompletion, "beforeCompletion was not called"),
            () -> assertTrue(s.called_afterCompletion, "afterCompletion was not called")
        );
    }


    @Test
    public void testTxCommitFailInterposedSyncBeforeCompletion() throws Exception {
        // Suppress warnings from beforeCompletion() logging
        ((JavaEETransactionManagerSimplified) txManager).getLogger().setLevel(Level.SEVERE);

        txManager.begin();
        Transaction tx = txManager.getTransaction();
        TestSync s = new TestSync(true);
        ((JavaEETransactionImpl) tx).registerInterposedSynchronization(s);
        assertEquals("Active", getStatusAsString(txManager.getStatus()));

        final RollbackException rollbackException = assertThrows(RollbackException.class, () -> tx.commit());
        assertAll(
            () -> assertThat("rollbackException cause", rollbackException.getCause(), instanceOf(MyRuntimeException.class)),
            () -> assertEquals("RolledBack", getStatusAsString(tx.getStatus())),
            () -> assertTrue(s.called_beforeCompletion, "beforeCompletion was not called"),
            () -> assertTrue(s.called_afterCompletion, "afterCompletion was not called")
        );
    }


    @Test
    public void testTxCommitRollbackBeforeCompletion() throws Exception {
        // Suppress warnings from beforeCompletion() logging
        ((JavaEETransactionManagerSimplified) txManager).getLogger().setLevel(Level.SEVERE);

        txManager.begin();
        Transaction tx = txManager.getTransaction();
        TestSync s = new TestSync(txManager);
        tx.registerSynchronization(s);
        assertEquals("Active", getStatusAsString(txManager.getStatus()));

        final RollbackException rollbackException = assertThrows(RollbackException.class, () -> tx.commit());
        assertAll(
            () -> assertNull(rollbackException.getCause(), "rollbackException cause"),
            () -> assertEquals("RolledBack", getStatusAsString(tx.getStatus())),
            () -> assertTrue(s.called_beforeCompletion, "beforeCompletion was not called"),
            () -> assertTrue(s.called_afterCompletion, "afterCompletion was not called")
        );
    }


    private UserTransaction createUtx() {
        UserTransaction utx = new UserTransactionImpl();
        InvocationManager im = new org.glassfish.api.invocation.InvocationManagerImpl();
        ((UserTransactionImpl) utx).setForTesting(txManager, im);
        return utx;
    }

    static class TestSync implements Synchronization {

        // Used to validate the calls
        private final boolean fail;
        private TransactionManager t;

        protected boolean called_beforeCompletion;
        protected boolean called_afterCompletion;

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
                        System.out.println("**Calling setRollbackOnly **");
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
            System.out.println("**Called afterCompletion with status:  " + getStatusAsString(status));
            called_afterCompletion = true;
        }
    }

    static class MyRuntimeException extends RuntimeException {

        public MyRuntimeException(String msg) {
            super(msg);
        }
    }

    static class TestResource implements XAResource {

        @Override
        public void commit(Xid xid, boolean onePhase) throws XAException {
        }


        @Override
        public boolean isSameRM(XAResource xaresource) throws XAException {
            return false;
        }


        @Override
        public void rollback(Xid xid) throws XAException {
        }


        @Override
        public int prepare(Xid xid) throws XAException {
            return XAResource.XA_OK;
        }


        @Override
        public boolean setTransactionTimeout(int i) throws XAException {
            return true;
        }


        @Override
        public int getTransactionTimeout() throws XAException {
            return 0;
        }


        @Override
        public void forget(Xid xid) throws XAException {
        }


        @Override
        public void start(Xid xid, int flags) throws XAException {
        }


        @Override
        public void end(Xid xid, int flags) throws XAException {
        }


        @Override
        public Xid[] recover(int flags) throws XAException {
            return null;
        }

    }

}
