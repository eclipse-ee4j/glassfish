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

package org.glassfish.admin.amx.logging;

import javax.management.MBeanOperationInfo;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.core.AMXMBeanMetadata;
import java.util.Map;
import org.glassfish.admin.amx.annotation.Description;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.annotation.Param;
import org.glassfish.external.arc.Stability;
import org.glassfish.external.arc.Taxonomy;

/**
 * Supports accessing logging information in multiple ways. The following are supported:
 * <ul>
 * <li>Emission of pseudo real-time JMX Notifications when a
 * log record is created--see {@link LogRecordEmitter}</li>
 * <li>Access to existing log file contents--see {@link LogFileAccess}</li>
 * <li>Querying for log entries--see {@link LogQuery}</li>
 * </ul>
 *
 * @since AS 9.0
 */
@Taxonomy(stability = Stability.EXPERIMENTAL)
@AMXMBeanMetadata(singleton = true, globalSingleton = true, leaf = true)
public interface Logging extends AMXProxy, LogQuery
// LogFileAccess not implemented yet
// LogRecordEmitter not implemented yet
// LogAnalyzer not implemented yet
{

    /**
     * Sets the log level of the Logger for the specified module. This operation
     * will not effect a change to the corresponding loggin configuration for that module.
     *
     * @param module a module name as specified in {@link LogModuleNames}.
     * @param level a log level
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    @Description("Sets the log level of the Logger for the specified module")
    public void setModuleLogLevel(@Param(name = "moduleName") final String moduleName,
        @Param(name = "level") final String level);


    /**
     * Gets the log level of the Logger for the specified module, which may or may not
     * be the same as that found in the configuration.
     *
     * @param moduleName a module name as specified in {@link LogModuleNames}
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    @Description("Gets the log level of the Logger for the specified module")
    public String getModuleLogLevel(@Param(name = "moduleName") final String moduleName);


    /**
     * Sets the log level of the Logger for the specified module. This operation
     * will not effect a change to the corresponding loggin configuration for that module.
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    @Description("Sets the value of one or more logging properties")
    public void updateLoggingProperties(@Param(name = "properties") final Map<String, String> properties);


    /**
     * Gets all the logging properties in the logging.properties file
     */
    @ManagedAttribute
    @Description("Gets all the logging properties")
    public Map<String, String> getLoggingProperties();


    /**
     * Gets the configuration properties for logging
     */
    @ManagedAttribute
    @Description("Get logging configuration properties")
    public Map<String, String> getLoggingAttributes();


    /**
     * Sets the value of one or more of the logging configuration properties .
     */
    @ManagedOperation(impact = MBeanOperationInfo.ACTION)
    @Description("Set value of the value of one or more of the logging configuration properties.")
    public void updateLoggingAttributes(@Param(name = "properties") final Map<String, String> properties);


    /**
     * This method may be used to verify that your Logging listener is working
     * correctly.
     *
     * @param level the log level of the log message.
     * @param message the message to be placed in Notif.getMessage()
     */
    @ManagedOperation(impact = MBeanOperationInfo.INFO)
    public void testEmitLogMessage(@Param(name = "level") final String level,
        @Param(name = "message") final String message);
}







