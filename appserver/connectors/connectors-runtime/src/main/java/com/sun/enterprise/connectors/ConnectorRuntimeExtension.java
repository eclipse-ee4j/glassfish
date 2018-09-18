/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.deployment.Application;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.util.Collection;

/**
 * @author Shalini M
 */

@Contract
public interface ConnectorRuntimeExtension {

    /**
     * Return the collection of system resources and pools.
     *
     * @return collection of system resources and pools.
     */
    Collection<Resource> getAllSystemRAResourcesAndPools();


    void registerDataSourceDefinitions(Application application);

    void unRegisterDataSourceDefinitions(Application application);

    public Object lookupDataSourceInDAS(ResourceInfo resourceInfo) throws ConnectorRuntimeException;

    public DeferredResourceConfig getDeferredResourceConfig(Object resource,
                                                            Object pool, String resType, String raName)
            throws ConnectorRuntimeException;

    public String getResourceType(ConfigBeanProxy cb);

    public boolean isConnectionPoolReferredInServerInstance(PoolInfo poolInfo);

    public PoolInfo getPoolNameFromResourceJndiName(ResourceInfo resourceInfo);
}
