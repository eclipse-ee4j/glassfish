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

import org.glassfish.j2ee.statistics.BoundedRangeStatistic;
import org.glassfish.j2ee.statistics.Statistic;

/**
 * An implementation of AverageRangeStatistic that provides ways to change the state externally
 * through mutators.
 * Convenience class that is useful for components that gather the statistical data.
 * By merely changing the count (which is a mandatory measurement), rest of the statistical
 * information could be deduced.
 *
 * @author Larry White
 * @author Kedar Mhaswade
 * @see AverageRangeStatisticImpl for an immutable implementation
 * @since S1AS8.1
 * @version 1.0
 */
public class MutableAverageRangeStatisticImpl implements AverageRangeStatistic, MutableCountStatistic {

    /** DEFAULT_UPPER_BOUND is maximum value Long can attain */
    public static final long DEFAULT_MAX_BOUND = java.lang.Long.MAX_VALUE;

    private MutableBoundedRangeStatisticImpl    mutableBoundedRangeStat = null;
    private long                                numberOfSamples;
    private long                                runningTotal;
    private String                              description = null;

    /** Constructs an instance of MutableAverageRangeStatisticImpl that encapsulates the given Statistic.
     * The only parameter denotes the initial state of this statistic. It is
     * guaranteed that the initial state is preserved internally, so that one
     * can reset to the initial state.
     * @param initial           an instance of BoundedRangeStatistic that represents initial state
     */
    public MutableAverageRangeStatisticImpl(BoundedRangeStatistic initial) {
        mutableBoundedRangeStat = new MutableBoundedRangeStatisticImpl(initial);
        numberOfSamples = 0L;
        runningTotal = 0L;
        description = initial.getDescription();
    }

    @Override
    public Statistic modifiableView() {
        return this;
    }

    @Override
    public Statistic unmodifiableView() {
        return ( new AverageRangeStatisticImpl(
            this.getCurrent(),               // this is the actual changing statistic
            this.getHighWaterMark(),         // highWaterMark may change per current
            this.getLowWaterMark(),          // lowWaterMark may change per current
            mutableBoundedRangeStat.getUpperBound(),    // upperBound is not designed to change
            mutableBoundedRangeStat.getLowerBound(),    // lowerBound is not designed to change
            mutableBoundedRangeStat.getName(),          // name does not change
            mutableBoundedRangeStat.getUnit(),          // unit does not change
            mutableBoundedRangeStat.getDescription(),   // description does not change
            this.getLastSampleTime(),        // changes all the time!
            this.getStartTime(),             // changes if reset is called earlier
            this.numberOfSamples,       // this is the current number of samples
            this.runningTotal           // this is the current running total
        ));
    }

    @Override
    public void reset() {
        mutableBoundedRangeStat.reset();
        this.resetAverageStats();
    }

    private void resetAverageStats() {
        numberOfSamples = 0L;
        runningTotal = 0L;
    }

    @Override
    public void setCount(long current) {
        mutableBoundedRangeStat.setCount(current);
        if(DEFAULT_MAX_BOUND - runningTotal < current) {
            this.resetAverageStats();
        }
        numberOfSamples++;
        runningTotal += current;
    }

    @Override
    public long getAverage() {
        if(numberOfSamples == 0) {
            return -1;
        } else {
            return runningTotal / numberOfSamples;
        }
    }

    @Override
    public long getCurrent() {
        return mutableBoundedRangeStat.getCurrent();
    }

    @Override
    public String getDescription() {
        return description;
        //return mutableBoundedRangeStat.getDescription();
    }

    @Override
    public long getHighWaterMark() {
        return mutableBoundedRangeStat.getHighWaterMark();
    }

    @Override
    public long getLastSampleTime() {
        return mutableBoundedRangeStat.getLastSampleTime();
    }

    @Override
    public long getLowWaterMark() {
        long result = mutableBoundedRangeStat.getLowWaterMark();
        if(result == DEFAULT_MAX_BOUND) {
            result = 0L;
        }
        return result;
    }

    @Override
    public String getName() {
        return mutableBoundedRangeStat.getName();
    }

    @Override
    public long getStartTime() {
        return mutableBoundedRangeStat.getStartTime();
    }

    @Override
    public String getUnit() {
        return mutableBoundedRangeStat.getUnit();
    }

}
