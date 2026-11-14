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

package org.glassfish.orb.http.client;

import java.net.URI;
import java.util.Hashtable;

import javax.naming.Context;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Joining the caller's transaction can be switched off, and is on by default.
 * <p>
 * Against a server that cannot import transactions, every call made inside a
 * container transaction is refused and dooms the caller's transaction. The
 * switch is for that deployment; it must not change anything for anyone who
 * does not set it.
 */
class AmbientTransactionSwitchTest {

    public interface Pinger {
        void ping();
    }

    private static final String PROVIDER = "http://localhost:8080/glassfish-services";

    private RecordingTransport transport;

    private AmbientTransactionTest.FakeTransaction transaction;

    @BeforeEach
    void setUp() {
        AmbientTransactionTest.BOUND.clear();
        AmbientTransactionTest.started = null;
        AmbientTransaction.forget();
        ClientTransactionContext.disassociate();
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, AmbientTransactionTest.Factory.class.getName());

        transport = new RecordingTransport();
        transaction = new AmbientTransactionTest.FakeTransaction();
        AmbientTransactionTest.BOUND.put("java:comp/TransactionSynchronizationRegistry",
                new AmbientTransactionTest.FakeRegistry(transaction));
        AmbientTransactionTest.BOUND.put("java:appserver/TransactionManager",
                new AmbientTransactionTest.FakeManager(transaction));
    }

    @AfterEach
    void tearDown() {
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        ClientTransactionContext.disassociate();
        AmbientTransaction.forget();
    }

    private void ping(ClientConfiguration config) {
        HttpEjbClient client = new HttpEjbClient(config, transport, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
        client.createProxy(Pinger.class, new EjbLocator("app", "module", null, "PingerBean")).ping();
    }

    @Test
    @DisplayName("by default a call made inside a container transaction joins it")
    void joinsByDefault() {
        ping(ClientConfiguration.builder(URI.create(PROVIDER)).build());

        assertEquals(1, transaction.enlisted);
        assertEquals(1, transport.calls.size());
    }

    @Test
    @DisplayName("with joining switched off the call is made and nothing is enlisted")
    void switchedOffSendsTheCallWithoutTheTransaction() {
        ping(ClientConfiguration.builder(URI.create(PROVIDER)).joinAmbientTransaction(false).build());

        assertEquals(0, transaction.enlisted, "the caller's transaction was joined");
        assertNull(ClientTransactionContext.current());
        assertEquals(1, transport.calls.size(), "the call itself must still be made");
    }

    @Test
    @DisplayName("the JNDI property switches joining off, and only the value false does")
    void thePropertySwitchesJoiningOff() throws Exception {
        assertTrue(joins(null), "default");
        assertFalse(joins("false"));
        assertFalse(joins(" FALSE "));
        // A typo must not turn atomicity off by accident.
        assertTrue(joins("flase"));
        assertTrue(joins("no"));
        assertTrue(joins("true"));
    }

    private static boolean joins(String value) throws Exception {
        Hashtable<String, Object> env = new Hashtable<>();
        env.put(Context.PROVIDER_URL, PROVIDER);
        if (value != null) {
            env.put(HttpInitialContextFactory.PROP_AMBIENT_TRANSACTION, value);
        }
        HttpNamingContext context = (HttpNamingContext) new HttpInitialContextFactory().getInitialContext(env);
        try {
            return context.configuration().joinAmbientTransaction();
        } finally {
            context.close();
        }
    }
}
