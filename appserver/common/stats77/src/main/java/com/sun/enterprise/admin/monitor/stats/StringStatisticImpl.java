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

/**
 * A basic implementation of the StringStatistic Interface. All the instances
 * of this class are immutable.
 * @author Muralidhar Vempaty
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */
public final class StringStatisticImpl extends StatisticImpl implements StringStatistic {

    private final String strVal;

    /**
     * Constructor
     * @param str    The current value of this statistic
     * @param name      The name of the statistic
     * @param unit      The unit of measurement for this statistic
     * @param desc      A brief description of the statistic
     * @param startTime Time in milliseconds at which the measurement was started
     * @param sampleTime Time at which the last measurement was done.
     **/
    public StringStatisticImpl(String str, String name, String unit,
                               String desc, long startTime, long sampleTime) {

        super(name, unit, desc, startTime, sampleTime);
        strVal = str;
    }

    /**
     * Returns the String value of the statistic.
     */
    public String getCurrent() {
        return strVal;
    }

    public String toString() {
        return super.toString() + NEWLINE +
            "Current: " + getCurrent();
    }

}
