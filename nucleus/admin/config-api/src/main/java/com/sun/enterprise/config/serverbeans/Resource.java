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

import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Tag interface for all types of resource.
 *
 * @author Jerome Dochez
 */
@Configured
public interface Resource extends ConfigBeanProxy {

    String OBJECT_TYPES = "(system-all|system-all-req|system-admin|system-instance|user)";

    /**
     * Gets the value of the {@code objectType} property. where object-type defines the type
     * of the resource. It can be:
     *
     * <ul>
     *     <li>system-all - These are system resources for all instances and DAS</li>
     *     <li>system-all-req - These are system-all resources that are required to be
     *     configured in the system (cannot be deleted)</li>
     *     <li>system-admin - These are system resources only in DAS</li>
     *     <li>system-instance - These are system resources only in instances (and not DAS)</li>
     *     <li>user - User resources (This is the default for all elements)</li>
     * </ul>
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "user")
    @Pattern(regexp = OBJECT_TYPES, message = "Valid values: " + OBJECT_TYPES)
    String getObjectType();

    /**
     * Sets the value of the {@code objectType} property.
     *
     * @param objectType allowed object is {@link String}
     * @throws PropertyVetoException if the change is unacceptable to one of the listeners.
     */
    void setObjectType(String objectType) throws PropertyVetoException;

    /**
     * Gets the value of {@code deploymentOrder}.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "100", dataType = Integer.class)
    String getDeploymentOrder();

    /**
     * Sets the value of the {@code deploymentOrder}.
     *
     * @param deploymentOrder allowed object is {@link String}
     * @throws PropertyVetoException if the change is unacceptable to one of the listeners.
     */
    void setDeploymentOrder(String deploymentOrder) throws PropertyVetoException;

    default String getIdentity() {
        return null;
    }

    /**
     * Gets {@code true} if this resource should be copied to any new instance or cluster.
     */
    static boolean copyToInstance(Resource resource) {
        String ot = resource.getObjectType();
        return "system-all".equals(ot) || "system-all-req".equals(ot) || "system-instance".equals(ot);
    }
}
