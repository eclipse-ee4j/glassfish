/*
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

package com.sun.ejb;

import jakarta.ejb.LockType;

import java.util.concurrent.TimeUnit;

/**
 * MethodLockInfo caches various attributes of lock attributes
 *
 * @author Mahesh Kannan
 */

public class MethodLockInfo {

    private static final int NO_TIMEOUT = -32767;

    private LockType lockType = LockType.WRITE;

    private long timeout = NO_TIMEOUT;

    private TimeUnit timeUnit;

    public MethodLockInfo() {}

    public void setLockType(LockType type) {
        lockType = type;
    }

    public void setTimeout(long value, TimeUnit unit) {
        timeout = value;
        timeUnit = unit;
    }

    public boolean isReadLockedMethod() {
        return (lockType == LockType.READ);
    }

    public boolean isWriteLockedMethod() {
        return (lockType == LockType.WRITE);
    }

    public boolean hasTimeout() {
        return (timeout != NO_TIMEOUT);
    }

    public long getTimeout() {
        return timeout;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public String toString() {
        return lockType + ":" + timeout + ":" + timeUnit;
    }

}
