/*
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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.Configured;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.glassfish.api.admin.config.ConfigExtension;

/**
 * group-management-service(GMS) is an in-process service that provides cluster monitoring and group communication
 * services. GMS notifies registered modules in an application server instance when one or more members in the cluster
 * fail (become unreachable). GMS also provides the ability to send and receive messages between a group of processes.
 * GMS is a abstraction layer that plugs-in group communication technologies which rely on a configurable stack of
 * protocols. Each of these protocols has properties that can be changed for a given network and deployment topology.
 * These relevant configurable protocols are: failure-detection enables its members to periodically monitor other group
 * members to determine their availability in the group. group-discovery is used for discovery of group & its members.
 * failure-detection.verify-failure-timeout-in-millis verifies suspect instances by adding a verification layer to mark
 * a failure suspicion as a confirmed failure.
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
@SuppressWarnings({ "deprecation" })
public interface GroupManagementService extends PropertyBag, ConfigExtension {

    /**
     * Gets the value of the groupManagementService property.
     *
     * @return possible object is {@link GroupManagementService }
     * @since glassfish v3.1
     */
    @Element //(required=true)
    @NotNull
    FailureDetection getFailureDetection();

    /**
     * Sets the value of the failureDetection property
     *
     * @param value allowed object is {@link FailureDetection }
     * @since glassfish v3.1
     */
    void setFailureDetection(FailureDetection value) throws PropertyVetoException;

    /**
     * Gets the value of the groupDiscoveryTimeoutInMillis property.
     *
     * Amount of time in milliseconds that GMS waits for discovery of other members in this group. Must be a positive
     * integer.
     *
     * @return possible object is {@link String }
     * @since glassfish v3.1
     */
    @Attribute(defaultValue = "5000")
    @Min(value = 1000)
    @Max(value = 120000)
    String getGroupDiscoveryTimeoutInMillis();

    /**
     * Sets the value of the groupDiscoveryTimeoutInMillis property.
     *
     * @param value allowed object is {@link String }
     * @since glassfish v3.1
     */
    void setGroupDiscoveryTimeoutInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the fdProtocolMaxTries property.
     *
     * Maximum number of attempts to try before GMS confirms that a failure is suspected in the group. Must be a positive
     * integer.
     *
     * @return possible object is {@link String }
     * @deprecate Replaced by {@link FailureDetection.getMaxMissedHeartbeats()}.
     */
    /*
     * Moved to FailureDetection in v3.1.
     * V2
     */
    @Deprecated
    @Attribute
    //@Attribute (defaultValue="3")
    //@Min(value=1)
    String getFdProtocolMaxTries();

    /**
     * Sets the value of the fdProtocolMaxTries property.
     *
     * @param value allowed object is {@link String }
     * @deprecate Replaced by {@link FailureDetection.setMaxMissedHeartbeats(String)}
     */
    /*
     * Moved to FailureDetection in v3.1.
     */
    @Deprecated
    void setFdProtocolMaxTries(String value) throws PropertyVetoException;

    /**
     * Gets the value of the fdProtocolTimeoutInMillis property.
     *
     * Period of time between monitoring attempts to detect failure. Must be a positive integer.
     *
     * @return possible object is {@link String }
     * @deprecate Replaced by {@link FailureDetection.getHeartbeatFrequency()}.
     */
    /*
     * Moved to FailureDetection in v3.1.
     */
    @Attribute
    @Deprecated
    //@Attribute (defaultValue="2000")
    //@Min(value=1000)
    //@Max(value=120000)
    String getFdProtocolTimeoutInMillis();

    /**
     * Sets the value of the fdProtocolTimeoutInMillis property.
     *
     * @param value allowed object is {@link String }
     * @deprecate Replaced by {@link FailureDetection.setHeartbeatFrequency(String)}.
     */
    /*
     * Moved to FailureDetection in v3.1.
     */
    @Deprecated
    void setFdProtocolTimeoutInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mergeProtocolMaxIntervalInMillis property.
     *
     * Specifies the maximum amount of time to wait to collect sub-group information before performing a merge. Must be a
     * positive integer.
     *
     * @return possible object is {@link String }
     * @deprecate
     */
    //@Attribute (defaultValue="10000")
    @Deprecated
    @Attribute
    //@Min(value=10000)
    //@Max(value=15000)
    String getMergeProtocolMaxIntervalInMillis();

    /**
     * Sets the value of the mergeProtocolMaxIntervalInMillis property.
     *
     * @param value allowed object is {@link String }
     * @deprecate
     */
    /* Not needed by gms in v3.1, was not used in v2. */
    @Deprecated
    void setMergeProtocolMaxIntervalInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the mergeProtocolMinIntervalInMillis property.
     *
     * Specifies the minimum amount of time to wait to collect sub-group information before performing a merge. Must be a
     * positive integer
     *
     * @return possible object is {@link String }
     * @deprecate
     */
    /* Not needed by gms in v3.1, was not used in v2. Remove default value.*/
    @Deprecated
    @Attribute
    //@Attribute (defaultValue="5000")
    //@Min(value=1000)
    //@Max(value=10000)
    String getMergeProtocolMinIntervalInMillis();

    /**
     * Sets the value of the mergeProtocolMinIntervalInMillis property.
     *
     * @param value allowed object is {@link String }
     * @deprecate
     */
    @Deprecated
    void setMergeProtocolMinIntervalInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the pingProtocolTimeoutInMillis property.
     *
     * Amount of time in milliseconds that GMS waits for discovery of other members in this group. Must be a positive
     * integer.
     *
     * @return possible object is {@link String }
     * @deprecate
     * @see #getGroupDiscoveryTimeoutInMillis()
     */
    /* renamed in v3.1 */
    @Deprecated
    @Attribute
    //@Attribute (defaultValue="5000")
    //@Min(value=1000)
    //@Max(value=120000)
    String getPingProtocolTimeoutInMillis();

    /**
     * Sets the value of the pingProtocolTimeoutInMillis property.
     *
     * @param value allowed object is {@link String }
     * @deprecate
     * @see #setGroupDiscoveryTimeoutInMillis(String)
     */
    /* renamed in v3.1 to GroupDiscoveryTimeoutInMillis */
    @Deprecated
    void setPingProtocolTimeoutInMillis(String value) throws PropertyVetoException;

    /**
     * Gets the value of the vsProtocolTimeoutInMillis property.
     *
     * After this timeout a suspected failure is marked as verified. Must be a positive integer.
     *
     * @return possible object is {@link String }
     * @deprecate Replaced by {@link FailureDetection.getVerifyFailureWaittimeInMillis()}.
     */
    /*
     * Moved to FailureDetection in v3.1.
     * V2
     */
    @Deprecated
    @Attribute
    //@Attribute (defaultValue="1500")
    //@Min(value=1500)
    //@Max(value=120000)
    String getVsProtocolTimeoutInMillis();

    /**
     * Sets the value of the vsProtocolTimeoutInMillis property.
     *
     * @param value allowed object is {@link String }
     * @deprecate Replaced by {@link FailureDetection.setVerifyFailureWaittimeInMillis(String)}.
     */
    /* Moved to FailureDetection in v3.1
     */
    @Deprecated
    void setVsProtocolTimeoutInMillis(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
