/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.util;

import java.security.Principal;

import org.glassfish.security.services.api.authorization.AuthorizationAdminConstants;

/**
 * A simple Principal that indicates that the Subject has been authenticated as another server in the domain.
 *
 * @author tjquinn
 */
public class AdminIndicatorPrincipal implements Principal {

    private final String adminIndicator;

    public AdminIndicatorPrincipal(final String adminIndicator) {
        this.adminIndicator = adminIndicator;
    }

    @Override
    public String getName() {
        return AuthorizationAdminConstants.SERVER;
    }

    public String getIndicator() {
        return adminIndicator;
    }

}
