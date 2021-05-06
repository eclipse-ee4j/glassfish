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

package org.glassfish.admin.amx.util;

import java.util.regex.Pattern;

/**
Useful utilities for regex handling
 */
public final class RegexUtil
{
    private RegexUtil()
    {
        // disallow instantiation
    }

    private final static char BACKSLASH = '\\';

    /**
    These characters will be escaped by wildcardToJavaRegex()
     */
    public static final String REGEX_SPECIALS = BACKSLASH + "[]^$?+{}()|-!";

    /**
    Converts each String to a Pattern using wildcardToJavaRegex

    @param exprs        String[] of expressions
    @return        Pattern[], one for each String
     */
    public static Pattern[] exprsToPatterns(final String[] exprs)
    {
        return (exprsToPatterns(exprs, 0));
    }

    /**
    Converts each String to a Pattern using wildcardToJavaRegex, passing the flags.

    @param exprs        String[] of expressions
    @param flags        flags to pass to Pattern.compile
    @return        Pattern[], one for each String
     */
    public static Pattern[] exprsToPatterns(final String[] exprs, int flags)
    {
        final Pattern[] patterns = new Pattern[exprs.length];

        for (int i = 0; i < exprs.length; ++i)
        {
            patterns[i] = Pattern.compile(wildcardToJavaRegex(exprs[i]), flags);
        }
        return (patterns);
    }

    /**
    Supports the single wildcard "*".  There is no support for searching for
    a literal "*".

    Convert a string to a form suitable for passing to java.util.regex.
     */
    public static String wildcardToJavaRegex(String input)
    {
        String converted = input;

        if (input != null)
        {
            final int length = input.length();
            final StringBuffer buf = new StringBuffer();

            for (int i = 0; i < length; ++i)
            {
                final char theChar = input.charAt(i);

                if (theChar == '.')
                {
                    buf.append("[.]");
                }
                else if (theChar == '*')
                {
                    buf.append(".*");
                }
                else if (REGEX_SPECIALS.indexOf(theChar) >= 0)
                {
                    // '[' begins a set of characters
                    buf.append("" + BACKSLASH + theChar);
                }
                else
                {
                    buf.append(theChar);
                }
            }

            converted = buf.toString();

        }
        return (converted);
    }

}

