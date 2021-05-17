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

import org.glassfish.api.Param;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;
import static org.glassfish.config.support.Constants.NAME_APP_REGEX;

import java.beans.PropertyVetoException;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;

/**
 * References to applications deployed to the server instance
 */

/* @XmlType(name = "") */

@Configured
public interface ApplicationRef extends ConfigBeanProxy, Payload {

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "enabled", optional = true, defaultValue = "true")
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the virtualServers property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getVirtualServers();

    /**
     * Sets the value of the virtualServers property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "virtualservers", optional = true)
    public void setVirtualServers(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lbEnabled property. A boolean flag that causes any and all load-balancers using this
     * application to consider this application unavailable to them.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getLbEnabled();

    /**
     * Sets the value of the lbEnabled property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLbEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the disableTimeoutInMinutes property. The time, in minutes, that it takes this application to reach
     * a quiescent state after having been disabled
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "30")
    public String getDisableTimeoutInMinutes();

    /**
     * Sets the value of the disableTimeoutInMinutes property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDisableTimeoutInMinutes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ref property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_APP_REGEX, message = "{appref.invalid.name}", payload = ApplicationRef.class)
    public String getRef();

    /**
     * Sets the value of the ref property.
     *
     * @param value allowed object is {@link String }
     */
    @Param(name = "reference-name", primary = true)
    public void setRef(String value) throws PropertyVetoException;

}
