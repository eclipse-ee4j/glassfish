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

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;

/**
 * {@link Writer} that spools the output and writes to another {@link Writer} later.
 *
 * @author Kohsuke Kawaguchi
 */
public /*for now, until Hudson migration completes*/ final class CharSpool extends Writer {
    private List<char[]> buf;

    private char[] last = new char[1024];
    private int pos;

    @Override
    public void write(char cbuf[], int off, int len) {
        while (len > 0) {
            int sz = Math.min(last.length - pos, len);
            System.arraycopy(cbuf, off, last, pos, sz);
            len -= sz;
            off += sz;
            pos += sz;
            renew();
        }
    }

    private void renew() {
        if (pos < last.length)
            return;

        if (buf == null)
            buf = new LinkedList<char[]>();
        buf.add(last);
        last = new char[1024];
        pos = 0;
    }

    @Override
    public void write(int c) {
        renew();
        last[pos++] = (char) c;
    }

    @Override
    public void flush() {
        // noop
    }

    @Override
    public void close() {
        // noop
    }

    public void writeTo(Writer w) throws IOException {
        if (buf != null) {
            for (char[] cb : buf) {
                w.write(cb);
            }
        }
        w.write(last, 0, pos);
    }
}
