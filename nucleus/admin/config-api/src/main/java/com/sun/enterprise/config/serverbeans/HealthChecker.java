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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Each cluster would be configured for a ping based health check mechanism.
 */
@Configured
public interface HealthChecker extends ConfigBeanProxy {

    /**
     * Gets the value of the {@code url} property.
     *
     *<p>URL to ping to determine the health state of a listener. This must be a relative URL.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getUrl();

    /**
     * Sets the value of the {@code url} property.
     *
     * @param url allowed object is {@link String}
     */
    void setUrl(String url) throws PropertyVetoException;

    /**
     * Gets the value of the {@code intervalInSeconds} property.
     *
     * <p>Interval, in seconds, between health checks. A value of {@code 0} means that
     * the health check is disabled. Default is {@code 30} seconds.
     *
     * <p>Must be {@code 0} or greater.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "30")
    @Min(value = 0)
    String getIntervalInSeconds();

    /**
     * Sets the value of the {@code intervalInSeconds} property.
     *
     * @param interval allowed object is {@link String}
     */
    void setIntervalInSeconds(String interval) throws PropertyVetoException;

    /**
     * Gets the value of the {@code timeoutInSeconds} property.
     *
     * <p>Maximum time, in seconds, that a server must respond to a health check request
     * to be considered healthy. Default is {@code 10 seconds}.
     *
     * <p>Must be greater than {@code 0}.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "10")
    @Min(value = 1)
    String getTimeoutInSeconds();

    /**
     * Sets the value of the {@code timeoutInSeconds} property.
     *
     * @param timeout allowed object is {@link String}
     */
    void setTimeoutInSeconds(String timeout) throws PropertyVetoException;
}
