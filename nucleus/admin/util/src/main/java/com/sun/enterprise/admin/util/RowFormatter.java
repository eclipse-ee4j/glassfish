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

package com.sun.enterprise.admin.util;

/**
 * This is a utility class which will be used to format data where tabular formats cannot be used
 *
 * @author Bhakti Mehta
 */
public class RowFormatter {

    private int numRows = -1;
    private final String[] headings;

    public RowFormatter(String[] h) {
        headings = h;
        numRows = headings.length;
    }

    /**
     * This will return a String of the format HEADING1 :value1 HEADING2 :value 2
     *
     * @param objs : The values which are to be displayed
     * @return The String containing the formatted headings and values
     */
    public String addColumn(Object[] objs) {
        // check to make sure the number of rows is the same as what we already have
        if (numRows != -1) {
            if (objs.length != numRows) {
                throw new IllegalArgumentException(String.format("invalid number of columns (%d), expected (%d)", objs.length, numRows));
            }
        }

        int longestValue = 0;
        for (int i = 0; i < numRows; i++) {
            if (headings != null && headings[i].length() > longestValue) {
                longestValue = headings[i].length();
            }

        }
        longestValue += 2;
        StringBuilder sb = new StringBuilder();

        StringBuilder formattedline = new StringBuilder("%1$-" + longestValue + "s:%2$-1s");
        for (int i = 0; i < numRows; i++) {

            sb.append(String.format(formattedline.toString(), headings[i], objs[i])).append("\n");
        }
        return sb.toString();
    }
}
