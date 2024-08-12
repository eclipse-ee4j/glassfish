/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.management.support;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * See also com.sun.enterprise.management.support.SystemInfoImpl
 *
 * @see com.sun.appserv.management.base.SystemInfo
 * @see com.sun.enterprise.management.support.SystemInfoImpl
 */
public final class SystemInfoData {
    private static final SystemInfoData INSTANCE = new SystemInfoData();

    private final Map<String, Long> mPerformanceMillis;
    private final Map<String, Long> mUnmodifiablePerformanceMillis;

    private SystemInfoData() {
        mPerformanceMillis = Collections.synchronizedMap(new HashMap<String, Long>());
        mUnmodifiablePerformanceMillis = Collections.unmodifiableMap(mPerformanceMillis);
    }

    public static SystemInfoData getInstance() {
        return INSTANCE;
    }

    /**
     * Add a performance metric.
     */
    public synchronized void addPerformanceMillis(final String name, final long millis) {
        if (mPerformanceMillis.containsKey(name)) {
            throw new IllegalStateException();
        }
        mPerformanceMillis.put(name, millis);
    }

    /**
     * @return unmodifiable Map of performance data
     */
    public Map<String, Long> getPerformanceMillis() {
        return mUnmodifiablePerformanceMillis;
    }
}
