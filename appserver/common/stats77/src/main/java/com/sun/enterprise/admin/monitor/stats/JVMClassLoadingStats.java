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
 * about the JVM Classloading subsystem
 * @since 8.1
 */
public interface JVMClassLoadingStats extends Stats {

    /**
     * Returns the number of classes that are currently loaded
     * in the JVM
     * @return CountStatistic   The total number of classes currently loaded
     */
    CountStatistic getLoadedClassCount();

    /**
     * Returns the total number of classes that have been loaded,
     * since the JVM began execution
     * @return CountStatistic   The total number of classes loaded
     */
    CountStatistic getTotalLoadedClassCount();

    /**
     * Returns the number of classes that have been unloaded from the JVM
     * @return CountStatistic   number of unloaded classes
     */
    CountStatistic getUnloadedClassCount();

}
