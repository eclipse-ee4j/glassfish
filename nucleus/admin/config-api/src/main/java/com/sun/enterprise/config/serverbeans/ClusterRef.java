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

import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;
import jakarta.validation.Payload;
import org.jvnet.hk2.config.*;
import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;

import java.beans.PropertyVetoException;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Element relating a reference to a cluster to be load balanced to an (optional) health-checker
 *
 */

/* @XmlType(name = "", propOrder = {
    "healthChecker"
}) */

@Configured
@ReferenceConstraint(skipDuringCreation = true, payload = ClusterRef.class)
public interface ClusterRef extends ConfigBeanProxy, Ref, Payload {

    /**
     * Gets the value of the ref property.
     *
     * @return possible object is {@link String }
     */
    @Override
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_SERVER_REGEX)
    @ReferenceConstraint.RemoteKey(message = "{resourceref.invalid.cluster-ref}", type = Cluster.class)
    public String getRef();

    /**
     * Sets the value of the ref property.
     *
     * @param value allowed object is {@link String }
     */
    @Override
    public void setRef(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lbPolicy property.
     *
     * load balancing policy to be used for this cluster. Possible values are round-robin , weighted-round-robin or
     * user-defined. round-robin is the default. For weighted-round-robin, the weights of the instance are considered while
     * load balancing. For user-defined, the policy is implemented by a shared library which is loaded by the load balancer
     * and the instance selected is delegated to the loaded module.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "round-robin")
    public String getLbPolicy();

    /**
     * Sets the value of the lbPolicy property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLbPolicy(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lbPolicyModule property.
     *
     * Specifies the absolute path to the shared library implementing the user-defined policy. This should be specified only
     * when the lb-policy is user-defined. The shared library should exist and be readable in the machine where load
     * balancer is running.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getLbPolicyModule();

    /**
     * Sets the value of the lbPolicyModule property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLbPolicyModule(String value) throws PropertyVetoException;

    /**
     * Gets the value of the healthChecker property.
     *
     * Each cluster would be configured for a ping based health check mechanism.
     *
     * @return possible object is {@link HealthChecker }
     */
    @Element
    public HealthChecker getHealthChecker();

    /**
     * Sets the value of the healthChecker property.
     *
     * @param value allowed object is {@link HealthChecker }
     */
    public void setHealthChecker(HealthChecker value) throws PropertyVetoException;
}
