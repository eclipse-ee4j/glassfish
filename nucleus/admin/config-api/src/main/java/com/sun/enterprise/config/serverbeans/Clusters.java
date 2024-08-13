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

package com.sun.enterprise.config.serverbeans;

import java.util.List;

import org.glassfish.api.I18n;
import org.glassfish.config.support.Create;
import org.glassfish.config.support.Delete;
import org.glassfish.config.support.TypeAndNameResolver;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Clusters configuration.
 *
 * <p>Maintain a list of {@link Cluster} active configurations.
 */
@Configured
public interface Clusters extends ConfigBeanProxy {

    /**
     * Return the list of clusters currently configured.
     *
     * @return list of {@link Cluster }
     */
    @Element
    @Create(
            value = "create-cluster",
            decorator = Cluster.Decorator.class,
            i18n = @I18n("create.cluster.command")
    )
    @Delete(
            value = "delete-cluster",
            resolver = TypeAndNameResolver.class,
            decorator = Cluster.DeleteDecorator.class,
            i18n = @I18n("delete.cluster.command")
    )
    List<Cluster> getCluster();

    /**
     * Return the cluster with the given {@code name}, or {@code null} if no such cluster exists.
     *
     * @param name the name of the cluster
     * @return the Cluster object, or null if no such server
     */
    default Cluster getCluster(String name) {
        for (Cluster cluster : getCluster()) {
            if (cluster.getName().equals(name)) {
                return cluster;
            }
        }
        return null;
    }
}
