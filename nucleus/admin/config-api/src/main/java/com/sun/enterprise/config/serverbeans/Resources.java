/*
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

import org.jvnet.hk2.config.*;

import java.util.*;

/**
 * Applications can lookup resources registered in the server. These can be through portable JNDI names (eg:
 * resource-ref in standard deployment descriptors like ejb-jar.xml, web.xml etc.,) or by doing direct lookup.
 *
 * Each of the resource has valid target for defining the resource-ref. (eg: JdbcResource can be referred from Server,
 * Cluster, Stand Alone Instance, ServerResource can be referred from Server, Cluster, Stand Alone Instance, Config)
 */

/*
  Some of the resource types (sub types of "Resource" config bean) are :
  @XmlType(name = "", propOrder = {
    "CustomResource Or
    ExternalJndiResource Or
    JdbcResourceOrMailResource Or
    AdminObjectResource Or
    ConnectorResource Or
    ResourceAdapterConfig Or
    JdbcConnectionPool Or
    ConnectorConnectionPool Or
    ServerResource"
}) */

@Configured
public interface Resources extends ConfigBeanProxy {

    /**
     * Returns a list of Resources like Custom Resource Or External Jndi Resource Or Jdbc Resource Or Mail Resource Or Admin
     * Object Resource Or Connector Resource Or Resource Adapter Config Or Jdbc Connection Pool Or Connector Connection
     * Pool.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
     * customResource Or ExternalJndiResource Or JdbcResource Or MailResource Or AdminObjectResource Or ConnectorResource Or
     * ResourceAdapterConfig Or JdbcConnectionPool Or ConnectorConnectionPool.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     *    get(CustomResource Or ExternalJndiResource Or JdbcResource Or MailResource Or AdminObjectResource Or
     *    ConnectorResource Or ResourceAdapterConfig Or JdbcConnectionPool Or ConnectorConnectionPool).add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Any sub type(s) of resource is allowed.
     */
    @Element("*")
    public List<Resource> getResources();

    @DuckTyped
    public <T> Collection<T> getResources(Class<T> type);

    @DuckTyped
    public <T> Resource getResourceByName(Class<T> type, String name);

    /*
    @DuckTyped
    public Collection<BindableResource> getResourcesOfPool(String connectionPoolName);
    */

    public class Duck {

        public static <T> Collection<T> getResources(Resources resources, Class<T> type) {
            Collection<T> filteredResources = new ArrayList<T>();
            for (Resource resource : resources.getResources()) {
                if (type.isInstance(resource)) {
                    filteredResources.add(type.cast(resource));
                }
            }
            return filteredResources;
        }

        public static <T> Resource getResourceByName(Resources resources, Class<T> type, String name) {
            Resource foundRes = null;
            Iterator itr = resources.getResources(type).iterator();
            while (itr.hasNext()) {
                Resource res = (Resource) (itr.next());
                String resourceName = res.getIdentity();
                if (name.equals(resourceName)) {
                    foundRes = res;
                    break;
                }
            }
            // make sure that the "type" provided and the matched resource are compatible.
            // eg: its possible that the requested resource is "ConnectorResource",
            // and matching resource is "JdbcResource" as we filter based on
            // the generic type (in this case BindableResource) and not on exact type.
            if (type != null && foundRes != null && type.isAssignableFrom(foundRes.getClass())) {
                return foundRes;
            } else {
                return null;
            }
        }
    }
}
