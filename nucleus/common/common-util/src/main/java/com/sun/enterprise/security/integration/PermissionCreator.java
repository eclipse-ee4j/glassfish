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

package com.sun.enterprise.security.integration;

import java.security.Permission;
import java.security.UnresolvedPermission;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

//this implementation is based on sun.security.provider.PolicyFile.java
public class PermissionCreator {

    private static final Class[] PARAMS0 = { };
    private static final Class[] PARAMS1 = { String.class };
    private static final Class[] PARAMS2 = { String.class, String.class };

    @SuppressWarnings("unused")
    public static final Permission getInstance(String type, String name,
            String actions) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException,
            NoSuchMethodException, InvocationTargetException {

        Class<?> pc = null;

        try {
            pc = Class.forName(type, true,
                Thread.currentThread().getContextClassLoader());
        } catch (ClassNotFoundException e) {
            //this class is not recognized
            Permission pm = new UnresolvedPermission(type, name, actions, null);
            return pm;  // policy provider suppose to translate this permission later
        }

        if (name == null && actions == null) {
            try {
                Constructor<?> c = pc.getConstructor(PARAMS0);
                return (Permission) c.newInstance(new Object[] {});
            } catch (NoSuchMethodException ne) {
                try {
                    Constructor<?> c = pc.getConstructor(PARAMS1);
                    return (Permission) c.newInstance(new Object[] { name });
                } catch (NoSuchMethodException ne1) {
                    Constructor<?> c = pc.getConstructor(PARAMS2);
                    return (Permission) c.newInstance(new Object[] { name,
                            actions });
                }
            }
        } else {
            if (name != null && actions == null) {
                try {
                    Constructor<?> c = pc.getConstructor(PARAMS1);
                    return (Permission) c.newInstance(new Object[] { name });
                } catch (NoSuchMethodException ne) {
                    Constructor<?> c = pc.getConstructor(PARAMS2);
                    return (Permission) c.newInstance(new Object[] { name,
                            actions });
                }
            } else {
                Constructor<?> c = pc.getConstructor(PARAMS2);
                return (Permission) c
                        .newInstance(new Object[] { name, actions });
            }
        }
    }

}
