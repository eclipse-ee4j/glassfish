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

package org.glassfish.connectors.config;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import jakarta.validation.constraints.NotNull;

/**
 *
 */

/* @XmlType(name = "") */

@Configured
public interface BackendPrincipal extends ConfigBeanProxy {

    /**
     * Gets the value of the userName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    public String getUserName();

    /**
     * Sets the value of the userName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setUserName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the password property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getPassword();

    /**
     * Sets the value of the password property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setPassword(String value) throws PropertyVetoException;



}
