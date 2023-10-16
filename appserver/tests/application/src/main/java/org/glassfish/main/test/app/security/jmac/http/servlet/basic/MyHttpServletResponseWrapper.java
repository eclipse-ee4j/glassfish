/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.test.app.security.jmac.http.servlet.basic;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

class MyHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private MyPrintWriter myPrintWriter = null;

    MyHttpServletResponseWrapper(HttpServletResponse response) {
        super(response);
        try {
            myPrintWriter = new MyPrintWriter(response.getWriter());
        } catch(Exception ex) {
            ex.printStackTrace();
            throw new IllegalStateException(ex.toString(), ex);
        }
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return myPrintWriter;
    }

    int getAdjustedCount() {
        return myPrintWriter.getAdjustedCount();
    }
}

class MyPrintWriter extends PrintWriter {
    private int count = 0; // count up to '\r'

    public MyPrintWriter(Writer writer) {
        super(writer);
    }

    // our jsp writer only use write char[] off len
    @Override
    public void write(char[] cbuf, int off, int len) {
        count += len - numOfCR(cbuf, off, len);
        super.write(cbuf, off, len);
    }

    public int getAdjustedCount() {
        return count;
    }

    private int numOfCR(char[] cbuf, int off, int len) {
        int numCR = 0;
        if (cbuf != null && off < cbuf.length) {
            for (int i = off; i <= len -1 && i < cbuf.length; i++) {
                if (cbuf[i] == '\r') {
                    numCR++;
                }
            }
        }
        return numCR;
    }
}
