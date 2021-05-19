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

/**
 *
 * @author kedar
 */
public final class TokenValue implements Comparable {

    public final String token;
    public final String value;
    public final String preDelimiter;
    public final String postDelimiter;
    public final String delimitedToken;

    public static final String DEFAULT_DELIMITER = "%%%";

    /**
     * Creates a new instance of TokenValue - with default delimiter. Also note that if the value contains any '\'
     * characters, then these are appended to by another '\' character to work around the Java byte code interpretation.
     * Note that none of the arguments can be null. The value of delimiter is given by DEFAULT_DELIMITER.
     *
     * @param token a String that is the name of the token in this TokenValue.
     * @param value a String that is the value of the token.
     * @throws IllegalArgumentException in case of null values.
     * @see #TokenValue(java.lang.String, java.lang.String, java.lang.String)
     * @see #DEFAULT_DELIMITER
     */

    public TokenValue(String token, String value) {
        this(token, value, DEFAULT_DELIMITER);
    }

    public TokenValue(String token, String value, String delimiter) {
        this(token, value, delimiter, delimiter);
    }

    public TokenValue(String token, String value, String preDelimiter, String postDelimiter) {
        if (token == null || value == null || preDelimiter == null || postDelimiter == null) {
            throw new IllegalArgumentException("Null Argument");
        }
        this.token = token;
        /* Because of escaping process of a '\' by Java's bytecode
         * interpreter in string literals */
        this.value = escapeBackslashes(value);
        this.preDelimiter = preDelimiter;
        this.postDelimiter = postDelimiter;
        this.delimitedToken = preDelimiter + token + postDelimiter;
    }

    public TokenValue(TokenValue other) {
        this.token = other.token;
        this.value = other.value;
        this.preDelimiter = other.preDelimiter;
        this.postDelimiter = other.postDelimiter;
        this.delimitedToken = other.delimitedToken;
    }

    @Override
    public int compareTo(Object other) {
        final TokenValue otherTokenValue = (TokenValue) other;
        return (this.token.compareTo(otherTokenValue.token));
    }

    @Override
    public boolean equals(Object other) {
        boolean same = false;
        if (other instanceof TokenValue) {
            same = delimitedToken.equals(((TokenValue) other).delimitedToken) && value.equals(((TokenValue) other).value);
        }
        return same;
    }

    @Override
    public int hashCode() {
        int result = 43;
        result = 17 * result + token.hashCode();
        result = 17 * result + preDelimiter.hashCode();
        result = 17 * result + postDelimiter.hashCode();
        result = 17 * result + value.hashCode();

        return (result);
    }

    @Override
    public String toString() {
        return delimitedToken + "=" + value;
    }

    /** Just appends additional '\' characters in the passed string. */
    private String escapeBackslashes(String anyString) {
        final char BACK_SLASH = '\\';
        final StringBuffer escaped = new StringBuffer();
        for (int i = 0; i < anyString.length(); i++) {
            final char ch = anyString.charAt(i);
            escaped.append(ch);
            if (ch == BACK_SLASH) {
                escaped.append(BACK_SLASH);
            }
        }
        return escaped.toString();
    }
}
