/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import java.util.Date;

/**
 * This class holds the metadata for a calendar-based timer.
 */
public class ScheduledTimerDescriptor extends DescribableDescriptor {

    private static final long serialVersionUID = 1L;
    private String second = "0";
    private String minute = "0";
    private String hour = "0";

    private String dayOfMonth = "*";
    private String month = "*";
    private String dayOfWeek = "*";
    private String year = "*";

    private String timezoneID;

    private Date start;

    private Date end;

    private MethodDescriptor timeoutMethod;

    private boolean persistent = true;

    private String info;

    public void setSecond(String second) {
        this.second = second;
    }

    public String getSecond() {
        return second;
    }

    public void setMinute(String minute) {
        this.minute = minute;
    }

    public String getMinute() {
        return minute;
    }

    public void setHour(String hour) {
        this.hour = hour;
    }

    public String getHour() {
        return hour;
    }

    public void setDayOfMonth(String dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }

    public String getDayOfMonth() {
        return dayOfMonth;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getYear() {
        return year;
    }

    public void setTimezone(String timezoneID) {
        this.timezoneID = timezoneID;
    }

    public String getTimezone() {
        return timezoneID;
    }

    public void setStart(Date start) {
        this.start = start == null ? null : new Date(start.getTime());
    }

    public Date getStart() {
        return start == null ? null : new Date(start.getTime());
    }

    public void setEnd(Date end) {
        this.end = end == null ? null : new Date(end.getTime());
    }

    public Date getEnd() {
        return end == null ? null : new Date(end.getTime());
    }

    public void setPersistent(boolean flag) {
        this.persistent = flag;
    }

    public boolean getPersistent() {
        return persistent;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getInfo() {
        return info;
    }


    public void setTimeoutMethod(MethodDescriptor methodDescriptor) {
        this.timeoutMethod = methodDescriptor;
    }


    public MethodDescriptor getTimeoutMethod() {
        return timeoutMethod;
    }

    @Override
    public String toString() {
        return "ScheduledTimerDescriptor [second=" + second
                + ";minute=" + minute
                + ";hour=" + hour
                + ";dayOfMonth=" + dayOfMonth
                + ";month=" + month
                + ";dayOfWeek=" + dayOfWeek
                + ";year=" + year
                + ";timezoneID=" + timezoneID
                + ";start=" + start
                + ";end=" + end
                + ";" + timeoutMethod // MethodDescriptor prints it's name
                + ";persistent=" + persistent
                + ";info=" + info
                + "]";
    }
}
