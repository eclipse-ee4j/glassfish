/*
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

import com.sun.enterprise.resource.ResourceHandle;
import com.sun.appserv.connectors.internal.api.PoolingException;

import jakarta.resource.ResourceException;
import java.util.Set;

/**
 * @author Tony Ng
 */
public interface ResourceAllocator {

    public ResourceHandle createResource()
            throws PoolingException;

    public void fillInResourceObjects(ResourceHandle resource)
            throws PoolingException;

    public void closeUserConnection(ResourceHandle resource)
            throws PoolingException;

    public void destroyResource(ResourceHandle resource)
            throws PoolingException;

    public boolean matchConnection(ResourceHandle h);

    public boolean supportsReauthentication();

    public boolean isTransactional();

    public void cleanup(ResourceHandle resource) throws PoolingException;

    public boolean shareableWithinComponent();

    public Object getSharedConnection(ResourceHandle h)
            throws PoolingException;

    public Set getInvalidConnections(Set connectionSet)
            throws ResourceException;

    public boolean isConnectionValid(ResourceHandle resource);

    public boolean hasValidatingMCF();
}
