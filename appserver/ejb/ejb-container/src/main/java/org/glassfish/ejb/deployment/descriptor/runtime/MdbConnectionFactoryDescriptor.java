/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.deployment.descriptor.runtime;

import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;

import org.glassfish.deployment.common.Descriptor;

/**
 * iAS specific DD Element (see the ias-ejb-jar_2_0.dtd for this element)
 *
 * @author Ludo
 * @since JDK 1.4
 */
public class MdbConnectionFactoryDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private String jndiName;
    private ResourcePrincipalDescriptor defaultResourcePrincipal;

    /**
     * Getter for property defaultResourcePrincipal.
     *
     * @return Value of property defaultResourcePrincipal.
     */
    public ResourcePrincipalDescriptor getDefaultResourcePrincipal() {
        return defaultResourcePrincipal;
    }


    /**
     * Setter for property defaultResourcePrincipal.
     *
     * @param defaultResourcePrincipal New value of property defaultResourcePrincipal.
     */
    public void setDefaultResourcePrincipal(ResourcePrincipalDescriptor defaultResourcePrincipal) {
        this.defaultResourcePrincipal = defaultResourcePrincipal;
    }


    /**
     * Getter for property jndiName.
     *
     * @return Value of property jndiName.
     */
    public java.lang.String getJndiName() {
        return jndiName;
    }


    /**
     * Setter for property jndiName.
     *
     * @param jndiName New value of property jndiName.
     */
    public void setJndiName(java.lang.String jndiName) {
        this.jndiName = jndiName;
    }

}
