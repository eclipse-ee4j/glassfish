/*
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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * failure-detection enables its members to periodically monitor other group members to determine their availability in
 * the group. group-discovery is used for discovery of group & its members.
 * failure-detection.verify-failure-timeout-in-millis verifies suspect instances by adding a verification layer to mark
 * a failure suspicion as a confirmed failure.
 *
 * @since glassfish v3.1
 */
@Configured
@SuppressWarnings("unused")
public interface FailureDetection extends ConfigBeanProxy {
    /**
     * Gets the value of the maxMissedHeartbeats property.
     * <p/>
     * Maximum number of attempts to try before GMS confirms that a failure is suspected in the group. Must be a positive
     * integer.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "3")
    @Min(value = 1)
    String getMaxMissedHeartbeats();

    /**
     * Sets the value of the maxMissedHeartbeats property.
     *
     * @param value allowed object is {@link String }
     */
    void setMaxMissedHeartbeats(String value) throws PropertyVetoException;

    /**
     * Gets the value of the heartbeatFrequencyInMillis property.
     * <p/>
     * Must be a positive integer.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "2000")
    @Min(value = 1000)
    @Max(value = 120000)
    String getHeartbeatFrequencyInMillis();

    /**
     * Sets the value of heartbeatFrequencyInMillis property.
     * <p/>
     *
     * @param value allowed is {@link String }
     */
    void setHeartbeatFrequencyInMillis(String value) throws PropertyVetoException;

    /**
     * Sets the value of the verifyFailureWaittimeInMillis property.
     *
     * @param value allowed object is {@link String }
     */
    void setVerifyFailureWaittimeInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the verifyFailureWaittimeInMillis property.
     * <p/>
     * After this timeout a suspected failure is marked as verified. Must be a positive integer.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "1500")
    @Min(value = 1500)
    @Max(value = 120000)
    String getVerifyFailureWaittimeInMillis();

    /**
     * sets the value of the verifyFailureConnectTimeoutInMillis.
     * <p/>
     *
     * @param value allowed object is {@link String}
     * @since glassfish v3.1
     */
    void setVerifyFailureConnectTimeoutInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the verifyFailureConnectTimeoutInMillis.
     *
     * @since glassfish v3.1
     */
    @Attribute(defaultValue = "10000")
    @Min(value = 3000)
    @Max(value = 120000)
    String getVerifyFailureConnectTimeoutInMillis();
}
