/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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
import java.util.List;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Group-management-service(GMS) is an in-process service that provides cluster
 * monitoring and group communication services. GMS notifies registered modules
 * in an application server instance when one or more members in the cluster fail
 * (become unreachable). GMS also provides the ability to send and receive messages
 * between a group of processes. GMS is an abstraction layer that plugs-in group
 * communication technologies which rely on a configurable stack of protocols.
 * Each of these protocols has properties that can be changed for a given network
 * and deployment topology. These relevant configurable protocols are:
 *
 * <ul>
 * <li>failure-detection enables its members to periodically monitor other group members
 * to determine their availability in the group.</li>
 * <li>group-discovery is used for discovery of group & its members.</li>
 * <li>failure-detection.verify-failure-timeout-in-millis verifies suspect instances
 * by adding a verification layer to mark a failure suspicion as a confirmed failure.</li>
 * </ul>
 */
@Configured
public interface GroupManagementService extends PropertyBag, ConfigExtension {

    /**
     * Gets the value of the {@code groupManagementService} property.
     *
     * @return possible object is {@link GroupManagementService}
     * @since glassfish v3.1
     */
    @Element
    @NotNull
    FailureDetection getFailureDetection();

    /**
     * Sets the value of the {@code failureDetection} property.
     *
     * @param failureDetection allowed object is {@link FailureDetection}
     * @since glassfish v3.1
     */
    void setFailureDetection(FailureDetection failureDetection) throws PropertyVetoException;

    /**
     * Gets the value of the {@code groupDiscoveryTimeoutInMillis} property.
     *
     * <p>Amount of time in milliseconds that GMS waits for discovery of other members
     * in this group. Must be a positive integer.
     *
     * @return possible object is {@link String}
     * @since glassfish v3.1
     */
    @Attribute(defaultValue = "5000")
    @Min(value = 1000)
    @Max(value = 120000)
    String getGroupDiscoveryTimeoutInMillis();

    /**
     * Sets the value of the {@code groupDiscoveryTimeoutInMillis} property.
     *
     * @param groupDiscoveryTimeout allowed object is {@link String}
     * @since glassfish v3.1
     */
    void setGroupDiscoveryTimeoutInMillis(String groupDiscoveryTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code fdProtocolMaxTries} property.
     *
     * <p>Maximum number of attempts to try before GMS confirms that a failure is suspected
     * in the group. Must be a positive integer.
     *
     * @return possible object is {@link String}
     * @deprecated Replaced by {@link FailureDetection#getMaxMissedHeartbeats()}.
     */
    @Deprecated
    @Attribute
    String getFdProtocolMaxTries();

    /**
     * Sets the value of the {@code fdProtocolMaxTries} property.
     *
     * @param fdProtocolMaxTries allowed object is {@link String}
     * @deprecated Replaced by {@link FailureDetection#setMaxMissedHeartbeats(String)}
     */
    @Deprecated
    void setFdProtocolMaxTries(String fdProtocolMaxTries) throws PropertyVetoException;

    /**
     * Gets the value of the {@code fdProtocolTimeoutInMillis} property.
     *
     * <p>Period of time between monitoring attempts to detect failure. Must be a positive integer.
     *
     * @return possible object is {@link String}
     * @deprecated Replaced by {@link FailureDetection#getHeartbeatFrequencyInMillis()}.
     */
    @Attribute
    @Deprecated
    String getFdProtocolTimeoutInMillis();

    /**
     * Sets the value of the {@code fdProtocolTimeoutInMillis} property.
     *
     * @param fdProtocolTimeout allowed object is {@link String}
     * @deprecated Replaced by {@link FailureDetection#setHeartbeatFrequencyInMillis(String)}.
     */
    @Deprecated
    void setFdProtocolTimeoutInMillis(String fdProtocolTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code mergeProtocolMaxIntervalInMillis} property.
     *
     * <p>Specifies the maximum amount of time to wait to collect subgroup information
     * before performing a merge. Must be a positive integer.
     *
     * @return possible object is {@link String}
     * @deprecated
     */
    @Deprecated
    @Attribute
    String getMergeProtocolMaxIntervalInMillis();

    /**
     * Sets the value of the {@code mergeProtocolMaxIntervalInMillis} property.
     *
     * @param maxInterval allowed object is {@link String}
     * @deprecated
     */
    /* Not needed by gms in v3.1, was not used in v2. */
    @Deprecated
    void setMergeProtocolMaxIntervalInMillis(String maxInterval) throws PropertyVetoException;

    /**
     * Gets the value of the {@code mergeProtocolMinIntervalInMillis} property.
     *
     * <p>Specifies the minimum amount of time to wait to collect subgroup information
     * before performing a merge. Must be a positive integer
     *
     * @return possible object is {@link String}
     * @deprecated
     */
    /* Not needed by gms in v3.1, was not used in v2. Remove default value.*/
    @Deprecated
    @Attribute
    String getMergeProtocolMinIntervalInMillis();

    /**
     * Sets the value of the {@code mergeProtocolMinIntervalInMillis} property.
     *
     * @param minInterval allowed object is {@link String}
     * @deprecated
     */
    @Deprecated
    void setMergeProtocolMinIntervalInMillis(String minInterval) throws PropertyVetoException;

    /**
     * Gets the value of the {@code pingProtocolTimeoutInMillis} property.
     *
     * <p>Amount of time in milliseconds that GMS waits for discovery of other members
     * in this group. Must be a positive integer.
     *
     * @return possible object is {@link String}
     * @deprecated
     * @see #getGroupDiscoveryTimeoutInMillis()
     */
    /* renamed in v3.1 */
    @Deprecated
    @Attribute
    String getPingProtocolTimeoutInMillis();

    /**
     * Sets the value of the {@code pingProtocolTimeoutInMillis} property.
     *
     * @param pingProtocolTimeout allowed object is {@link String}
     * @deprecated
     * @see #setGroupDiscoveryTimeoutInMillis(String)
     */
    /* renamed in v3.1 to GroupDiscoveryTimeoutInMillis */
    @Deprecated
    void setPingProtocolTimeoutInMillis(String pingProtocolTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code vsProtocolTimeoutInMillis} property.
     *
     * <p>After this timeout a suspected failure is marked as verified. Must be a positive integer.
     *
     * @return possible object is {@link String}
     * @deprecated Replaced by {@link FailureDetection#getVerifyFailureWaittimeInMillis()}.
     */
    @Deprecated
    @Attribute
    String getVsProtocolTimeoutInMillis();

    /**
     * Sets the value of the {@code vsProtocolTimeoutInMillis} property.
     *
     * @param vsProtocolTimeout allowed object is {@link String}
     * @deprecated Replaced by {@link FailureDetection#setVerifyFailureWaittimeInMillis(String)}.
     */
    @Deprecated
    void setVsProtocolTimeoutInMillis(String vsProtocolTimeout) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
