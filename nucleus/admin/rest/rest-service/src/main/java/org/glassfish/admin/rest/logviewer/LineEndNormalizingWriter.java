/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.logviewer;

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * Finds the lone LF and converts that to CR+LF.
 *
 * <p>
 * Internet Explorer's <tt>XmlHttpRequest.responseText</tt> seems to normalize the line end, and if we only send LF
 * without CR, it will not recognize that as a new line. To work around this problem, we use this filter to always
 * convert LF to CR+LF.
 *
 * @author Kohsuke Kawaguchi
 */
public /*for now, until Hudson migration completes*/ class LineEndNormalizingWriter extends FilterWriter {

    private boolean seenCR;

    public LineEndNormalizingWriter(Writer out) {
        super(out);
    }

    @Override
    public void write(char cbuf[]) throws IOException {
        write(cbuf, 0, cbuf.length);
    }

    @Override
    public void write(String str) throws IOException {
        write(str, 0, str.length());
    }

    @Override
    public void write(int c) throws IOException {
        if (!seenCR && c == LF)
            super.write("\r\n");
        else
            super.write(c);
        seenCR = (c == CR);
    }

    @Override
    public void write(char cbuf[], int off, int len) throws IOException {
        int end = off + len;
        int writeBegin = off;

        for (int i = off; i < end; i++) {
            char ch = cbuf[i];
            if (!seenCR && ch == LF) {
                // write up to the char before LF
                super.write(cbuf, writeBegin, i - writeBegin);
                super.write("\r\n");
                writeBegin = i + 1;
            }
            seenCR = (ch == CR);
        }

        super.write(cbuf, writeBegin, end - writeBegin);
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        int end = off + len;
        int writeBegin = off;

        for (int i = off; i < end; i++) {
            char ch = str.charAt(i);
            if (!seenCR && ch == LF) {
                // write up to the char before LF
                super.write(str, writeBegin, i - writeBegin);
                super.write("\r\n");
                writeBegin = i + 1;
            }
            seenCR = (ch == CR);
        }

        super.write(str, writeBegin, end - writeBegin);
    }

    private static final int CR = 0x0D;
    private static final int LF = 0x0A;
}
