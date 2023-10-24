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

package com.sun.enterprise.resource.pool.datastructure;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.allocator.ResourceAllocator;

import java.util.List;

/**
 * Represents a pool datastructure. Helps to plug-in various implementations that the pool can use.<br>
 * Datastructure need to synchronize the operations.
 *
 * @author Jagadish Ramu
 */
public interface DataStructure {

    String DS_TYPE_DEFAULT = "LIST";
    String DS_TYPE_CIRCULAR_LIST = "CIRCULAR_LIST";
    String DS_TYPE_PARTITIONED = "PARTITIONED";

    /**
     * Set maxSize based on the new max pool size set on the connection pool during a reconfiguration.
     *
     * @param maxSize
     */
    void setMaxSize(int maxSize);

    /**
     * creates a new resource and adds to the datastructure.
     *
     * @param allocator ResourceAllocator
     * @param count Number (units) of resources to create
     * @return int number of resources added.
     * @throws PoolingException when unable to create a resource
     */
    int addResource(ResourceAllocator allocator, int count) throws PoolingException;

    /**
     * get a resource from the datastructure
     *
     * @return ResourceHandle
     */
    ResourceHandle getResource();

    /**
     * remove the specified resource from the datastructure
     *
     * @param resource ResourceHandle
     */
    void removeResource(ResourceHandle resource);

    /**
     * returns the resource to the datastructure
     *
     * @param resource ResourceHandle
     */
    void returnResource(ResourceHandle resource);

    /**
     * get the count of free resources in the datastructure
     *
     * @return int count
     */
    int getFreeListSize();

    /**
     * remove & destroy all resources from the datastructure.
     */
    void removeAll();

    /**
     * get total number of resources in the datastructure
     *
     * @return int count
     */
    int getResourcesSize();

    /**
     * Get all resources in the datastructure Note : do not use this for normal usages as it can potentially represent all
     * resources (including the ones in use). This is used under special circumstances where there is a need to process all
     * resources.
     *
     * @return List<ResourceHandle>
     */
    List<ResourceHandle> getAllResources();
}
