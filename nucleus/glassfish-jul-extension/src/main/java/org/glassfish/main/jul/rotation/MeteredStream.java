/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.rotation;

import java.io.IOException;
import java.io.OutputStream;


/**
 * A metered stream is a subclass of OutputStream that
 * <ul>
 * <li>forwards all its output to a target stream
 * <li>keeps track of how many bytes have been written
 * <li>after this stream is closed the behavior remains on the target stream.
 * <li>further {@link #close()} calls don't do anything.
 * </ul>
 */
// Note: The original class is in java.util.logging.FileHandler,
//       but was copyied into GlassFish by the Sun and was modified.
final class MeteredStream extends OutputStream {

    private final OutputStream out;
    private long written;
    private volatile boolean isOpen;

    /**
     * @param out wrapped targed output stream
     * @param written initial value, usually size of the output file which already exists.
     */
    public MeteredStream(final OutputStream out, final long written) {
        this.out = out;
        this.written = written;
        this.isOpen = true;
    }


    /**
     * @return count of bytes written by this stream instance plus number given in constructor
     */
    public long getBytesWritten() {
        return this.written;
    }


    @Override
    public void write(int b) throws IOException {
        out.write(b);
        written++;
    }


    @Override
    public void write(byte[] buff) throws IOException {
        out.write(buff);
        written += buff.length;
    }


    @Override
    public void write(byte[] buff, int off, int len) throws IOException {
        out.write(buff, off, len);
        written += len;
    }


    @Override
    public void flush() throws IOException {
        out.flush();
    }


    @Override
    public void close() throws IOException {
        if (isOpen) {
            isOpen = false;
            flush();
            out.close();
        }
    }
}
