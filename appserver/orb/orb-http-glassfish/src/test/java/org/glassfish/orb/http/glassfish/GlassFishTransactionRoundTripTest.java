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

package org.glassfish.orb.http.glassfish;

import jakarta.transaction.SystemException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.ClientTransactionContext;
import org.glassfish.orb.http.client.HttpTransport;
import org.glassfish.orb.http.client.HttpUserTransaction;
import org.glassfish.orb.http.client.HttpXAResource;
import org.glassfish.orb.http.protocol.Protocol;
import org.glassfish.orb.http.protocol.Xids;
import org.glassfish.orb.http.server.ServerExchange;
import org.glassfish.orb.http.server.TransactionDispatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The client, the wire format, the transaction dispatch and the real
 * {@link GlassFishTransactionBridge}, with only the socket and the transaction
 * manager replaced.
 * <p>
 * The point is the branch's format id. On the XA routes the xid is minted by
 * the caller's own transaction manager, so the bridge sees whatever format
 * that manager uses; a bridge that only drove xids of its own format would
 * answer every legitimate two-phase commit with XAER_NOTA.
 */
class GlassFishTransactionRoundTripTest {

    private static final ClientConfiguration CONFIG = ClientConfiguration
            .builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH))
            .build();

    /** The format GlassFish's own JavaEEXid uses, which is what an enlisted HttpXAResource gets. */
    private static final Xid CLIENT = new Xids.SimpleXid(987654321, new byte[] { 7, 7, 7, 7 }, new byte[] { 1 });

    private final List<String> terminatorCalls = new ArrayList<>();
    private final List<String> managerCalls = new ArrayList<>();
    private final GlassFishTransactionBridge bridge = new GlassFishTransactionBridge(
            TerminatorRecorder.manager(terminatorCalls, managerCalls));
    private final HttpTransport transport = new DispatchingTransport(new TransactionDispatcher(bridge));

    @AfterEach
    void tearDown() {
        ClientTransactionContext.disassociate();
    }

    @Test
    @DisplayName("an enlisted HttpXAResource with a client-minted xid: invoke, prepare, commit succeed")
    void ambientStyleTwoPhaseCommit() throws Exception {
        HttpXAResource resource = new HttpXAResource(CONFIG, transport);

        // The invocation: the server imports the branch around the bean call.
        resource.start(CLIENT, XAResource.TMNOFLAGS);
        bridge.recreate(CLIENT, 30);
        bridge.release(CLIENT);
        resource.end(CLIENT, XAResource.TMSUCCESS);
        terminatorCalls.clear();

        assertEquals(XAResource.XA_OK, resource.prepare(CLIENT));
        resource.commit(CLIENT, false);

        assertEquals(List.of("prepare", "commit"), terminatorCalls);
    }

    @Test
    @DisplayName("the same branch is also rolled back through the XA route")
    void ambientStyleRollback() throws Exception {
        HttpXAResource resource = new HttpXAResource(CONFIG, transport);
        bridge.recreate(CLIENT, 30);
        bridge.release(CLIENT);
        terminatorCalls.clear();

        resource.rollback(CLIENT);

        assertEquals(List.of("rollback"), terminatorCalls);
    }

    @Test
    @DisplayName("positive control: a user transaction begun on this server is committed")
    void userTransactionCommit() throws Exception {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, transport);

        transaction.begin();
        transaction.commit();

        assertEquals(List.of("prepare", "commit"), terminatorCalls);
    }

    @Test
    @DisplayName("a user transaction end for a client-minted branch is refused: it must not one-phase complete it")
    void userTransactionCommitRefusesAClientMintedXid() {
        HttpUserTransaction transaction = new HttpUserTransaction(CONFIG, transport);
        // The caller's own coordinator owns this branch; a UT end must not complete it.
        ClientTransactionContext.associate(CLIENT, 30);

        SystemException thrown = assertThrows(SystemException.class, transaction::commit);

        XAException cause = assertInstanceOf(XAException.class, thrown.getCause());
        assertEquals(XAException.XAER_NOTA, cause.errorCode);
        assertTrue(terminatorCalls.isEmpty(), "terminator was called: " + terminatorCalls);
        assertTrue(managerCalls.isEmpty(), "manager was called: " + managerCalls);
    }

    /** Hands each request to the dispatcher through an in-memory exchange. */
    private record DispatchingTransport(TransactionDispatcher dispatcher) implements HttpTransport {

        @Override
        public Response exchange(Request request) throws IOException {
            Exchange exchange = new Exchange(request);
            dispatcher.dispatch(exchange);
            return new Response(exchange.status, null, exchange.headers,
                    new ByteArrayInputStream(exchange.body.toByteArray()));
        }

        @Override
        public CompletableFuture<Response> exchangeAsync(Request request) {
            try {
                return CompletableFuture.completedFuture(exchange(request));
            } catch (IOException e) {
                return CompletableFuture.failedFuture(e);
            }
        }

        @Override
        public String negotiatedVersion() {
            return "in-memory";
        }

        @Override
        public void close() {
        }
    }

    private static final class Exchange implements ServerExchange {

        private final String method;
        private final byte[] path;
        private final Map<String, String> query = new HashMap<>();

        int status = 200;
        final Map<String, List<String>> headers = new HashMap<>();
        final ByteArrayOutputStream body = new ByteArrayOutputStream();

        Exchange(HttpTransport.Request request) {
            this.method = request.method();
            this.path = request.uri().getRawPath().getBytes(StandardCharsets.UTF_8);
            String rawQuery = request.uri().getRawQuery();
            if (rawQuery != null) {
                for (String pair : rawQuery.split("&")) {
                    int eq = pair.indexOf('=');
                    if (eq > 0) {
                        query.put(pair.substring(0, eq),
                                URLDecoder.decode(pair.substring(eq + 1), StandardCharsets.UTF_8));
                    }
                }
            }
        }

        @Override
        public String method() {
            return method;
        }

        @Override
        public byte[] pathBytes() {
            return path;
        }

        @Override
        public int pathOffset() {
            return 0;
        }

        @Override
        public int pathLength() {
            return path.length;
        }

        @Override
        public String requestHeader(String name) {
            return null;
        }

        @Override
        public String queryParameter(String name) {
            return query.get(name);
        }

        @Override
        public InputStream requestBody() {
            return new ByteArrayInputStream(new byte[0]);
        }

        @Override
        public void setStatus(int status) {
            this.status = status;
        }

        @Override
        public void setResponseHeader(String name, String value) {
            headers.computeIfAbsent(name, k -> new ArrayList<>()).add(value);
        }

        @Override
        public void writeBody(ByteBuffer[] buffers) {
            for (ByteBuffer b : buffers) {
                byte[] bytes = new byte[b.remaining()];
                b.duplicate().get(bytes);
                body.writeBytes(bytes);
            }
        }

        @Override
        public String authenticatedUser() {
            return null;
        }
    }
}
