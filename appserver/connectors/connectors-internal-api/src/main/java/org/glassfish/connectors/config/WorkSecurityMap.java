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

import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.*;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.beans.PropertyVetoException;
import java.util.List;

@Configured
@ResourceTypeOrder(deploymentOrder= ResourceDeploymentOrder.WORKSECURITYMAP_RESOURCE)
public interface WorkSecurityMap  extends /*Named,*/ ConfigBeanProxy, Resource {

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setDescription(String value) throws PropertyVetoException;


    /**
     * Gets the value of the ra name
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    @Pattern(regexp="[^',][^',\\\\]*")
    public String getResourceAdapterName();

    /**
     * Sets the value of the ra name
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceAdapterName(String value) throws PropertyVetoException;

    /**
     * Gets the group map
     * @return group map
     *
     */
    @Element
    @NotNull
    public List<GroupMap> getGroupMap();

    /**
     * gets the principal map
     * @return principal map
     */
    @Element
    @NotNull
    public List<PrincipalMap> getPrincipalMap();

    /**
     *  Name of the configured object
     *
     * @return name of the configured object
     */
    @Attribute(required=true, key=true)
    @Pattern(regexp="[^',][^',\\\\]*")
    @NotNull
    public String getName();

    public void setName(String value) throws PropertyVetoException;

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(WorkSecurityMap wsm){
            return ("resource-adapter : " + wsm.getResourceAdapterName()
                    + " : security-map : " +  wsm.getName());
        }
    }
}
