/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id$
 */
package com.sun.ts.tests.ejb30.common.helper;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.sun.ts.lib.util.TestUtil.NEW_LINE;

public final class Helper {
    private static Logger logger = Logger.getLogger("com.sun.ts.tests.ejb30");

    private Helper() {
    }

    public static Logger getLogger() {
        return logger;
    }

    public static String compareResultList(final List expected, final List actual) {
        String reason;
        if (expected.equals(actual)) {
            reason = "Got expected result list: " + expected;
            logger.info(reason);
        } else {
            reason = NEW_LINE + "Expecting result list: " + expected + NEW_LINE + "         , but actual: " + actual;
            throw new RuntimeException(reason);
        }
        return reason;
    }

    public static String compareResultList(final String[] expected, final List actual) {
        return compareResultList(Arrays.asList(expected), actual);
    }

    /**
     * Similar to JUnit Assert, but this class appends message to both true and false outcome. This class also makes use of
     * autoboxing to handle primitives.
     *
     * @param messagePrefix
     * @param expected
     * @param actual
     * @return a String showing a true outcome
     * @throws java.lang.RuntimeException if comparison is false
     */
    public static String assertEquals(final String messagePrefix, final Object expected, final Object actual) throws RuntimeException {
        final StringBuilder sb = new StringBuilder();
        assertEquals(messagePrefix, expected, actual, sb);
        return sb.toString();
    }

    public static void assertEquals(final String messagePrefix, final Object expected, final Object actual, final StringBuilder sb)
        throws RuntimeException {
        sb.append(NEW_LINE);
        if (messagePrefix != null) {
            sb.append(NEW_LINE).append(messagePrefix).append(" ");
        }
        if (equalsOrNot(expected, actual)) {
            sb.append("Got the expected result:").append(actual).append("\t");
        } else {
            sb.append("Expecting ").append(expected).append(", but actual ").append(actual);
            throw new RuntimeException(sb.toString());
        }
    }

    public static String assertNotEquals(final String messagePrefix, final Object expected, final Object actual) throws RuntimeException {
        final StringBuilder sb = new StringBuilder();
        assertNotEquals(messagePrefix, expected, actual, sb);
        return sb.toString();
    }

    public static void assertNotEquals(final String messagePrefix, final Object expected, final Object actual, final StringBuilder sb)
        throws RuntimeException {
        sb.append(NEW_LINE);
        if (messagePrefix != null) {
            sb.append(messagePrefix).append(" ");
        }
        if (!equalsOrNot(expected, actual)) {
            sb.append("Got the expected NotEquals result. compareTo:").append(expected).append(", actual:").append(actual).append("\t");
        } else {
            sb.append("Expecting NotEquals, but got equals. compareTo:").append(expected).append(", and actual: ").append(actual);
            throw new RuntimeException(sb.toString());
        }
    }

    public static void assertCloseEnough(final String messagePrefix, long expected, long actual, long ignoreableDiff, StringBuilder sb) {
        sb.append(NEW_LINE);
        if (messagePrefix != null) {
            sb.append(messagePrefix).append(" ");
        }
        long dif = Math.abs(actual - expected);
        if (dif <= ignoreableDiff) {
            sb.append("Got the expected result:");
            sb.append("the diff between expected " + expected + ", and actual " + actual + " is " + dif
                + ", equals or less than the ignoreableDiff " + ignoreableDiff);
        } else {
            throw new RuntimeException("the diff between expected " + expected + ", and actual " + actual + " is " + dif
                + ", greater than the ignoreableDiff " + ignoreableDiff);
        }
    }

    public static String assertGreaterThan(final String messagePrefix, long arg1, long arg2) throws RuntimeException {
        final StringBuilder sb = new StringBuilder();
        assertGreaterThan(messagePrefix, arg1, arg2, sb);
        return sb.toString();
    }

    public static void assertGreaterThan(final String messagePrefix, long arg1, long arg2, final StringBuilder sb) throws RuntimeException {
        sb.append(NEW_LINE);
        if (messagePrefix != null) {
            sb.append(messagePrefix).append(" ");
        }
        if (arg1 > arg2) {
            sb.append("Got the expected result:").append(arg1).append(">").append(arg2).append("\t");
        } else {
            sb.append("Expecting ").append(arg1).append(">").append(arg2).append(", but failed");
            throw new RuntimeException(sb.toString());
        }
    }

    public static void busyWait(long millis) {
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Begin busy wait " + millis + " milliseconds");
        }
        long end = System.currentTimeMillis() + millis;
        do {
        } while (System.currentTimeMillis() < end);
        if (logger.isLoggable(Level.FINER)) {
            logger.finer("Finished busy wait " + millis + " milliseconds");
        }
    }

    public static void preDestroy(Object obj) {
        logger.info("In PreDestroy of " + obj);
    }

    public static void main(String[] args) {
        System.out.println(assertEquals("compare strings", "a", "a"));
        System.out.println(assertEquals("compare primitives", 100, 100));
        int i = 0;
        int j = 0;
        System.out.println(assertEquals("compare declared primitives", i, j));

        StringBuilder sb = new StringBuilder();
        sb.append("Check the following: ");
        assertEquals("String:", "a", "a", sb);
        assertEquals("int:", 5, 5, sb);
        assertEquals("double:", 5.5, 5.5, sb);
        assertEquals("long:", 9999L, 9999L, sb);

        // compare different String objects
        assertEquals("Object:", new String("st"), new String("st"), sb);
        System.out.println(sb.toString());

        busyWait(4000);
    }

    private static boolean equalsOrNot(final Object expected, final Object actual) {
        boolean sameOrNot = false;
        if (expected == null) {
            if (actual == null) {
                sameOrNot = true;
            }
        } else {
            sameOrNot = expected.equals(actual);
        }
        return sameOrNot;
    }
}
