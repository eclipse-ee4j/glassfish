/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;

@Configured
@ReferenceConstraint(skipDuringCreation = true, payload = ServerRef.class)
public interface ServerRef extends ConfigBeanProxy, Ref, Payload {

    // defines the default value for lb-enabled attribute
    String LBENABLED_DEFAULT_VALUE = "true";

    /**
     * Gets the value of the {@code ref} property.
     *
     * <p>A reference to the name of a server defined elsewhere.
     *
     * @return possible object is {@link String}
     */
    @Override
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{server.invalid.name}", payload = ServerRef.class)
    @ReferenceConstraint.RemoteKey(message = "{resourceref.invalid.server-ref}", type = Server.class)
    String getRef();

    /**
     * Sets the value of the {@code ref} property.
     *
     * @param ref allowed object is {@link String}
     */
    @Override
    void setRef(String ref) throws PropertyVetoException;

    /**
     * Gets the value of the {@code disableTimeoutInMinutes} property.
     *
     * <p>The time, in minutes, that it takes this server to reach a quiescent state
     * after having been disabled.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "30")
    String getDisableTimeoutInMinutes();

    /**
     * Sets the value of the {@code disableTimeoutInMinutes} property.
     *
     * @param disableTimeout allowed object is {@link String}
     */
    void setDisableTimeoutInMinutes(String disableTimeout) throws PropertyVetoException;

    /**
     * Gets the value of the {@code lbEnabled} property.
     *
     * <p>Causes any and all load-balancers using this server to consider this server
     * available to them.
     *
     * <p>Defaults to {@code available}({@code true}).
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = LBENABLED_DEFAULT_VALUE, dataType = Boolean.class)
    String getLbEnabled();

    /**
     * Sets the value of the {@code lbEnabled} property.
     *
     * @param lbEnabled allowed object is {@link String}
     */
    void setLbEnabled(String lbEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * <p>A boolean flag that causes the server to be enabled to serve end-users, or not.
     *
     * <p>Default is to be {@code enabled} ({@code true}).
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code healthChecker} property.
     *
     * @return possible object is {@link HealthChecker}
     */
    @Element("health-checker")
    HealthChecker getHealthChecker();

    /**
     * Sets the value of the {@code healthChecker} property.
     *
     * @param healthChecker allowed object is {@link HealthChecker}
     */
    void setHealthChecker(HealthChecker healthChecker) throws PropertyVetoException;
}
