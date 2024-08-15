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

import org.glassfish.j2ee.statistics.TimeStatistic;

/**
 * An implementation of a TimeStatistic. All instances of this class are
 * immutable. Provides all the necessary accessors for properties.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 */
public class TimeStatisticImpl extends StatisticImpl implements TimeStatistic {

    private final long count;
    private final long maxTime;
    private final long minTime;
    private final long totTime;
    private static final StringManager localStrMgr =
                StringManager.getManager(TimeStatisticImpl.class);

    @Override
    public final String toString() {
        return super.toString() + NEWLINE +
            "Count: " + getCount() + NEWLINE +
            "MinTime: " + getMinTime() + NEWLINE +
            "MaxTime: " + getMaxTime() + NEWLINE +
            "TotalTime: " + getTotalTime();
    }

    public TimeStatisticImpl(String name) {
        this(name, StatisticImpl.DEFAULT_UNIT);
    }
    public TimeStatisticImpl(String name, String unit) {
        this(StatisticImpl.DEFAULT_VALUE, StatisticImpl.DEFAULT_VALUE,
            StatisticImpl.DEFAULT_VALUE, StatisticImpl.DEFAULT_VALUE, name,  unit,
            Util.getDescriptionFromName(name), Util.getInitTime()[0], Util.getInitTime()[1]);
    }


    /**
     * Constructs an immutable instance of TimeStatistic.
     *
     * @param name The name of the statistic
     * @param unit The unit of measurement for this statistic
     * @param desc A brief description of the statistic
     */
    public TimeStatisticImpl(String name, String unit, String desc) {
        this(StatisticImpl.DEFAULT_VALUE, StatisticImpl.DEFAULT_VALUE,
            StatisticImpl.DEFAULT_VALUE, StatisticImpl.DEFAULT_VALUE, name,  unit,
            desc, Util.getInitTime()[0], Util.getInitTime()[1]);

    }

    /**
     * Constructs an immutable instance of TimeStatistic.
     *
     * @deprecated use the other TimeStatisticImpl constructors.
     *             Counter, maxtime, mintime, totaltime, starttime
     *              last sampletime are automatically calculated
     *              at the first measurement.
     * @param counter   The number of times an operation has been invoked since
     *                  measurement started
     * @param maximumTime   The maximum time it took to complete one invocation
     *                      of an operation, since the measurement started
     * @param minimumTime   The minimum time it took to complete one invocation
     *                      of an opeation, since the measurement started
     * @param totalTime     The total amount of time spent in all invocations,
     *                      over the duration of the measurement
     * @param name  The name of the statistic
     * @param unit  The unit of measurement for this statistic
     * @param desc  A brief description of the statistic
     * @param startTime Time in milliseconds at which the measurement was started
     * @param sampleTime    Time at which the last measurement was done.
     */
    @Deprecated
    public TimeStatisticImpl(long counter, long maximumTime, long minimumTime,
                              long totalTime, String name, String unit,
                             String desc, long startTime, long sampleTime) {

        super(name, unit, desc, startTime, sampleTime);
        count = counter;
        maxTime = maximumTime;
        minTime = minimumTime;
        totTime = totalTime;
    }

    /**
     * Returns the number of times an operation was invoked
     * @return long indicating the number of invocations
     */
    @Override
    public long getCount() {
        return count;
    }

    /**
     * Returns the maximum amount of time that it took for one invocation of an
     * operation, since measurement started.
     * @return long indicating the maximum time for one invocation
     */
    @Override
    public long getMaxTime() {
        return maxTime;
    }

    /**
     * Returns the minimum amount of time that it took for one invocation of an
     * operation, since measurement started.
     * @return long indicating the minimum time for one invocation
     */
    @Override
    public long getMinTime() {
        return minTime;
    }

    /**
     * Returns the amount of time that it took for all invocations,
     * since measurement started.
     * @return long indicating the total time for all invocation
     */
    @Override
    public long getTotalTime() {
        return totTime;
    }

    private static class Util {
        /**
         * A method to get the description from a name. Can be simple property file
         * pair reader. Note that name is invariant, whereas the descriptions are
         * localizable.
         */
        private static String getDescriptionFromName(String name) {
            return (localStrMgr.getString("describes_string")  + name);
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
