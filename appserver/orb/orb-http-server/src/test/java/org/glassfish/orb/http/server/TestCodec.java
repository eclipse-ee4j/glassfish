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
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;

import org.glassfish.orb.http.protocol.JavaSerializationMarshaller;
import org.glassfish.orb.http.protocol.Marshaller;

/**
 * A second codec, present only so that "more than one" is a real situation
 * in these tests rather than a hypothetical one.
 * <p>
 * It encodes exactly like the built-in codec and only announces a different
 * token: the point under test is the negotiation, not the bytes. Its
 * priority is left at the floor so it does not displace {@code jser} as the
 * default anywhere else.
 */
public class TestCodec implements Marshaller {

    static final String CODEC = "testcodec";

    private final Marshaller delegate = new JavaSerializationMarshaller();

    @Override
    public String codec() {
        return CODEC;
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
