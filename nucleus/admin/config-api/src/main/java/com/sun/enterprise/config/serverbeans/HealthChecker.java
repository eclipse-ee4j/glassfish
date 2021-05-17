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

import java.beans.PropertyVetoException;
import java.io.Serializable;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

/**
 * Each cluster would be configured for a ping based health check mechanism
 */

/* @XmlType(name = "") */

@Configured
public interface HealthChecker extends ConfigBeanProxy {

    /**
     * Gets the value of the url property.
     *
     * URL to ping so as to determine the health state of a listener. This must be a relative URL.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getUrl();

    /**
     * Sets the value of the url property.
     *
     * @param value allowed object is {@link String }
     */
    public void setUrl(String value) throws PropertyVetoException;

    /**
     * Gets the value of the intervalInSeconds property.
     *
     * Interval, in seconds, between health checks. A value of "0" means that the health check is disabled. Default is 30
     * seconds. Must be 0 or greater.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "30")
    @Min(value = 0)
    public String getIntervalInSeconds();

    /**
     * Sets the value of the intervalInSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIntervalInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the timeoutInSeconds property.
     *
     * Maximum time, in seconds, that a server must respond to a health check request to be considered healthy. Default is
     * 10 seconds. Must be greater than 0.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "10")
    @Min(value = 1)
    public String getTimeoutInSeconds();

    /**
     * Sets the value of the timeoutInSeconds property.
     *
     * @param value allowed object is {@link String }
     */
    public void setTimeoutInSeconds(String value) throws PropertyVetoException;
}
