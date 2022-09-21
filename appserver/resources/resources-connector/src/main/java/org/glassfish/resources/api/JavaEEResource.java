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

package org.glassfish.resources.api;

import com.sun.enterprise.repository.ResourceProperty;

import java.util.Set;

import org.glassfish.resourcebase.resources.api.ResourceInfo;

/**
 * Interface representing J2EE Resource info.
 *
 * @author Kenneth Saks
 */
public interface JavaEEResource {

    /**
     * Resource Types
     */
    int JMS_DESTINATION = 1;
    int JMS_CNX_FACTORY = 2;
    int JDBC_RESOURCE = 3;
    int JDBC_XA_RESOURCE = 4;
    int JDBC_DRIVER = 5;
    int CONNECTOR_RESOURCE = 6;
    int RESOURCE_ADAPTER = 7;

    // START OF IASRI #4626188
    int JDBC_CONNECTION_POOL = 8;
    int PMF_RESOURCE = 9;
    int EXTERNAL_JNDI_RESOURCE = 10;
    int CUSTOM_RESOURCE = 11;
    // START OF IASRI #4650786
    int MAIL_RESOURCE = 12;
    // END OF IASRI #4650786
    // END OF IASRI #4626188

    /**
     * Resource Info. Immutable.
     */
    ResourceInfo getResourceInfo();

    /**
     * Resource type.  Defined above. Immutable.
     */
    int getType();

    /**
     * Set containing elements of type ResourceProperty.
     * Actual property names are resource type specific.
     *
     * @return Shallow copy of resource property set. If
     *         resource has 0 properties, empty set is
     *         returned.
     */
    Set<ResourceProperty> getProperties();

    /**
     * Add a property. Underlying set is keyed by
     * property name.  The new property overrides any
     * existing property with same name.
     */
    void addProperty(ResourceProperty property);

    /**
     * Remove a property. Underlying set is keyed by
     * property name.
     *
     * @return true if property was removed, false if
     *         property was not found
     */
    boolean removeProperty(ResourceProperty property);

    /**
     * Get a property with the given name.
     *
     * @return ResourceProperty or null if not found.
     */
    ResourceProperty getProperty(String propertyName);

    void setEnabled(boolean value);

    boolean isEnabled();

    void setDescription(String value);

    String getDescription();
}
