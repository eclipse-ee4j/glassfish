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

package com.sun.enterprise.connectors.authentication;

import com.sun.enterprise.deployment.ResourcePrincipalDescriptor;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author Kanwar Oberoi
 */
public class RuntimeSecurityMap {

    private final HashMap<String, ResourcePrincipalDescriptor> userMap;
    private final HashMap<String, ResourcePrincipalDescriptor> groupMap;

    public RuntimeSecurityMap() {
        this.userMap = new HashMap<>();
        this.groupMap = new HashMap<>();
    }

    @SuppressWarnings("unchecked")
    public RuntimeSecurityMap(HashMap<String, ResourcePrincipalDescriptor> userMap, HashMap<String, ResourcePrincipalDescriptor> groupMap) {
        this.userMap = (HashMap<String, ResourcePrincipalDescriptor>) userMap.clone();
        this.groupMap = (HashMap<String, ResourcePrincipalDescriptor>) groupMap.clone();
    }

    @Override
    public boolean equals(Object map) {
        if (map instanceof RuntimeSecurityMap) {
            RuntimeSecurityMap runtimeSecurityMap = (RuntimeSecurityMap) map;
            return Objects.equals(userMap, runtimeSecurityMap.userMap) && Objects.equals(groupMap, runtimeSecurityMap.groupMap);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.userMap, this.groupMap);
    }

    public boolean isEmpty() {
        return this.userMap.isEmpty() && this.groupMap.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, ResourcePrincipalDescriptor> getUserMap() {
        return (HashMap<String, ResourcePrincipalDescriptor>) this.userMap.clone();
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, ResourcePrincipalDescriptor> getGroupMap() {
        return (HashMap<String, ResourcePrincipalDescriptor>) this.groupMap.clone();
    }
}
