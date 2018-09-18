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

package com.sun.enterprise.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Tom Mueller
 */
public class ColumnFormatter {
    private int numCols = -1;
    private String headings[];
    private List<String[]> valList = new ArrayList<String[]>();

    public ColumnFormatter(String headings[]) {
        this.headings = headings;
        numCols = headings.length;
    }

    public ColumnFormatter() {
        this.headings = null;
    }

    public void addRow(Object values[]) throws IllegalArgumentException {
        // check to make sure the number of columns is the same as what we already have
        if (numCols != -1) {
            if (values.length != numCols) {
                throw new IllegalArgumentException(
                        Strings.get("column.internal", values.length, numCols));
            }
        }
        numCols = values.length;
        String v[] = new String[numCols];
        for (int i = 0; i < v.length; i++) {
            v[i] = values[i] == null ? "" : values[i].toString();
        }
        valList.add(v);
    }

    /**
     * Get the content of all rows along with the headings as a List of Map.
     * Note : If there are duplicate headings, latest entry and its value will take precedence.
     * This can be useful in case the CLI is used by GUI via REST as GUI expects a List of Map.
     * @return List of Map all entries in in the ColumnFormatter
     */
    public List<Map<String,String>> getContent(){
        List<Map<String,String>> rows = new ArrayList<Map<String, String>>();

        for(String[] values : valList){
            Map<String,String> entry = new TreeMap<String, String>();
            int i = 0;
            for(String value : values){
                entry.put(headings[i], value);
                i++;
            }
            rows.add(entry);
        }
        return rows;
    }

    @Override
    public String toString() {
        // no data
        if (numCols == -1) {
            return "";
        }

        int longestValue[] = new int[numCols];
        for (String v[] : valList) {
            for (int i = 0; i < v.length; i++) {
                if (v[i].length() > longestValue[i]) {
                   longestValue[i] = v[i].length();
                }
            }
        }

        StringBuilder formattedLineBuf = new StringBuilder();
        for (int i = 0; i < numCols; i++) {
            if (headings != null && headings[i].length() > longestValue[i]) {
                longestValue[i] = headings[i].length();
            }
            longestValue[i] += 2;
            formattedLineBuf.append("%-")
                    .append(longestValue[i])
                    .append("s");
        }
        String formattedLine = formattedLineBuf.toString();
        StringBuilder sb = new StringBuilder();

        boolean havePrev = false;
        if (headings != null) {
            sb.append(String.format(formattedLine, (Object[])headings));
            havePrev = true;
        }

        // no linefeed at the end!!!
        for (String v[] : valList) {
            if (havePrev) {
                sb.append('\n');
            }
            sb.append(String.format(formattedLine, (Object[])v));
            havePrev = true;
        }

        return sb.toString();
    }
}
