/*
 * Copyright (c) 2013, 2021 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.appclient.common;

import com.sun.enterprise.security.ee.perms.SMGlobalPolicyUtil;
import com.sun.enterprise.security.integration.PermsHolder;

import java.io.IOException;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;

public class ClientClassLoaderDelegate {

    protected static final String PERMISSIONS_XML = "META-INF/permissions.xml";

    private URLClassLoader cl;

    private PermsHolder permHolder;

    public ClientClassLoaderDelegate(URLClassLoader cl) {
        this.cl = cl;
    }

    public PermissionCollection getCachedPerms(CodeSource codesource) {
        return permHolder.getCachedPerms(codesource);
    }

    public PermissionCollection getPermissions(CodeSource codesource, PermissionCollection parentPC) {
        return permHolder.getPermissions(codesource, parentPC);
    }

}
