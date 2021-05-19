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

import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Statistic;

/**
 * An implementation of MutableCountStatistic that provides ways to change the state externally
 * through mutators.
 * Convenience class that is useful for components that gather the statistical data.
 *
 * @author Kedar Mhaswade
 * @see CountStatisticImpl for an immutable implementation
 * @since S1AS8.0
 * @version 1.0
 */
public class MutableCountStatisticImpl implements CountStatistic, MutableCountStatistic {

    private final CountStatistic    initial;
    private long                    count;
    private long                    lastSampleTime;
    private long                    startTime;

    /**
     * Constructs an instance of MutableCountStatistic that encapsulates the given Statistic.
     * The only parameter denotes the initial state of this statistic. It is
     * guaranteed that the initial state is preserved internally, so that one
     * can reset to the initial state.
     *
     * @param initial an instance of CountStatistic that represents initial state
     */
    public MutableCountStatisticImpl(CountStatistic initial) {
        this.initial        = initial;
        this.count          = initial.getCount();
        this.lastSampleTime = initial.getLastSampleTime();
        this.startTime      = lastSampleTime;
    }


    /**
     * Resets to the initial state. It is guaranteed that following changes occur
     * to the statistic if this method is called:
     * <ul>
     * <li>The current value is reset to its initial value.</li>
     * <li>The lastSampleTime is reset to <b> current time in milliseconds. </b></li>
     * <li>The startTime is reset to lastSampleTime.</li>
     * </ul>
     * The remaining meta data in the statistic is unchanged.
     */
    @Override
    public void reset() {
        this.count          = initial.getCount();
        this.lastSampleTime = System.currentTimeMillis();
        this.startTime      = this.lastSampleTime;
    }


    /**
     * Changes the value of the encapsulated CountStatistic to the given value.
     * Since this is the only mutator exposed here, here are the other side effects
     * of calling this method:
     * <ul>
     * <li>lastSampleTime is set to <b> current time in milliseconds. </b></li>
     * </ul>
     * In a real-time system with actual probes for measurement, the lastSampleTime
     * could be different from the instant when this method is called, but that is deemed
     * insignificant.
     *
     * @param count long that represents the current value of the Statistic.
     */
    @Override
    public void setCount(long count) {
        this.count = count;
        this.lastSampleTime = System.currentTimeMillis();
    }


    /**
     * This method is the essence of this class. It provides the read-only view of encapsulated
     * Statistic. If the clients have to know the Statistic, this is what should
     * be called by actual data collecting component to return the value to them.
     * The principle advantage is from the data collecting component's standpoint, in
     * that it does not have to create instances of CountStatistic when its
     * current value is queried/measured.
     *
     * @see #reset
     * @see #setCount
     * @return instance of CountStatistic
     */
    @Override
    public Statistic unmodifiableView() {
        return ( new CountStatisticImpl(
            this.count,                 // this is the actual changing statistic
            initial.getName(),          // name does not change
            initial.getUnit(),          // unit does not change
            initial.getDescription(),   // description does not change
            this.lastSampleTime,        // changes all the time!
            this.startTime              // changes if reset is called earlier
        ));
    }

    @Override
    public long getLastSampleTime() {
    return ( this.lastSampleTime );
    }

    @Override
    public long getStartTime() {
    return ( this.startTime );
    }

    @Override
    public String getName() {
    return ( initial.getName() );
    }

    @Override
    public String getDescription() {
    return ( initial.getDescription() );
    }

    @Override
    public String getUnit() {
    return ( initial.getUnit());
    }

    @Override
    public Statistic modifiableView() {
    return ( this );
    }

    @Override
    public long getCount() {
    return ( this.count );
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
