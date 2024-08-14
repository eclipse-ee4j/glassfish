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

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;

import org.glassfish.api.Param;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

import static org.glassfish.config.support.Constants.NAME_APP_REGEX;

/**
 * References to applications deployed to the server instance
 */
@Configured
public interface ApplicationRef extends ConfigBeanProxy, Payload {

    /**
     * Gets the value of the {@code enabled} property.
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
    @Param(name = "enabled", optional = true, defaultValue = "true")
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code virtualServers} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getVirtualServers();

    /**
     * Sets the value of the {@code virtualServers} property.
     *
     * @param virtualServers allowed object is {@link String}
     */
    @Param(name = "virtualservers", optional = true)
    void setVirtualServers(String virtualServers) throws PropertyVetoException;

    /**
     * Gets the value of the {@code lbEnabled} property. A boolean flag that causes
     * any and all load-balancers using this application to consider this application
     * unavailable to them.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getLbEnabled();

    /**
     * Sets the value of the {@code lbEnabled} property.
     *
     * @param lbEnabled allowed object is {@link String}
     */
    void setLbEnabled(String lbEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code disableTimeoutInMinutes} property. The time,
     * in minutes, that it takes this application to reach a quiescent state after
     * having been disabled.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "30")
    String getDisableTimeoutInMinutes();

    /**
     * Sets the value of the {@code disableTimeoutInMinutes} property.
     *
     * @param disableTimeoutInMinutes allowed object is {@link String}
     */
    void setDisableTimeoutInMinutes(String disableTimeoutInMinutes) throws PropertyVetoException;

    /**
     * Gets the value of the {@code ref} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_APP_REGEX, message = "{appref.invalid.name}", payload = ApplicationRef.class)
    String getRef();

    /**
     * Sets the value of the {@code ref} property.
     *
     * @param ref allowed object is {@link String}
     */
    @Param(name = "reference-name", primary = true)
    void setRef(String ref) throws PropertyVetoException;

}
