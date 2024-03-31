/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security.auth.login;

import java.io.Serializable;
import java.security.Principal;

import org.glassfish.security.common.UserPrincipal;

public class DistinguishedPrincipalCredential implements UserPrincipal, Serializable {

    private static final long serialVersionUID = 1L;

    private final Principal principal;

    public DistinguishedPrincipalCredential(Principal principal) {
        this.principal = principal;
    }

    public Principal getPrincipal() {
        return principal;
    }

    @Override
    public String toString() {
        return "DistingushedPrincipal[" + principal + "]";
    }

    @Override
    public String getName() {
        if (principal == null) {
            return null;
        }

        return principal.getName();
    }
}
