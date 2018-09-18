/*
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

import java.io.Serializable;
import java.util.List;

/**
 * @author Kanwar Oberoi
 */
public class ConnectorSecurityMap implements Serializable {
    private String name;

    private List<String> principals;

    private List<String> userGroups;

    private EisBackendPrincipal backendPrincipal;

    public ConnectorSecurityMap(String name, List<String> principals,
                                List<String> userGroups, EisBackendPrincipal backendPrincipal) {
        this.name = name;
        this.principals = principals;
        this.userGroups = userGroups;
        this.backendPrincipal = backendPrincipal;
    }

    /**
     * @return Returns the backendPrincipal.
     */
    public EisBackendPrincipal getBackendPrincipal() {
        return this.backendPrincipal;
    }

    /**
     * @param backendPrincipal The backendPrincipal to set.
     */
    public void setBackendPrincipal(EisBackendPrincipal backendPrincipal) {
        this.backendPrincipal = backendPrincipal;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the principals.
     */
    public List<String> getPrincipals() {
        return this.principals;
    }

    /**
     * @param principals The principals to set.
     */
    public void setPrincipals(List<String> principals) {
        this.principals = principals;
    }

    /**
     * @return Returns the userGroups.
     */
    public List<String> getUserGroups() {
        return this.userGroups;
    }

    /**
     * @param userGroups The userGroups to set.
     */
    public void setUserGroups(List<String> userGroups) {
        this.userGroups = userGroups;
    }
}
