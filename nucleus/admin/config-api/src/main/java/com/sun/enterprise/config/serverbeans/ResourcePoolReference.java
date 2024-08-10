/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;

/**
 * This is an abstraction for a resource that refers a resource-pool. Samples are jdbc-resource and connector-resource.
 */
public interface ResourcePoolReference {

    /**
     * Gets the value of the poolName property.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @NotNull
    String getPoolName();

    /**
     * Sets the value of the poolName property.
     *
     * @param value allowed object is {@link String }
     */
    void setPoolName(String value) throws PropertyVetoException;
}
