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

import java.security.Principal;
import java.util.Set;

/**
 * Interface class consisting of methods for securityMap functionality. For a given principal/userGrooup, a mapping is
 * done to a backendPrincipal which is actually used to authenticate/get connection to the backend.
 *
 * @author Srikanth P
 */
public interface AuthenticationService {

    /**
     * Maps the principal name to the backendPrincipal.
     *
     * @param principal Name of the principal
     * @param principalSet principalSet
     * @return mapped backendPrincipal.
     */
    ResourcePrincipalDescriptor mapPrincipal(Principal principal, Set<Principal> principalSet);

}
