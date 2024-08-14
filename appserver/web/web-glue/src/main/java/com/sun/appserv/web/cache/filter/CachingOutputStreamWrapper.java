/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.web.cache.filter;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * an output stream wrapper to cache response bytes
 */
public class CachingOutputStreamWrapper extends ServletOutputStream {

    ByteArrayOutputStream baos;

    public CachingOutputStreamWrapper() {
        this.baos = new ByteArrayOutputStream(4096);
    }

    /**
     * Write the specified byte to our output stream.
     *
     * @param b The byte to be written
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void write(int b) throws IOException {
        baos.write(b);
    }

    /**
     * Write <code>b.length</code> bytes from the specified byte array
     * to our output stream.
     *
     * @param b The byte array to be written
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void write(byte b[]) throws IOException {
        baos.write(b, 0, b.length);
    }

    /**
     * Write <code>len</code> bytes from the specified byte array, starting
     * at the specified offset, to our output stream.
     *
     * @param b The byte array containing the bytes to be written
     * @param off Zero-relative starting offset of the bytes to be written
     * @param len The number of bytes to be written
     *
     * @exception IOException if an input/output error occurs
     */
    @Override
    public void write(byte b[], int off, int len) throws IOException {
        baos.write(b, off, len);
    }

    /**                                                                    `
     * Flush any buffered data for this output stream, which also causes the
     * response to be committed.
     */
    @Override
    public void flush() throws IOException {
        // nothing to do with cached bytes
    }

    /**
     * Close this output stream, causing any buffered data to be flushed and
     * any further output data to throw an IOException.
     */
    @Override
    public void close() throws IOException {
        // nothing to do with cached bytes
    }

    /**
     * This method can be used to determine if data can be written without blocking.
     * @return true if a write to this ServletOutputStream will succeed, otherwise returns false.
     */
    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new IllegalStateException();
    }

    /**
     * return the cached bytes
     */
    public byte[] getBytes() {
        return baos.toByteArray();
    }
}
