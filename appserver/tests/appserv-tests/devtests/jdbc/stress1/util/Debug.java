/*
 * Copyright (c) 2001, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.stress1.util;

/**
 * This class makes it easier to print out debug statements.
 * The Debug.print statements are printed to System.err
 * if debuggingOn = true.
 */


public final class Debug {

    public static final boolean debuggingOn = true;

    public static final void print(String msg) {
        if (debuggingOn) {
            System.err.println("Debug: " + msg);
        }
    }

    public static final void print(String msg, Object object) {
        if (debuggingOn) {
            System.err.println("Debug: " + msg);
            System.err.println("       " + object.getClass().getName());
        }
    }
}
