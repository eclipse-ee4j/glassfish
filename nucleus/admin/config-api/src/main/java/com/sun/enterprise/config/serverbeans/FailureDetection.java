/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * <ul>
 * <li>failure-detection enables its members to periodically monitor other group members
 * to determine their availability in the group.</li>
 * <li>group-discovery is used for discovery of group & its members.</li>
 * <li>failure-detection.verify-failure-timeout-in-millis verifies suspect instances
 * by adding a verification layer to mark failure suspicion as a confirmed failure.</li>
 * </ul>
 *
 * @since glassfish v3.1
 */
@Configured
public interface FailureDetection extends ConfigBeanProxy {

    /**
     * Gets the value of the {@code maxMissedHeartbeats} property.
     *
     * <p>Maximum number of attempts to try before GMS confirms that a failure
     * is suspected in the group. Must be a positive integer.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "3")
    @Min(value = 1)
    String getMaxMissedHeartbeats();

    /**
     * Sets the value of the {@code maxMissedHeartbeats} property.
     *
     * @param maxMissedHeartbeats allowed object is {@link String}
     */
    void setMaxMissedHeartbeats(String maxMissedHeartbeats) throws PropertyVetoException;

    /**
     * Gets the value of the {@code heartbeatFrequencyInMillis} property.
     *
     * <p>Must be a positive integer.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "2000")
    @Min(value = 1000)
    @Max(value = 120000)
    String getHeartbeatFrequencyInMillis();

    /**
     * Sets the value of {@code heartbeatFrequencyInMillis} property.
     *
     * @param heartbeatFrequency allowed is {@link String}
     */
    void setHeartbeatFrequencyInMillis(String heartbeatFrequency) throws PropertyVetoException;

    /**
     * Sets the value of the {@code verifyFailureWaittimeInMillis} property.
     *
     * @param verifyFailureWaittime allowed object is {@link String}
     */
    void setVerifyFailureWaittimeInMillis(String verifyFailureWaittime) throws PropertyVetoException;

    /**
     * Gets the value of the {@code verifyFailureWaittimeInMillis} property.
     *
     * <p>After this timeout a suspected failure is marked as verified. Must be a positive integer.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "1500")
    @Min(value = 1500)
    @Max(value = 120000)
    String getVerifyFailureWaittimeInMillis();

    /**
     * Sets the value of the {@code verifyFailureConnectTimeoutInMillis}.
     *
     * @param verifyFailureConnectTimeout allowed object is {@link String}
     * @since glassfish v3.1
     */
    void setVerifyFailureConnectTimeoutInMillis(String verifyFailureConnectTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code verifyFailureConnectTimeoutInMillis}.
     *
     * @since glassfish v3.1
     */
    @Attribute(defaultValue = "10000")
    @Min(value = 3000)
    @Max(value = 120000)
    String getVerifyFailureConnectTimeoutInMillis();
}
