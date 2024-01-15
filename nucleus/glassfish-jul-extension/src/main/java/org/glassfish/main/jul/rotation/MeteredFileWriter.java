/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
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

import java.io.OutputStreamWriter;
import java.nio.charset.Charset;


/**
 * {@link OutputStreamWriter} knowing how much bytes was already written to the output stream.
 */
class MeteredFileWriter extends OutputStreamWriter {

    private final MeteredStream output;
    private final Charset encoding;

    /**
     * Creates the writer.
     *
     * @param output stream
     * @param encoding {@link Charset} used for encoding.
     */
    public MeteredFileWriter(final MeteredStream output, final Charset encoding) {
        super(output, encoding);
        this.output = output;
        this.encoding = encoding;
    }


    @Override
    public String getEncoding() {
        return this.encoding.name();
    }


    /**
     * @return count of bytes written by this stream instance plus number given in constructor
     */
    public long getBytesWritten() {
        return output.getBytesWritten();
    }


    /**
     * Returns short info about this class.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[charset=" + getEncoding() + ", writtenBytes=" + getBytesWritten() + ']';
    }
}
