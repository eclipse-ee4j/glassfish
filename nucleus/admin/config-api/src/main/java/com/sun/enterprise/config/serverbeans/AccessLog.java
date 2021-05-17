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
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.types.PropertyBag;
import java.beans.PropertyVetoException;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;

/**
 * Access log configuration
 */
@Configured
@SuppressWarnings("unused")
public interface AccessLog extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the format attribute, which specifies the format of the access log.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "%client.name% %auth-user-name% %datetime% %request% %status% %response.length%")
    String getFormat();

    /**
     * Sets the value of the format attribute.
     *
     * @param value allowed object is {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setFormat(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotation-policy attribute.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "time")
    String getRotationPolicy();

    /**
     * Sets the value of the rotation-policy attribute.
     *
     * @param value allowed object is {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     *
     */
    void setRotationPolicy(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotation-interval-in-minutes attribute. The time interval in minutes between two successive
     * rotations of the access logs.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "1440")
    @Min(value = 1)
    @Max(value = Integer.MAX_VALUE)
    String getRotationIntervalInMinutes();

    /**
     * Sets the value of the rotation-interval-in-minutes attribute.
     *
     * @param value allowed object is {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setRotationIntervalInMinutes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotation-suffix attribute. The suffix to be added to the access-log name after rotation.
     * Acceptable values include those supported by java.text.SimpleDateFormat and "%YYYY;%MM;%DD;-%hh;h%mm;m%ss;s".
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "yyyy-MM-dd")
    String getRotationSuffix();

    /**
     * Sets the value of the rotation-suffix attribute.
     *
     * @param value allowed object is {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setRotationSuffix(String value) throws PropertyVetoException;

    /**
     * Gets the value of the rotation-enabled attribute. The flag for enabling the access-log rotation
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getRotationEnabled();

    /**
     * Sets the value of the rotation-enabled attribute.
     *
     * @param value allowed object is {@link String }
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setRotationEnabled(String value) throws PropertyVetoException;

    /**
     * Size in bytes of the buffer where access log calls are stored. If the value is less than 5120, a warning message is
     * issued, and the value is set to 5120
     *
     * @return the buffer-size
     */
    @Attribute(defaultValue = "32768")
    String getBufferSizeBytes();

    void setBufferSizeBytes(String value);

    /**
     * Number of seconds before the log is written to the disk. The access log is written when the buffer is full or when
     * the interval expires. If the value is 0, the buffer is always written even if it is not full. This means that each
     * time the server is accessed, the log message is stored directly to the file
     *
     * @return the write interval in seconds
     */
    @Attribute(defaultValue = "300")
    String getWriteIntervalSeconds();

    void setWriteIntervalSeconds(String value);

    /**
     * Gets the maximum number of rotated access log files that are to be kept.
     *
     * <p>
     * A negative value must be interpreted as no limit.
     *
     * @return the max number of log files
     */
    @NotNull
    @Attribute(defaultValue = "-1", dataType = Integer.class)
    String getMaxHistoryFiles();

    /**
     * Sets the maximum number of rotated access log files that are to be kept.
     *
     * @param value the maximum number of log files
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setMaxHistoryFiles(String value) throws PropertyVetoException;

}
