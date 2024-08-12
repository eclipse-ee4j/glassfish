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

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 *
 */
@Configured
public interface DiagnosticService extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the {@code computeChecksum} property.
     *
     * <p>Boolean attribute. Indicates whether checksum of binaries is computed.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getComputeChecksum();

    /**
     * Sets the value of the {@code computeChecksum} property.
     *
     * @param computeChecksum allowed object is {@link String}
     */
    void setComputeChecksum(String computeChecksum) throws PropertyVetoException;

    /**
     * Gets the value of the {@code verifyConfig} property.
     *
     * <p>A {@code Boolean} attribute which indicates whether output of {@code verify-config}
     * asadmin command is included in the diagnostic report.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getVerifyConfig();

    /**
     * Sets the value of the {@code verifyConfig} property.
     *
     * @param verifyConfig allowed object is {@link String}
     */
    void setVerifyConfig(String verifyConfig) throws PropertyVetoException;

    /**
     * Gets the value of the {@code captureInstallLog} property.
     *
     * <p>{@code Boolean} attribute which indicated whether the log generated during
     * installation of the application server is captured.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getCaptureInstallLog();

    /**
     * Sets the value of the {@code captureInstallLog} property.
     *
     * @param captureInstallLog allowed object is {@link String}
     */
    void setCaptureInstallLog(String captureInstallLog) throws PropertyVetoException;

    /**
     * Gets the value of the {@code captureSystemInfo} property.
     *
     * <p>{@code Boolean} attribute which specifies whether OS level information is
     * collected as part of diagnostic report.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getCaptureSystemInfo();

    /**
     * Sets the value of the {@code captureSystemInfo} property.
     *
     * @param captureSystemInfo allowed object is {@link String}
     */
    void setCaptureSystemInfo(String captureSystemInfo) throws PropertyVetoException;

    /**
     * Gets the value of the {@code captureHadbInfo} property.
     *
     * <p>{@code Boolean} attribute to indicate if HADB related information is collected.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getCaptureHadbInfo();

    /**
     * Sets the value of the {@code captureHadbInfo} property.
     *
     * @param captureHadbInfo allowed object is {@link String}
     */
    void setCaptureHadbInfo(String captureHadbInfo) throws PropertyVetoException;

    /**
     * Gets the value of the {@code captureAppDd} property.
     *
     * <p>{@code Boolean} attribute. If {@code true}, application deployment descriptors
     * in plain text are captured as part of diagnostic report. If Deployment descriptors
     * contain any confidential information, it's recommended to set it to {@code false}.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getCaptureAppDd();

    /**
     * Sets the value of the {@code captureAppDd} property.
     *
     * @param captureAppDd allowed object is {@link String}
     */
    void setCaptureAppDd(String captureAppDd) throws PropertyVetoException;

    /**
     * Gets the value of the {@code minLogLevel} property.
     *
     * <p>The log levels can be changed using one of the seven levels. Please refer
     * JSR 047 to understand the Log Levels. The default level is {@code INFO}, meaning
     * that messages at that level or higher ({@code WARNING}, {@code SEVERE}) are
     * captured as part of the diagnostic report. If set to {@code OFF}, log contents
     * will not be captured as part of diagnostic report.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "INFO")
    String getMinLogLevel();

    /**
     * Sets the value of the {@code minLogLevel} property.
     *
     * @param minLogLevel allowed object is {@link String}
     */
    void setMinLogLevel(String minLogLevel) throws PropertyVetoException;

    /**
     * Gets the value of the {@code maxLogEntries} property.
     *
     * <p>Max number of log entries being captured as part of diagnostic report.
     * A non-negative value.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "500")
    @Min(value = 0)
    String getMaxLogEntries();

    /**
     * Sets the value of the {@code maxLogEntries} property.
     *
     * @param maxLogEntries allowed object is {@link String}
     */
    void setMaxLogEntries(String maxLogEntries) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    @Element("*")
    List<DiagnosticServiceExtension> getExtensions();

    /**
     * Get an {@code extension} of the specified type.
     *
     * <p>If there is more than one, it is undefined as to which one is returned.
     */
    default <T extends DiagnosticServiceExtension> T getExtensionByType(Class<T> type) {
        for (DiagnosticServiceExtension extension : getExtensions()) {
            try {
                return type.cast(extension);
            } catch (Exception e) {
                // ignore, not the right type.
            }
        }
        return null;
    }
}
