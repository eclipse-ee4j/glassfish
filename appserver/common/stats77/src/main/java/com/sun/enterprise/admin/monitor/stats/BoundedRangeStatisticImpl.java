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

package com.sun.enterprise.admin.monitor.stats;
import com.sun.enterprise.util.i18n.StringManager;

import org.glassfish.j2ee.statistics.BoundedRangeStatistic;

/**
 * An implementation of a BoundedRangeStatistic. All instances of this class are
 * immutable. Provides all the necessary accessors for properties.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @verison 1.0
 */
public final class BoundedRangeStatisticImpl extends StatisticImpl implements
    BoundedRangeStatistic {

    private final long currentVal;
    private final long highWaterMark;
    private final long lowWaterMark;
    private final long upperBound;
    private final long lowerBound;
    private static final StringManager localStrMgr =
                StringManager.getManager(BoundedRangeStatisticImpl.class);

    @Override
    public String toString() {
        return super.toString() + NEWLINE +
            "Current: " + getCurrent() + NEWLINE +
            "LowWaterMark: " + getLowWaterMark() + NEWLINE +
            "HighWaterMark: " + getHighWaterMark() + NEWLINE +
            "LowerBound: " + getLowerBound() + NEWLINE +
            "UpperBound: " + getUpperBound();
    }


    /** DEFAULT_UPPER_BOUND is maximum value Long can attain */
    public static final long DEFAULT_MAX_BOUND = java.lang.Long.MAX_VALUE;
    /** DEFAULT_LOWER_BOUND is same as DEFAULT_VALUE i.e. 0 */
    public static final long DEFAULT_MIN_BOUND = DEFAULT_VALUE;

    /**
     * A constructor that creates an instance of class. Following are the defaults:
     * <ul>
     *    <li> Unit is defaulted to empty string. </li>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     *    <li> Current Value is initialized to StatisticImpl#DEFAULT_VALUE.</li>
     *  <li> UpperBound is initialized to StatisticImpl#DEFAULT_MAX_BOUND. </li>
     *  <li> LowerBound is initialized to StatisticImpl#DEFAULT_MIN_BOUND. </li>
     *  <li> HighWaterMark is initialized to Current Value. </li>
     *  <li> LowWaterMark is initialized to Current Value. </li>
     * </ul>
     * @param        name        String that indicates the name of this statistic
     */
    public BoundedRangeStatisticImpl(String name) {
        this(name, DEFAULT_UNIT);
    }

    /**
     * A constructor that creates an instance of class. Following are the defaults:
     * <ul>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     *    <li> Current Value is initialized to StatisticImpl#DEFAULT_VALUE.</li>
     *  <li> UpperBound is initialized to StatisticImpl#DEFAULT_MAX_BOUND. </li>
     *  <li> LowerBound is initialized to StatisticImpl#DEFAULT_MIN_BOUND. </li>
     *  <li> HighWaterMark is initialized to Current Value. </li>
     *  <li> LowWaterMark is initialized to Current Value. </li>
     * </ul>
     * @param        name        String that indicates the name of this statistic
     * @param        unit        String that indicates the unit of this statistic
     */
    public BoundedRangeStatisticImpl(String name, String unit) {
        this(name, unit, DEFAULT_VALUE);
    }

    /**
     * A constructor that creates an instance of class. Following are the defaults:
     * <ul>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     *  <li> UpperBound is initialized to StatisticImpl#DEFAULT_MAX_BOUND. </li>
     *  <li> LowerBound is initialized to StatisticImpl#DEFAULT_MIN_BOUND. </li>
     *  <li> HighWaterMark is initialized to Current Value. </li>
     *  <li> LowWaterMark is initialized to Current Value. </li>
     * </ul>
     * @param        name        String that indicates the name of this statistic
     * @param        unit        String that indicates the unit of this statistic
     * @param       desc        A brief description of the statistic
     */
    public BoundedRangeStatisticImpl(String name, String unit, String desc, long value, long max, long min) {
        this (value,
                value,
                value,
                max,
                min,
                name,
                unit,
                desc,
                Util.getInitTime()[0],
                Util.getInitTime()[1]
                );

    }

        /**
     * A constructor that creates an instance of class. Following are the defaults:
     * <ul>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     *  <li> UpperBound is initialized to StatisticImpl#DEFAULT_MAX_BOUND. </li>
     *  <li> LowerBound is initialized to StatisticImpl#DEFAULT_MIN_BOUND. </li>
     *  <li> HighWaterMark is initialized to Current Value. </li>
     *  <li> LowWaterMark is initialized to Current Value. </li>
     * </ul>
     * @param        name        String that indicates the name of this statistic
     * @param        unit        String that indicates the unit of this statistic
     */
    public BoundedRangeStatisticImpl(String name, String unit, long value) {
        this(name, unit, value, DEFAULT_MAX_BOUND, DEFAULT_MIN_BOUND);
    }

    /**
     * A constructor that creates an instance of class. Following are the defaults:
     * <ul>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     *  <li> HighWaterMark is initialized to Current Value. </li>
     *  <li> LowWaterMark is initialized to Current Value. </li>
     * </ul>
     * @param        name        String that indicates the name of this statistic
     * @param        unit        String that indicates the unit of this statistic
     * @param        value        long that indicates the initial value of this statistic
     * @param        max            long that indicates the maximum permissible value of this statistic
     * @param        min            long that indicates the minimum permissible value of this statistic
     */
    public BoundedRangeStatisticImpl(String name, String unit, long value,
        long max, long min) {
        this(name, unit, value, max, min, value, value);
    }

    /**
     * A constructor that creates an instance of class. Following are the defaults:
     * <ul>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     * </ul>
     * @param        name        String that indicates the name of this statistic
     * @param        unit        String that indicates the unit of this statistic
     * @param        value        long that indicates the initial value of this statistic
     * @param        max            long that indicates the maximum permissible value of this statistic
     * @param        min            long that indicates the minimum permissible value of this statistic
     * @param        highMark    long that indicates the high watermark value of this statistic
     * @param        lowMark        long that indicates the low watermark value of this statistic
     */
    public BoundedRangeStatisticImpl(String name, String unit, long value,
        long max, long min, long highMark, long lowMark) {
            this (value,
                highMark,
                lowMark,
                max,
                min,
                name,
                unit,
                Util.getDescriptionFromName(name),
                Util.getInitTime()[0],
                Util.getInitTime()[1]
                );
    }

    /**
     * Constructs an immutable instance of BoundedRangeStatisticImpl.
     * @param curVal    The current value of this statistic
     * @param highMark  The highest value of this statistic, since measurement
     *                  started
     * @param lowMark   The lowest value of this statistic, since measurement
     *                  started
     * @param upper     The upper limit of this statistic
     * @param lower     The lower limit of this statistic
     * @param name      The name of the statistic
     * @param unit      The unit of measurement for this statistic
     * @param desc      A brief description of the statistic
     * @param startTime Time in milliseconds at which the measurement was started
     * @param sampleTime Time at which the last measurement was done.
     */
    public BoundedRangeStatisticImpl(long curVal, long highMark, long lowMark,
                                     long upper, long lower, String name,
                                     String unit, String desc, long startTime,
                                     long sampleTime) {
        super(name, unit, desc, startTime, sampleTime);
        currentVal = curVal;
        highWaterMark = highMark;
        lowWaterMark = lowMark;
        upperBound = upper;
        lowerBound = lower;
    }

    /**
     * Returns the current value of this statistic.
     * @return long indicating the current value
     */
    @Override
    public long getCurrent() {
        return currentVal;
    }

    /**
     * Returns the highest value of this statistic, since measurement started.
     * @return long indicating high water mark
     */
    @Override
    public long getHighWaterMark() {
        return highWaterMark;
    }

    /**
     * Returns the lowest value of this statistic, since measurement started.
     * @return long indicating low water mark
     */
    @Override
    public long getLowWaterMark() {
        return lowWaterMark;
    }

    /**
     * Return the lowest possible value, that this statistic is permitted to attain.
     * @return long indicating the lower bound
     */
    @Override
    public long getLowerBound() {
        return lowerBound;
    }

    /**
     * Returns the highest possible value, that this statistic is permitted to attain.
     * @return long indicating the higher bound
     */
    @Override
    public long getUpperBound() {
        return upperBound;
    }

    private static class Util {
        /**
         * A method to get the description from a name. Can be simple property file
         * pair reader. Note that name is invariant, whereas the descriptions are
         * localizable.
         */
        private static String getDescriptionFromName(String name) {
            return (localStrMgr.getString("describes_string") + name);
        }

        /**
         * Returns an array of two longs, that represent the times at the time of call.
         * The idea is not to call expensive System#currentTimeMillis twice for two
         * successive operations.
         */
        private static long[] getInitTime() {
            final long time = System.currentTimeMillis();
            return ( new long[]{time, time} );
        }
    }
}
