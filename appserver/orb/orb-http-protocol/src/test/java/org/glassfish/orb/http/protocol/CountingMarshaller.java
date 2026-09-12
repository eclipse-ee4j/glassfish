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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputFilter;
import java.io.OutputStream;

/**
 * A codec that exists only to be discovered.
 * <p>
 * It is never named by the tests that assert discovery: it is reachable
 * purely through {@code META-INF/services}, which is the whole point.
 */
public class CountingMarshaller implements Marshaller {

    static final String CODEC = "counting";

    @Override
    public String codec() {
        return CODEC;
    }

    @Override
    public int priority() {
        // Above the built-in floor, so preferred() must choose this one.
        return 50;
    }

    @Override
    public ObjectWriter newWriter(OutputStream out) throws IOException {
        return new JavaSerializationMarshaller().newWriter(out);
    }

    @Override
    public ObjectReader newReader(InputStream in, ClassLoader loader, ObjectInputFilter filter)
            throws IOException {
        return new JavaSerializationMarshaller().newReader(in, loader, filter);
    }
}
