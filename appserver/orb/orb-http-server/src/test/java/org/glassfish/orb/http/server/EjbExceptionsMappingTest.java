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

import com.sun.ejb.containers.InternalEJBContainerException;

import jakarta.ejb.AccessLocalException;
import jakarta.ejb.EJBAccessException;
import jakarta.ejb.EJBException;
import jakarta.ejb.EJBTransactionRequiredException;
import jakarta.ejb.EJBTransactionRolledbackException;
import jakarta.ejb.NoSuchEJBException;
import jakarta.ejb.NoSuchObjectLocalException;
import jakarta.ejb.TransactionRequiredLocalException;
import jakarta.ejb.TransactionRolledbackLocalException;

import java.lang.reflect.Method;
import java.rmi.AccessException;
import java.rmi.NoSuchObjectException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.omg.CORBA.NO_PERMISSION;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.CORBA.TRANSACTION_REQUIRED;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The exceptions GlassFish's IIOP business-view wrapper tells apart reach an
 * HTTP client as the same types, and the container's internal wrapper never
 * does.
 *
 * <p>The CORBA and container types are test stand-ins recognised by class name,
 * with the shapes the container produces: the CORBA exception carries the
 * original as its cause, and {@code InternalEJBContainerException} carries the
 * bean's {@code EJBException}. That the real container produces them is not
 * covered here.
 */
class EjbExceptionsMappingTest {

    private static Method method() throws NoSuchMethodException {
        return Object.class.getMethod("toString");
    }

    private static Throwable toClient(Throwable thrown) throws Exception {
        Throwable toClient = EjbExceptions.toClientException(thrown, method());
        assertFalse(InternalTypes.inChain(toClient), "a container or CORBA type would travel to the client");
        return toClient;
    }

    @Test
    @DisplayName("a denied call is an EJBAccessException, whichever shape the denial arrives in")
    void aDeniedCallIsAnAccessException() throws Exception {
        AccessException denied = new AccessException("no role");

        Throwable fromCorba = toClient(new NO_PERMISSION("denied", denied));

        assertInstanceOf(EJBAccessException.class, fromCorba);
        assertSame(denied, fromCorba.getCause());
        assertTrue(fromCorba.getMessage().contains("NO_PERMISSION"), fromCorba.getMessage());
        assertInstanceOf(EJBAccessException.class, toClient(denied));
        assertInstanceOf(EJBAccessException.class, toClient(new AccessLocalException("no role")));
    }

    @Test
    @DisplayName("a call that needs a transaction and has none is an EJBTransactionRequiredException")
    void aMissingTransactionIsATransactionRequiredException() throws Exception {
        Throwable original = new TransactionRequiredLocalException("mandatory");

        Throwable fromCorba = toClient(new TRANSACTION_REQUIRED("required", original));

        assertInstanceOf(EJBTransactionRequiredException.class, fromCorba);
        assertSame(original, fromCorba.getCause());
        assertInstanceOf(EJBTransactionRequiredException.class, toClient(original));
    }

    @Test
    @DisplayName("a call on a bean that is gone is a NoSuchEJBException")
    void aRemovedBeanIsANoSuchEjbException() throws Exception {
        Throwable original = new NoSuchObjectException("removed");

        Throwable fromCorba = toClient(new OBJECT_NOT_EXIST("gone", original));

        assertInstanceOf(NoSuchEJBException.class, fromCorba);
        assertSame(original, fromCorba.getCause());
        assertInstanceOf(NoSuchEJBException.class, toClient(original));
        assertInstanceOf(NoSuchEJBException.class, toClient(new NoSuchObjectLocalException("removed")));
    }

    @Test
    @DisplayName("an empty CORBA message does not leave a dangling colon in the client's message")
    void anEmptyMessageHasNoDanglingColon() throws Exception {
        Throwable toClient = toClient(new NO_PERMISSION("", null));

        assertEquals("org.omg.CORBA.NO_PERMISSION", toClient.getMessage());
    }

    @Test
    @DisplayName("the container's internal wrapper is removed and the bean's exception reaches the client")
    void theInternalWrapperIsRemoved() throws Exception {
        EJBException bean = new EJBException("boom", new IllegalStateException("cause"));

        Throwable toClient = toClient(new InternalEJBContainerException("boom", bean));

        assertSame(bean, toClient);
        assertEquals("boom", toClient.getMessage());
    }

    @Test
    @DisplayName("a container type inside the bean's exception is replaced by a plain copy")
    void aContainerTypeInsideTheBeansExceptionIsNotShipped() throws Exception {
        EJBException bean = new EJBException("boom", new NO_PERMISSION("denied", new IllegalStateException("deep")));

        Throwable toClient = toClient(new InternalEJBContainerException("boom", bean));

        assertEquals(EJBException.class, toClient.getClass());
        assertEquals("boom", toClient.getMessage());
        assertTrue(toClient.getCause().getMessage().contains("NO_PERMISSION"), String.valueOf(toClient.getCause()));
        assertTrue(toClient.getCause().getCause().getMessage().contains("deep"));
    }

    @Test
    @DisplayName("what the wrapper carries is still mapped when it is one of the four")
    void aMappedTypeBehindTheWrapperIsMapped() throws Exception {
        Throwable carried = new TransactionRolledbackLocalException("y");

        assertInstanceOf(EJBTransactionRolledbackException.class,
                toClient(new InternalEJBContainerException("x", carried)));
    }

    @Test
    @DisplayName("a container type among the suppressed exceptions is not shipped either")
    void aSuppressedContainerTypeIsNotShipped() throws Exception {
        IllegalStateException thrown = new IllegalStateException("boom");
        thrown.addSuppressed(new NO_PERMISSION("denied", null));

        Throwable toClient = toClient(thrown);

        assertEquals(EJBException.class, toClient.getClass());
        assertEquals(1, toClient.getCause().getSuppressed().length);
    }
}
