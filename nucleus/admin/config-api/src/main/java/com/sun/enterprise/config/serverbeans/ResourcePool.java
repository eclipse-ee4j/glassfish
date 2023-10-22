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

import com.sun.enterprise.config.serverbeans.customvalidators.ResourceNameConstraint;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import jakarta.validation.Payload;

import org.jvnet.hk2.config.Attribute;

import static org.glassfish.config.support.Constants.NAME_APP_REGEX;

@ResourceNameConstraint(message = "{resourcename.invalid.character}", payload = ResourcePool.class)
public interface ResourcePool extends Resource, Payload {

    /**
     * Gets the value of the {@code name} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_APP_REGEX, message = "{resourcepool.invalid.name.key}", payload = ResourcePool.class)
    String getName();

    /**
     * Sets the value of the {@code name} property.
     *
     * @param name allowed object is {@link String}
     */
    void setName(String name) throws PropertyVetoException;

    default String getIdentity() {
        return getName();
    }

    /**
     * Gets the value of the {@code ping} property.
     *
     * <p>Property to ping pool during creation.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getPing();

    /**
     * Sets the value of the {@code ping} property.
     *
     * @param ping allowed object is {@link String}
     */
    void setPing(String ping) throws PropertyVetoException;
}
