/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.server.logging;

import java.util.regex.Pattern;

/**
 * Helper class that provides methods to detect the log format of a record.
 *
 */
public class LogFormatHelper {

    private static final int ODL_SUBSTRING_LEN = 5;

    private static final String ODL_LINE_BEGIN_REGEX = "\\[(\\d){4}";

    private static final class PatternHolder {
        private static final Pattern ODL_PATTERN = Pattern.compile(ODL_LINE_BEGIN_REGEX);
    }

    /**
     * Determines whether the given line is the beginning of a UniformLogFormat log record.
     * @param line
     * @return
     */
    public static boolean isUniformFormatLogHeader(String line) {
        if (line.startsWith("[#|") && countOccurrences(line, '|') > 4) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determines whether the given line is the beginning of a ODL log record.
     * @param line
     * @return
     */
    public static boolean isODLFormatLogHeader(String line) {
        if (line.length() > ODL_SUBSTRING_LEN
                && PatternHolder.ODL_PATTERN.matcher(
                        line.substring(0, ODL_SUBSTRING_LEN))
                        .matches()
                && countOccurrences(line, '[') > 4) {
            return true;
        } else {
            return false;
        }
    }

    private static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

}
