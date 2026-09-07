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

package org.glassfish.orb.http.protocol;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutesAndContentTypeTest {

    @Test
    void contentTypeRoundTrips() throws Exception {
        ContentType type = ContentType.of(ContentType.CODEC_JSER, ContentType.KIND_INVOCATION);
        assertEquals("application/x-gf-jser-invocation; version=1", type.toHeaderValue());

        ContentType parsed = ContentType.parse(type.toHeaderValue());
        assertEquals(type, parsed);
        assertTrue(parsed.isVersionSupported());
    }

    @Test
    @DisplayName("an unknown version parses but is reported unsupported, so the server can answer 406")
    void parsesAnUnsupportedVersion() throws Exception {
        ContentType parsed = ContentType.parse("application/x-gf-jbmar-response; charset=utf-8; version=7");
        assertEquals("jbmar", parsed.codec());
        assertEquals("response", parsed.kind());
        assertEquals(7, parsed.version());
        assertFalse(parsed.isVersionSupported());
    }

    @Test
    void rejectsForeignContentTypes() {
        assertThrows(ProtocolException.class, () -> ContentType.parse("application/json"));
        assertThrows(ProtocolException.class, () -> ContentType.parse(null));
        assertThrows(ProtocolException.class, () -> ContentType.parse("application/x-gf-nodash"));
        assertThrows(ProtocolException.class, () -> ContentType.parse("application/x-gf-a-b; version=x"));
    }

    @Test
    void invokePathRoundTrips() throws Exception {
        String path = EjbRoutes.invokePath(Protocol.CONTEXT_PATH,
                "myapp", "mymodule", null, "GreeterBean",
                EjbRoutes.encodeSessionId(null),
                "com.acme.Greeter", "greet",
                new String[] { "java.lang.String", "int" });

        assertEquals("/glassfish-services/ejb/v1/invoke/myapp/mymodule/-/GreeterBean/-/"
                + "com.acme.Greeter/greet/java.lang.String,int", path);

        EjbRoutes.Invocation parsed = EjbRoutes.parseInvocation(PathScanner.scan(path));
        assertEquals("myapp", parsed.appName());
        assertEquals("mymodule", parsed.moduleName());
        assertNull(parsed.distinctName());
        assertEquals("GreeterBean", parsed.beanName());
        assertNull(parsed.sessionId());
        assertEquals("com.acme.Greeter", parsed.viewClass());
        assertEquals("greet", parsed.methodName());
        assertArrayEquals(new String[] { "java.lang.String", "int" }, parsed.paramTypes());
    }

    @Test
    @DisplayName("a no-argument method and an array parameter both survive the path")
    void encodesAwkwardParameterTypes() throws Exception {
        String noArgs = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "a", "m", null, "B",
                EjbRoutes.ABSENT, "com.acme.V", "ping", new String[0]);
        assertTrue(noArgs.endsWith("/ping/-"));
        assertEquals(0, EjbRoutes.parseInvocation(PathScanner.scan(noArgs)).paramTypes().length);

        String arrays = EjbRoutes.invokePath(Protocol.CONTEXT_PATH, "a", "m", null, "B",
                EjbRoutes.ABSENT, "com.acme.V", "sum", new String[] { "[Ljava.lang.String;", "[[I" });
        EjbRoutes.Invocation parsed = EjbRoutes.parseInvocation(PathScanner.scan(arrays));
        assertArrayEquals(new String[] { "[Ljava.lang.String;", "[[I" }, parsed.paramTypes());
    }

    @Test
    void sessionIdRoundTrips() throws Exception {
        byte[] session = { 9, 8, 7, 6, 5 };
        String encoded = EjbRoutes.encodeSessionId(session);
        assertArrayEquals(session, EjbRoutes.decodeSessionId(encoded));
        assertNull(EjbRoutes.decodeSessionId(EjbRoutes.ABSENT));
        assertNull(EjbRoutes.decodeSessionId(null));
        assertEquals(EjbRoutes.ABSENT, EjbRoutes.encodeSessionId(null));
    }

    @Test
    @DisplayName("a JNDI name keeps its slashes as real path separators")
    void namingPathKeepsSlashes() throws Exception {
        String path = NamingRoutes.path(Protocol.CONTEXT_PATH, Protocol.OP_LOOKUP,
                "java:global/myapp/GreeterBean");
        assertEquals("/glassfish-services/naming/v1/lookup/java:global/myapp/GreeterBean", path);
        assertFalse(path.contains("%2F"), "encoding slashes would trip proxies and servlet containers");

        NamingRoutes.Request parsed = NamingRoutes.parse(PathScanner.scan(path));
        assertEquals(Protocol.OP_LOOKUP, parsed.operation());
        assertEquals("java:global/myapp/GreeterBean", parsed.jndiName());
    }

    @Test
    void rootContextIsAddressable() throws Exception {
        String path = NamingRoutes.path(Protocol.CONTEXT_PATH, Protocol.OP_LIST, "");
        NamingRoutes.Request parsed = NamingRoutes.parse(PathScanner.scan(path));
        assertEquals("", parsed.jndiName());
    }

    @Test
    void namingMethodsFollowHttpSemantics() {
        assertEquals("POST", NamingRoutes.methodFor(Protocol.OP_LOOKUP));
        assertEquals("GET", NamingRoutes.methodFor(Protocol.OP_LIST));
        assertEquals("PUT", NamingRoutes.methodFor(Protocol.OP_BIND));
        assertEquals("PATCH", NamingRoutes.methodFor(Protocol.OP_REBIND));
        assertEquals("DELETE", NamingRoutes.methodFor(Protocol.OP_UNBIND));
        assertThrows(IllegalArgumentException.class, () -> NamingRoutes.methodFor("frobnicate"));
    }

    @Test
    void transactionContextIsReservedOnTheWire() throws Exception {
        ChunkedOutput out = new ChunkedOutput();
        InvocationEnvelope.writeTxContext(out, TxContext.NONE);
        assertEquals(1, out.size(), "an absent transaction must cost exactly one byte");

        ChunkedInput in = new ChunkedInput(out.toByteBuffers());
        assertEquals(TxContext.NONE, InvocationEnvelope.readTxContext(in));

        TxContext present = new TxContext(TxContext.TYPE_OUTFLOWED, 131077,
                new byte[] { 1, 2, 3 }, new byte[] { 4, 5 });
        ChunkedOutput out2 = new ChunkedOutput();
        InvocationEnvelope.writeTxContext(out2, present);
        assertEquals(present, InvocationEnvelope.readTxContext(new ChunkedInput(out2.toByteBuffers())));
    }

    @Test
    @DisplayName("a hostile length prefix in the transaction header is refused")
    void rejectsImplausibleXidLengths() throws Exception {
        ChunkedOutput out = new ChunkedOutput();
        java.io.DataOutputStream data = new java.io.DataOutputStream(out);
        data.writeByte(TxContext.TYPE_REMOTE);
        data.writeInt(0);
        data.writeInt(Integer.MAX_VALUE);
        data.flush();

        assertThrows(ProtocolException.class,
                () -> InvocationEnvelope.readTxContext(new ChunkedInput(out.toByteBuffers())));
    }
}
