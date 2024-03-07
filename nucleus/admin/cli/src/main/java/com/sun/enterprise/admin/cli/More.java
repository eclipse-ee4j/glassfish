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

import java.io.Writer;
import java.io.Reader;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.BufferedReader;

/**
 * More.java
 *
 * @author <a href="mailto:toby.h.ferguson@sun.com">Toby H Ferguson</a>
 *
 * This class provides a primitive more(1) functionality, paging through the given file, with an optional pagelength.
 */

public class More {
    private BufferedReader in;
    private BufferedWriter out;
    private String quit;
    private String prompt;

    public More(int linesPerPage, Reader src, Writer dest, Reader fromUser, Writer toUser, String quitPrefix, String prompt)
            throws IOException {
        in = new BufferedReader(fromUser);
        out = new BufferedWriter(toUser);
        quit = quitPrefix;
        this.prompt = prompt;

        Pager pager = new Pager(linesPerPage, src, dest);
        do {
            pager.nextPage();
        } while (pager.hasNext() && wantsToContinue());
    }

    /**
     * Return false iff the line read from the user starts with the quit character. This is a blocking call, waiting on
     * input from the user
     */
    boolean wantsToContinue() throws IOException {
        out.write(prompt);
        out.newLine();
        out.flush();
        String line = in.readLine();
        return line != null && !line.startsWith(quit);
    }
}
