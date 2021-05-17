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

import com.sun.enterprise.config.serverbeans.Resource;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.*;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;
import static org.glassfish.config.support.Constants.NAME_REGEX;

import org.glassfish.quality.ToDo;

import jakarta.validation.constraints.Pattern;
/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

/**
 *  This element is for configuring the resource adapter. These values
 * (properties) over-rides the default values present in ra.xml. The name
 * attribute has to be unique . It is optional for PE. It is used mainly for EE.
 */

@Configured
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-resource-adapter-config"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-resource-adapter-config")
})
@ResourceTypeOrder(deploymentOrder= ResourceDeploymentOrder.RESOURCEADAPTERCONFIG_RESOURCE)
public interface ResourceAdapterConfig extends ConfigBeanProxy, Resource, PropertyBag {

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
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
     * Gets the value of the threadPoolIds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getThreadPoolIds();

    /**
     * Sets the value of the threadPoolIds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setThreadPoolIds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the resourceAdapterName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @Pattern(regexp="[^',][^',\\\\]*")
    public String getResourceAdapterName();

    /**
     * Sets the value of the resourceAdapterName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setResourceAdapterName(String value) throws PropertyVetoException;

    /**
        Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();

    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(ResourceAdapterConfig resource){
            return resource.getResourceAdapterName();
        }
    }
}
