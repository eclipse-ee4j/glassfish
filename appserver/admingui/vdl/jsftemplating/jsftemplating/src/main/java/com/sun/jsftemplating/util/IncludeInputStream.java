/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.jsftemplating.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * <p>
 * This <code>InputStream</code> looks for lines beginning with "#include '<em>filename</em>'" where filename is the
 * name of a file to include. It replaces the "#include" line with contents of the specified file. Any other line
 * beginning with '#' is illegal.
 * </p>
 *
 * @author Ken Paulsen (ken.paulsen@sun.com)
 */
public class IncludeInputStream extends FilterInputStream {

    /**
     * <p>
     * Constructor.
     * </p>
     */
    public IncludeInputStream(InputStream input) {
        super(input);
    }

    /**
     * <p>
     * This overriden method implements the include feature.
     * </p>
     *
     * @return The next character.
     */
    @Override
    public int read() throws IOException {
        int intChar = -1;
        if (redirStream != null) {
            // We are already redirecting, delegate
            intChar = redirStream.read();
            if (intChar != -1) {
                return intChar;
            }

            // Found end of redirect file, stop delegating
            redirStream = null;
        }

        // Read next character
        intChar = super.read();
        char ch = (char) intChar;

        // If we were at the end of the line, check for new line w/ #
        if (eol) {
            // Check to see if we have a '#'
            if (ch == '#') {
                intChar = startInclude();
            } else {
                eol = false;
            }
        }

        // Flag EOL if we're at the end of a line
        if (ch == 0x0A || ch == 0x0D) {
            eol = true;
        }

        return intChar;
    }

    @Override
    public int available() throws IOException {
        return 0;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (bytes == null) {
            throw new NullPointerException();
        } else if (off < 0 || off > bytes.length || len < 0 || off + len > bytes.length || off + len < 0) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }

        int c = read();
        if (c == -1) {
            return -1;
        }
        bytes[off] = (byte) c;

        int i = 1;
        try {
            for (; i < len; i++) {
                c = read();
                if (c == -1) {
                    break;
                }
                if (bytes != null) {
                    bytes[off + i] = (byte) c;
                }
            }
        } catch (IOException ee) {
            ee.printStackTrace();
        }
        return i;
    }

    /**
     *
     */
    private int startInclude() throws IOException {
        // Mark this spot in case #include doesn't match
        mark(MARK_LIMIT);

        // We have a line beginning w/ '#', verify we have "#include"
        int ch;
        for (int count = 0; count < INCLUDE_LEN; count++) {
            // look for include
            ch = super.read();
            if (Character.toLowerCase((char) ch) != INCLUDE.charAt(count)) {
                reset(); // Restore stream position
                return '#';
            }
        }

        // Skip whitespace...
        ch = super.read();
        while (ch == ' ' || ch == '\t') {
            ch = super.read();
        }

        // Skip '"' or '\''
        if (ch == '"' || ch == '\'') {
            ch = super.read();
        }

        // Read the file name
        StringBuffer buf = new StringBuffer("");
        while (ch != '"' && ch != '\'' && ch != 0x0A && ch != 0x0D && ch != -1) {
            buf.append((char) ch);
            ch = super.read();
        }

        // Skip ending '"' or '\'', if any
        if (ch == '"' || ch == '\'') {
            ch = super.read();
        }

        // Get the file name...
        String filename = buf.toString();

        // Look for the file
        URL url = FileUtil.searchForFile(filename, null);
        if (url != null) {
            redirStream = new IncludeInputStream(new BufferedInputStream(url.openStream()));
        } else {
            // Throw a FnF exception...
            throw new FileNotFoundException(filename);
        }

        // Read the first character from the file to return
        return redirStream.read();
    }

    /**
     * <p>
     * Simple test case (requires a test file).
     * </p>
     */
    public static void main(String[] args) {
        try {
            IncludeInputStream stream = new IncludeInputStream(new FileInputStream(args[0]));
            int ch = '\n';
            while (ch != -1) {
                System.out.print((char) ch);
                ch = stream.read();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean eol = true;
    private IncludeInputStream redirStream = null;

    private static final String INCLUDE = "include";
    private static final int INCLUDE_LEN = INCLUDE.length();

    private static final int MARK_LIMIT = 128;
}
