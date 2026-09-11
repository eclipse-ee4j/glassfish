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

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MarshallersTest {

    @Test
    @DisplayName("a codec on the class path is used without anyone naming it")
    void aProviderOnTheClassPathIsPreferred() {
        // Nothing here mentions CountingMarshaller by type. It is on the class
        // path with a service file, and that alone changes what is used - the
        // property the whole feature rests on.
        assertEquals(CountingMarshaller.CODEC, Marshallers.preferred().codec());
    }

    @Test
    @DisplayName("the built-in codec is always available as a floor")
    void theBuiltInCodecIsAlwaysPresent() {
        assertTrue(Marshallers.find(ContentType.CODEC_JSER).isPresent());
        assertEquals(ContentType.CODEC_JSER,
                Marshallers.find(ContentType.CODEC_JSER).orElseThrow().codec());
    }

    @Test
    @DisplayName("a codec this JVM does not have is reported as absent, not guessed")
    void anUnknownCodecIsAbsent() {
        assertTrue(Marshallers.find("no-such-codec").isEmpty());
    }

    @Test
    @DisplayName("codecs are ranked, best first, and the ranking is stable")
    void codecsAreRankedBestFirst() {
        List<String> codecs = Marshallers.codecs();
        assertEquals(CountingMarshaller.CODEC, codecs.get(0));
        assertTrue(codecs.contains(ContentType.CODEC_JSER));
        // Discovery is cached, so a second call must not reshuffle.
        assertEquals(codecs, Marshallers.codecs());
    }

    @Test
    @DisplayName("a codec handed to us is available even though nothing could find it")
    void aRegisteredCodecBecomesAvailable() {
        // Stands in for a provider in another OSGi bundle: reachable by the
        // container, invisible to a ServiceLoader walking a class path.
        Marshaller handed = new Marshaller() {

            @Override
            public String codec() {
                return "handed-in";
            }

            @Override
            public ObjectWriter newWriter(java.io.OutputStream out) throws java.io.IOException {
                return new JavaSerializationMarshaller().newWriter(out);
            }

            @Override
            public ObjectReader newReader(java.io.InputStream in, ClassLoader loader,
                    java.io.ObjectInputFilter filter) throws java.io.IOException {
                return new JavaSerializationMarshaller().newReader(in, loader, filter);
            }
        };

        assertTrue(Marshallers.find("handed-in").isEmpty());
        Marshallers.register(handed);
        assertTrue(Marshallers.find("handed-in").isPresent());

        // Registering the same token again must not change the answer.
        Marshallers.register(handed);
        assertSame(handed, Marshallers.find("handed-in").orElseThrow());
    }

    @Test
    @DisplayName("discovery is cached rather than repeated per invocation")
    void discoveryIsCached() {
        assertSame(Marshallers.preferred(), Marshallers.preferred());
    }
}
