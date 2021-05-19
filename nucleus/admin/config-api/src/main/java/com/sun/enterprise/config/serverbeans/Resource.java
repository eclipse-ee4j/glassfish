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
import org.jvnet.hk2.config.DuckTyped;

import jakarta.validation.constraints.Pattern;
import java.beans.PropertyVetoException;

/**
 * Tag interface for all types of resource.
 *
 * @author Jerome Dochez
 */
@Configured
public interface Resource extends ConfigBeanProxy {

    /**
     * Gets the value of the objectType property. where object-type defines the type of the resource. It can be: system-all
     * - These are system resources for all instances and DAS system-all-req - These are system-all resources that are
     * required to be configured in the system (cannot be deleted). system-admin - These are system resources only in DAS
     * system-instance - These are system resources only in instances (and not DAS) user - User resources (This is the
     * default for all elements)
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "user")
    @Pattern(regexp = "(system-all|system-all-req|system-admin|system-instance|user)")
    String getObjectType();

    /**
     * Sets the value of the objectType property.
     *
     * @param value allowed object is {@link String }
     * @throws PropertyVetoException if the change is unacceptable to one of the listeners.
     */
    void setObjectType(String value) throws PropertyVetoException;

    /**
     * Gets the value of deployment-order.
     *
     * @return
     */
    @Attribute(defaultValue = "100", dataType = Integer.class)
    String getDeploymentOrder();

    /**
     * Sets the value of the deployment order.
     *
     * @param value
     * @throws PropertyVetoException
     */
    void setDeploymentOrder(String value) throws PropertyVetoException;

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(Resource resource) {
            return null;
        }

        /*
         * True if this resource should be copied to any new instance or cluster.
         * Note: this isn't a DuckTyped method because it requires every subclass
         * to implement this method.
         */
        public static boolean copyToInstance(Resource resource) {
            String ot = resource.getObjectType();
            return "system-all".equals(ot) || "system-all-req".equals(ot) || "system-instance".equals(ot);
        }
    }
}
