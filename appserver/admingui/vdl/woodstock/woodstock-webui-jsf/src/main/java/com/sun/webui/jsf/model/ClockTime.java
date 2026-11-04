/*
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

import java.io.Serializable;

/**
 * Clock time model.
 */
public final class ClockTime implements Serializable {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 3125735012249146691L;

    /**
     * Holds value of property hour.
     */
    private Integer hour;

    /**
     * Holds value of property minute.
     */
    private Integer minute;

    /**
     * Creates a new instance of ClockTime.
     */
    public ClockTime() {
    }

    /**
     * Getter for property hour.
     * @return Value of property hour.
     */
    public Integer getHour() {
        return this.hour;
    }

    /**
     * Setter for property hour.
     * @param newHour New value of property hour.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public void setHour(final Integer newHour) {
        if (newHour > -1 && newHour < 24) {
            this.hour = newHour;
        } else {
            throw new RuntimeException();
        }
    }

    /**
     * Getter for property minute.
     * @return Value of property minute.
     */
    public Integer getMinute() {
        return this.minute;
    }

    /**
     * Setter for property minute.
     * @param newMinute New value of property minute.
     */
    @SuppressWarnings("checkstyle:magicnumber")
    public void setMinute(final Integer newMinute) {
        if (newMinute > -1 && newMinute < 60) {
            this.minute = newMinute;
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof ClockTime) {
            return (((ClockTime) obj).getHour().equals(hour)
                    && ((ClockTime) obj).getMinute().equals(minute));
        }
        return false;
    }

    @Override
    @SuppressWarnings("checkstyle:magicnumber")
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash;
        if (this.hour != null) {
            hash = hash + this.hour.hashCode();
        }
        hash = 31 * hash;
        if (this.minute != null) {
            hash = hash + this.minute.hashCode();
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(this.getClass().getName());
        buffer.append(": ");
        buffer.append(String.valueOf(hour));
        buffer.append(":");
        buffer.append(String.valueOf(minute));
        return buffer.toString();
    }
}
