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

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Why code generation is off by default, kept as a test rather than a comment.
 * <p>
 * Fory compiles a serializer per type and defines it in the class loader of
 * the type itself - the "neighbour" - and that generated class references
 * Fory's own runtime. On a class path one loader sees both and it works, which
 * is why every other test here passes with generation either way.
 * <p>
 * A container is not a class path. The type being encoded belongs to the
 * application or to the protocol bundle; Fory is embedded in this one; and a
 * loader that owns the type but cannot see Fory cannot link what was defined
 * in it. Generation then fails with "Create sequential serializer failed",
 * naming the type rather than the visibility behind it.
 * <p>
 * Three arrangements were measured. With the type's loader unable to see Fory,
 * generation fails. Bridging the loader handed to Fory does not help, because
 * the loader that matters is the type's own and Fory picks that one itself.
 * Only making the type's loader see Fory works - which is possible for this
 * transport's own types and not for an application's, since an application
 * bundle cannot be made to import a codec it never asked for.
 * <p>
 * So the reflective path is the default. This test pins what that path must
 * keep doing; the constraint above is recorded in the class documentation of
 * {@link ForyMarshaller}.
 */
class CodegenIsolationTest {

    record Point(int x, int y) implements Serializable {
    }

    private Object roundTrip(Object value) throws Exception {
        Marshaller marshaller = new ForyMarshaller();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (Marshaller.ObjectWriter writer = marshaller.newWriter(out)) {
            writer.writeObject(value);
            writer.flush();
        }
        ObjectInputFilter filter = JavaSerializationMarshaller.defaultFilter();
        try (Marshaller.ObjectReader reader = marshaller.newReader(
                new ByteArrayInputStream(out.toByteArray()), getClass().getClassLoader(), filter)) {
            return reader.readObject();
        }
    }

    @Test
    @DisplayName("a record round trips on the reflective path, which is the one that runs")
    void aRecordRoundTripsWithoutCodeGeneration() throws Exception {
        // A record is the shape that first exposed this: final fields, no
        // no-argument constructor, and the type the naming reply carries.
        assertEquals(new Point(3, 4), roundTrip(new Point(3, 4)));
    }

    @Test
    @DisplayName("the default is the path that works everywhere, not the fastest one")
    void generationIsOffUnlessAskedFor() {
        // Deliberate: a codec that works in tests and fails in the container
        // is worse than one that is merely slower than it could be.
        assertEquals(Boolean.getBoolean("org.glassfish.orb.http.codec.fory.codegen"),
                ForyMarshaller.codeGenerationEnabled());
    }
}
