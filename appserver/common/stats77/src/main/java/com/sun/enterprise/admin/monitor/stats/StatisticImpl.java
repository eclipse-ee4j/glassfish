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

import java.io.Serializable;

import org.glassfish.j2ee.statistics.Statistic;

/**
 * An abstract class providing implementation of the Statistic interface
 * The intent is for this to be subclassed by all the StatisticImpls.
 *
 * @author Muralidhar Vempaty
 * @since S1AS8.0
 * @version 1.0
 */
public abstract class StatisticImpl implements Statistic,Serializable {

    private final String statisticName;
    private String statisticDesc;
    private final String statisticUnit;
    private final long startTime;
    private final long sampleTime;

    /** DEFAULT_UNIT is an empty string */
    public static final String    DEFAULT_UNIT;
    public static final StringManager localStrMgr;
    /** DEFAULT_VALUE of any statistic is 0 */
    public static final long    DEFAULT_VALUE    = java.math.BigInteger.ZERO.longValue();

    static {
        localStrMgr = StringManager.getManager(StatisticImpl.class);
        DEFAULT_UNIT = localStrMgr.getString("count_string");
    }

    protected static final String NEWLINE = System.getProperty( "line.separator" );
    /**
     * Constructor
     * @param name      The name of the statistic
     * @param unit      The unit of measurement for this statistic
     * @param desc      A brief description of the statistic
     * @param startTime Time in milliseconds at which the measurement was started
     * @param sampleTime Time at which the last measurement was done.
     **/
    protected StatisticImpl(String name, String unit, String desc,
                          long start_time, long sample_time) {

        statisticName = name;
        statisticUnit = unit;
        statisticDesc = desc;
        startTime = start_time;
        sampleTime = sample_time;
    }

    /**
     * returns the name of the statistic
     */
    @Override
    public String getName() {
        return this.statisticName;
    }

    /**
     * returns the description of the statistic
     */
    @Override
    public String getDescription() {
        return this.statisticDesc;
    }

    /**
     * returns the unit of measurement for the statistic
     */
    @Override
    public String getUnit() {
        return this.statisticUnit;
    }

    /**
     * returns the time in millis, at which the last measurement was taken
     */
    @Override
    public long getLastSampleTime() {
        return this.sampleTime;
    }

    /**
     * returns the time in millis, at which the first measurement was taken
     */
    @Override
    public long getStartTime() {
        return this.startTime;
    }

    /** This is a hack. This method allows us to internatinalize the descriptions.
        See bug Id: 5045413
    */
    public void setDescription(final String desc) {
        this.statisticDesc = desc;
    }

    @Override
    public String toString() {
        return "Statistic " + getClass().getName() + NEWLINE +
            "Name: " + getName() + NEWLINE +
            "Description: " + getDescription() + NEWLINE +
            "Unit: " + getUnit() + NEWLINE +
            "LastSampleTime: " + getLastSampleTime() + NEWLINE +
            "StartTime: " + getStartTime();
    }
}






