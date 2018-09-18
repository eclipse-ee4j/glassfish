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

package com.sun.enterprise.admin.cli;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

/**
 * Pager.java
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 */

class Pager {
    private BufferedReader in;
    private BufferedWriter out;
    private int pageLength;
    private String line;

    /**
     * Construct an object which will copy one pages worth of lines
     * at a time from the input to the
     * the output.
     *
     * No attempt is made under any circumstances to close the input
     * or output.
     *
     * @param lines the number of lines in a page. A number less
     * than 0 means copy all the input to the output.
     * @param in the source of the copy operation
     * @param out the destination of the copy operation
     * @throws IOException if there's a problem reading from, or
     * writing to, the source or destination
     */
    Pager(int lines, Reader in, Writer out) throws IOException {
        this.in = new BufferedReader(in);
        this.out = new BufferedWriter(out);
        pageLength = lines;
        nextLine();
    }

    /**
     * Copy the next page worth of lines from input to output
     */
    void nextPage() throws IOException {
        for (int i = 0; (pageLength < 0 || i < pageLength) && hasNext(); i++) {
            out.write(line);
            out.newLine();
            nextLine();
        }
        out.flush();
    }

    /**
     * Indicate if there are lines left to be copied
     * @return true iff there is at least one line left to be copied
     */
    boolean hasNext() {
        return line != null;
    }

    /**
     * Get the next line and copy it inot the internal buffer so's
     * we can answer the hasNext() question
     */
    private void nextLine() throws IOException {
        line = in.readLine();
    }
}
