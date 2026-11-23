/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2020 Payara Services Ltd.
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

package com.sun.webui.jsf.model.scheduler;

import com.sun.webui.jsf.util.LogUtil;
import com.sun.webui.jsf.util.ThemeUtilities;

import jakarta.faces.context.FacesContext;

import java.io.Serializable;
import java.util.Calendar;

// Delete the setters once you have reimplemented this not to
// use the default Serializable mechanism, but the same as
// in the converter....
/**
 * Repeat interval.
 */
public final class RepeatInterval implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 6773122235537978959L;

    /**
     * Constant for one time.
     */
    public static final String ONETIME = "ONETIME";

    /**
     * Constant for hourly.
     */
    public static final String HOURLY = "HOURLY";

    /**
     * Constant for daily.
     */
    public static final String DAILY = "DAILY";

    /**
     * Constant for weekly.
     */
    public static final String WEEKLY = "WEEKLY";

    /**
     * Constant for monthly.
     */
    public static final String MONTHLY = "MONTHLY";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * One time repeat interval.
     */
    private static RepeatInterval onetimeRi;

    /**
     * Hourly repeat interval.
     */
    private static RepeatInterval hourlyRi;

    /**
     * Daily repeat interval.
     */
    private static RepeatInterval dailyRi;

    /**
     * Weekly repeat interval.
     */
    private static RepeatInterval weeklyRi;

    /**
     * Monthly repeat interval.
     */
    private static RepeatInterval monthlyRi;

    /**
     * Calendar field.
     */
    private Integer calField;

    /**
     * The key.
     */
    private String key;

    /**
     * The representation.
     */
    private String representation;

    /**
     * The default repeat unit string.
     */
    private String defaultRepeatUnitString;

    /**
     * Create a new instance.
     */
    private RepeatInterval() {
    }

    /**
     * Create a new instance.
     * @param newCalFieldInt calendar
     * @param newKey key
     * @param newRepresentation representation
     * @param newRepeatUnit repeat unit
     */
    private RepeatInterval(final int newCalFieldInt, final String newKey,
            final String newRepresentation, final String newRepeatUnit) {

        if (DEBUG) {
            log("Create new RI");
        }
        this.calField = newCalFieldInt;
        this.key = newKey;
        this.representation = newRepresentation;
        this.defaultRepeatUnitString = newRepeatUnit;
        if (DEBUG) {
            log("Representation is " + this.representation);
        }
    }

    /**
     * Get instance.
     * @param repr representation
     * @return RepeatInterval
     */
    public static RepeatInterval getInstance(final String repr) {
        if (DEBUG) {
            log("getInstance(" + repr + ")");
        }

        if (repr.equals(ONETIME)) {
            if (onetimeRi == null) {
                onetimeRi = new RepeatInterval(-1, "Scheduler.oneTime",
                        ONETIME, null);
            }
            return onetimeRi;
        } else if (repr.equals(HOURLY)) {
            if (hourlyRi == null) {
                hourlyRi = new RepeatInterval(Calendar.HOUR_OF_DAY,
                        "Scheduler.hourly", HOURLY, RepeatUnit.HOURS);
            }
            return hourlyRi;
        }
        if (repr.equals(DAILY)) {
            if (dailyRi == null) {
                dailyRi = new RepeatInterval(Calendar.DATE,
                        "Scheduler.daily", DAILY, RepeatUnit.DAYS);
            }
            return dailyRi;
        }
        if (repr.equals(WEEKLY)) {
            if (weeklyRi == null) {
                weeklyRi = new RepeatInterval(Calendar.WEEK_OF_YEAR,
                        "Scheduler.weekly", WEEKLY, RepeatUnit.WEEKS);
            }
            return weeklyRi;
        }
        if (repr.equals(MONTHLY)) {
            if (monthlyRi == null) {
                monthlyRi = new RepeatInterval(Calendar.MONTH,
                        "Scheduler.monthly", MONTHLY, RepeatUnit.MONTHS);
            }
            return monthlyRi;
        }
        return null;
    }

    /**
     * Getter for property calendarField.
     * @return Value of property calendarField.
     */
    public Integer getCalendarField() {
        return calField;
    }

    /**
     * Setter for property calendarField.
     * @param newCalField new value
     */
    public void setCalendarField(final Integer newCalField) {
        this.calField = newCalField;
    }

    /**
     * Set the key.
     * @param newKey new value
     */
    public void setKey(final String newKey) {
        this.key = newKey;
    }

    /**
     * Get the key.
     * @return String
     */
    public String getKey() {
        return key;
    }

    /**
     * Set the representation.
     * @param newRepresentation new value
     */
    public void setRepresentation(final String newRepresentation) {
        this.representation = newRepresentation;
    }

    /**
     * Get the representation.
     * @return String
     */
    public String getRepresentation() {
        return representation;
    }

    /**
     * Getter for property labelKey.
     * @param context faces context
     * @return Value of property labelKey.
     */
    public String getLabel(final FacesContext context) {
        return ThemeUtilities.getTheme(context).getMessage(key);
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof RepeatInterval)) {
            return false;
        }
        RepeatInterval ri = (RepeatInterval) object;
        if (getCalendarField() == null) {
            if (ri.getCalendarField() != null) {
                return false;
            }
        } else if (!getCalendarField().equals(ri.getCalendarField())) {
            return false;
        }
        if (getRepresentation() == null) {
            if (ri.getRepresentation() != null) {
                return false;
            }
        } else if (!getRepresentation().equals(ri.getRepresentation())) {
            return false;
        }
        if (getKey() == null) {
            if (ri.getKey() != null) {
                return false;
            }
        } else if (!getKey().equals(ri.getKey())) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 71 * hash;
        if (this.calField != null) {
            hash = hash + this.calField.hashCode();
        }
        hash = 71 * hash;
        if (this.key != null) {
            hash = hash + this.key.hashCode();
        }
        hash = 71 * hash;
        if (this.representation != null) {
            hash = hash + this.representation.hashCode();
        }
        hash = 71 * hash;
        if (this.defaultRepeatUnitString != null) {
            hash = hash + this.defaultRepeatUnitString.hashCode();
        }
        return hash;
    }

    /**
     * Get the default repeat unit.
     * @return RepeatUnit
     */
    public RepeatUnit getDefaultRepeatUnit() {
        if (defaultRepeatUnitString == null) {
            return null;
        }
        return RepeatUnit.getInstance(defaultRepeatUnitString);
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(RepeatInterval.class.getName() + "::" + msg);
    }
}
