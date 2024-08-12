/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.glassfish.flashlight.statistics.impl;

import java.util.concurrent.atomic.AtomicLong;

import org.glassfish.flashlight.datatree.impl.AbstractTreeNode;
import org.glassfish.flashlight.statistics.Average;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Harpreet Singh
 */
@Service(name = "average")
@PerLookup
public class AverageImpl extends AbstractTreeNode implements Average {

    /** DEFAULT_UPPER_BOUND is maximum value Long can attain */
    public static final long DEFAULT_MAX_BOUND = java.lang.Long.MAX_VALUE;
    /** DEFAULT_LOWER_BOUND is same as DEFAULT_VALUE i.e. 0 */
    public static final long DEFAULT_VALUE = java.math.BigInteger.ZERO.longValue();
    public static final long DEFAULT_MIN_BOUND = DEFAULT_VALUE;
    /** DEFAULT_VALUE of any statistic is 0 */
    protected static final String NEWLINE = System.getProperty("line.separator");

    AtomicLong min = new AtomicLong(DEFAULT_MIN_BOUND);
    AtomicLong max = new AtomicLong(0);

    AtomicLong times = new AtomicLong(0);
    AtomicLong sum = new AtomicLong(0);
    private long startTime = 0;
    private AtomicLong lastSampleTime = new AtomicLong(0);

    private String NAME = "average";
    private String DESCRIPTION = "Average RangeStatistic";
    private String UNIT = java.lang.Long.class.toString();

    public AverageImpl() {
        super.name = NAME;
        super.enabled = true;
        startTime = System.currentTimeMillis();
    }

    /*
     *
     * TBD: Remove reference to getSampleTime -> see comment on getSampleTime
     * method
     */
    @Override
    public void addDataPoint(long value) {
        if (min.get() == DEFAULT_MIN_BOUND) // initial seeding
        {
            min.set(value);
        }
        if (value < min.get()) {
            min.set(value);
        } else if (value > max.get()) {
            max.set(value);
        }
        sum.addAndGet(value);
        times.incrementAndGet();
        // TBD: remove this code, once getSampleTime is refactored
        lastSampleTime.set(getSampleTime());
    }

    @Override
    public double getAverage() {
        double total = sum.doubleValue();
        double count = times.doubleValue();
        double avg = total / count;
        return (Double.isNaN(avg) ? 0 : avg);
    }

    @Override
    public void setReset() {
        times.set(0);
        sum.set(0);

    }

    @Override
    public long getMin() {
        return min.get();
    }

    @Override
    public long getMax() {
        return max.get();
    }

    @Override
    public String toString() {
        return "Statistic " + getClass().getName() + NEWLINE + "Name: " + getName() + NEWLINE + "Description: " + getDescription() + NEWLINE
                + "Unit: " + getUnit() + NEWLINE +
                //           "LastSampleTime: " + getLastSampleTime() + NEWLINE +
                "StartTime: " + getStartTime();
    }

    @Override
    public Object getValue() {
        return getAverage();
    }

    @Override
    public long getSize() {
        return times.get();
    }

    @Override
    public long getHighWaterMark() {
        return getMax();
    }

    @Override
    public long getLowWaterMark() {
        return getMin();
    }

    @Override
    public long getCurrent() {
        return Double.valueOf(getAverage()).longValue();
    }

    @Override
    public String getUnit() {
        return this.UNIT;
    }

    @Override
    public String getDescription() {
        return this.DESCRIPTION;
    }

    @Override
    public long getStartTime() {
        return this.startTime;
    }

    @Override
    public long getTotal() {
        return sum.get();

    }

    /*
     * TBD
     * This is an inefficient implementation. Should schedule a Timer task
     * that gets a timeout event every 30s or so and updates this value
     */
    private long getSampleTime() {
        return System.currentTimeMillis();

    }

    @Override
    public long getLastSampleTime() {
        return this.lastSampleTime.longValue();
    }
}
