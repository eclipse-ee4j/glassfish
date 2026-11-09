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

/**
 * Repeat unit.
 */
public final class RepeatUnit implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = -8055799403044778734L;

    /**
     * Constant for {@code HOURS}.
     */
    public static final String HOURS = "HOURS";

    /**
     * Constant for {@code DAYS}.
     */
    public static final String DAYS = "DAYS";

    /**
     * Constant for {@code WEEKS}.
     */
    public static final String WEEKS = "WEEKS";

    /**
     * Constant for {@code MONTHS}.
     */
    public static final String MONTHS = "MONTHS";

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Hours repeat unit.
     */
    private static RepeatUnit hoursRi;

    /**
     * Days repeat unit.
     */
    private static RepeatUnit daysRi;

    /**
     * Weeks repeat unit.
     */
    private static RepeatUnit weeksRi;

    /**
     * Months repeat unit.
     */
    private static RepeatUnit monthsRi;

    /**
     * Calendar field.
     */
    private Integer calField;

    /**
     * Calendar key.
     */
    private String key;

    /**
     * Calendar representation.
     */
    private String representation;

    /**
     * Create a new instance.
     */
    public RepeatUnit() {
    }

    /**
     * Create a new instance.
     * @param newCalField calendar field
     * @param newKey calendar key
     * @param newRepresentation calendar representation
     */
    private RepeatUnit(final int newCalField, final String newKey,
            final String newRepresentation) {

        if (DEBUG) {
            log("Create new RU");
        }
        this.calField = newCalField;
        this.key = newKey;
        this.representation = newRepresentation;
        if (DEBUG) {
            log("Representation is " + this.representation);
        }
    }

    /**
     * Get a repeat unit.
     * @param newRepresentation representation
     * @return RepeatUnit
     */
    public static RepeatUnit getInstance(final String newRepresentation) {

        if (DEBUG) {
            log("getInstance(" + newRepresentation + ")");
        }

        if (newRepresentation.equals(HOURS)) {
            if (hoursRi == null) {
                hoursRi = new RepeatUnit(Calendar.HOUR_OF_DAY,
                        "Scheduler.hours", HOURS);
            }
            return hoursRi;
        }
        if (newRepresentation.equals(DAYS)) {
            if (daysRi == null) {
                daysRi = new RepeatUnit(Calendar.DATE,
                        "Scheduler.days", DAYS);
            }
            return daysRi;
        }
        if (newRepresentation.equals(WEEKS)) {
            if (weeksRi == null) {
                weeksRi = new RepeatUnit(Calendar.WEEK_OF_YEAR,
                        "Scheduler.weeks", WEEKS);
            }
            return weeksRi;
        }
        if (newRepresentation.equals(MONTHS)) {
            if (monthsRi == null) {
                monthsRi = new RepeatUnit(Calendar.MONTH,
                        "Scheduler.months", MONTHS);
            }
            return monthsRi;
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
     * @param newCalField new calendar field
     */
    public void setCalendarField(final Integer newCalField) {
        this.calField = newCalField;
    }

    /**
     * Set the key.
     * @param newKey key
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
     * @param newRepresentation representation
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
        if (!(object instanceof RepeatUnit)) {
            return false;
        }

        RepeatUnit ru = (RepeatUnit) object;

        if (getKey() == null) {
            if (ru.getKey() != null) {
                return false;
            }
        } else if (!getKey().equals(ru.getKey())) {
            return false;
        }

        if (getRepresentation() == null) {
            if (ru.getRepresentation() != null) {
                return false;
            }
        } else if (!getRepresentation().equals(ru.getRepresentation())) {
            return false;
        }

        if (getCalendarField() == null) {
            if (ru.getCalendarField() != null) {
                return false;
            }
        } else if (!getCalendarField().equals(ru.getCalendarField())) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash;
        if (this.calField != null) {
            hash = hash + this.calField.hashCode();
        }
        hash = 29 * hash;
        if (this.key != null) {
            hash = hash + this.key.hashCode();
        }
        hash = 29 * hash;
        if (this.representation != null) {
            hash = hash + this.representation.hashCode();
        }
        return hash;
    }

    /**
     * Log a message to the standard output.
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(RepeatUnit.class.getName() + "::" + msg);
    }
}
