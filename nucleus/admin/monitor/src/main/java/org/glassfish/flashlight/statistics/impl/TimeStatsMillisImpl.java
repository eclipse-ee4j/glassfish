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

import org.glassfish.flashlight.statistics.TimeStatsMillis;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * @author Harpreet Singh
 */
@Service(name = "timeStatsMillis")
@PerLookup
public class TimeStatsMillisImpl extends TimeStatsAbstractImpl implements TimeStatsMillis {

    private String NAME = "timeStatsMillis";
    private String UNIT = "Milli seconds";
    private String DESCRIPTION = "TimeStatistic Milli Seconds";

    public TimeStatsMillisImpl() {
        super.setName(NAME);
        super.setEnabled(true);
    }

    @Override
    public void entry() {
        super.postEntry(System.currentTimeMillis());
    }

    @Override
    public void exit() {
        super.postExit(System.currentTimeMillis());
    }

    @Override
    public Object getValue() {
        return getTime();
    }

    public String getUnit() {
        return this.UNIT;
    }

    public String getDescription() {
        return this.DESCRIPTION;
    }

    public String toString() {
        return "Statistic " + getClass().getName() + NEWLINE + "Name: " + getName() + NEWLINE + "Description: " + getDescription() + NEWLINE
                + "Unit: " + getUnit() + NEWLINE + "LastSampleTime: " + getLastSampleTime() + NEWLINE + "StartTime: " + getStartTime();
    }
}
