/*
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.webui.jsf.util;

import java.io.IOException;
import java.io.Writer;

/**
 * This is based on {@link java.io.StringWriter} but backed by a
 * {@link StringBuilder} instead.
 * This class is not thread safe.
 */
public final class FastStringWriter extends Writer {

    /**
     * Underlying string builder.
     */
    private StringBuilder builder;

    /**
     * Constructs a new {@code FastStringWriter} instance
     * using the default capacity of {@code 16}.
     */
    public FastStringWriter() {
        builder = new StringBuilder();
    }

    /**
     * Constructs a new {@code FastStringWriter} instance
     * using the specified {@code initialCapacity}.
     *
     * @param initialCapacity specifies the initial capacity of the buffer
     * @throws IllegalArgumentException if initialCapacity is less than zero
     */
    public FastStringWriter(final int initialCapacity)
        throws IllegalArgumentException {

        if (initialCapacity < 0) {
            throw new IllegalArgumentException();
        }
        builder = new StringBuilder(initialCapacity);
    }

    @Override
    public void write(final char[] cbuf, final int off, final int len)
            throws IOException {

        if ((off < 0) || (off > cbuf.length) || (len < 0)
                || ((off + len) > cbuf.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        builder.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }


    @Override
    public void write(final String str) {
        write(str, 0, str.length());
    }

    @Override
    public void write(final String str, final int off, final int len) {
        builder.append(str.substring(off, off + len));
    }

    /**
     * Get the {@code StringBuilder} itself.
     * @return StringBuilder holding the current buffer value.
     */
    public StringBuilder getBuffer() {
        return builder;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
