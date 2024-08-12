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

package org.glassfish.connectors.config;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

@Configured
public interface GroupMap extends ConfigBeanProxy {

    /**
     * Gets the value of the EISGroup - a Group in the EIS security
     * domain that is being mapped to a Group in the application server's
     * security domain.
     *
     * @return possible object is {@link String}
     */
    @Attribute(key = true)
    @NotNull
    String getEisGroup();

    /**
     * Sets the value of the EISGroup - a Group in the EIS security
     * domain that is being mapped to a Group in the application server's
     * security domain.
     *
     * @param group allowed object is {@link String}
     */
    void setEisGroup(String group) throws PropertyVetoException;

    /**
     * Gets the value of the MappedGroup - a Group that is valid in
     * the application server's security domain.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getMappedGroup();

    /**
     * Sets the value of the MappedGroup - a Group that is valid in
     * the application server's security domain.
     *
     * @param group allowed object is {@link String}
     */
    void setMappedGroup(String group) throws PropertyVetoException;
}
