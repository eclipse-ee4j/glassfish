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

package com.sun.enterprise.admin.monitor.stats;
import org.glassfish.j2ee.statistics.CountStatistic;
import org.glassfish.j2ee.statistics.Stats;

/**
 * A Stats interface, to expose information
 * about the Operating system on which the JVM is running
 * @since 8.1
 */
public interface JVMOperatingSystemStats extends Stats {

    /**
     * Returns the operating system architecture
     * @return StringStatistic
     */
    StringStatistic getArch();

    /**
     * Returns the number of processors available to the JVM
     * @return CountStatistic
     */
    CountStatistic getAvailableProcessors();

    /**
     * Returns the operating system name
     * @return StringStatistic
     */
    StringStatistic getName();

    /**
     * Returns the operating system version
     * @return StringStatistic
     */
    StringStatistic getVersion();
}
