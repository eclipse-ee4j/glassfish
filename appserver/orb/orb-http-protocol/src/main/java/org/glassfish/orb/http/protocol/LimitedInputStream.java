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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Fails once more than a fixed number of bytes have been read.
 *
 * <p>Java serialization's filter is not consulted for a string or for block
 * data, so a body made of one huge string is bounded by nothing else; the
 * transport wraps every body it decodes in one of these.
 */
public final class LimitedInputStream extends FilterInputStream {

    private final long limit;
    private long read;

    /**
     * @param in the body
     * @param limit the most bytes that may be read from it
     */
    public LimitedInputStream(InputStream in, long limit) {
        super(in);
        this.limit = limit;
    }

    /**
     * @param body a request or response body
     * @return {@code body}, failing after {@link JavaSerializationMarshaller#maxBytes()}
     */
    public static InputStream bound(InputStream body) {
        return new LimitedInputStream(body, JavaSerializationMarshaller.maxBytes());
    }

    private void charge(long n) throws IOException {
        read += n;
        if (read > limit) {
            throw new ProtocolException("the body is longer than the limit of " + limit + " bytes");
        }
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b >= 0) {
            charge(1);
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            charge(n);
        }
        return n;
    }

    @Override
    public long skip(long n) throws IOException {
        long skipped = super.skip(n);
        if (skipped > 0) {
            charge(skipped);
        }
        return skipped;
    }
}
