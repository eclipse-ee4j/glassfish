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

import java.util.*;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

public class ArgumentTokenizer {
    protected int currentPosition;
    protected int maxPosition;
    protected String str;
    protected StringBuilder token = new StringBuilder();

    private static final LocalStringsImpl strings = new LocalStringsImpl(ArgumentTokenizer.class);

    public static class ArgumentException extends Exception {
        public ArgumentException(String s) {
            super(s);
        }
    }

    /**
     * Construct a tokenizer for the specified string.
     *
     * @param str a string to be parsed.
     */
    public ArgumentTokenizer(String str) {
        currentPosition = 0;
        this.str = str;
        maxPosition = str.length();
    }

    /**
     * Skip white space.
     */
    protected void skipWhiteSpace() {
        while ((currentPosition < maxPosition) && Character.isWhitespace(str.charAt(currentPosition))) {
            currentPosition++;
        }
    }

    /**
     * Test if there are more tokens available from this tokenizer's string.
     *
     * @return <code>true</code> if there are more tokens available from this tokenizer's string; <code>false</code>
     * otherwise.
     */
    public boolean hasMoreTokens() {
        skipWhiteSpace();
        return (currentPosition < maxPosition);
    }

    /**
     * Return the next token from this tokenizer.
     *
     * @return the next token from this tokenizer.
     * @exception NoSuchElementException if there are no more tokens in this tokenizer's string.
     */
    public String nextToken() throws ArgumentTokenizer.ArgumentException {
        skipWhiteSpace();
        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException(strings.get("token.noMoreTokens"));
        }
        return scanToken();
    }

    /**
     * Return the next token starting at the current position, assuming whitespace has already been skipped.
     */
    protected String scanToken() throws ArgumentTokenizer.ArgumentException {
        while (currentPosition < maxPosition) {
            char c = str.charAt(currentPosition++);
            if (c == '"' || c == '\'') {
                char quote = c;
                while (currentPosition < maxPosition) {
                    c = str.charAt(currentPosition++);
                    if (c == '\\' && quote == '"') {
                        if (currentPosition >= maxPosition)
                            throw new ArgumentTokenizer.ArgumentException(strings.get("token.escapeAtEOL"));
                        c = str.charAt(currentPosition++);
                        if (!(c == '\\' || c == '"' || c == '\''))
                            token.append('\\');
                    } else if (c == quote) {
                        break;
                    }
                    token.append(c);
                }
                if (c != quote)
                    throw new ArgumentTokenizer.ArgumentException(strings.get("token.unbalancedQuotes"));
            } else if (c == '\\') {
                if (currentPosition >= maxPosition)
                    throw new ArgumentTokenizer.ArgumentException(strings.get("token.escapeAtEOL"));
                c = str.charAt(currentPosition++);
                token.append(c);
            } else if (Character.isWhitespace(c)) {
                break;
            } else {
                token.append(c);
            }
        }
        String s = token.toString();
        token.setLength(0);
        return s;
    }
}
