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

package org.glassfish.common.util.admin;

import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.NoSuchElementException;

/**
 * Separate parameters to admin commands into tokens.
 * Mostly used for parameters that take a list of values
 * separated by colons.
 *
 * @author Bill Shannon
 */
public class ParamTokenizer {
    protected int currentPosition;
    protected int maxPosition;
    protected String str;
    protected char delimiter;
    protected StringBuilder token = new StringBuilder();

    public static final LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(ParamTokenizer.class);

    /**
     * Construct a tokenizer for the specified string.
     *
     * @param   str            a string to be parsed.
     */
    public ParamTokenizer(String str, char delimiter) {
        currentPosition = 0;
        this.str = str;
        this.delimiter = delimiter;
        maxPosition = str.length();
    }

    /**
     * Test if there are more tokens available from this tokenizer's string.
     *
     * @return  <code>true</code> if there are more tokens available from this
     *          tokenizer's string; <code>false</code> otherwise.
     */
    public boolean hasMoreTokens() {
        return (currentPosition < maxPosition);
    }

    /**
     * Return the next token from this tokenizer.
     *
     * @return     the next token from this tokenizer.
     * @exception  NoSuchElementException  if there are no more tokens in this
     *               tokenizer's string.
     */
    public String nextToken() throws NoSuchElementException {
        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException(
                localStrings.getLocalString("NoMoreTokens", "No more tokens"));
        }
        return scanToken(false);
    }

    /**
     * Return the next token from this tokenizer.
     * Keep escapes and quotes intact.
     *
     * @return     the next token from this tokenizer.
     * @exception  NoSuchElementException  if there are no more tokens in this
     *               tokenizer's string.
     */
    public String nextTokenKeepEscapes() throws NoSuchElementException {
        if (currentPosition >= maxPosition) {
            throw new NoSuchElementException(
                localStrings.getLocalString("NoMoreTokens", "No more tokens"));
        }
        return scanToken(true);
    }

    /**
     * Return the next token starting at the current position.
     */
    protected String scanToken(boolean keep) throws IllegalArgumentException {
        while (currentPosition < maxPosition) {
            char c = str.charAt(currentPosition++);
            if (c == '"' || c == '\'') {
                if (keep)
                    token.append(c);
                char quote = c;
                while (currentPosition < maxPosition) {
                    c = str.charAt(currentPosition++);
                    if (c == '\\' && quote == '"') {
                        if (currentPosition >= maxPosition)
                            throw new IllegalArgumentException(
                                localStrings.getLocalString("EscapeAtEOL",
                                    "Escape at EOL"));
                        c = str.charAt(currentPosition++);
                        if (keep)
                            token.append('\\');
                    } else if (c == quote) {
                        break;
                    }
                    token.append(c);
                }
                if (c != quote)
                    throw new IllegalArgumentException(
                        localStrings.getLocalString("UnbalancedQuotes",
                            "Unbalanced quotes"));
                if (keep)
                    token.append(c);
            } else if (c == delimiter) {
                break;
            } else if (c == '\\') {
                if (currentPosition >= maxPosition)
                    throw new IllegalArgumentException(
                        localStrings.getLocalString("EscapeAtEOL",
                            "Escape at EOL"));
                c = str.charAt(currentPosition++);
                if (keep)
                    token.append('\\');
                token.append(c);
            } else {
                token.append(c);
            }
        }
        String s = token.toString();
        token.setLength(0);
        return s;
    }
}
