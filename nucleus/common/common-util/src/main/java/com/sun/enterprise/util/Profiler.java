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

/*
 * Profiler.java
 *
 * Created on September 17, 2001, 12:42 PM
 */
package com.sun.enterprise.util;

/**
 * A easy-to-use class that wraps one global ProfilerImpl object.  Use it to begin
 * and end profiling in one 'profiling thread'.  I.e. use this object to get timing for
 * sub-operations.  Use separate ProfilerImpl objects to get timings for overlapping
 * profile needs.
 *
 * <p> WARNING: Call reset at the end to avoid memory leaks.
 *
 * @author  bnevins
 * @version
 */
public class Profiler {

    private Profiler() {
    }

    /** Reset the global ProfilerImpl instance.
     **/
    public static void reset() {
        profiler.reset();
    }

    /** Start timing an item.
     **/
    public static void beginItem() {
        profiler.beginItem();
    }

    /** Start timing an item.
     * @param desc - Descriptive text for the item
     */
    public static void beginItem(String desc) {
        profiler.beginItem(desc);
    }

    /** Stop timing of the latest item
     */
    public static void endItem() {
        profiler.endItem();
    }

    /** return a String report of all the timings
     * @return  */
    public static String report() {
        return profiler.toString();
    }

    /**
     * Convenience method to avoid endItem() beginItem() bracketing
     * @param desc - Descriptive text for the item
     */
    public static void subItem(String desc) {
        endItem();
        beginItem(desc);
    }

    ////////////////////////////////////////////////////////////////////////////
    /**
     * @param notUsed  */
    public static void main(String[] notUsed) {
        try {
            profiler.beginItem("first item");
            Thread.sleep(3000);
            profiler.beginItem("second item here dude whoa yowser yowser");
            Thread.sleep(1500);
            profiler.endItem();
            profiler.endItem();
            System.out.println("" + profiler);
        } catch (Exception e) {
        }
    }
    static ProfilerImpl profiler = new ProfilerImpl();
}
