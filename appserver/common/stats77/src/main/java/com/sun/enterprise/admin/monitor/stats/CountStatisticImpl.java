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

import org.glassfish.j2ee.statistics.CountStatistic;

/** An implementation of a CountStatistic. All instances of this class are
 * immutable. Provides all the necessary accessors for properties.
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @verison 1.0
 */

public class CountStatisticImpl extends StatisticImpl implements CountStatistic {

    private long count;
    private static final StringManager localStrMgr =
                StringManager.getManager(CountStatisticImpl.class);

    /**
     * Constructs an instance of this class with following default values:
     * <ul>
     *    <li> Unit is empty string. </li>
     *    <li> Current Value is StatisticImpl#DEFAULT_VALUE. </li>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     * </ul>
     * @param    name            String indicating the name of the statistic
     */
    public CountStatisticImpl(String name) {
        this(name, DEFAULT_UNIT);
    }
    /**
     * Constructs an instance of this class with following default values:
     * <ul>
     *    <li> Current Value is StatisticImpl#DEFAULT_VALUE. </li>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     * </ul>
     * @param    name            String indicating the name of the statistic
     * @param    unit            String indicating the unit of the statistic
     */
    public CountStatisticImpl(String name, String unit) {
        this(name, unit, DEFAULT_VALUE);
    }
    /**
     * Constructs an instance of this class with following default values:
     * <ul>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     * </ul>
     * @param    name            String indicating the name of the statistic
     * @param    unit            String indicating the unit of the statistic
     * @param   desc            A brief description of the statistic
     */
    public CountStatisticImpl(String name, String unit, String desc) {
        this(DEFAULT_VALUE, name, unit, desc, Util.getInitTime()[0], Util.getInitTime()[1]);
    }
    /**
     * Constructs an instance of this class with following default values:
     * <ul>
     *    <li> Description is calculated from the name passed in. This may well be read from a properties file to address i18n. </li>
     *    <li> LastSampleTime is time at the time of calling this method.</li>
     *    <li> StartTime is the same as LastSampleTime. </li>
     * </ul>
     * @param    name            String indicating the name of the statistic
     * @param    unit            String indicating the unit of the statistic
     * @param    value            long indicating the unit of the statistic
     */
    public CountStatisticImpl(String name, String unit, long value) {
        this(value, name, unit, Util.getDescriptionFromName(name), Util.getInitTime()[0], Util.getInitTime()[1]);
    }

    /** Constructs an immutable instance of CountStatistic with given parameters.
     * @param curVal    The current value of this statistic
     * @param name      The name of the statistic
     * @param unit      The unit of measurement for this statistic
     * @param desc      A brief description of the statistic
     * @param startTime Time in milliseconds at which the measurement was started
     * @param sampleTime Time at which the last measurement was done.
     **/
    public CountStatisticImpl(long countVal, String name, String unit,
                              String desc, long sampleTime, long startTime) {

        super(name, unit, desc, startTime, sampleTime);
        count = countVal;
    }

    public String toString() {
        return super.toString() + NEWLINE + "Count: " + getCount();
    }


    /**
     * Returns the current value of this statistic.
     * @return long indicating current value
     */
    public long getCount() {
        return count;
    }

    private static class Util {
        /** A method to get the description from a name. Can be simple property file
         * pair reader. Note that name is invariant, whereas the descriptions are
         * localizable.
         */
        private static String getDescriptionFromName(String name) {
            return (localStrMgr.getString("describes_string") + name);
        }

        /** Returns an array of two longs, that represent the times at the time of call.
         * The idea is not to call expensive System#currentTimeMillis twice for two
         * successive operations.
         */
        private static long[] getInitTime() {
            final long time = System.currentTimeMillis();
            return ( new long[]{time, time} );
        }
    }
}
