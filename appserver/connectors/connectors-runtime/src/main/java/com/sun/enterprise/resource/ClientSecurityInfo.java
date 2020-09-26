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

package com.sun.enterprise.resource;

import com.sun.enterprise.deployment.ResourcePrincipal;

import jakarta.resource.spi.ConnectionRequestInfo;

/**
 * This class represents the client-specific information associated
 * with a resource. Used for pool partitioning
 *
 * @author Tony Ng
 */
public class ClientSecurityInfo {

    // union: either store Principal or ConnectionRequestInfo
    private ResourcePrincipal prin;
    private ConnectionRequestInfo info;

    static private final int NULL_HASH_CODE = Integer.valueOf(1).hashCode();

    public ClientSecurityInfo(ResourcePrincipal prin) {
        if (prin == null) {
            throw new NullPointerException("Principal is null");
        }
        this.prin = prin;
        this.info = null;
    }

    public ClientSecurityInfo(ConnectionRequestInfo info) {
        // info can be null
        this.prin = null;
        this.info = info;
    }


    public ResourcePrincipal getPrincipal() {
        return prin;
    }

    public ConnectionRequestInfo getConnectionRequestInfo() {
        return info;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (obj instanceof ClientSecurityInfo) {
            ClientSecurityInfo other = (ClientSecurityInfo) obj;
            return ((isEqual(prin, other.prin)) &&
                    (isEqual(info, other.info)));
        }
        return false;
    }

    public int hashCode() {
        int result = NULL_HASH_CODE;
        if (prin != null) {
            result = prin.hashCode();
        }
        if (info != null) {
            result += info.hashCode();
        }
        return result;
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null) {
            return (b == null);
        } else {
            return (a.equals(b));
        }
    }

    public String toString() {
        return "ClientSecurityInfo: prin=" + prin + " info=" + info;
    }
}
