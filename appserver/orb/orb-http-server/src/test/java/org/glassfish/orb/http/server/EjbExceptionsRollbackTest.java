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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

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
