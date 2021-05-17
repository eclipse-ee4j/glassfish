/*
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

package org.glassfish.admin.rest.readers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/**
 * @author rajeshwar patil
 */
public class InputReader {

    /**
     * Construct a InputReader from a string.
     *
     * @param reader A reader.
     */
    public InputReader(Reader reader) {
        this.reader = reader.markSupported() ? reader : new BufferedReader(reader);
        this.useLastChar = false;
        this.index = 0;
    }

    /**
     * Construct a InputReader from a string.
     *
     * @param s A source string.
     */
    public InputReader(String s) {
        this(new StringReader(s));
    }

    /**
     * Back up one character.
     */
    public void back() throws InputException {
        if (useLastChar || index <= 0) {
            throw new InputException("Stepping back two steps is not supported");
        }
        index -= 1;
        useLastChar = true;
    }

    /**
     * Get the next character in the source string.
     *
     * @return The next character, or 0 if past the end of the source string.
     */
    public char next() throws InputException {
        if (this.useLastChar) {
            this.useLastChar = false;
            if (this.lastChar != 0) {
                this.index += 1;
            }
            return this.lastChar;
        }
        int c;
        try {
            c = this.reader.read();
        } catch (IOException exc) {
            throw new InputException(exc);
        }

        if (c <= 0) { // End of stream
            this.lastChar = 0;
            return 0;
        }
        this.index += 1;
        this.lastChar = (char) c;
        return this.lastChar;
    }

    /**
     * Resturns InputException to signal a syntax error.
     *
     * @param message The error message.
     * @return A InputException object, suitable for throwing
     */
    public InputException error(String message) {
        return new InputException(message + toString());
    }

    /**
     * Get the next char in the string, skipping whitespace.
     *
     * @throws InputException
     * @return A character, or 0 if there are no more characters.
     */
    public char nextNonSpace() throws InputException {
        for (;;) {
            char c = next();
            if (c == 0 || c > ' ') {
                return c;
            }
        }
    }

    /**
     * Get the next n characters.
     *
     * @param n The number of characters to take.
     * @return A string of n characters.
     * @throws InputException Substring bounds error if there are not n characters remaining in the source string.
     */
    public String next(int n) throws InputException {
        if (n == 0) {
            return "";
        }

        char[] buffer = new char[n];
        int pos = 0;

        if (this.useLastChar) {
            this.useLastChar = false;
            buffer[0] = this.lastChar;
            pos = 1;
        }

        try {
            int len;
            while ((pos < n) && ((len = reader.read(buffer, pos, n - pos)) != -1)) {
                pos += len;
            }
        } catch (IOException exc) {
            throw new InputException(exc);
        }
        this.index += pos;

        if (pos < n) {
            throw error("Substring bounds error");
        }

        this.lastChar = buffer[n - 1];
        return new String(buffer);
    }

    /**
     * Determine if the source string still contains characters that next() can consume.
     *
     * @return true if not yet at the end of the source.
     */
    public boolean more() throws InputException {
        char nextChar = next();
        if (nextChar == 0) {
            return false;
        }
        back();
        return true;
    }

    private int index;
    private Reader reader;
    private char lastChar;
    private boolean useLastChar;

}
