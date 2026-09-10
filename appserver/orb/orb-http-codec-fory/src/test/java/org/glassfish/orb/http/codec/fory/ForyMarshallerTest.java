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


package org.glassfish.orb.http.codec.fory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.orb.http.protocol.Marshaller;
import org.glassfish.orb.http.protocol.Marshallers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ForyMarshallerTest {

    private final Marshaller marshaller = new ForyMarshaller();

    private static final ObjectInputFilter ALLOW_ALL = info -> ObjectInputFilter.Status.ALLOWED;

    static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        Node peer;
        transient String secret;

        Node(String name) {
            this.name = name;
        }
    }

    static class Holder implements Serializable {
        private static final long serialVersionUID = 1L;
        Node left;
        Node right;
        List<Node> all = new ArrayList<>();
    }

    private Object roundTrip(Object value) throws Exception {
        return roundTrip(value, ALLOW_ALL);
    }

    private Object roundTrip(Object value, ObjectInputFilter filter) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), filter)) {
            return reader.readObject();
        }
    }

    @Test
    @DisplayName("the codec is chosen simply by being on the class path")
    void theCodecIsDiscovered() {
        // The application names nothing. This module is present, so this is
        // the codec that gets used.
        assertEquals(ForyMarshaller.CODEC, Marshallers.preferred().codec());
        assertTrue(Marshallers.find(ForyMarshaller.CODEC).isPresent());
    }

    @Test
    @DisplayName("a cycle closes on the same object, as it does under Java serialization")
    void aCycleIsPreserved() throws Exception {
        Node first = new Node("first");
        Node second = new Node("second");
        first.peer = second;
        second.peer = first;

        Node decoded = (Node) roundTrip(first);
        assertEquals("first", decoded.name);
        assertEquals("second", decoded.peer.name);
        // Without reference tracking this either recurses forever or arrives
        // as an ever deeper chain of copies.
        assertSame(decoded, decoded.peer.peer);
    }

    @Test
    @DisplayName("a shared reference keeps its identity rather than arriving duplicated")
    void sharedReferencesKeepIdentity() throws Exception {
        Node shared = new Node("shared");
        Holder holder = new Holder();
        holder.left = shared;
        holder.right = shared;
        holder.all.add(shared);

        Holder decoded = (Holder) roundTrip(holder);
        assertSame(decoded.left, decoded.right);
        assertSame(decoded.left, decoded.all.get(0));
    }

    @Test
    @DisplayName("transient fields are not transmitted")
    void transientFieldsAreNotTransmitted() throws Exception {
        Node node = new Node("visible");
        node.secret = "must not travel";

        Node decoded = (Node) roundTrip(node);
        assertEquals("visible", decoded.name);
        assertNull(decoded.secret);
    }

    @Test
    @DisplayName("several objects written in sequence read back in the same order")
    void framingKeepsObjectsSeparate() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject("first");
            writer.writeObject(42);
            writer.writeObject(new Node("third"));
            writer.flush();
        }

        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), ALLOW_ALL)) {
            assertEquals("first", reader.readObject());
            assertEquals(42, reader.readObject());
            assertEquals("third", ((Node) reader.readObject()).name);
        }
    }

    @Test
    @DisplayName("the deserialization filter governs this codec too")
    void theFilterIsEnforced() throws Exception {
        Node node = new Node("rejected");
        ObjectInputFilter refuseNode = info -> info.serialClass() == Node.class
                ? ObjectInputFilter.Status.REJECTED
                : ObjectInputFilter.Status.ALLOWED;

        // Changing codec must not widen what an attacker can instantiate.
        assertThrows(Exception.class, () -> roundTrip(node, refuseNode));
    }

    @Test
    @DisplayName("a reader without a filter is refused rather than left unguarded")
    void aMissingFilterIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> marshaller.newReader(new ByteArrayInputStream(new byte[0]),
                        getClass().getClassLoader(), null));
    }

    @Test
    @DisplayName("a truncated frame is reported, not silently half decoded")
    void aTruncatedFrameIsReported() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(new Node("whole"));
            writer.flush();
        }
        byte[] full = out.toByteArray();
        byte[] cut = new byte[full.length - 3];
        System.arraycopy(full, 0, cut, 0, cut.length);

        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(cut), getClass().getClassLoader(), ALLOW_ALL)) {
            assertThrows(Exception.class, reader::readObject);
        }
    }

    @Test
    void theCodecTokenIsStable() {
        assertEquals("fory", marshaller.codec());
        assertNotNull(Marshallers.find("fory").orElse(null));
    }
}
