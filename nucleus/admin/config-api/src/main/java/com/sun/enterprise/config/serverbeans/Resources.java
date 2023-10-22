/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.config.serverbeans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.glassfish.api.naming.SimpleJndiName;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Applications can lookup resources registered in the server. These can be
 * through portable JNDI names (eg: {@code resource-ref} in standard
 * deployment descriptors like {@code ejb-jar.xml}, {@code web.xml} etc.)
 * or by doing direct lookup.
 *
 * <p>Each of the resource has valid target for defining the {@code resource-ref}
 * (eg: JdbcResource can be referred from Server, Cluster, Stand Alone Instance,
 * ServerResource can be referred from Server, Cluster, Stand Alone Instance, Config)
 */
@Configured
public interface Resources extends ConfigBeanProxy {

    /**
     * Returns a list of Resources like Custom Resource Or External Jndi Resource
     * Or Jdbc Resource Or Mail Resource Or Admin Object Resource Or Connector Resource
     * Or Resource Adapter Config Or Jdbc Connection Pool Or Connector Connection Pool.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * customResource Or ExternalJndiResource Or JdbcResource Or MailResource
     * Or AdminObjectResource Or ConnectorResource Or ResourceAdapterConfig
     * Or JdbcConnectionPool Or ConnectorConnectionPool.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    get(CustomResource Or ExternalJndiResource Or JdbcResource Or MailResource Or AdminObjectResource Or
     *    ConnectorResource Or ResourceAdapterConfig Or JdbcConnectionPool Or ConnectorConnectionPool).add(newItem);
     * </pre>
     *
     * <p>Any subtype(s) of resource is allowed.
     */
    @Element("*")
    List<Resource> getResources();

    default <T> Collection<T> getResources(Class<T> type) {
        Collection<T> filteredResources = new ArrayList<>();
        for (Resource resource : getResources()) {
            if (type.isInstance(resource)) {
                filteredResources.add(type.cast(resource));
            }
        }
        return filteredResources;
    }

    default <T extends Resource> T getResourceByName(Class<T> type, SimpleJndiName name) {
        T foundResource = null;
        for (T resource : getResources(type)) {
            String resourceName = resource.getIdentity();
            if (name.toString().equals(resourceName)) {
                foundResource = resource;
                break;
            }
        }
        // make sure that the "type" provided and the matched resource are compatible.
        // eg: it's possible that the requested resource is "ConnectorResource",
        // and matching resource is "JdbcResource" as we filter based on
        // the generic type (in this case BindableResource) and not on exact type.
        if (type == null || foundResource == null || !type.isAssignableFrom(foundResource.getClass())) {
            return null;
        }
        return foundResource;
    }
}
