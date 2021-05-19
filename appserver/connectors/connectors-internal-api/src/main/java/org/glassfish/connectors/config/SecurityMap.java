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
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import static org.glassfish.config.support.Constants.NAME_REGEX;

import java.beans.PropertyVetoException;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * Perform mapping from principal received during Servlet/EJB authentication,
 * to credentials accepted by the EIS. This mapping is optional.
 * It is possible to map multiple (server) principal to same backend principal
 *
 */

/* @XmlType(name = "", propOrder = {
    "principalOrUserGroup",
    "backendPrincipal"
}) */

@Configured
public interface SecurityMap extends ConfigBeanProxy {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    @Pattern(regexp=NAME_REGEX)
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the backendPrincipal property.
     *
     * @return possible object is
     *         {@link BackendPrincipal }
     */
    @Element(required=true)
    public BackendPrincipal getBackendPrincipal();

    /**
     * Sets the value of the backendPrincipal property.
     *
     * @param value allowed object is
     *              {@link BackendPrincipal }
     */
    public void setBackendPrincipal(BackendPrincipal value) throws PropertyVetoException;

    /**
     * get the list of principals to be mapped to backend-principal
     * @return list of principals
     */
    @Element
    public List<String> getPrincipal();

    void setPrincipal(List<String> principals) throws PropertyVetoException;


    /**
     * get the list of user-groups to be mapped to backend principal
     * @return list of user-groups
     */
    @Element
    public List<String> getUserGroup();

    void setUserGroup(List<String> userGroups) throws PropertyVetoException;
}
