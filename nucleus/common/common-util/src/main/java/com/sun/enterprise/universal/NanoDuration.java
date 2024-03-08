/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

/**
 * Convert a nanosec duration into something readable
 * @author bnevins
 * Thread Safe.
 * Immutable
 */
public final class NanoDuration {
    public NanoDuration(long nsec) {
        // if > 1 minute -- use Duration
        if(nsec >= NSEC_PER_MINUTE) {
            duration = new Duration(nsec / ((long)NSEC_PER_MILLISECOND));
            return;
        }

        double ns = nsec;

        if(ns >= NSEC_PER_SECOND)
            numSeconds = ns / NSEC_PER_SECOND;

        else if(ns >= NSEC_PER_MILLISECOND)
            numMilliSeconds = ns / NSEC_PER_MILLISECOND;

        else if(ns >= NSEC_PER_MICROSECOND)
            numMicroSeconds = ns / NSEC_PER_MICROSECOND;

        else
            numNanoSeconds = nsec; // not a double!
    }

    @Override
    public String toString() {
        if(duration != null)
            return duration.toString();

        final String fmt = "%.2f %s";
        String s;

        if(numSeconds > 0.0)
            s = String.format(fmt, numSeconds, "seconds");
        else if(numMilliSeconds > 0.0)
            s = String.format(fmt, numMilliSeconds, "msec");
        else if(numMicroSeconds > 0.0)
            s = String.format(fmt, numMicroSeconds, "usec");
        else if(numNanoSeconds > 0.0)
            s = String.format(fmt, numNanoSeconds, "nsec");
        else
            s = String.format(fmt, 0.0, "nsec"); // unlikely!

        return s;
    }

    // possibly useful constants
    public final static double NSEC_PER_MICROSECOND = 1000;
    public final static double NSEC_PER_MILLISECOND = 1000 * 1000;
    public final static double NSEC_PER_SECOND = 1000 * 1000 * 1000;
    public final static double NSEC_PER_MINUTE = 60 * 1000 * 1000 * 1000;

    private double numSeconds = -1.0;
    private double numMilliSeconds = -1.0;
    private double numMicroSeconds = -1.0;
    private double numNanoSeconds = -1.0;
    private Duration duration;
}
