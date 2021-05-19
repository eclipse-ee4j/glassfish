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
import org.glassfish.j2ee.statistics.RangeStatistic;

/**
 * An implementation of a RangeStatistic. All instances of this class are
 * immutable. Provides all the necessary accessors for properties.
 *
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @verison 1.0
 */

public final class RangeStatisticImpl extends StatisticImpl implements RangeStatistic {

    private final long currentVal;
    private final long highWaterMark;
    private final long lowWaterMark;

    /**
     * Constructs an immutable instance of RangeStatistic.
     *
     * @param curVal    The current value of this statistic
     * @param highMark  The highest value of this statistic, since measurement
     *                  started
     * @param lowMark   The lowest value of this statistic, since measurement
     *                  started
     * @param name      The name of the statistic
     * @param unit      The unit of measurement for this statistic
     * @param desc      A brief description of the statistic
     * @param startTime Time in milliseconds at which the measurement was started
     * @param sampleTime Time at which the last measurement was done.
     */
    public RangeStatisticImpl(long curVal, long highMark, long lowMark,
                              String name, String unit, String desc,
                              long startTime, long sampleTime) {

        super(name, unit, desc, startTime, sampleTime);
        currentVal = curVal;
        highWaterMark = highMark;
        lowWaterMark = lowMark;
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

    @Override
    public String toString() {
        return super.toString() + NEWLINE +
            "Current: " + getCurrent() + NEWLINE +
            "LowWaterMark: " + getLowWaterMark() + NEWLINE +
            "HighWaterMark: " + getHighWaterMark();
    }

}
