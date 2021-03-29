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

package org.glassfish.admin.monitor.jvm.statistics;

import java.lang.management.ManagementFactory;

/**
 * Provides V2 compatible JVM stats
 *
 * This is a temporary solution until we decide on the usage JSR77 stats interfaces
 *
 * @author Sreenivas Munnangi
 */

public class V2JVMStats {

    private final long DEFAULT_VALUE = java.math.BigInteger.ZERO.longValue();

    public long getUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    public long getMin() {
        return DEFAULT_VALUE;
    }

    public long getMax() {
        return Runtime.getRuntime().maxMemory();
    }

    public long getLow() {
        return DEFAULT_VALUE;
    }

    public long getHigh() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getCount() {
        return Runtime.getRuntime().totalMemory();
    }

}
