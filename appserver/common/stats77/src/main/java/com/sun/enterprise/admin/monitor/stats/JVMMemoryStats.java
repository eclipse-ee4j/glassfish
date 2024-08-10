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
 * A Stats interface, to expose the monitoring information
 * about the JVM memory subsystem. This interfaces exposes
 * the memory usage information for the heap and the non-heap
 * areas of the memory subsystem.
 *
 * @since 8.1
 */
public interface JVMMemoryStats extends Stats {

    /**
     * Returns the approximate number of objects, that are
     * pending finalization.
     * @return CountStatistic   Objects pending finalization
     */
    CountStatistic getObjectPendingFinalizationCount();

    /**
     * Returns the size of the heap initially requested by the JVM
     * @return CountStatistic initial heap size in bytes
     */
    CountStatistic getInitHeapSize();

    /**
     * Returns the size of the heap currently in use
     * @return CountStatistic current heap usage in bytes
     */
    CountStatistic getUsedHeapSize();

    /**
     * Returns the maximum amount of memory in bytes that can be used
     * for memory management
     * @return CountStatistic maximum heap size in bytes
     */
    CountStatistic getMaxHeapSize();

    /**
     * Returns the amount of memory in bytes that is committed
     * for the JVM to use
     * @return CountStatistic memory committed for the jvm in bytes
     */
    CountStatistic getCommittedHeapSize();

    /**
     * Returns the size of the non=heap area initially
     * requested by the JVM
     * @return CountStatistic initial size of the non-heap area in bytes
     */
    CountStatistic getInitNonHeapSize();

    /**
     * Returns the size of the non-heap area currently in use
     * @return CountStatistic current usage of the non-heap area in bytes
     */
    CountStatistic getUsedNonHeapSize();

    /**
     * Returns the maximum amount of memory in bytes that can be used
     * for memory management
     * @return CountStatistic maximum non-heap area size in bytes
     */
    CountStatistic getMaxNonHeapSize();

    /**
     * Returns the amount of memory in bytes that is committed
     * for the JVM to use
     * @return CountStatistic memory committed for the jvm in bytes
     */
    CountStatistic getCommittedNonHeapSize();

}
