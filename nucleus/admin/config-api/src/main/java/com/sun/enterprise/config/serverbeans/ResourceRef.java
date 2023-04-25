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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

@Configured
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-resource-ref"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-resource-ref")
})
public interface ResourceRef extends ConfigBeanProxy {

    String PATTERN_REF = "[^':,]+";

    /**
     * Determines whether the resource is active or ignored.
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
     * References the name attribute of a resources.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = PATTERN_REF, message = "Pattern: " + PATTERN_REF)
    String getRef();

    /**
     * Sets the value of the {@code ref} property.
     *
     * @param ref allowed object is {@link String}
     */
    void setRef(String ref) throws PropertyVetoException;
}
