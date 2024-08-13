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
 * ProfilerImpl.java
 *
 * Created on September 17, 2001, 12:42 PM
 */
package com.sun.enterprise.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Simple class for profiling code.  beginItem/endItem pairs start and stop the timing for an item.
 *
 * @author  bnevins
 */
public class ProfilerImpl {

    /** Create an empty object
     */
    public ProfilerImpl() {
    }

    /**Reset all the timing information
     */
    public void reset() {
        currItem = null;
        items.clear();
        numBegins = 0;
        numEnds = 0;
        numActualEnds = 0;
    }

    /** Start timing an item.
     **/
    public void beginItem() {
        beginItem("No Description");
    }

    /** Start timing an item.
     * @param desc - Descriptive text for the item
     **/
    public void beginItem(String desc) {
        //if(currItem != null)
        //Reporter.assert(currItem.hasEnded());

        currItem = new Item(desc);
        items.add(currItem);
        ++numBegins;
    }

    /** Stop timing an item and store the information.
     **/
    public void endItem() {
        ++numEnds;
        Item item = getLastNotEnded();

        if (item != null) {
            item.end();
        }
        ++numActualEnds;
    }

    /** Return a formatted String with the timing information
     **/
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nBegins: ").append(numBegins).append(", Ends: ").append(numEnds)
                .append(", Actual Ends: ").append(numActualEnds).append("\n");

        sb.append(Item.getHeader());
        sb.append("\n");


        for (Iterator iter = items.iterator(); iter.hasNext();) {
            Item item = (Item) iter.next();
            sb.append(item.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////
    private Item getLastNotEnded() {
        int index = items.size();

        while (--index >= 0) {
            Item item = (Item) items.get(index);

            if (!item.hasEnded()) {
                return item;
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////
    private static class Item {

        Item(String desc) {
            title = desc;
            startTime = System.currentTimeMillis();
            endTime = startTime;
            setLongestTitle(title.length());
        }

        boolean hasEnded() {
            return ended;
            //return endTime > startTime;
        }

        void end() {
            endTime = System.currentTimeMillis();
            ended = true;
        }

        @Override
        public String toString() {
            long finish = hasEnded() ? endTime : System.currentTimeMillis();

            String totalTime = "" + (finish - startTime);

            if (totalTime.equals("0")) {
                totalTime = "< 1";
            }

            String desc = StringUtils.padRight(title, longestTitle + 1);
            String time = StringUtils.padLeft(totalTime, 8);

            if (!hasEnded()) {
                time += "  ** STILL RUNNING **";
            }

            return desc + time;
        }

        public static String getHeader() {
            return "\n" + StringUtils.padRight("Description", longestTitle + 1) + StringUtils.padLeft("msec", 8);
        }

        private static void setLongestTitle(int len) {
            synchronized (lock) {
                if (len > longestTitle) {
                    longestTitle = len;
                }
            }
        }
        String title;
        long startTime;
        long endTime;
        static int longestTitle = 12;
        private final static Object lock = new Object();
        boolean ended = false;
    }
    ////////////////////////////////////////////////////////////////////////////
    Item currItem = null;
    List<Item> items = new ArrayList<Item>();
    int numBegins = 0;
    int numEnds = 0;
    int numActualEnds = 0;

    ////////////////////////////////////////////////////////////////////////////
    /** Simple unit test
     **/
    public static void main(String[] notUsed) {
        ProfilerImpl p = new ProfilerImpl();

        try {
            p.beginItem("first item");
            Thread.sleep(3000);
            p.beginItem("second item here dude whoa yowser yowser");
            Thread.sleep(1500);
            p.endItem();
            p.endItem();
            System.out.println("" + p);
        } catch (Exception e) {
        }
    }
}
