/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.orb.http.server;

import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.TransactionRolledbackLocalException;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a call refused because the caller's transaction can only roll back
 * looks like to the caller.
 *
 * <p>What this does not cover: which exception the real container's remote
 * business view hands the dispatcher for such a call (that needs a running
 * server); the mapping is checked for the two types the specification names.
 */
class EjbExceptionsRollbackTest {

    private static Method method() throws NoSuchMethodException {
        return Object.class.getMethod("toString");
    }

    @Test
    @DisplayName("the container's rolled-back refusal reaches the caller as EJBTransactionRolledbackException")
    void aRolledBackRefusalKeepsItsFlavour() throws Exception {
        Throwable thrown = new TransactionRolledbackLocalException("Client's transaction aborted");

        Throwable toClient = EjbExceptions.toClientException(thrown, method());

        assertInstanceOf(EJBTransactionRolledbackException.class, toClient);
        assertSame(thrown, toClient.getCause());
        assertEquals(true, toClient.getMessage().contains("Client's transaction aborted"), toClient.getMessage());
    }

    @Test
    @DisplayName("an EJBTransactionRolledbackException from the container is passed on as it is")
    void aRolledBackExceptionIsNotWrappedAgain() throws Exception {
        Throwable thrown = new EJBTransactionRolledbackException("doomed");

        assertSame(thrown, EjbExceptions.toClientException(thrown, method()));
    }

    @Test
    @DisplayName("a wrapped rollback refusal reaches the caller as EJBTransactionRolledbackException")
    void aWrappedRolledBackRefusalKeepsItsFlavour() throws Exception {
        Throwable local = new TransactionRolledbackLocalException("Client's transaction aborted");

        Throwable toClient = EjbExceptions.toClientException(new RuntimeException("mapped", local), method());

        assertInstanceOf(EJBTransactionRolledbackException.class, toClient);
        assertSame(local, toClient.getCause());
    }

    @Test
    @DisplayName("the CORBA shape of the refusal is mapped too, and no CORBA type is sent to the client")
    void theCorbaShapeIsMappedAndNotShipped() throws Exception {
        Throwable local = new TransactionRolledbackLocalException("Client's transaction aborted");
        Throwable corba = new TRANSACTION_ROLLEDBACK("rolled back", local);

        Throwable toClient = EjbExceptions.toClientException(corba, method());

        assertInstanceOf(EJBTransactionRolledbackException.class, toClient);
        assertSame(local, toClient.getCause());
        assertFalse(hasCorbaInChain(toClient), "a CORBA type would travel to the client");
        assertTrue(toClient.getMessage().contains("TRANSACTION_ROLLEDBACK"), toClient.getMessage());
    }

    @Test
    @DisplayName("a CORBA rollback that carries nothing else is still a rollback, with a plain cause chain")
    void aBareCorbaRollbackIsMapped() throws Exception {
        Throwable corba = new TRANSACTION_ROLLEDBACK("rolled back", new IllegalStateException("inner"));

        Throwable toClient = EjbExceptions.toClientException(corba, method());

        assertInstanceOf(EJBTransactionRolledbackException.class, toClient);
        assertFalse(hasCorbaInChain(toClient));
        assertTrue(toClient.getCause().getMessage().contains("inner"), String.valueOf(toClient.getCause()));
    }

    @Test
    @DisplayName("another CORBA system exception is an EJBException without a CORBA cause")
    void otherCorbaExceptionsAreNotShipped() throws Exception {
        Throwable toClient = EjbExceptions.toClientException(
                new NO_PERMISSION("denied", new java.rmi.AccessException("no")), method());

        assertInstanceOf(EJBException.class, toClient);
        assertNotSame(EJBTransactionRolledbackException.class, toClient.getClass());
        assertFalse(hasCorbaInChain(toClient));
        assertTrue(toClient.getMessage().contains("NO_PERMISSION"), toClient.getMessage());
    }

    private static boolean hasCorbaInChain(Throwable t) {
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getClass().getName().startsWith("org.omg.")) {
                return true;
            }
        }
        return false;
    }

    // Characterization test: it passes without the change under test and pins
    // behaviour that is meant to stay.
    @Test
    @DisplayName("any other system exception is still an EJBException")
    void otherSystemExceptionsAreStillFlattened() throws Exception {
        Throwable toClient = EjbExceptions.toClientException(new IllegalStateException("boom"), method());

        assertInstanceOf(EJBException.class, toClient);
        assertNotSame(EJBTransactionRolledbackException.class, toClient.getClass());
    }
}
