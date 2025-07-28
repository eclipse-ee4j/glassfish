/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.universal;

import org.glassfish.main.jdke.i18n.LocalStringsImpl;

/**
 * Convert a msec duration into weeks, hours, minutes, seconds
 * @author bnevins
 * Thread Safe.
 * Immutable
 */
@Deprecated
public final class Duration {
    public Duration(long msec) {
        long msecLeftover = msec;

        numWeeks = msecLeftover / MSEC_PER_WEEK;
        msecLeftover -= numWeeks * MSEC_PER_WEEK;

        numDays = msecLeftover / MSEC_PER_DAY;
        msecLeftover -= numDays * MSEC_PER_DAY;

        numHours = msecLeftover / MSEC_PER_HOUR;
        msecLeftover -= numHours * MSEC_PER_HOUR;

        numMinutes = msecLeftover / MSEC_PER_MINUTE;
        msecLeftover -= numMinutes * MSEC_PER_MINUTE;

        numSeconds = msecLeftover / MSEC_PER_SECOND;
        msecLeftover -= numSeconds * MSEC_PER_SECOND;

        numMilliSeconds = msecLeftover;
    }

    /**
     * Use more compact output
     * ref: https://glassfish.dev.java.net/issues/show_bug.cgi?id=12606
     */
    final public void setTerse() {
        terse = true;
    }

    @Override
    public String toString() {
        if(terse)
            return toStringTerse();
        else
            return toStringRegular();
    }

    private String toStringTerse() {
        String s = "";

        if(numWeeks > 0)
            s = strings.get("weeks_t", numWeeks, numDays);
        else if(numDays > 0)
            s = strings.get("days_t", numDays);
        else if(numHours > 0)
            s = strings.get("hours_t", numHours, numMinutes);
        else if(numMinutes > 0)
            s = strings.get("minutes_t", numMinutes, numSeconds);
        else if(numSeconds > 0)
            s = strings.get("seconds_t", numSeconds);
        else
            s = strings.get("milliseconds_t", numMilliSeconds);

        return s;
    }

    private String toStringRegular() {
        String s = "";

        if(numWeeks > 0)
            s = strings.get("weeks", numWeeks, numDays, numHours, numMinutes, numSeconds);
        else if(numDays > 0)
            s = strings.get("days", numDays, numHours, numMinutes, numSeconds);
        else if(numHours > 0)
            s = strings.get("hours", numHours, numMinutes, numSeconds);
        else if(numMinutes > 0)
            s = strings.get("minutes", numMinutes, numSeconds);
        else if(numSeconds > 0)
            s = strings.get("seconds", numSeconds);
        else
            s = strings.get("milliseconds", numMilliSeconds);

        return s;
    }

    public final long numWeeks;
    public final long numDays;
    public final long numHours;
    public final long numMinutes;
    public final long numSeconds;
    public final long numMilliSeconds;

    // possibly useful constants
    public final static long MSEC_PER_SECOND = 1000;
    public final static long MSEC_PER_MINUTE = 60 * MSEC_PER_SECOND;
    public final static long MSEC_PER_HOUR = MSEC_PER_MINUTE * 60;
    public final static long MSEC_PER_DAY = MSEC_PER_HOUR * 24;
    public final static long MSEC_PER_WEEK = MSEC_PER_DAY * 7;

    private final LocalStringsImpl strings = new LocalStringsImpl(Duration.class);
    private boolean terse;

}
