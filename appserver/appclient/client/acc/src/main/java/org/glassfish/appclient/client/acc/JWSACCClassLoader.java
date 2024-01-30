/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client.acc;

import java.net.URL;
import java.security.CodeSource;
import java.security.PermissionCollection;

import org.glassfish.appclient.common.ClientClassLoaderDelegate;
import org.glassfish.common.util.GlassfishUrlClassLoader;

public class JWSACCClassLoader extends GlassfishUrlClassLoader {

    private final ClientClassLoaderDelegate clientCLDelegate;

    public JWSACCClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);

        clientCLDelegate = new ClientClassLoaderDelegate(this);
    }


    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {
        if (System.getSecurityManager() == null) {
            return super.getPermissions(codesource);
        }

        // when security manager is enabled, find the declared permissions
        if (clientCLDelegate.getCachedPerms(codesource) != null) {
            return clientCLDelegate.getCachedPerms(codesource);
        }

        return clientCLDelegate.getPermissions(codesource, super.getPermissions(codesource));
    }

}
