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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;
import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 * Syntax for supplying system properties as name value pairs
 */

/* @XmlType(name = "", propOrder = {
    "description"
}) */

@Configured
@RestRedirects({ @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-system-properties"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-system-property") })
public interface SystemProperty extends ConfigBeanProxy {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_REGEX)
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @NotNull
    public String getValue();

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDescription(String value) throws PropertyVetoException;

}
