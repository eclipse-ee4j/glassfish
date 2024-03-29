/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.enterprise.deployment.ScheduledTimerDescriptor;

import jakarta.ejb.ScheduleExpression;

import org.glassfish.common.util.timer.TimerSchedule;

/**
 * A runtime representation of the user-defined calendar-based timeout expression for an enterprise
 * bean timer.
 *
 * @author mvatkina
 */
public class EJBTimerSchedule extends TimerSchedule {

    private boolean automatic;
    private String methodName;
    private int paramCount;

    /** Construct EJBTimerSchedule instance with all defaults.
     */
    public EJBTimerSchedule() {
        super();
    }

    /** Construct EJBTimerSchedule instance from a given ScheduleExpression.
      * Need to copy all values because ScheduleExpression is mutable
      * and can be modified by the user.
      */
    public EJBTimerSchedule(ScheduleExpression se) {
        second(se.getSecond());
        minute(se.getMinute());
        hour(se.getHour());
        dayOfMonth(se.getDayOfMonth());
        month(se.getMonth());
        dayOfWeek(se.getDayOfWeek());
        year(se.getYear());
        timezone(se.getTimezone());

        // Create local copies
        start(se.getStart());
        end(se.getEnd());

        configure();
    }

    /** Construct EJBTimerSchedule instance from a given Schedule annotation.
      */
    public EJBTimerSchedule(ScheduledTimerDescriptor sd, String methodName, int paramCount) {
        second(sd.getSecond());
        minute(sd.getMinute());
        hour(sd.getHour());
        dayOfMonth(sd.getDayOfMonth());
        month(sd.getMonth());
        dayOfWeek(sd.getDayOfWeek());
        year(sd.getYear());
        timezone(sd.getTimezone());
        start(sd.getStart());
        end(sd.getEnd());

        this.methodName = methodName;
        this.paramCount = paramCount;
        this.automatic = true;

        configure();
    }

    /** Construct EJBTimerSchedule instance with all defaults.
     * The subclass will call back for additional parsing.
     */
    public EJBTimerSchedule(String s) {
        super(s);

        // Parse the rest of elements
        String[] sp = s.split(" # ");
        automatic = Boolean.parseBoolean(sp[10]);

        if (sp.length == 13) {
            methodName = sp[11];
            paramCount = Integer.parseInt(sp[12]);
        }
    }

    public EJBTimerSchedule setAutomatic(boolean b) {
        automatic = b;
        return this;
    }

    public boolean isAutomatic() {
        return automatic;
    }

    public String getTimerMethodName() {
        return methodName;
    }

    public int getMethodParamCount() {
        return paramCount;
    }

    @Override
    public String getScheduleAsString() {
        StringBuffer s = new StringBuffer(super.getScheduleAsString())
               .append(" # ").append(automatic);

        if (automatic) {
            s.append(" # ").append(methodName).append(" # ").append(paramCount);
        }

        return s.toString();
    }

    public ScheduleExpression getScheduleExpression() {
        return new ScheduleExpression().
                second(getSecond()).
                minute(getMinute()).
                hour(getHour()).
                dayOfMonth(getDayOfMonth()).
                month(getMonth()).
                dayOfWeek(getDayOfWeek()).
                year(getYear()).
                timezone(getTimeZoneID()).
                start(getStart()).
                end(getEnd());

    }

    @Override
    public int hashCode() {
        return getScheduleAsString().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof EJBTimerSchedule) && super.equals(o);
    }

    /**
     * Returns true if this Schedule can calculate its next timeout
     * without errors.
     */
    public static boolean isValid(ScheduledTimerDescriptor s) {
        EJBTimerSchedule ts = new EJBTimerSchedule(s, null, 0);
        ts.getNextTimeout();

        return true;
    }

    @Override
    protected boolean isExpectedElementCount(String[] el) {
        return el.length == 11 || el.length == 13;
    }

}
