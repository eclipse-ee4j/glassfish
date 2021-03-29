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
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

import java.beans.PropertyVetoException;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;

/**
 *
 */

/* @XmlType(name = "") */

@Configured
@RestRedirects({ @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-resource-ref"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-resource-ref") })
public interface ResourceRef extends ConfigBeanProxy {

    /**
     * Determines whether the resource is active or ignored.
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

    /**
     * References the name attribute of a resources, such as an {@link org.glassfish.connectors.config.JdbcResource} or
     * {@link org.glassfish.connectors.config.JdbcConnectionPool}.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = "[^':,][^':,]*")
    String getRef();

    /**
     * Sets the value of the ref property.
     *
     * @param value allowed object is {@link String }
     */
    void setRef(String value) throws PropertyVetoException;

}
