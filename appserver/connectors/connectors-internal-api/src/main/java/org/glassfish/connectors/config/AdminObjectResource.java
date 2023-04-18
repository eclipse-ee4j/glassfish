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

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * The {@code admin-object-resource} element describes an administered object
 * for a inbound resource adapter.
 */
@Configured
@ResourceConfigCreator(commandName="create-admin-object")
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-admin-object"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-admin-object")
})
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.ADMIN_OBJECT_RESOURCE)
@UniqueResourceNameConstraint(message = "{resourcename.isnot.unique}", payload = AdminObjectResource.class)
public interface AdminObjectResource extends ConfigBeanProxy, BindableResource, Resource, PropertyBag {

    /**
     * Interface definition for the administered object.
     *
     * @return the interface definition
     */
    @Attribute
    @NotNull
    String getResType();

    /**
     * Sets the Interface definition for the administered object.
     *
     * @param resType allowed object is {@link String}
     */
    void setResType(String resType) throws PropertyVetoException;

    /**
     * Gets the value of the {@code resAdapter} property.
     *
     * <p>Name of the inbound resource adapter.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getResAdapter();

    /**
     * Gets the value of the (admin object) {@code className} property.
     *
     * <p>Name of the admin object class.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getClassName();

    /**
     * Sets the value of the (admin object) {@code className} property.
     *
     * @param className allowed object is {@link String}
     */
    void setClassName(String className) throws PropertyVetoException;

    /**
     * Sets the value of the {@code resAdapter} property.
     *
     * @param resAdapter allowed object is {@link String}
     */
    void setResAdapter(String resAdapter) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute (defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code description} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the {@code description} property.
     *
     * @param description allowed object is {@link String}
     */
    void setDescription(String description) throws PropertyVetoException;

    /**
        Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props" )
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    default String getIdentity() {
        return getJndiName();
    }
}
