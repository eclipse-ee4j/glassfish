/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.resources.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

import static org.glassfish.resources.admin.cli.ResourceConstants.CONNECTION_POOL_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.JNDI_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RESOURCE_ADAPTER_CONFIG_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.RES_ADAPTER_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_NAME;
import static org.glassfish.resources.admin.cli.ResourceConstants.WORK_SECURITY_MAP_RA_NAME;

/**
 * Class which represents the Resource.
 */
public class Resource {
    public static final String CUSTOM_RESOURCE          = "custom-resource";
    public static final String CONNECTOR_RESOURCE       = "connector-resource";
    public static final String ADMIN_OBJECT_RESOURCE    = "admin-object-resource";
    public static final String JDBC_RESOURCE            = "jdbc-resource";
    public static final String MAIL_RESOURCE            = "mail-resource";
    public static final String EXTERNAL_JNDI_RESOURCE   = "external-jndi-resource";

    public static final String JDBC_CONNECTION_POOL     = "jdbc-connection-pool";
    public static final String CONNECTOR_CONNECTION_POOL = "connector-connection-pool";

    public static final String RESOURCE_ADAPTER_CONFIG  = "resource-adapter-config";
    public static final String PERSISTENCE_MANAGER_FACTORY_RESOURCE = "persistence-manager-factory-resource";
    public static final String CONNECTOR_SECURITY_MAP    = "security-map";
    public static final String CONNECTOR_WORK_SECURITY_MAP    = "work-security-map";

    public static final List BINDABLE_RESOURCES = Collections.unmodifiableList(
            Arrays.asList(
                CUSTOM_RESOURCE,
                CONNECTOR_RESOURCE,
                ADMIN_OBJECT_RESOURCE,
                JDBC_RESOURCE,
                MAIL_RESOURCE,
                EXTERNAL_JNDI_RESOURCE
            ));

    public static final List RESOURCE_POOL = Collections.unmodifiableList(
            Arrays.asList(
                JDBC_CONNECTION_POOL,
                CONNECTOR_CONNECTION_POOL
            ));

    private final String resType;
    private final ResourceAttributes attributes = new ResourceAttributes();
    private final Properties props = new Properties();
    private String sDescription = null;

    public Resource(String type) {
        resType = type;
    }

    public String getType() {
        return resType;
    }

    public ResourceAttributes getAttributes() {
        return attributes;
    }

    public void setAttribute(String name, String value) {
        attributes.set(name, value);
    }

    public void setAttribute(String name, String[] value) {
        attributes.set(name, value);
    }

    public void setAttribute(String name, Properties value) {
        attributes.set(name, value);
    }

    public void setDescription(String sDescription) {
        this.sDescription = sDescription;
    }

    public String getDescription() {
        return sDescription;
    }

    public void setProperty(String name, String value) {
        props.setProperty(name, value);
    }

    public Properties getProperties() {
        return props;
    }

    //Used to figure out duplicates in a List<Resource>
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ( !(obj instanceof Resource) ) {
            return false;
        }
        Resource otherr = (Resource) obj;
        return otherr.getType().equals(this.getType())
            && otherr.getProperties().equals(this.getProperties())
            && otherr.getAttributes().equals(this.getAttributes());
    }

    //when a class overrides equals, override hashCode as well.
    @Override
    public int hashCode() {
        return Objects.hash(getAttributes(), getProperties(), getType());
    }

    //Used to figure out conflicts in a List<Resource>
    //A Resource is said to be in conflict with another Resource if the two
    //Resources have the same Identity [attributes that uniquely identify a Resource]
    //but different properties
    public boolean isAConflict(Resource r) {
        //If the two resource have the same identity
        if (hasSameIdentity(r)) {
            //If the two resources are not equal, then there is
            //conflict
            if (!r.equals(this)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the specified resource has the same identity as
     * this resource.
     */
    private boolean hasSameIdentity(Resource r) {

        //For two resources to have the same identity, atleast their types should match

        if(BINDABLE_RESOURCES.contains(this.getType())){
            if(!BINDABLE_RESOURCES.contains(r.getType())){
                return false;
            }
        }else if (RESOURCE_POOL.contains(this.getType())){
            if(!RESOURCE_POOL.contains(r.getType())){
                return false;
            }
        }else if (!(r.getType().equals(this.getType()))) {
            return false;
        }

        String rType = r.getType();

        //For all resources, their identity is their jndi-name
        if (rType.equals(CUSTOM_RESOURCE)|| rType.equals(EXTERNAL_JNDI_RESOURCE)
             || rType.equals(JDBC_RESOURCE)|| rType.equals(PERSISTENCE_MANAGER_FACTORY_RESOURCE)
             || rType.equals(CONNECTOR_RESOURCE)|| rType.equals(ADMIN_OBJECT_RESOURCE) || rType.equals(MAIL_RESOURCE)) {
            return isEqualAttribute(r, JNDI_NAME);
        }

        //For pools the identity is limited to pool name
        if (rType.equals(JDBC_CONNECTION_POOL) || rType.equals(CONNECTOR_CONNECTION_POOL)) {
            return isEqualAttribute(r, CONNECTION_POOL_NAME);
        }

        if (rType.equals(RESOURCE_ADAPTER_CONFIG)) {
            return isEqualAttribute(r, RES_ADAPTER_NAME);
        }

        if(rType.equals(CONNECTOR_WORK_SECURITY_MAP)){
            return isEqualAttribute(r,WORK_SECURITY_MAP_NAME) && isEqualAttribute(r, WORK_SECURITY_MAP_RA_NAME);
        }

        return false;
    }

    /**
     * Compares the attribute with the specified name
     * in this resource with the passed in resource and checks
     * if they are <code>equal</code>
     * <p>
     * Supports just string attribute values!
     */
    private boolean isEqualAttribute(Resource r, String name) {
        return getAttribute(r, name).equals(getAttribute(this, name));
    }

    /**
     * Utility method to get an <code>Attribute</code> of the given name
     * in the specified resource
     */
    private String getAttribute(Resource r, String name) {
        return r.getAttributes().getString(name);
    }

    @Override
    public String toString(){
        final String rType = getType();
        final String identity;
        if (rType.equals(CUSTOM_RESOURCE) || rType.equals(EXTERNAL_JNDI_RESOURCE) || rType.equals(JDBC_RESOURCE)
            || rType.equals(PERSISTENCE_MANAGER_FACTORY_RESOURCE) || rType.equals(CONNECTOR_RESOURCE)
            || rType.equals(ADMIN_OBJECT_RESOURCE) || rType.equals(MAIL_RESOURCE)) {
            identity = getAttribute(this, JNDI_NAME);
        } else if (rType.equals(JDBC_CONNECTION_POOL) || rType.equals(CONNECTOR_CONNECTION_POOL)) {
            identity = getAttribute(this, CONNECTION_POOL_NAME);
        } else if (rType.equals(RESOURCE_ADAPTER_CONFIG)) {
            identity = getAttribute(this, RESOURCE_ADAPTER_CONFIG_NAME);
        } else if (rType.equals(CONNECTOR_WORK_SECURITY_MAP)) {
            identity = getAttribute(this, WORK_SECURITY_MAP_NAME);
        } else {
            identity = "";
        }

        return identity + " of type " + resType;
    }

}
