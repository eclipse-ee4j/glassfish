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
import org.glassfish.j2ee.statistics.Stats;
import org.glassfish.j2ee.statistics.CountStatistic;
import com.sun.enterprise.admin.monitor.stats.StringStatistic;

/**
 * A Stats interface to expose information about the JVM Runtime
 * @since 8.1
 */

public interface JVMRuntimeStats extends Stats {
    
    /**
     * Returns the name representing the running JVM
     * @return StringStatistic  the name of the running JVM
     */
    public StringStatistic getName();
    
    
    /**
     * Returns the JVM implementation name
     * @return StringStatistic  JVM implementation name
     */
    public StringStatistic getVmName();
    
    /**
     * Returns the JVM implementation vendor
     * @return StringStatistic  JVM implementation vendor
     */
    public StringStatistic getVmVendor();
    
    /**
     * Returns the JVM implementation version
     * @return StringStatistic JVM implementation version
     */
    public StringStatistic getVmVersion();
    
    /**
     * Returns the JVM specification name
     * @return StringStatistic  JVM specification name
     */
    public StringStatistic getSpecName();
    
    /**
     * Returns the JVM specification vendor
     * @return StringStatistic  JVM specification vendor
     */
    public StringStatistic getSpecVendor();
    
    /**
     * Returns the JVM specification version
     * @return StringStatistic  JVM specification version
     */
    public StringStatistic getSpecVersion();
    
    /**
     * Returns the management spec version implemented by the 
     * JVM
     * @return  StringStatistic Management specification version
     */
    public StringStatistic getManagementSpecVersion();
    
    /**
     * Returns the classpath that is used by the system class loader
     * to search for class files
     * @return StringStatistic  Java class path
     */
    public StringStatistic getClassPath();
    
    /**
     * returns the Java library path
     * @return StringStatistic  Java library path
     */
    public StringStatistic getLibraryPath();
    
    /**
     * Returns the classpath that is used by the bootstrap class loader
     * to search for class files
     * @return StringStatistic  the boot classpath
     */
    public StringStatistic getBootClasspath();
    
    /**
     * Returns the input arguments passed to the JVM. Does not include
     * the arguments to the main method
     * @return StringStatistic  arguments to the JVM
     */
    public StringStatistic getInputArguments();
    
    /**
     * Returns the uptime of the JVM in milliseconds
     * @return CountStatistic   Uptime in milliseconds
     */
    public CountStatistic getUptime();
    
    
}
