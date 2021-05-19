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
 * An implementation of MutableCountStatistic that provides ways to change the state externally
 * through mutators.
 * Convenience class that is useful for components that gather the statistical data.
 * By merely changing the count (which is a mandatory measurement), rest of the statistical
 * information could be deduced.
 *
 * @author Kedar Mhaswade
 * @see BoundedRangeStatisticImpl for an immutable implementation
 * @since S1AS8.0
 * @version 1.0
 */
public class MutableBoundedRangeStatisticImpl implements BoundedRangeStatistic, MutableCountStatistic {

    private final BoundedRangeStatistic     initial;
    private long                            current;
    private long                            lastSampleTime;
    private long                            startTime;
    private long                            lowWaterMark;
    private long                            highWaterMark;

    /** Constructs an instance of MutableCountStatistic that encapsulates the given Statistic.
     * The only parameter denotes the initial state of this statistic. It is
     * guaranteed that the initial state is preserved internally, so that one
     * can reset to the initial state.
     * @param initial           an instance of BoundedRangeStatistic that represents initial state
     */
    public MutableBoundedRangeStatisticImpl(BoundedRangeStatistic initial) {
        this.initial        = initial;
        this.current        = initial.getCurrent();
        this.lastSampleTime = initial.getLastSampleTime();
        this.startTime      = initial.getStartTime();
        this.lowWaterMark   = initial.getLowWaterMark();
        this.highWaterMark  = initial.getHighWaterMark();
    }

    /** Resets to the initial state. It is guaranteed that following changes occur
     * to the statistic if this method is called:
     * <ul>
     *  <li> The current value is reset to initial value. </li>
     *  <li> The lastSampleTime is reset to <b> current time in milliseconds. </b> </li>
     *  <li> The startTime is reset to lastSampleTime. </li>
     *  <li> The highWaterMark is reset to the initial value. </li>
     *  <li> The lowWaterMark is reset to the initial value. </li>
     * </ul>
     * The remaining meta data in the encapsulated statistic is unchanged. The
     * upper and lower bounds are untouched.
    */
    @Override
    public void reset() {
        this.current                = initial.getCurrent();
        this.lastSampleTime         = System.currentTimeMillis();
        this.startTime              = this.lastSampleTime;
        this.highWaterMark          = initial.getHighWaterMark();
        this.lowWaterMark           = initial.getLowWaterMark();
    }

    /** Changes the current value of the encapsulated BoundedRangeStatistic to the given value.
     * Since this is the only mutator exposed here, here are the other side effects
     * of calling this method:
     * <ul>
     *  <li> lastSampleTime is set to <b> current time in milliseconds. </b> </li>
     *  <li> highWaterMark is accordingly adjusted. </li>
     *  <li> lowWaterMark is accordingly adjusted. </li>
     * </ul>
     * In a real-time system with actual probes for measurement, the lastSampleTime
     * could be different from the instant when this method is called, but that is deemed insignificant.
     * @param count         long that represents the current value of the Statistic.
     */
    @Override
    public void setCount(long current) {
        this.current            = current;
        this.lastSampleTime     = System.currentTimeMillis();

        this.lowWaterMark   = (current < this.lowWaterMark) ? (current) : (this.lowWaterMark);
        this.highWaterMark  = (current > this.highWaterMark) ? (current) : (this.highWaterMark);
    this.lastSampleTime = System.currentTimeMillis();
    }

    /** This method is the essence of this class. It provides the read-only view of encapsulated
     * Statistic. If the clients have to know the Statistic, this is what should
     * be called by actual data collecting component to return the value to them.
     * The principle advantage is from the data collecting component's standpoint, in
     * that it does not have to create instances of BoundedRangeStatistic when its
     * current value is queried/measured.
     * @see #reset
     * @see #setCount
     * @return      instance of BoundedRangeStatistic
     */
    @Override
    public Statistic unmodifiableView() {
        return ( new BoundedRangeStatisticImpl(
            this.current,               // this is the actual changing statistic
            this.highWaterMark,         // highWaterMark may change per current
            this.lowWaterMark,          // lowWaterMark may change per current
            initial.getUpperBound(),    // upperBound is not designed to change
            initial.getLowerBound(),    // lowerBound is not designed to change
            initial.getName(),          // name does not change
            initial.getUnit(),          // unit does not change
            initial.getDescription(),   // description does not change
            this.startTime,              // changes if reset is called earlier
            this.lastSampleTime        // changes all the time!
        ));
    }

    @Override
    public String getDescription() {
    return ( initial.getDescription());
    }

    @Override
    public long getLastSampleTime() {
    return ( this.lastSampleTime );
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
    public String getUnit() {
    return ( initial.getUnit() );
    }

    @Override
    public Statistic modifiableView() {
    return ( this );
    }

    @Override
    public long getCurrent() {
    return ( this.current );
    }

    @Override
    public long getHighWaterMark() {
    return ( this.highWaterMark );
    }

    @Override
    public long getLowWaterMark() {
    return ( this.lowWaterMark );
    }

    @Override
    public long getLowerBound() {
    return ( initial.getLowerBound() );
    }

    @Override
    public long getUpperBound() {
    return ( initial.getUpperBound() );
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
