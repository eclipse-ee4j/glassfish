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

package com.sun.enterprise.security.integration;

import java.lang.reflect.InvocationTargetException;
import java.security.Permission;
import java.security.UnresolvedPermission;

//this implementation is based on sun.security.provider.PolicyFile.java
public class PermissionCreator {

    private static final Class<?>[] PARAMS0 = {};
    private static final Class<?>[] PARAMS1 = { String.class };
    private static final Class<?>[] PARAMS2 = { String.class, String.class };

    public static final Permission getInstance(String type, String name, String actions)
        throws ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        Class<?> permissionClass = null;

        try {
            permissionClass = Class.forName(type, true, Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            // This class is not recognized
            return new UnresolvedPermission(type, name, actions, null); // policy provider suppose to translate this permission later
        }

        if (name == null && actions == null) {
            try {
                return (Permission) permissionClass.getConstructor(PARAMS0).newInstance(new Object[] {});
            } catch (NoSuchMethodException ne) {
                try {
                    return (Permission) permissionClass.getConstructor(PARAMS1).newInstance(new Object[] { name });
                } catch (NoSuchMethodException ne1) {
                    return (Permission) permissionClass.getConstructor(PARAMS2).newInstance(new Object[] { name, actions });
                }
            }
        } else {
            if (name != null && actions == null) {
                try {
                    return (Permission) permissionClass.getConstructor(PARAMS1).newInstance(new Object[] { name });
                } catch (NoSuchMethodException ne) {
                    return (Permission) permissionClass.getConstructor(PARAMS2).newInstance(new Object[] { name, actions });
                }
            } else {
                return (Permission) permissionClass.getConstructor(PARAMS2).newInstance(new Object[] { name, actions });
            }
        }
    }

}
