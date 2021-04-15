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

package com.sun.enterprise.admin.cli;

public class StringEditDistance {

    /**
     * Computes the edit distance between two strings.
     *
     * <p>
     * The complexity is O(nm) where n=a.length() and m=b.length().
     */
    public static int editDistance(String a, String b) {
        return new StringEditDistance(a, b).calc();
    }

    /**
     * Finds the string in the <code>group</code> closest to <code>key</code> and returns it.
     *
     * @return null if group.length==0.
     */
    public static String findNearest(String key, String[] group) {
        int c = Integer.MAX_VALUE;
        String r = null;
        for (int i = 0; i < group.length; i++) {
            int ed = editDistance(key, group[i]);
            if (c > ed) {
                c = ed;
                r = group[i];
            }
        }
        return r;
    }

    /** cost vector. */
    private int[] cost;

    /** back buffer. */
    private int[] back;

    /** Two strings to be compared. */
    private final String a, b;

    private StringEditDistance(String a, String b) {
        this.a = a;
        this.b = b;
        cost = new int[a.length() + 1];
        back = new int[a.length() + 1];
        // back buffer
        for (int i = 0; i <= a.length(); i++)
            cost[i] = i;
    }

    /**
     * Swaps two buffers.
     */
    private void flip() {
        int[] t = cost;
        cost = back;
        back = t;
    }

    private int min(int a, int b, int c) {
        return Math.min(a, Math.min(b, c));
    }

    private int calc() {
        for (int j = 0; j < b.length(); j++) {
            flip();
            cost[0] = j + 1;
            for (int i = 0; i < a.length(); i++) {
                int match = (a.charAt(i) == b.charAt(j)) ? 0 : 1;
                cost[i + 1] = min(back[i] + match, cost[i] + 1, back[i + 1] + 1);
            }
        }
        return cost[a.length()];
    }
}
