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

import org.glassfish.security.services.api.authorization.AuthorizationAdminConstants;

/**
 * Indicates that the identity of a Subject has, at least partially, been derived using a token.
 * <p>
 * One main use of this principal is to allow us to trust incoming requests from remote hosts even if secure admin is
 * disabled if the request has an admin token.
 * 
 * @author tjquinn
 */
public class AdminTokenPrincipal extends TokenPrincipal {

    public AdminTokenPrincipal(final String token) {
        super(AuthorizationAdminConstants.ADMIN_TOKEN, token);
    }
}
