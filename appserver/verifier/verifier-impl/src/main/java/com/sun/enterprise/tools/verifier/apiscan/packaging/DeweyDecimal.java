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

package com.sun.enterprise.tools.verifier.apiscan.packaging;

class DeweyDecimal {

    private int major = 0, minor = 0, micro = 0;

    public DeweyDecimal() {
    }

    public DeweyDecimal(String s) {
        s = s.trim();
        int idxOfFirstDot = s.indexOf('.', 0);
        if (idxOfFirstDot == -1) {
            major = Integer.parseInt(s);
            return;
        } else {
            major = Integer.parseInt(s.substring(0, idxOfFirstDot));
        }
        int idxOfSecondDot = s.indexOf('.', idxOfFirstDot + 1);
        if (idxOfSecondDot == -1) {
            minor = Integer.parseInt(s.substring(idxOfFirstDot + 1));
            return;
        } else {
            minor =
                    Integer.parseInt(
                            s.substring(idxOfFirstDot + 1, idxOfSecondDot));
        }
        micro = Integer.parseInt(s.substring(idxOfSecondDot + 1));
    }

    public boolean isCompatible(DeweyDecimal another) {
        if (another == null) return false;
        if (major < another.major) {
            return false;
        } else if (major == another.major) {
            if (minor < another.minor) {
                return false;
            } else if (minor == another.minor) {
                return micro >= another.micro;
            }
            //this.minor> another.minor && this.major==another.major, hence return true
            return true;
        }
        //this.major> another.major, hence return true
        return true;
    }

    public boolean isCompatible(String another) {
        if (another == null) return false;
        return isCompatible(new DeweyDecimal(another));
    }

    //provides value semantics, hence we should overrise hashCode and equals method.
    public int hashCode() {
        return major + minor + micro;
    }

    public boolean equals(Object o) {
        if (o == null) return false;
        try {
            DeweyDecimal other = (DeweyDecimal) o;
            return major == other.major && minor == other.minor &&
                    micro == other.micro;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String toString() {
        return "" + major + "." + minor + "." + micro; // NOI18N
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println(
                    "Usage: " + DeweyDecimal.class.getName() + // NOI18N
                    " <s1 in the format 1.2.3> <s2 in the format 5.5.6>"); // NOI18N
            System.exit(1);
        }
        DeweyDecimal d1 = new DeweyDecimal(args[0]);
        DeweyDecimal d2 = new DeweyDecimal(args[1]);
        System.out.println(d1 + ".isCompatible(" + d1 + ")=" + d1.isCompatible( // NOI18N
                d1));
        System.out.println(d2 + ".isCompatible(" + d2 + ")=" + d2.isCompatible( // NOI18N
                d2));
        System.out.println(d1 + ".isCompatible(" + d2 + ")=" + d1.isCompatible( // NOI18N
                d2));
        System.out.println(d2 + ".isCompatible(" + d1 + ")=" + d2.isCompatible( // NOI18N
                d1));
        System.out.println(d1 + ".equals(" + d1 + ")=" + d1.equals(d1)); // NOI18N
        System.out.println(d2 + ".equals(" + d2 + ")=" + d2.equals(d2)); // NOI18N
        System.out.println(d1 + ".equals(" + d2 + ")=" + d1.equals(d2)); // NOI18N
        System.out.println(d2 + ".equals(" + d1 + ")=" + d2.equals(d1)); // NOI18N
        System.out.println(d1 + ".hashCode()=" + d1.hashCode()); // NOI18N
        System.out.println(d2 + ".hashCode()=" + d2.hashCode()); // NOI18N
    }
}
