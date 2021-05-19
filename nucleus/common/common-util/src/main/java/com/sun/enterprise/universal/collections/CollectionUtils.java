/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal.collections;

import java.util.*;

/**
 * all-static methods for handling operations with Collections
 * @author bnevins
 */
public class CollectionUtils {
    private CollectionUtils() {

    }
    /**
     * Convert a Properties object, which is a Map<Object,Object> into
     * a Map<String,String>
     * @param p The Properties object to convert
     * @return The converted Map
     */
    public static Map<String,String> propertiesToStringMap(Properties p)
    {
        Map<String,String> map = new HashMap<String,String>();
        Set<Map.Entry<Object,Object>> entries = p.entrySet();

        for(Map.Entry<Object,Object> entry : entries) {
            Object name = entry.getKey();
            Object value = entry.getValue();

            if(name == null)
                continue; // impossible.  Ignore if I was wrong...
            if(value == null)
                map.put(name.toString(), null);
            else
                map.put(name.toString(), value.toString());
        }
        return map;
    }

    /**
     * Tired of dumping a String representation of a Map?
     * Then call me!
     * @param map The map to turn into a printable String
     * @return The pretty String
     */
    public static String toString(Map<String,String> map) {
        String[] arr = toStringArray(map);
        StringBuilder sb = new StringBuilder();

        for(String s : arr) {
            sb.append(s);
            sb.append(EOL);
        }
        return sb.toString();
    }

    /**
     * Convert a String[] into a space-delimited String
     * @param arr The String array to convert
     * @return The pretty String
     */
    public static String toString(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for(String s : arr) {
            sb.append(s);
            sb.append(' ');
        }
        return sb.toString();
    }
    /**
     * Convert a String[] into a newline-delimited String
     * @param arr The String array to convert
     * @return The pretty String
     */
    public static String toStringLines(String[] arr) {
        StringBuilder sb = new StringBuilder();
        for(String s : arr) {
            sb.append(s);
            sb.append('\n');
        }
        return sb.toString();
    }
    /**
     * Convert a List of String into a space-delimited String
     * @param arr The String array to convert
     * @return The pretty String
     */
    public static String toString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for(String s : list) {
            sb.append(s);
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String[] toStringArray(Map<String,String> map) {
        Set<String> set = map.keySet();
        String[] ss = new String[map.size()];
        int i = 0;

        for(String name : set) {
            String value = map.get(name);
            String s = name;

            if(value != null) {
                s += "=" + value;
            }
            ss[i++] = s;
        }
        return ss;
    }
    private static final String EOL = System.getProperty("line.separator");
}
