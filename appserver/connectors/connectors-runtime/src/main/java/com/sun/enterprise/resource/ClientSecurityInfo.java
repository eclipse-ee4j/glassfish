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

package com.sun.enterprise.resource;

import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;

import jakarta.resource.spi.ConnectionRequestInfo;

/**
 * This class represents the client-specific information associated with a resource. Used for pool partitioning
 *
 * @author Tony Ng
 */
public class ClientSecurityInfo {

    static private final int NULL_HASH_CODE = Integer.valueOf(1).hashCode();

    // union: either store Principal or ConnectionRequestInfo
    private ResourcePrincipalDescriptor resourcePrincipalDescriptor;
    private ConnectionRequestInfo connectionRequestInfo;

    public ClientSecurityInfo(ResourcePrincipalDescriptor resourcePrincipalDescriptor) {
        if (resourcePrincipalDescriptor == null) {
            throw new NullPointerException("Principal is null");
        }

        this.resourcePrincipalDescriptor = resourcePrincipalDescriptor;
        this.connectionRequestInfo = null;
    }

    public ClientSecurityInfo(ConnectionRequestInfo info) {
        // info can be null
        this.resourcePrincipalDescriptor = null;
        this.connectionRequestInfo = info;
    }

    public ResourcePrincipalDescriptor getPrincipal() {
        return resourcePrincipalDescriptor;
    }

    public ConnectionRequestInfo getConnectionRequestInfo() {
        return connectionRequestInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null) {
            return false;
        }

        if (obj instanceof ClientSecurityInfo) {
            ClientSecurityInfo other = (ClientSecurityInfo) obj;
            return ((isEqual(resourcePrincipalDescriptor, other.resourcePrincipalDescriptor)) && (isEqual(connectionRequestInfo, other.connectionRequestInfo)));
        }

        return false;
    }

    @Override
    public int hashCode() {
        int result = NULL_HASH_CODE;
        if (resourcePrincipalDescriptor != null) {
            result = resourcePrincipalDescriptor.hashCode();
        }
        if (connectionRequestInfo != null) {
            result += connectionRequestInfo.hashCode();
        }
        return result;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) {
            return (b == null);
        }

        return a.equals(b);
    }

    @Override
    public String toString() {
        return "ClientSecurityInfo: prin=" + resourcePrincipalDescriptor + " info=" + connectionRequestInfo;
    }
}
