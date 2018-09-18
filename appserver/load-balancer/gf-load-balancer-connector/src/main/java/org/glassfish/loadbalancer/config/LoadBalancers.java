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

package org.glassfish.loadbalancer.config;

import org.glassfish.api.I18n;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.DuckTyped;

import org.glassfish.config.support.*;

import java.util.List;

import com.sun.enterprise.config.serverbeans.DomainExtension;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "loadBalancer"
}) */

// general solution needed; this is intermediate solution
@Configured
public interface LoadBalancers extends ConfigBeanProxy, DomainExtension {

    /**
     * Gets the value of the loadBalancer property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the loadBalancer property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLoadBalancer().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link LoadBalancer }
     */
    @Element
    @Delete(value="delete-http-lb", resolver= TypeAndNameResolver.class,
            decorator=LoadBalancer.DeleteDecorator.class,
            i18n=@I18n("delete.http.lb.command"))
    @Listing(value="list-http-lbs", i18n=@I18n("list.http.lbs.command"))
    public List<LoadBalancer> getLoadBalancer();

    /**
     * Return the load balancer config with the given name,
     * or null if no such load balancer exists.
     *
     * @param   name    the name of the lb config
     * @return          the LoadBalancer object, or null if no such lb config
     */

    @DuckTyped
    public LoadBalancer getLoadBalancer(String name);

    class Duck {
        public static LoadBalancer getLoadBalancer(LoadBalancers instance, String name) {
            for (LoadBalancer lb : instance.getLoadBalancer()) {
                if (lb.getName().equals(name)) {
                    return lb;
                }
            }
            return null;
        }
    }
}
