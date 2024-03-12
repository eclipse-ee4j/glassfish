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

/** An implementation of a AverageRangeStatistic. All instances of this class are
 * immutable. Provides all the necessary accessors for properties.
 * @author Larry White
 * @author Kedar Mhaswade
 * @since S1AS8.1
 * @version 1.0
 */

/**
 *
 * @author  lwhite
 */
public class AverageRangeStatisticImpl implements
    AverageRangeStatistic /*BoundedRangeStatistic*/ {

    private BoundedRangeStatisticImpl boundedRangeStatistic = null;
    private long                                numberOfSamples;
    private long                                runningTotal;


    /**
     * Constructs an immutable instance of AverageRangeStatisticImpl.
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
     * @param numberOfSamples number of samples at present
     * @param runningTotal running total of sampled data at present
     **/
    public AverageRangeStatisticImpl(long curVal, long highMark, long lowMark,
                                     long upper, long lower, String name,
                                     String unit, String desc, long startTime,
                                     long sampleTime, long numberOfSamples,
                                     long runningTotal) {

        boundedRangeStatistic = new BoundedRangeStatisticImpl(curVal, highMark, lowMark,
                                     upper, lower, name,
                                     unit, desc, startTime,
                                     sampleTime);

        this.numberOfSamples = numberOfSamples;
        this.runningTotal = runningTotal;
    }

    /**
     * Constructs an immutable instance of AverageRangeStatisticImpl.
     * @param stats a BoundedRangeStatisticImpl
     * @param numberOfSamples number of samples at present
     * @param runningTotal running total of sampled data at present
     **/
    public AverageRangeStatisticImpl(BoundedRangeStatisticImpl stats,
                long numberOfSamples, long runningTotal) {
        boundedRangeStatistic = stats;
        this.numberOfSamples = numberOfSamples;
        this.runningTotal = runningTotal;
    }

    public long getCurrent() {
        return boundedRangeStatistic.getCurrent();
    }

    public String getDescription() {
        return boundedRangeStatistic.getDescription();
    }

    public long getHighWaterMark() {
        return boundedRangeStatistic.getHighWaterMark();
    }

    public long getLastSampleTime() {
        return boundedRangeStatistic.getLastSampleTime();
    }

    public long getLowWaterMark() {
        return boundedRangeStatistic.getLowWaterMark();
    }
/*
    public long getLowerBound() {
        return boundedRangeStatistic.getLowerBound();
    }
     */

    public String getName() {
        return boundedRangeStatistic.getName();
    }

    public long getStartTime() {
        return boundedRangeStatistic.getStartTime();
    }

    public String getUnit() {
        return boundedRangeStatistic.getUnit();
    }
/*
    public long getUpperBound() {
        return boundedRangeStatistic.getUpperBound();
    }
     */

    public long getAverage() {
        if(numberOfSamples == 0) {
            return -1;
        } else {
            return runningTotal / numberOfSamples;
        }
    }
    /** This is a hack. This method allows us to internatinalize the descriptions.
        See bug Id: 5045413
    */
    public void setDescription(final String desc) {
        this.boundedRangeStatistic.setDescription(desc);
    }

}
