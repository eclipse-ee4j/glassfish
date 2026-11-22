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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;
import java.net.URI;
import java.util.Hashtable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.naming.Context;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.protocol.InvocationEnvelope;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.TxContext;
import org.glassfish.orb.http.protocol.Xids;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        Future<String> pingLater();
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
    @DisplayName("guard: by default a call made inside a container transaction joins it")
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

    private TxContext sentTransaction(int call) throws Exception {
        return InvocationEnvelope.readTxContext(new ByteArrayInputStream(transport.requestBodies.get(call)));
    }

    private void leaveAnAmbientBranchOnTheThread() throws Exception {
        transaction.timeout = 30;
        ping(ClientConfiguration.builder(URI.create(PROVIDER)).build());
        assertNotNull(ClientTransactionContext.current(), "precondition: the joining call left its branch");
        assertTrue(sentTransaction(0).isPresent(), "guard: the joining call carries its branch");
        assertNotNull(transport.requestHeaders.get(0).get(Protocol.H_TXN_TIMEOUT), "guard: and its timeout");
    }

    @Test
    @DisplayName("with joining switched off a branch left on the thread by a joining call is not sent")
    void switchedOffDoesNotSendABranchLeftOnTheThread() throws Exception {
        leaveAnAmbientBranchOnTheThread();

        ping(ClientConfiguration.builder(URI.create(PROVIDER)).joinAmbientTransaction(false).build());

        assertEquals(2, transport.calls.size());
        assertFalse(sentTransaction(1).isPresent(), "the call carries a transaction it was told not to join");
        assertNull(transport.requestHeaders.get(1).get(Protocol.H_TXN_TIMEOUT), "and its timeout");
    }

    @Test
    @DisplayName("the same for an asynchronous call")
    void switchedOffDoesNotSendABranchLeftOnTheThreadAsync() throws Exception {
        leaveAnAmbientBranchOnTheThread();

        HttpEjbClient client = new HttpEjbClient(
                ClientConfiguration.builder(URI.create(PROVIDER)).joinAmbientTransaction(false).build(),
                transport, new JavaSerializationMarshaller(), JavaSerializationMarshaller.defaultFilter());
        client.createProxy(Pinger.class, new EjbLocator("app", "module", null, "PingerBean")).pingLater();

        assertEquals(2, transport.calls.size());
        assertFalse(sentTransaction(1).isPresent(), "the call carries a transaction it was told not to join");
        assertNull(transport.requestHeaders.get(1).get(Protocol.H_TXN_TIMEOUT), "and its timeout");
    }

    /** A branch begun the way {@code HttpUserTransaction} does: not through a foreign manager, so not ambient. */
    private static Xid userTransactionBranch() {
        return new Xids.SimpleXid(0x4A5441, new byte[] { 7, 7 }, new byte[] { 8 });
    }

    @Test
    @DisplayName("with joining switched off a branch begun through HttpUserTransaction is still sent")
    void switchedOffStillSendsAUserTransactionBranch() throws Exception {
        ClientTransactionContext.associate(userTransactionBranch(), 30);

        ping(ClientConfiguration.builder(URI.create(PROVIDER)).joinAmbientTransaction(false).build());

        assertEquals(1, transport.calls.size());
        assertTrue(sentTransaction(0).isPresent(), "the client's own transaction was dropped");
        assertNotNull(transport.requestHeaders.get(0).get(Protocol.H_TXN_TIMEOUT), "and its timeout");
    }

    /** Announces a codec the server will refuse, over the built-in encoding. */
    private static final class ExoticMarshaller implements Marshaller {
        private final Marshaller delegate = new JavaSerializationMarshaller();

        @Override
        public String codec() {
            return "exotic";
        }

        @Override
        public ObjectWriter newWriter(OutputStream out) throws IOException {
            return delegate.newWriter(out);
        }

        @Override
        public ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter)
                throws IOException {
            return delegate.newReader(in, loader, filter);
        }
    }

    @Test
    @DisplayName("the retry of an asynchronous call after a codec downgrade carries the first attempt's transaction")
    void theAsynchronousRetryCarriesTheTransactionOfTheFirstAttempt() throws Exception {
        ClientTransactionContext.associate(userTransactionBranch(), 30);
        transport.refuseUnsupportedCodec = 1;
        transport.asyncOnAnotherThread = true;
        HttpEjbClient client = new HttpEjbClient(
                ClientConfiguration.builder(URI.create(PROVIDER)).joinAmbientTransaction(false).build(),
                transport, new ExoticMarshaller(), JavaSerializationMarshaller.defaultFilter());

        client.invokeAsync(new EjbLocator("app", "module", null, "PingerBean"), Pinger.class,
                Pinger.class.getMethod("ping"), new Object[0]).handle((value, error) -> null)
                .get(30, TimeUnit.SECONDS);

        assertEquals(2, transport.calls.size(), "expected the refused attempt and one retry");
        assertTrue(sentTransaction(0).isPresent(), "guard: the first attempt carries the transaction");
        assertTrue(sentTransaction(1).isPresent(), "the retry was sent without the transaction");
        assertNotNull(transport.requestHeaders.get(1).get(Protocol.H_TXN_TIMEOUT), "and without its timeout");
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

    @Test
    @DisplayName("the property can also be a system property, and the JNDI environment wins over it")
    void thePropertyIsAlsoASystemProperty() throws Exception {
        System.setProperty(HttpInitialContextFactory.PROP_AMBIENT_TRANSACTION, "false");
        try {
            assertFalse(joins(null));
            assertTrue(joins("true"), "the environment must win");
        } finally {
            System.clearProperty(HttpInitialContextFactory.PROP_AMBIENT_TRANSACTION);
        }
        assertTrue(joins(null));
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
