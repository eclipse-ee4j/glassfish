/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource.pool;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.allocator.ResourceAllocator;

import java.util.Hashtable;

import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * This resource pool is created when connection pooling is switched off Hence no pooling happens in this resource pool
 *
 * @author Kshitiz Saxena
 * @since 9.1
 */
public class UnpooledResource extends ConnectionPool {

    private PoolSize poolSize;

    /** Creates a new instance of UnpooledResourcePool */
    public UnpooledResource(PoolInfo poolInfo, Hashtable env) throws PoolingException {
        super(poolInfo, env);

        // No pool is being maintained, hence no pool cleanup is needed
        // in case of failure
        failAllConnections = false;
    }

    @Override
    protected synchronized void initPool(ResourceAllocator allocator) throws PoolingException {
        if (poolInitialized) {
            return;
        }

        // Nothing needs to be done as pooling is disabled
        poolSize = new PoolSize(maxPoolSize);
        poolInitialized = true;
    }

    @Override
    protected ResourceHandle prefetch(ResourceSpec spec, ResourceAllocator alloc) {
        return null;
    }

    @Override
    protected void reconfigureSteadyPoolSize(int oldSteadyPoolSize, int newSteadyPoolSize) throws PoolingException {
        // No-op as the steady pool size should not be reconfigured when connection
        // pooling is switched off
    }

    @Override
    protected ResourceHandle getUnenlistedResource(ResourceSpec spec, ResourceAllocator alloc) throws PoolingException {

        this.poolSize.increment();
        final ResourceHandle handle;
        try {
            handle = createSingleResource(alloc);
        } catch (PoolingException | RuntimeException ex) {
            this.poolSize.decrement();
            throw ex;
        }
        handle.getResourceState().reset();

        // TODO: document that all get(Unenlisted)Resource methods must return state busy resource
        // TODO: rename variables, they currently have 2 or 3 names by default: handle, resource and resourceHandle
        setResourceStateToBusy(handle);
        return handle;
    }

    @Override
    public void resourceErrorOccurred(ResourceHandle resourceHandle) throws IllegalStateException {
        freeUnenlistedResource(resourceHandle);
    }

    @Override
    protected void freeUnenlistedResource(ResourceHandle resourceHandle) {
        this.poolSize.decrement();
        deleteResource(resourceHandle);
    }
}
