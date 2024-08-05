/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.resource;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.enterprise.connectors.PoolMetaData;

import java.io.Serializable;
import java.util.Objects;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;

/**
 * ResourceSpec is used as a key to locate the correct resource pool
 */
public class ResourceSpec implements Serializable {

    private static final long serialVersionUID = 1L;

    static public final int JDBC_URL = 0;
    static public final int JNDI_NAME = 1;
    static public final int JMS = 2;

    private final SimpleJndiName resourceId;
    private final int resourceIdType;

    private boolean pmResource;
    private boolean nonTxResource;
    private boolean isXA_;

    private boolean lazyEnlistable_;
    private boolean lazyAssociatable_;
    private Object connectionToAssoc_;

    private PoolInfo poolInfo;

    public ResourceSpec(SimpleJndiName resourceId, int resourceIdType) {
        Objects.requireNonNull(resourceId, "resourceId");
        this.resourceId = resourceId;
        this.resourceIdType = resourceIdType;

        if (resourceId.hasSuffix(ConnectorConstants.NON_TX_JNDI_SUFFIX)) {
            nonTxResource = true;
        }
        if (resourceId.hasSuffix(ConnectorConstants.PM_JNDI_SUFFIX)) {
            pmResource = true;
        }
    }

    public ResourceSpec(SimpleJndiName resourceId, int resourceIdType, PoolMetaData poolMetaData) {
        Objects.requireNonNull(resourceId, "resourceId");
        this.resourceId = resourceId;
        this.resourceIdType = resourceIdType;

        if (poolMetaData.isNonTx() || resourceId.hasSuffix(ConnectorConstants.NON_TX_JNDI_SUFFIX)) {
            nonTxResource = true;
        }
        if (poolMetaData.isPM() || resourceId.hasSuffix(ConnectorConstants.PM_JNDI_SUFFIX)) {
            pmResource = true;
        }

        if (poolMetaData.isLazyEnlistable() && !nonTxResource && !pmResource) {
            lazyEnlistable_ = true;
        }

        if (poolMetaData.isLazyAssociatable() && !nonTxResource && !pmResource) {
            lazyAssociatable_ = true;
            // The rationale behind doing this is that in the PoolManagerImpl
            // when we return from getResource called by associateConnections,
            // enlistment should happen immediately since we are associating on
            // first use anyway,
            lazyEnlistable_ = false;
        }

    }

    public PoolInfo getPoolInfo() {
        return poolInfo;
    }

    public void setPoolInfo(PoolInfo poolInfo) {
        this.poolInfo = poolInfo;
    }

    /**
     * The logic is * If the connectionpool exist then equality check is against * connectionPoolName * * If connection is
     * null then equality check is made against * resourceId and resourceType *
     */

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other instanceof ResourceSpec) {
            ResourceSpec obj = (ResourceSpec) other;
            if (poolInfo == null) {
                return (resourceId.equals(obj.resourceId) && resourceIdType == obj.resourceIdType);
            }
            return (poolInfo.equals(obj.poolInfo));
        }
        return false;
    }

    /**
     * If the connectionpool exist then hashcode of connectionPoolName is returned.
     * <p/>
     * If connectionpool is null return the hashcode of resourceId + resourceIdType
     */
    @Override
    public int hashCode() {
        if (poolInfo == null) {
            return resourceId.hashCode() + resourceIdType;
        }
        return poolInfo.hashCode();
    }

    public String getResourceId() {
        return resourceId.toString();
    }

    public boolean isPM() {
        return pmResource;
    }

    /**
     * Returns the status of the noTxResource flag
     *
     * @return true if this resource is a noTx resource
     */

    public boolean isNonTx() {
        return nonTxResource;
    }

    public boolean isXA() {
        return isXA_;
    }

    public void markAsXA() {
        isXA_ = true;
    }

    public boolean isLazyEnlistable() {
        return lazyEnlistable_;
    }

    public void setLazyEnlistable(boolean lazyEnlist) {
        lazyEnlistable_ = lazyEnlist;
    }

    public boolean isLazyAssociatable() {
        return lazyAssociatable_;
    }

    public void setLazyAssociatable(boolean lazyAssoc) {
        lazyAssociatable_ = lazyAssoc;
    }

    public void setConnectionToAssociate(Object conn) {
        connectionToAssoc_ = conn;
    }

    public Object getConnectionToAssociate() {
        return connectionToAssoc_;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("<ResourceSpec ");
        sb.append("connectionPoolName : ").append(poolInfo);
        sb.append(", isXA_ : ").append(isXA_);
        sb.append(", resoureId : ").append(resourceId);
        sb.append(", resoureIdType : ").append(resourceIdType);
        sb.append(", pmResource : ").append(pmResource);
        sb.append(", nonTxResource : ").append(nonTxResource);
        sb.append(", lazyEnlistable : ").append(lazyEnlistable_);
        sb.append(", lazyAssociatable : ").append(lazyAssociatable_);
        sb.append("/>");
        return sb.toString();
    }
}
