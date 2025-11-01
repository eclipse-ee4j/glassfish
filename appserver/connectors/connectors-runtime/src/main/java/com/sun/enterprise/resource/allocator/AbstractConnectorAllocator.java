/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.PoolingException;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.AssocWithThreadResourceHandle;
import com.sun.enterprise.resource.ClientSecurityInfo;
import com.sun.enterprise.resource.ResourceHandle;
import com.sun.enterprise.resource.ResourceSpec;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ValidatingManagedConnectionFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;


/**
 * An abstract implementation of the <code>ResourceAllocator</code> interface
 * that houses all the common implementation(s) of the various connector allocators.
 * All resource allocators except <code>BasicResourceAllocator</code> extend this
 * abstract implementation
 *
 * @author Sivakumar Thyagarajan
 */
public abstract class AbstractConnectorAllocator implements ResourceAllocator {
    protected static final Logger LOG = LogDomains.getLogger(AbstractConnectorAllocator.class,LogDomains.RSR_LOGGER);

    protected PoolManager poolMgr;
    protected ResourceSpec spec;
    protected ConnectionRequestInfo reqInfo;
    protected Subject subject;
    protected ManagedConnectionFactory mcf;
    protected ConnectorDescriptor desc;
    protected ClientSecurityInfo info;


    public AbstractConnectorAllocator() {
    }

    public AbstractConnectorAllocator(PoolManager poolMgr,
                                      ManagedConnectionFactory mcf,
                                      ResourceSpec spec,
                                      Subject subject,
                                      ConnectionRequestInfo reqInfo,
                                      ClientSecurityInfo info,
                                      ConnectorDescriptor desc) {
        this.poolMgr = poolMgr;
        this.mcf = mcf;
        this.spec = spec;
        this.subject = subject;
        this.reqInfo = reqInfo;
        this.info = info;
        this.desc = desc;
    }

    @Override
    public Set getInvalidConnections(Set connectionSet) throws ResourceException {
        if (mcf instanceof ValidatingManagedConnectionFactory) {
            return ((ValidatingManagedConnectionFactory) this.mcf).getInvalidConnections(connectionSet);
        }
        return null;
    }

    @Override
    public boolean isConnectionValid(ResourceHandle h) {
        HashSet<ManagedConnection> conn = new HashSet<>();
        conn.add(h.getResource());
        Set<?> invalids = null;
        try {
            invalids = getInvalidConnections(conn);
        } catch (ResourceException re) {
            //ignore and continue??
            //there's nothing the container can do but log it.
            Object[] args = new Object[] {
                    h.getResourceSpec().getPoolInfo(),
                    re.getClass(),
                    re.getMessage() };
            LOG.log(Level.WARNING, "pool.get_invalid_connections_resourceexception", args);
            LOG.log(Level.FINE, "Failed to check if the connection is valid.", re);
        }
        return (invalids == null || invalids.isEmpty()) && !h.hasConnectionErrorOccurred();
    }

    @Override
    public void destroyResource(ResourceHandle resourceHandle)
            throws PoolingException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillInResourceObjects(ResourceHandle resourceHandle)
            throws PoolingException {
        throw new UnsupportedOperationException();
    }


    @Override
    public boolean isTransactional() {
        return true;
    }

    @Override
    public void cleanup(ResourceHandle h) throws PoolingException {
        try {
            ManagedConnection mc = h.getResource();
            mc.cleanup();
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "managed_con.cleanup-failed", ex);
            throw new PoolingException(ex.toString(), ex);
        }
    }

    @Override
    public boolean matchConnection(ResourceHandle h) {
        Set<ManagedConnection> set = new HashSet<>();
        set.add(h.getResource());
        try {
            ManagedConnection mc = mcf.matchManagedConnections(set, subject, reqInfo);
            return mc != null;
        } catch (ResourceException ex) {
            return false;
        }
    }

    @Override
    public void closeUserConnection(ResourceHandle resource) throws PoolingException {
        try {
            ManagedConnection mc = resource.getResource();
            mc.cleanup();
        } catch (ResourceException ex) {
            throw new PoolingException(ex);
        }
    }

    @Override
    public boolean shareableWithinComponent() {
        return false;
    }

    @Override
    public Object getSharedConnection(ResourceHandle h)
            throws PoolingException {
        throw new UnsupportedOperationException();
    }

    protected ResourceHandle createResourceHandle(ManagedConnection resource, ResourceSpec spec,
            ResourceAllocator alloc) {

        ConnectorConstants.PoolType pt = ConnectorConstants.PoolType.STANDARD_POOL;
        try {
            pt = ConnectorRuntime.getRuntime().getPoolType(spec.getPoolInfo());
        } catch (ConnectorRuntimeException cre) {
            LOG.log(Level.WARNING,"unable_to_determine_pool_type", spec.getPoolInfo());
        }
        if (pt == ConnectorConstants.PoolType.ASSOCIATE_WITH_THREAD_POOL) {
            return new AssocWithThreadResourceHandle(resource, spec, alloc);
        } else {
            return new ResourceHandle(resource, spec, alloc);
        }
    }

    @Override
    public boolean hasValidatingMCF() {
        return mcf instanceof ValidatingManagedConnectionFactory;
    }

}
