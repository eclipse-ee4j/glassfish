/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.resource.allocator.ResourceAllocator;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;

import java.util.Set;

/**
 * ResourceHandler to create/delete resource
 *
 * @author Jagadish Ramu
 */
public interface ResourceHandler {

    /**
     * destroys the given resource
     *
     * @param resourceHandle resource to be destroyed
     */
    void deleteResource(ResourceHandle resourceHandle);

    /**
     * create a new resource using the given resource-allocator
     *
     * @param allocator allocator to create a resource
     * @return newly created resource
     * @throws PoolingException when unable to create a resource
     */
    ResourceHandle createResource(ResourceAllocator allocator) throws PoolingException;

    /**
     * create a new resource and add it to pool (using default resource-allocator)
     *
     * @throws PoolingException when unable to create a resource
     */
    void createResourceAndAddToPool() throws PoolingException;

    /**
     * gets the invalid connections from the given connections set
     *
     * @param connections that need to be validated
     * @return invalid connections set
     * @throws ResourceException when unable to validate
     */
    Set<ManagedConnection> getInvalidConnections(Set<ManagedConnection> connections) throws ResourceException;

    /**
     * callback method to handle the case of invalid connection detected
     *
     * @param h connection that is invalid
     */
    void invalidConnectionDetected(ResourceHandle h);
}
