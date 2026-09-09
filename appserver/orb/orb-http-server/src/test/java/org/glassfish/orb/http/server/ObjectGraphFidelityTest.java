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


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.List;

import org.glassfish.orb.http.client.ClientConfiguration;
import org.glassfish.orb.http.client.EjbLocator;
import org.glassfish.orb.http.client.HttpEjbClient;
import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Protocol;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a remote view has to be able to carry.
 *
 * <p>Remote EJB is defined as pass-by-value with RMI semantics, and these are
 * the cases that separate an implementation of that from an approximation of
 * it. Payara's HTTP transport documents that object graphs "must form trees
 * without cyclic references" because it binds through JSON-B; every test here
 * is something that constraint would fail, and every one of them appears in
 * real applications that were written against IIOP and are not going to be
 * rewritten.
 */
class ObjectGraphFidelityTest {

    // ---- the shapes ---------------------------------------------------------

    static final class Node implements Serializable {

        private static final long serialVersionUID = 1L;

        final String name;
        Node next;

        Node(String name) {
            this.name = name;
        }
    }

    /** Two fields pointing at one object: identity has to survive, not just equality. */
    static final class Shared implements Serializable {

        private static final long serialVersionUID = 1L;

        final Node left;
        final Node right;

        Shared(Node left, Node right) {
            this.left = left;
            this.right = right;
        }
    }

    static final class WithTransient implements Serializable {

        private static final long serialVersionUID = 1L;

        final String kept;
        transient String dropped;

        WithTransient(String kept, String dropped) {
            this.kept = kept;
            this.dropped = dropped;
        }
    }

    /** Exercises the writeObject/readObject hooks, which a binding cannot honour. */
    static final class CustomSerialized implements Serializable {

        private static final long serialVersionUID = 1L;

        int value;

        CustomSerialized(int value) {
            this.value = value;
        }

        private void writeObject(ObjectOutputStream out) throws IOException {
            out.defaultWriteObject();
            out.writeInt(value * 2);
        }

        private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            // Doubled on the way out, halved on the way in: if the hooks were
            // skipped the value would come back doubled.
            value = in.readInt() / 2;
        }
    }

    enum Grade implements Serializable {
        FIRST, SECOND
    }

    interface Echo {

        Node echoNode(Node node);

        Shared echoShared(Shared shared);

        WithTransient echoTransient(WithTransient value);

        CustomSerialized echoCustom(CustomSerialized value);

        Grade echoGrade(Grade grade);

        Object echoObject(Object value);

        int[] echoInts(int[] values);

        String nullable(String value);

        String describe(String text);

        String describe(int number);

        String describe(String text, int number);
    }

    static final class EchoBean implements Echo {

        @Override
        public Node echoNode(Node node) {
            return node;
        }

        @Override
        public Shared echoShared(Shared shared) {
            return shared;
        }

        @Override
        public WithTransient echoTransient(WithTransient value) {
            return value;
        }

        @Override
        public CustomSerialized echoCustom(CustomSerialized value) {
            return value;
        }

        @Override
        public Grade echoGrade(Grade grade) {
            return grade;
        }

        @Override
        public Object echoObject(Object value) {
            return value;
        }

        @Override
        public int[] echoInts(int[] values) {
            return values;
        }

        @Override
        public String nullable(String value) {
            return value == null ? "was null" : value;
        }

        @Override
        public String describe(String text) {
            return "String:" + text;
        }

        @Override
        public String describe(int number) {
            return "int:" + number;
        }

        @Override
        public String describe(String text, int number) {
            return "String,int:" + text + ',' + number;
        }
    }

    private Echo echo;

    @BeforeEach
    void setUp() {
        FakeContainer container = new FakeContainer();
        container.registerStateless("EchoBean", new EchoBean());
        Loopback loopback = new Loopback(new EjbDispatcher(container),
                new NamingDispatcher(new HttpRoundTripTest.FakeNaming()));
        HttpEjbClient client = new HttpEjbClient(
                ClientConfiguration.builder(URI.create("http://localhost:8080" + Protocol.CONTEXT_PATH)).build(),
                loopback, new JavaSerializationMarshaller(),
                JavaSerializationMarshaller.defaultFilter());
        echo = client.createProxy(Echo.class, new EjbLocator("myapp", "mymodule", null, "EchoBean"));
    }

    // ---- the cases ----------------------------------------------------------

    @Test
    @DisplayName("a cyclic graph survives - the case a JSON binding cannot express at all")
    void cyclesSurvive() {
        Node first = new Node("first");
        Node second = new Node("second");
        first.next = second;
        second.next = first;

        Node returned = echo.echoNode(first);

        assertEquals("first", returned.name);
        assertEquals("second", returned.next.name);
        assertSame(returned, returned.next.next, "the cycle must close on the same object");
    }

    @Test
    @DisplayName("a shared reference stays shared, rather than becoming two equal copies")
    void sharedReferencesKeepTheirIdentity() {
        Node shared = new Node("shared");
        Shared returned = echo.echoShared(new Shared(shared, shared));

        assertSame(returned.left, returned.right,
                "duplicating it would silently double any state the bean mutates through one of them");
    }

    @Test
    void transientFieldsAreNotTransmitted() {
        WithTransient returned = echo.echoTransient(new WithTransient("kept", "dropped"));

        assertEquals("kept", returned.kept);
        assertNull(returned.dropped, "transient means transient on the wire too");
    }

    @Test
    @DisplayName("writeObject and readObject are honoured")
    void customSerializationHooksRun() {
        assertEquals(21, echo.echoCustom(new CustomSerialized(21)).value);
    }

    @Test
    void enumsComeBackAsTheSameConstant() {
        assertSame(Grade.SECOND, echo.echoGrade(Grade.SECOND));
    }

    @Test
    @DisplayName("a declared Object parameter carries its runtime type")
    void polymorphicParametersKeepTheirType() {
        Object returned = echo.echoObject(new Node("polymorphic"));

        assertTrue(returned instanceof Node);
        assertEquals("polymorphic", ((Node) returned).name);
        assertEquals(List.of("a", "b"), echo.echoObject(List.of("a", "b")));
    }

    @Test
    void arraysAndPrimitivesRoundTrip() {
        int[] values = { 1, -2, Integer.MAX_VALUE, Integer.MIN_VALUE, 0 };
        int[] returned = echo.echoInts(values);

        assertArrayEquals(values, returned);
        assertNotSame(values, returned, "pass-by-value: the callee must not share the caller's array");
    }

    @Test
    void nullArgumentsAreNotConfusedWithAbsentOnes() {
        assertEquals("was null", echo.nullable(null));
        assertEquals("present", echo.nullable("present"));
    }

    @Test
    @DisplayName("overloads resolve to the right method, because the path carries the parameter types")
    void overloadedMethodsResolveCorrectly() {
        assertEquals("String:x", echo.describe("x"));
        assertEquals("int:7", echo.describe(7));
        assertEquals("String,int:x,7", echo.describe("x", 7));
    }
}
