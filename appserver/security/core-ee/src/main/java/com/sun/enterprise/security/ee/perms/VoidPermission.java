/*
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

package com.sun.enterprise.security.ee.perms;

import java.security.BasicPermission;
import java.security.Permission;

/**
 * a class used on permission restriction list to imply "No 'AllPermission' allowed" in permissions.xml.
 *
 * This permission can not imply any other permission
 */
public class VoidPermission extends BasicPermission {

    private static final long serialVersionUID = 5535516010244462567L;

    public VoidPermission() {
        this("VoidPermmission");
    }

    public VoidPermission(String name) {
        super(name);

    }

    public VoidPermission(String name, String actions) {
        super(name, actions);
    }

    @Override
    public boolean implies(Permission permission) {
        // always return false
        return false;
    }
}
