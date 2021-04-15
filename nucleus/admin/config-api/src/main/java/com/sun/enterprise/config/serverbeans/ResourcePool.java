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

import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.customvalidators.ResourceNameConstraint;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.DuckTyped;

import static org.glassfish.config.support.Constants.NAME_APP_REGEX;

import java.beans.PropertyVetoException;
import jakarta.validation.Payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@ResourceNameConstraint(message = "{resourcename.invalid.character}", payload = ResourcePool.class)
public interface ResourcePool extends Resource, Payload {
    /**
     * Gets the value of the name property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_APP_REGEX, message = "{resourcepool.invalid.name.key}", payload = ResourcePool.class)
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(ResourcePool resource) {
            return resource.getName();
        }
    }

    /**
     * Gets the value of the ping property.
     *
     * Property to ping pool during creation.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getPing();

    /**
     * Sets the value of the ping property.
     *
     * @param value allowed object is {@link String }
     */
    void setPing(String value) throws PropertyVetoException;

}
