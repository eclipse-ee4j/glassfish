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
 * server). The shapes are the ones the container's IIOP exception mapping
 * produces: the exception itself, or a CORBA {@code TRANSACTION_ROLLEDBACK}
 * that carries it as its cause.
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
    @DisplayName("the CORBA shape of the refusal is mapped too, and no CORBA type is sent to the client")
    void theCorbaShapeIsMappedAndNotShipped() throws Exception {
        Throwable local = new TransactionRolledbackLocalException("Client's transaction aborted");
        Throwable corba = Corba.rolledBack("rolled back", local);

        Throwable toClient = EjbExceptions.toClientException(corba, method());

        assertInstanceOf(EJBTransactionRolledbackException.class, toClient);
        assertSame(local, toClient.getCause());
        assertFalse(InternalTypes.inChain(toClient), "a CORBA type would travel to the client");
        assertTrue(toClient.getMessage().contains("TRANSACTION_ROLLEDBACK"), toClient.getMessage());
    }

    @Test
    @DisplayName("a CORBA rollback that carries nothing else is still a rollback, with a plain cause chain")
    void aBareCorbaRollbackIsMapped() throws Exception {
        Throwable corba = Corba.rolledBack("rolled back", new IllegalStateException("inner"));

        Throwable toClient = EjbExceptions.toClientException(corba, method());

        assertInstanceOf(EJBTransactionRolledbackException.class, toClient);
        assertFalse(InternalTypes.inChain(toClient));
        assertTrue(toClient.getCause().getMessage().contains("inner"), String.valueOf(toClient.getCause()));
    }

    @Test
    @DisplayName("a rollback inside the bean's own system exception is not the caller's rollback")
    void aRollbackBelowTheTopIsNotMapped() throws Exception {
        Throwable inner = new TransactionRolledbackLocalException("inner");

        for (Throwable thrown : new Throwable[] {
            new EJBException("bean failed", (Exception) inner),
            new RuntimeException("bean failed", inner),
            new IllegalStateException("bean failed", Corba.rolledBack("x", inner))
        }) {
            Throwable toClient = EjbExceptions.toClientException(thrown, method());

            assertEquals(EJBException.class, toClient.getClass(), String.valueOf(thrown));
            assertFalse(InternalTypes.inChain(toClient), String.valueOf(thrown));
        }
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
