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





import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;

/**
 * Pluggable object codec.
 * <p>
 * The codec token appears in every content type (see {@link ContentType}), so
 * one endpoint can serve several codecs and a client can ask for one it
 * understands. That indirection exists for a specific future: adding a JBoss
 * Marshalling ({@code jbmar}) implementation here is what would make this
 * protocol genuinely interoperable with WildFly clients, since the paths,
 * methods and header semantics already match their wire specification v1.
 */
public interface Marshaller {

    /** The codec token used in content types, e.g. {@code jser}. */
    String codec();

    /**
     * Ranks this codec against the others found on the class path.
     * <p>
     * {@link Marshallers} picks the highest ranked provider, so dropping a
     * faster codec onto the class path changes what an application uses
     * without the application naming it. The built-in Java serialization
     * codec ranks {@code 0} and is the floor: it is always present, so
     * there is always something to fall back to when a peer speaks nothing
     * else.
     *
     * @return the rank; higher wins
     */
    default int priority() {
        return 0;
    }

    ObjectWriter newWriter(OutputStream out) throws IOException;

    /**
     * @param loader the class loader used to resolve incoming classes -
     *               for a server dispatch this must be the application's
     *               loader, not the container's
     * @param filter deserialization filter; must not be {@code null}
     */
    ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter) throws IOException;

    interface ObjectWriter extends Closeable {
        void writeObject(Object o) throws IOException;

        void flush() throws IOException;
    }

    interface ObjectReader extends Closeable {
        Object readObject() throws IOException, ClassNotFoundException;
    }
}
