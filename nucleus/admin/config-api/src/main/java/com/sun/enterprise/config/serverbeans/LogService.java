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

package com.sun.enterprise.config.serverbeans;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * By default, logs would be kept in {@code $INSTANCE-ROOT/logs}. The following log files
 * will be stored under the {@code logs} directory.
 *
 * <ul>
 * <li>{@code access.log} keeps default virtual server HTTP access messages.</li>
 * <li>server.log keeps log messages from default virtual server.</li>
 * </ul>
 *
 * Messages from other configured virtual servers also go here, unless log-file is explicitly
 * specified in the virtual-server element.
 */
@Configured
public interface LogService extends ConfigBeanProxy {

    /**
     * Gets the value of the {@code file} property.
     *
     * <p>Can be used to rename or relocate server.log using absolute path.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getFile();

    /**
     * Sets the value of the {@code file} property.
     *
     * @param file allowed object is {@link String}
     */
    void setFile(String file) throws PropertyVetoException;

    /**
     * Gets the value of the {@code useSystemLogging} property.
     *
     * <p>If {@code true}, will utilize Unix syslog service or Windows Event Logging
     * to produce and manage logs.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getUseSystemLogging();

    /**
     * Sets the value of the {@code useSystemLogging} property.
     *
     * @param useSystemLogging allowed object is {@link String}
     */
    void setUseSystemLogging(String useSystemLogging) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logHandler} property.
     *
     * <p>Can plug in a custom log handler to add it to the chain of handlers to log into
     * a different log destination than the default ones given by the system
     * (which are Console, File and Syslog). It is a requirement that customers use the log
     * formatter provided by the system to maintain uniformity in log messages. The custom
     * log handler will be added at the end of the handler chain after File + Syslog Handler,
     * Console Handler and JMX Handler. User cannot replace the handler provided by the system,
     * because of loosing precious log statements. The Server Initialization will take care
     * of installing the custom handler with the system formatter initialized. The user
     * need to use JSR 047 Log Handler Interface to implement the custom handler.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getLogHandler();

    /**
     * Sets the value of the {@code logHandler} property.
     *
     * @param logHandler allowed object is {@link String}
     */
    void setLogHandler(String logHandler) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logFilter} property.
     *
     * <p>Can plug in a log filter to do custom filtering of log records.
     * By default, there is no log filter other than the log level filtering
     * provided by JSR 047 log API.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getLogFilter();

    /**
     * Sets the value of the {@code logFilter} property.
     *
     * @param logFilter allowed object is {@link String}
     */
    void setLogFilter(String logFilter) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logToConsole} property.
     *
     * <p>Logs will be sent to stderr when {@code asadmin start-domain verbose} is used.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getLogToConsole();

    /**
     * Sets the value of the {@code logToConsole} property.
     *
     * @param logToConsole allowed object is {@link String}
     */
    void setLogToConsole(String logToConsole) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logRotationLimitInBytes} property.
     *
     * <p>Log Files will be rotated when the file size reaches the limit.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "1")
    @Min(value = 1)
    String getLogRotationLimitInBytes();

    /**
     * Sets the value of the {@code logRotationLimitInBytes} property.
     *
     * @param logRotationLimit allowed object is {@link String}
     */
    void setLogRotationLimitInBytes(String logRotationLimit) throws PropertyVetoException;

    /**
     * Gets the value of the {@code logRotationTimelimitInMinutes} property.
     *
     * <p>This is a new attribute to enable time based log rotation. The Log File
     * will be rotated only if this value is non-zero and the valid range is
     * {@code 60} minutes (1 hour) to 10*24*60 minutes (10 days). If the value is zero
     * then the files will be rotated based on size specified in log-rotation-limit-in-bytes.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "0")
    @Min(value = 0)
    @Max(value = 14400)
    String getLogRotationTimelimitInMinutes();

    /**
     * Sets the value of the {@code logRotationTimelimitInMinutes} property.
     *
     * @param logRotationTimelimit allowed object is {@link String}
     */
    void setLogRotationTimelimitInMinutes(String logRotationTimelimit) throws PropertyVetoException;

    /**
     * Gets the value of the {@code alarms} property.
     *
     * <p>If {@code true}, will turn on alarms for the logger. The {@code SEVERE} and
     * {@code WARNING} messages can be routed through the JMX framework
     * to raise {@code SEVERE} and {@code WARNING} alerts. Alarms are turned off by default.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getAlarms();

    /**
     * Sets the value of the {@code alarms} property.
     *
     * @param alarms allowed object is {@link String}
     */
    void setAlarms(String alarms) throws PropertyVetoException;

    /**
     * Gets the value of the {@code retainErrorStatisticsForHours} property.
     *
     * <p>The number of hours since server start, for which error statistics should
     * be retained in memory. The default and minimum value is {@code 5} hours.
     * The maximum value allowed is {@code 500} hours. Note that larger values will incur
     * additional memory overhead.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "5")
    @Min(value = 5)
    @Max(value = 500)
    String getRetainErrorStatisticsForHours();

    /**
     * Sets the value of the {@code retainErrorStatisticsForHours} property.
     *
     * @param retainErrorStatistics allowed object is {@link String}
     */
    void setRetainErrorStatisticsForHours(String retainErrorStatistics) throws PropertyVetoException;

    /**
     * Gets the value of the {@code moduleLogLevels} property.
     *
     * @return possible object is {@link ModuleLogLevels}
     */
    @Element
    ModuleLogLevels getModuleLogLevels();

    /**
     * Sets the value of the {@code moduleLogLevels} property.
     *
     * @param moduleLogLevels allowed object is {@link ModuleLogLevels}
     */
    void setModuleLogLevels(ModuleLogLevels moduleLogLevels) throws PropertyVetoException;
}
