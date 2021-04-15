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

package com.sun.enterprise.admin.util;

import java.util.NoSuchElementException;

public class QuotedStringTokenizer {
    private final char[] ca;
    private String delimiters = "\t ";
    private final int numTokens;
    private int curToken = 0;
    private final CharIterator iterator;

    public QuotedStringTokenizer(String s) {
        this(s, null);
    }

    public QuotedStringTokenizer(String s, String delim) {
        if (null == s) {
            throw new IllegalArgumentException("null param");
        }
        ca = s.toCharArray();
        if (delim != null && delim.length() > 0) {
            delimiters = delim;
        }
        numTokens = _countTokens();
        iterator = new CharIterator(ca);
    }

    public int countTokens() {
        return numTokens;
    }

    public boolean hasMoreTokens() {
        return curToken < numTokens;
    }

    public String nextToken() {
        if (curToken == numTokens)
            throw new NoSuchElementException();
        final StringBuffer sb = new StringBuffer();
        boolean bQuote = false;
        boolean bEscaped = false;
        char c;
        while ((c = iterator.next()) != CharIterator.EOF) {
            boolean isDelimiter = isDelimiter(c);
            if (!isDelimiter && !bEscaped) {
                sb.append(c);
                if (c == '\"')
                    bQuote = !bQuote;
                char next = iterator.peekNext();
                if (next == CharIterator.EOF || (isDelimiter(next) && !bQuote))
                    break;
            } else if (bQuote || bEscaped) {
                sb.append(c);
            }
            if (c == '\\')
                bEscaped = !bEscaped;
            else
                bEscaped = false;
        }
        curToken++;
        return sb.toString();
    }

    boolean isDelimiter(char c) {
        return delimiters.indexOf(c) >= 0;
    }

    private int _countTokens() {
        int tokens = 0;
        boolean bQuote = false;
        boolean bEscaped = false;
        final CharIterator it = new CharIterator(ca);
        char c;

        while ((c = it.next()) != CharIterator.EOF) {
            char next = it.peekNext();
            if (!isDelimiter(c) && !bEscaped) {
                if (c == '\"')
                    bQuote = !bQuote;
                if (next == CharIterator.EOF || (isDelimiter(next) && !bQuote))
                    tokens++;
            } else if (next == CharIterator.EOF && bQuote) //eg :- "\" "
                tokens++;
            if (c == '\\')
                bEscaped = !bEscaped;
            else
                bEscaped = false;
        }
        return tokens;
    }

    private static final class CharIterator {
        static final char EOF = '\uFFFF';

        private final char[] carr;
        private int index = 0;

        private CharIterator(char[] ca) {
            carr = ca;
        }

        char next() {
            if (index >= carr.length)
                return EOF;
            char c = carr[index];
            ++index;
            return c;
        }

        char peekNext() {
            if (index >= carr.length)
                return EOF;
            return carr[index];
        }
    }
}
