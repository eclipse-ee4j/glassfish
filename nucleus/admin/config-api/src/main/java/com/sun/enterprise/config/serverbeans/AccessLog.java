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

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Access log configuration.
 */
@Configured
public interface AccessLog extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code format} attribute, which specifies the format of the access log.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%")
    String getFormat();

    /**
     * Sets the value of the {@code format} attribute.
     *
     * @param format allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setFormat(String format) throws PropertyVetoException;

    /**
     * Gets the value of the {@code rotation-policy} attribute.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "time")
    String getRotationPolicy();

    /**
     * Sets the value of the {@code rotation-policy} attribute.
     *
     * @param rotationPolicy allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     *
     */
    void setRotationPolicy(String rotationPolicy) throws PropertyVetoException;

    /**
     * Gets the value of the {@code rotation-interval-in-minutes} attribute.
     *
     * <p>The time interval in minutes between two successive rotations of the access logs.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "1440")
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    String getRotationIntervalInMinutes();

    /**
     * Sets the value of the {@code rotation-interval-in-minutes} attribute.
     *
     * @param rotationInterval allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setRotationIntervalInMinutes(String rotationInterval) throws PropertyVetoException;

    /**
     * Gets the value of the {@code rotation-suffix} attribute.
     *
     * <p>The suffix to be added to the {@code access-log} name after rotation.
     * Acceptable values include those supported by {@link java.text.SimpleDateFormat}
     * and "%YYYY;%MM;%DD;-%hh;h%mm;m%ss;s".
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "yyyy-MM-dd")
    String getRotationSuffix();

    /**
     * Sets the value of the {@code rotation-suffix} attribute.
     *
     * @param rotationSuffix allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setRotationSuffix(String rotationSuffix) throws PropertyVetoException;

    /**
     * Gets the value of the {@code rotation-enabled} attribute.
     *
     * <p>The flag for enabling the {@code access-log} rotation.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getRotationEnabled();

    /**
     * Sets the value of the {@code rotation-enabled} attribute.
     *
     * @param rotationEnabled allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setRotationEnabled(String rotationEnabled) throws PropertyVetoException;

    /**
     * Size in bytes of the buffer where access log calls are stored. If the value
     * is less than {@code 5120}, a warning message is  issued, and the value is set
     * to {@code 5120}
     *
     * @return the buffer size
     */
    @Attribute(defaultValue = "32768")
    String getBufferSizeBytes();

    void setBufferSizeBytes(String bufferSize);

    /**
     * Number of seconds before the log is written to the disk. The access log is written
     * when the buffer is full or when the interval expires. If the value is {@code 0},
     * the buffer is always written even if it is not full. This means that each
     * time the server is accessed, the log message is stored directly to the file.
     *
     * @return the write interval in seconds
     */
    @Attribute(defaultValue = "300")
    String getWriteIntervalSeconds();

    void setWriteIntervalSeconds(String writeInterval);

    /**
     * Gets the maximum number of rotated access log files that are to be kept.
     *
     * <p>A negative value must be interpreted as no limit.
     *
     * @return the max number of log files
     */
    @NotNull
    @Attribute(defaultValue = "-1", dataType = Integer.class)
    String getMaxHistoryFiles();

    /**
     * Sets the maximum number of rotated access log files that are to be kept.
     *
     * @param maxHistoryFiles the maximum number of log files
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setMaxHistoryFiles(String maxHistoryFiles) throws PropertyVetoException;
}
