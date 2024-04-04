/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.resource.allocator;

import java.util.Set;

import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.resource.ResourceHandle;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ManagedConnection;

/**
 * @author Tony Ng
 */
public interface ResourceAllocator {

    ResourceHandle createResource() throws PoolingException;

    void fillInResourceObjects(ResourceHandle resource) throws PoolingException;

    void closeUserConnection(ResourceHandle resource) throws PoolingException;

    void destroyResource(ResourceHandle resource) throws PoolingException;

    /**
     * Represents the "match-connections" configuration value of a connection pool.<br>
     * If true, enables connection matching. You can set to false if connections are homogeneous.
     * <p>
     * Jakarta documentation:
     * {@link jakarta.resource.spi.ManagedConnectionFactory#matchManagedConnections(Set, Subject, ConnectionRequestInfo)}
     * mentions: "criteria used for matching is specific to a resource adapter and is not prescribed by the Connector
     * specification."
     *
     * @param handle The resource handle to be matched.
     * @return True if matching applies to this handle, otherwise false
     */
    boolean matchConnection(ResourceHandle handle);

    boolean isTransactional();

    void cleanup(ResourceHandle resource) throws PoolingException;

    boolean shareableWithinComponent();

    Object getSharedConnection(ResourceHandle h) throws PoolingException;

    Set<ManagedConnection> getInvalidConnections(Set<ManagedConnection> connectionSet) throws ResourceException;

    boolean isConnectionValid(ResourceHandle resource);

    boolean hasValidatingMCF();
}
