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

package org.glassfish.jdbc.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePoolReference;
import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;
import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.api.Param;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.jvnet.hk2.config.*;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import java.beans.PropertyVetoException;
import java.util.List;

/**
 * JDBC javax.sql.(XA)DataSource resource definition
 */

/* @XmlType(name = "", propOrder = {
    "description",
    "property"
}) */

@Configured
@ResourceConfigCreator(commandName="create-jdbc-resource")
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-jdbc-resource"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-jdbc-resource")
})
@ResourceTypeOrder(deploymentOrder=ResourceDeploymentOrder.JDBC_RESOURCE)
@ReferenceConstraint(skipDuringCreation=true, payload=JdbcResource.class)
@UniqueResourceNameConstraint(message="{resourcename.isnot.unique}", payload=JdbcResource.class)
public interface JdbcResource extends ConfigBeanProxy, Resource,
        PropertyBag, BindableResource, Payload, ResourcePoolReference {

    /**
     * Gets the value of the poolName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Override
    @Attribute
    @NotNull
    @Param(name=name)
    @ReferenceConstraint.RemoteKey(message="{resourceref.invalid.poolname}", type=JdbcConnectionPool.class)
    String getPoolName();

    String name = "connectionpoolid";

    /**
     * Gets the value of the enabled property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Override
    @Attribute (defaultValue="true",dataType=Boolean.class)
    @Param
    String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    @Override
    void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @Param
    String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setDescription(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();


    @Override
    @DuckTyped
    String getIdentity();

    class Duck {
        public static String getIdentity(JdbcResource resource){
            return resource.getJndiName();
        }
    }
}
