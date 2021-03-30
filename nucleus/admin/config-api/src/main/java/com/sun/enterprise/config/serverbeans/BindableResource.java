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

import com.sun.enterprise.config.serverbeans.customvalidators.ResourceNameConstraint;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.DuckTyped;

import java.beans.PropertyVetoException;
import jakarta.validation.Payload;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@ResourceNameConstraint(message = "{resourcename.invalid.character}", payload = BindableResource.class)
public interface BindableResource extends Resource, Payload {
    /**
     * Gets the value of the jndiName property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = "[^',][^',\\\\]*")
    public String getJndiName();

    /**
     * Sets the value of the jndiName property.
     *
     * @param value allowed object is {@link String }
     */
    public void setJndiName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is {@link String }
     */
    void setEnabled(String value) throws PropertyVetoException;

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(BindableResource resource) {
            return resource.getJndiName();
        }
    }

}
