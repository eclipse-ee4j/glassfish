/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tests.progress;

import java.util.ArrayList;
import java.util.Collection;
import java.util.regex.Pattern;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.Progress;
import org.glassfish.api.admin.ProgressStatus;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * @author mmares
 */
@Service(name = "progress-custom")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("progress")
@Progress
public class ProgressCustomCommand implements AdminCommand {

    /** Value must be in for {@code [Nx][MINSEC-]MAXSEC}
     */
    @Param(primary=true, optional=true, multiple=true, defaultValue="0")
    String[] intervals;

    private static final Pattern keyPattern = Pattern.compile("([\\ds]+x){0,1}(\\d+-){0,1}\\d+");

    private static class Interval {

        private boolean valid = true;
        private String origInterval;
        private int multiplicator = 1;
        private int minSec = -1;
        private int maxSec = 0;
        private boolean spin = false;

        private Interval(String interval) {
            origInterval = interval;
            try {
                if (!keyPattern.matcher(interval).matches()) {
                    valid = false;
                    return;
                }
                int ind = interval.indexOf('x');
                if (ind > 0) {
                    String substring = interval.substring(0, ind);
                    if (substring.contains("s")) {
                        this.spin = true;
                    } else {
                        multiplicator = Integer.parseInt(substring);
                    }
                    interval = interval.substring(ind + 1);
                }
                ind = interval.indexOf('-');
                if (ind > 0) {
                    minSec = Integer.parseInt(interval.substring(0, ind));
                    interval = interval.substring(ind + 1);
                }
                if (!interval.isEmpty()) {
                    maxSec = Integer.parseInt(interval);
                }
                if (minSec > maxSec) {
                    int tmp = minSec;
                    minSec = maxSec;
                    maxSec = tmp;
                }
            } catch (Exception ex) {
                valid  = false;
            }
        }

        public boolean isSpin() {
            return this.spin;
        }

        public int getMultiplicator() {
            if (valid) {
                return multiplicator;
            }
            return 1;
        }

        public boolean isValid() {
            return valid;
        }

        public int getMaxSec() {
            return maxSec;
        }

        public long getMilis() {
            if (!valid) {
                return 0L;
            }
            if (minSec < 0) {
                return maxSec * 1000L;
            }
            return Math.round(Math.random() * ((maxSec - minSec) * 1000L)) + (minSec * 1000L);
        }

        @Override
        public String toString() {
            return origInterval;
        }

    }

    private Collection<Interval> parsedIntervals;

    private int getStepCount() {
        if (parsedIntervals == null) {
            return 0;
        }
        int result = 0;
        for (Interval interval : parsedIntervals) {
            result += interval.getMultiplicator();
        }
        return result;
    }

    @Override
    public void execute(AdminCommandContext context) {
        ProgressStatus ps = context.getProgressStatus();
        parsedIntervals = new ArrayList<>(intervals != null ? intervals.length : 0);
        ActionReport report = context.getActionReport();
        for (String interval : intervals) {
            parsedIntervals.add(new Interval(interval));
        }
        //Count
        if (parsedIntervals.isEmpty()) {
            report.setMessage("Done command process without waiting interval.");
            return;
        }
        ps.setTotalStepCount(getStepCount());
        int blockId = 0;
        for (Interval interval : parsedIntervals) {
            blockId++;
            if (interval.isValid()) {
                int multip = interval.getMultiplicator();
                if (interval.getMaxSec() == 0) {
                    ps.progress(multip, "Finished block without sleeping: [" + blockId + "] " + interval);
                } else {
                    for (int i = 0; i < multip; i++) {
                        if (i == 0) {
                            ps.progress(0, "Starting block [" + blockId + "] " + interval, interval.isSpin());
                        }
                        try {
                            Thread.sleep(interval.getMilis());
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                        if (i == (multip - 1)) {
                            ps.progress(1, "Finished block [" + blockId + "] " + interval);
                        } else {
                            ps.progress(1, "Block [" + blockId + "] " + interval + ", step: " + (i + 1));
                        }
                    }
                }
            } else {
                ps.progress(1, "Finished unparsable block [" +blockId + "] " + interval);
            }
        }
        report.setMessage("Finished command process in " + parsedIntervals.size() + " block(s).");
    }

}
