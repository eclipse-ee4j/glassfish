/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.DomainExtension;

import java.util.List;

import org.glassfish.api.I18n;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.glassfish.config.support.Delete;
import org.glassfish.config.support.Listing;
import org.glassfish.config.support.TypeAndNameResolver;

/**
 *
 */
// general solution needed; this is intermediate solution
@Configured
public interface LoadBalancers extends ConfigBeanProxy, DomainExtension {

    /**
     * Gets the value of the {@code loadBalancer} property.
     *
     * <p>This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present inside
     * the JAXB object. This is why there is not a {@code set} method for the
     * {@code loadBalancer} property.
     *
     * <p>For example, to add a new item, do as follows:
     *
     * <pre>
     *    getLoadBalancer().add(newItem);
     * </pre>
     *
     * <p>Objects of the following type(s) are allowed in the list{@link LoadBalancer}.
     */
    @Element
    @Delete(
            value = "delete-http-lb",
            resolver = TypeAndNameResolver.class,
            decorator = LoadBalancer.DeleteDecorator.class,
            i18n = @I18n("delete.http.lb.command")
    )
    @Listing(value = "list-http-lbs", i18n = @I18n("list.http.lbs.command"))
    List<LoadBalancer> getLoadBalancer();

    /**
     * Return the load balancer config with the given name,
     * or {@code null} if no such load balancer exists.
     *
     * @param name the name of the lb config
     *
     * @return the {@link LoadBalancer} object, or {@code null} if no such lb config
     */
    default LoadBalancer getLoadBalancer(String name) {
        for (LoadBalancer lb : getLoadBalancer()) {
            if (lb.getName().equals(name)) {
                return lb;
            }
        }
        return null;
    }
}
