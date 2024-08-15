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
import org.glassfish.j2ee.statistics.Statistic;
import org.glassfish.j2ee.statistics.TimeStatistic;


/**
 * An implementation of {@link MutableTimeStatistic} that eases the various
 * statistical calculations.
 *
 * @author <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.2 $
 */
public class MutableTimeStatisticImpl implements TimeStatistic, MutableTimeStatistic {

    private final TimeStatistic initial;
    private long methodCount;
    private long min;
    private long max;
    private long total; //possibility of an overflow?
    private long lastSampleTime;

    /**
     * Constructs an instance of this class from its immutable equivalent. Note that there are
     * some constraints on the parameter passed:
     * <ul>
     * <li>The maxTime, minTime and totTime of param must be same</li>
     * </ul>
     *
     * @param instance of (immutable) {@link TimeStatistic}
     */
    public MutableTimeStatisticImpl(TimeStatistic initial) {
        this.initial        = initial;
        methodCount         = initial.getCount();
        min    = initial.getMinTime();
        max    = initial.getMaxTime();
        total  = initial.getTotalTime();
        final boolean minMax = min == max;
        final boolean minTot = min == total;
        if (! (minMax && minTot)) {
            throw new IllegalArgumentException("Invalid initial values: " + min + ", " + max + ", " + total);
        }
        lastSampleTime = initial.getLastSampleTime();
    }


    /**
     * Increments the count of operation execution by 1 and also increases the time
     * consumed. A successful execution of method will have all the data updated as:
     * <ul>
     * <li>method count ++</li>
     * <li>max time, min time and total time are accordingly adjusted</li>
     * </ul>
     *
     * @param current long indicating time in whatever unit this statistic is calculated
     */
    @Override
    public void incrementCount(long current) {
        if (methodCount == 0) {
            total = max = min = current;
        } else {
            total += current;
            max = current >= max ? current : max;
            min = current >= min ? min : current;
        }
        methodCount++;
        lastSampleTime = System.currentTimeMillis();
    }


    /**
     * Resets the Statistic. Calling this method has following effect:
     * <ul>
     * <li>Initial state of this Statistic is restored as far as Count, Minimum/Maximum
     * and Total time of execution is considered.</li>
     * </ul>
     */
    @Override
    public void reset() {
        methodCount         = initial.getCount();
        min                 = initial.getMinTime();
        max                 = initial.getMaxTime();
        total               = initial.getTotalTime();
        lastSampleTime        = initial.getLastSampleTime();
    }


    /**
     * This method is the essence of this class. Returns the unmodifiable view
     * of this instance.
     *
     * @return an instance of {@link TimeStatistic}
     */
    @Override
    public Statistic unmodifiableView() {
        return new TimeStatisticImpl(
            this.methodCount,
            this.max,
            this.min,
            this.total,
            initial.getName(),
            initial.getUnit(),
            initial.getDescription(),
            initial.getStartTime(),
            this.lastSampleTime
        );
    }

    @Override
    public Statistic modifiableView() {
        return ( this );
    }

    @Override
    public long getCount() {
        return ( this.methodCount);
    }

    @Override
    public String getDescription() {
        return ( initial.getDescription() );
    }

    @Override
    public long getLastSampleTime() {
        return ( this.lastSampleTime );
    }

    @Override
    public long getMaxTime() {
        return ( this.max );
    }

    @Override
    public long getMinTime() {
        return ( this.min );
    }

    @Override
    public String getName() {
        return ( initial.getName() );
    }

    @Override
    public long getStartTime() {
        return ( initial.getStartTime() );
    }

    @Override
    public long getTotalTime() {
        return ( this.total );
    }

    @Override
    public String getUnit() {
        return ( initial.getUnit() );
    }
    /* hack: bug 5045413 */
    public void setDescription (final String s) {
        try {
            ((StatisticImpl)this.initial).setDescription(s);
        }
        catch(final Exception e) {
        }
    }
}
