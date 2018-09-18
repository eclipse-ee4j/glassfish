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

package org.glassfish.web.util;

/**
 * This class encodes HTML display content for preventing XSS.
 */
public class HtmlEntityEncoder {
    /**
     * xssStrings:
     *     " => 34, % => 37, & => 38, ' => 39, ( => 40,
     *     ) => 41, + => 43, ; => 59, < => 60, > => 62
     */
    private static String[] xssStrings = {  //34-62
        "&quot;", null,
        null, "&#37;", "&amp;", "&#39;", "&#40;",
        "&#41;", null, "&#43;", null, null,
        null, null, null, null, null,
        null, null, null, null, null,
        null, null, null, "&#59;", "&lt;",
        null, "&gt;"
    };

    private static final int START = 34;
    private static final char DEFAULT_CHAR = ' ';

    public static String encodeXSS(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return encodeXSS(obj.toString());
        }
    }

    /**
     * Encode
     * a) the following visible characters:
     *     " => 34, % => 37, & => 38, ' => 39, ( => 40,
     *     ) => 41, + => 43,
     *     ; => 59, < => 60,
     *     > => 62,
     * b) ignore control characters
     * c) ignore undefined characters
     */
    public static String encodeXSS(String s) {
        if (s == null) {
            return null;
        }

        int len = s.length();
        if (len == 0) {
            return s;
        }

        StringBuilder sb = null;
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            int ind =(int)c - START;
            if (ind > -1 && ind < xssStrings.length && xssStrings[ind] != null) {
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(s.substring(0, i));
                }
                sb.append(xssStrings[ind]);
            } else if (32 <= c && c <= 126 || 128 <= c && c <= 255 || c == 9
                    || Character.isWhitespace(c)) {
                 if (sb != null) {
                     sb.append(c);
                 }
            } else if (Character.isISOControl(c)) { // skip
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(s.substring(0, i));
                }
                sb.append(DEFAULT_CHAR);
            } else if (Character.isHighSurrogate(c)) {
                boolean valid = false;
                if (i + 1 < len) {
                    char nextC = s.charAt(i + 1);
                    if (Character.isLowSurrogate(nextC)) {
                        valid = true;
                        if (sb != null) {
                            sb.append(c);
                            sb.append(nextC);
                        }
                    }
                }
                if (!valid) {
                    if (sb == null) {
                        sb = new StringBuilder(len);
                        sb.append(s.substring(0, i));
                    }
                    sb.append(DEFAULT_CHAR);
                }
                i++; // a pair
            } else if (Character.isDefined(c)) {
                if (sb != null) {
                    sb.append(c);
                }
            } else { // skip
                if (sb == null) {
                    sb = new StringBuilder(len);
                    sb.append(s.substring(0, i));
                }
                sb.append(DEFAULT_CHAR);
            }
        }

        if (sb != null) {
            return sb.toString();
        } else {
            return s;
        }
    }
}
