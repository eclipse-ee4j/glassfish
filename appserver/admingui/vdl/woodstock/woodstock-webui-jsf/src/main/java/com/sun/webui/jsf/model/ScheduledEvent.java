/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2007, 2019 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.webui.jsf.model;

import com.sun.webui.jsf.model.scheduler.RepeatInterval;
import com.sun.webui.jsf.model.scheduler.RepeatUnit;
import com.sun.webui.jsf.util.LogUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Scheduled event.
 */
public final class ScheduledEvent implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 3470068141211045713L;

    /**
     * The start time, as a {@link java.util.Date}.
     */
    private Date startTime = null;

    /**
     * The end time, as a {@link java.util.Date}.
     */
    private Date endTime = null;

    /**
     * Date list.
     */
    private ArrayList<Object> dateList = null;

    /**
     * Whether the event is repeating or not.
     */
    private boolean repeatingEvent = false;

    /**
     * Holds value of property frequency.
     */
    private RepeatInterval frequency = null;

    /**
     * The repeat frequency. The value must be the Integer value of a calendar
     * field identifier (Calendar.HOUR_OF_DAY, etc). See
     * {@code java.util.Calendar} for details.
     * <p>
     * To specify that the event repeats for three months... </p>
     */
    private RepeatUnit durationUnit = null;

    /**
     * Holds value of property duration.
     */
    private Integer duration = null;

    /**
     * Debug flag.
     */
    private static final boolean DEBUG = false;

    /**
     * Retrieves the start time, as a {@link java.util.Date}.
     *
     * @return The start time, as a {@link java.util.Date}
     */
    public Date getStartTime() {
        return this.startTime;
    }

    /**
     * Sets the start time.
     *
     * @param newStartTime The start time, as a {@link java.util.Date}
     */
    public void setStartTime(final Date newStartTime) {
        dateList = null;
        this.startTime = newStartTime;
    }

    /**
     * The end time, as a {@link java.util.Date}.
     *
     * @return The end time, as a {@link java.util.Date}
     */
    public Date getEndTime() {
        return this.endTime;
    }

    /**
     * Setter for The end time, as a {@link java.util.Date}.
     *
     * @param newEndTime The end time, as a {@link java.util.Date}
     */
    public void setEndTime(final Date newEndTime) {
        this.endTime = newEndTime;
        dateList = null;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getClass().getName());
        if (startTime != null) {
            buffer.append(": Start time: ");
            buffer.append(startTime.toString());
        } else {
            buffer.append(": No start time.");
        }
        if (endTime != null) {
            buffer.append("\tEnd time: ");
            buffer.append(endTime.toString());
            buffer.append(" ");
        } else {
            buffer.append("\tNo end time. ");
        }
        if (isRepeatingEvent()) {
            buffer.append("\tThis is a repeating event. ");
            buffer.append("\t Repeat frequency (Calendar.field): ");
            buffer.append(String.valueOf(frequency));
            if (duration != null) {
                buffer.append("\tLimited duration of repeats.");
                buffer.append("\tDuration is ");
                buffer.append(String.valueOf(duration));
                buffer.append(" of unit (in Calendar.field) ");
                buffer.append(String.valueOf(durationUnit));
            }
        } else {
            buffer.append("\tThis is not a repeating event. ");
        }
        return buffer.toString();
    }

    /**
     * If true, indicates that this is a repeating event.
     *
     * @return true it this is a repeating event, false otherwise
     */
    public boolean isRepeatingEvent() {
        return this.repeatingEvent;
    }

    /**
     * Invoke this method with the value true to indicate that the event is
     * repeating, false if it is not repeating.
     *
     * @param newRepeatingEvent whether the event is repeating
     */
    public void setRepeatingEvent(final boolean newRepeatingEvent) {
        this.repeatingEvent = newRepeatingEvent;
    }

    /**
     * <p>
     * Get the repeat frequency. The value must be the Integer value of a
     * calendar field identifier (Calendar.HOUR_OF_DAY, etc). See
     * {@code java.util.Calendar} for details. </p>
     * <p>
     * To specify that the event repeats weekly... </p>
     *
     * @return Value of property frequency.
     */
    public RepeatInterval getRepeatInterval() {
        return this.frequency;
    }

    /**
     * Setter for the repeat frequency. The new value must be the Integer value
     * of a calendar field identifier (Calendar.HOUR_OF_DAY, etc). See
     * {@code java.util.Calendar} for details.
     * <p>
     * To specify that the event repeats weekly... </p>
     *
     * @param newFrequency New value of property frequency.
     */
    public void setRepeatInterval(final RepeatInterval newFrequency) {
        this.frequency = newFrequency;
        dateList = null;
    }

    /**
     * Get the unit (hours, weeks, days, etc) for the duration interval of the
     * event. The value must be the Integer value of a calendar field identifier
     * (Calendar.HOUR_OF_DAY, etc). See {@code java.util.Calendar} for details.
     * <p>
     * To specify that the event repeats for three months... </p>
     *
     * @return Value of property durationUnit.
     */
    public RepeatUnit getDurationUnit() {
        return this.durationUnit;
    }

    /**
     * Set the unit (hours, weeks, days, etc) for the duration interval of the
     * event. The value must be the Integer value of a calendar field identifier
     * (Calendar.HOUR_OF_DAY, etc). See {@code java.util.Calendar} for details.
     * <p>
     * To specify that the event repeats for three months... </p>
     *
     * @param newDurationUnit New value of property durationUnit.
     */
    public void setDurationUnit(final RepeatUnit newDurationUnit) {
        this.durationUnit = newDurationUnit;
        dateList = null;
    }

    /**
     * Get the number of units (see DurationUnit) for the duration interval of
     * the event. The value must be the Integer value of a calendar field
     * identifier (Calendar.HOUR_OF_DAY, etc). See {@code java.util.Calendar}
     * for details. To specify that the event repeats for three months...
     *
     * @return Value of property duration.
     */
    public Integer getDuration() {
        return this.duration;
    }

    /**
     * Set the number of units (see DurationUnit) for the duration interval of
     * the event. The value must be the Integer value of a calendar field
     * identifier (Calendar.HOUR_OF_DAY, etc). See {@code java.util.Calendar}
     * for details. To specify that the event repeats weekly...
     *
     * @param newDuration New value of property duration.
     */
    public void setDuration(final Integer newDuration) {
        this.duration = newDuration;
        dateList = null;
    }

    @Override
    public boolean equals(final Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof ScheduledEvent)) {
            return false;
        }
        ScheduledEvent event = (ScheduledEvent) object;
        if (getStartTime() == null) {
            if (event.getStartTime() != null) {
                return false;
            }
        } else if (!getStartTime().equals(event.getStartTime())) {
            return false;
        }
        if (getEndTime() == null) {
            if (event.getEndTime() != null) {
                return false;
            }
        } else if (!getEndTime().equals(event.getEndTime())) {
            return false;
        }
        if (getDuration() == null) {
            if (event.getDuration() != null) {
                return false;
            }
        } else if (!getDuration().equals(event.getDuration())) {
            return false;
        }
        if (getDurationUnit() == null) {
            if (event.getDurationUnit() != null) {
                return false;
            }
        } else if (!getDurationUnit().equals(event.getDurationUnit())) {
            return false;
        }
        if (getRepeatInterval() == null) {
            if (event.getRepeatInterval() != null) {
                return false;
            }
        } else if (!getRepeatInterval().equals(event.getRepeatInterval())) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 23 * hash;
        if (this.startTime != null) {
            hash = hash + this.startTime.hashCode();
        }
        hash = 23 * hash;
        if (this.endTime != null) {
            hash = hash + this.endTime.hashCode();
        }
        hash = 23 * hash;
        if (this.dateList != null) {
            hash = hash + this.dateList.hashCode();
        }
        hash = 23 * hash;
        if (this.repeatingEvent) {
            hash = hash + 1;
        }
        hash = 23 * hash;
        if (this.frequency != null) {
            hash = hash + this.frequency.hashCode();
        }
        hash = 23 * hash;
        if (this.durationUnit != null) {
            hash = hash + this.durationUnit.hashCode();
        }
        hash = 23 * hash;
        if (this.duration != null) {
            hash = hash + this.duration.hashCode();
        }
        return hash;
    }

    /**
     * Returns an iterator of dates which mark the start of scheduled event.If
     * no time has been set, an empty iterator is returned. If a time has been
     * set and the event is not repeating, an iterator with a single date
     * corresponding to the start time is returned provided it is before the
     * date specified in untilDate. If the event is repeating, all start times
     * before untilDate are returned.
     *
     * @param untilDate date filter
     * @return a {@link java.util.Iterator} whose items are
     * {@link java.util.Calendar}
     */
    public Iterator getDates(final Calendar untilDate) {
        return getDates(null, untilDate);
    }

    /**
     * Get an iterator of dates between the specified dates.
     *
     * @param fromDate date filter from
     * @param untilDate date filter until
     * @return Iterator
     */
    public Iterator getDates(final Calendar fromDate,
            final Calendar untilDate) {

        if (dateList != null) {
            return dateList.iterator();
        }

        dateList = new ArrayList<Object>();
        Date date = getStartTime();

        if (DEBUG) {
            if (date != null) {
                log("First event on " + date.toString());
            } else {
                log("No events scheduled");
            }
        }

        Date from = null;
        if (fromDate != null) {
            if (DEBUG) {
                log("Start date is " + fromDate.getTime().toString());
            }
            from = fromDate.getTime();
        } else if (DEBUG) {
            log("No start date");
        }

        if (DEBUG) {
            log("End date is " + untilDate.getTime().toString());
        }

        if (date != null && date.before(untilDate.getTime())) {

            Calendar startDate = (Calendar) (untilDate.clone());
            startDate.setTime(date);

            dateList.add((Calendar) startDate.clone());
            if (DEBUG) {
                log("Added date " + date.toString());
            }

            if (isRepeatingEvent()) {

                int interval = getRepeatInterval().getCalendarField();
                if (interval > -1) {

                    if (DEBUG) {
                        log("Repeating event");
                    }

                    Calendar endCalendar = (Calendar) (untilDate.clone());

                    Integer dur = getDuration();
                    RepeatUnit repeatUnit = getDurationUnit();
                    Integer durUnit = null;
                    if (repeatUnit != null) {
                        durUnit = repeatUnit.getCalendarField();
                    }
                    if (dur != null && durUnit != null) {
                        int durationValue = dur;
                        int durationField = durUnit;
                        if (durationValue > 0) {
                            endCalendar = (Calendar) (startDate.clone());
                            endCalendar.add(durationField, durationValue);
                            // now subtract 1 interval unit from the end date
                            endCalendar.set(interval,
                                    endCalendar.get(interval) - 1);
                            endCalendar.getTime();
                        }
                    }

                    Date end = endCalendar.getTime();
                    if (DEBUG) {
                        log("Using end date " + end.toString());
                    }
                    Date current = startDate.getTime();

                    while (current.before(end)) {
                        startDate.add(interval, 1);
                        current = startDate.getTime();
                        if (from != null) {
                            if (current.after(from)) {
                                dateList.add(startDate.clone());
                                if (DEBUG) {
                                    log("Added date " + current.toString());
                                }
                            }
                        } else {
                            dateList.add(startDate.clone());
                            if (DEBUG) {
                                log("Added date " + current.toString());
                            }
                        }
                    }
                }
            }
        }
        return dateList.iterator();
    }

    /**
     * Log a message to the standard output.
     *
     * @param msg message to log
     */
    private static void log(final String msg) {
        LogUtil.finest(ScheduledEvent.class.getName() + "::" + msg);
    }
}
