/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.rest.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;

/**
 *
 * @author tmoreau
 */
// TODO: unit tests
public class StringUtil {

    /**
     * Compare two lists of strings. TBD - support a form that ignores ordering?
     *
     * @param list1
     * @param list2
     * @return boolean indicating if the list of strings have the ssame values in the same order.
     */
    public static boolean compareStringLists(List<String> list1, List<String> list2) {
        // TBD : should compare irrespective of the order of the values
        return (list1.equals(list2));
    }

    public static boolean compareStrings(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        } else {
            return str1.equals(str2);
        }
    }

    public static boolean compareStringsIgnoreCase(String str1, String str2) {
        if (str1 == null) {
            return str2 == null;
        } else {
            return str1.equalsIgnoreCase(str2);
        }
    }

    /**
     * Return the message parts of an action report as a List<String>
     *
     * @param actionReport
     * @return List<String> containing actionReport's message parts.
     */
    public static List<String> getActionReportMessageParts(ActionReport actionReport) {
        List<String> parts = new ArrayList<String>();
        for (MessagePart part : actionReport.getTopMessagePart().getChildren()) {
            parts.add(part.getMessage());
        }
        return parts;
    }

    /**
     * Convert a List<String> to a comma-separated string. This is often used to format strings that are sent to admin
     * commands.
     *
     * @param strings
     * @return String a comma-separated string containing strings.
     */
    public static String getCommaSeparatedStringList(List<String> strings) {
        StringBuilder sb = new StringBuilder();
        if (strings != null) {
            boolean first = true;
            for (String str : strings) {
                if (!first) {
                    sb.append(",");
                }
                sb.append(str);
                first = false;
            }
        }
        return sb.toString();
    }

    /**
     * Convert a string containing a comma-separated list of strings into a List<String>. This is often used to parse
     * strings that are returned from admin commands.
     *
     * @param stringList
     * @return List<String> containing the strings in stringList.
     */
    public static List<String> parseCommaSeparatedStringList(String stringList) {
        List<String> list = new ArrayList<String>();
        if (stringList != null) {
            for (StringTokenizer st = new StringTokenizer(stringList, ","); st.hasMoreTokens();) {
                list.add(st.nextToken().trim());
            }
        }
        return list;
    }

    /**
     * Determines if a string is null/empty or not
     *
     * @param string
     * @return true if the string is not null and has a length greater than zero, false otherwise
     */
    public static boolean notEmpty(String string) {
        return (string != null && !string.isEmpty());
    }

    /**
     * Converts a null/empty/non-empty string to null or non-empty
     *
     * @param string
     * @return null if string is null or empty, otherwise returns string
     */
    public static String nonEmpty(String string) {
        return (notEmpty(string)) ? string : null;
    }

    /**
     * Converts a null/empty/non-empty string to empty or non-empty
     *
     * @param string
     * @return an empty string if string is null or empty, otherwise returns string
     */
    public static String nonNull(String string) {
        return (notEmpty(string)) ? string : "";
    }
}
