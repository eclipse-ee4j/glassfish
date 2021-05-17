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

package com.sun.enterprise.config.serverbeans;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * By default, logs would be kept in $INSTANCE-ROOT/logs. The following log files will be stored under the logs
 * directory. access.log keeps default virtual server HTTP access messages. server.log keeps log messages from default
 * virtual server. Messages from other configured virtual servers also go here, unless log-file is explicitly specified
 * in the virtual-server element.
 */

/* @XmlType(name = "", propOrder = {
    "moduleLogLevels",
    "property"
}) */

@Configured
public interface LogService extends ConfigBeanProxy {

    /**
     * Gets the value of the file property.
     *
     * Can be used to rename or relocate server.log using absolute path.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getFile();

    /**
     * Sets the value of the file property.
     *
     * @param value allowed object is {@link String }
     */
    public void setFile(String value) throws PropertyVetoException;

    /**
     * Gets the value of the useSystemLogging property.
     *
     * If true, will utilize Unix syslog service or Windows Event Logging to produce and manage logs.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    public String getUseSystemLogging();

    /**
     * Sets the value of the useSystemLogging property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUseSystemLogging(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logHandler property.
     *
     * Can plug in a custom log handler to add it to the chain of handlers to log into a different log destination than the
     * default ones given by the system (which are Console, File and Syslog). It is a requirement that customers use the log
     * formatter provided by the the system to maintain uniformity in log messages. The custom log handler will be added at
     * the end of the handler chain after File + Syslog Handler, Console Handler and JMX Handler. User cannot replace the
     * handler provided by the system, because of loosing precious log statements. The Server Initialization will take care
     * of installing the custom handler with the system formatter initialized. The user need to use JSR 047 Log Handler
     * Interface to implement the custom handler.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getLogHandler();

    /**
     * Sets the value of the logHandler property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLogHandler(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logFilter property.
     *
     * Can plug in a log filter to do custom filtering of log records. By default there is no log filter other than the log
     * level filtering provided by JSR 047 log API.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getLogFilter();

    /**
     * Sets the value of the logFilter property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLogFilter(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logToConsole property.
     *
     * logs will be sent to stderr when asadmin start-domain verbose is used
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    public String getLogToConsole();

    /**
     * Sets the value of the logToConsole property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLogToConsole(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logRotationLimitInBytes property.
     *
     * Log Files will be rotated when the file size reaches the limit.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "1")
    @Min(value = 1)
    public String getLogRotationLimitInBytes();

    /**
     * Sets the value of the logRotationLimitInBytes property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLogRotationLimitInBytes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the logRotationTimelimitInMinutes property.
     *
     * This is a new attribute to enable time based log rotation. The Log File will be rotated only if this value is
     * non-zero and the valid range is 60 minutes (1 hour) to 10*24*60 minutes (10 days). If the value is zero then the
     * files will be rotated based on size specified in log-rotation-limit-in-bytes.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "0")
    @Min(value = 0)
    @Max(value = 14400)
    public String getLogRotationTimelimitInMinutes();

    /**
     * Sets the value of the logRotationTimelimitInMinutes property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLogRotationTimelimitInMinutes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the alarms property.
     *
     * if true, will turn on alarms for the logger. The SEVERE and WARNING messages can be routed through the JMX framework
     * to raise SEVERE and WARNING alerts. Alarms are turned off by default.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    public String getAlarms();

    /**
     * Sets the value of the alarms property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAlarms(String value) throws PropertyVetoException;

    /**
     * Gets the value of the retainErrorStatisticsForHours property.
     *
     * The number of hours since server start, for which error statistics should be retained in memory. The default and
     * minimum value is 5 hours. The maximum value allowed is 500 hours. Note that larger values will incur additional
     * memory overhead.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "5")
    @Min(value = 5)
    @Max(value = 500)
    public String getRetainErrorStatisticsForHours();

    /**
     * Sets the value of the retainErrorStatisticsForHours property.
     *
     * @param value allowed object is {@link String }
     */
    public void setRetainErrorStatisticsForHours(String value) throws PropertyVetoException;

    /**
     * Gets the value of the moduleLogLevels property.
     *
     * @return possible object is {@link ModuleLogLevels }
     */
    @Element
    public ModuleLogLevels getModuleLogLevels();

    /**
     * Sets the value of the moduleLogLevels property.
     *
     * @param value allowed object is {@link ModuleLogLevels }
     */
    public void setModuleLogLevels(ModuleLogLevels value) throws PropertyVetoException;

}
