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

package com.sun.enterprise.deployment.runtime.common;

import org.glassfish.deployment.common.Descriptor;

import java.lang.reflect.Constructor;
import java.security.Principal;

/**
 * This is an in memory representation of the principal-name with its name of
 * the implementation class.
 * @author deployment dev team
 */
public class PrincipalNameDescriptor extends Descriptor {

    private static final String defaultClassName =
                "org.glassfish.security.common.PrincipalImpl";
    private String principalName = null;
    private String className = null;
    private transient ClassLoader cLoader = null;

    public PrincipalNameDescriptor() {}

    public String getName() {
        return principalName;
    }

    public String getClassName() {
        if (className == null) {
            return defaultClassName;
        }
        return className;
    }

    public void setName(String name) {
        principalName = name;
    }

    public void setClassName(String name) {
        className = name;
    }

    public void setClassLoader(ClassLoader c) {
        cLoader = c;
    }

    public Principal getPrincipal() {
        try {
            if (cLoader == null) {
                cLoader = Thread.currentThread().getContextClassLoader();
            }
            Class clazz = Class.forName(getClassName(), true, cLoader);
            Constructor constructor =
                            clazz.getConstructor(new Class[]{String.class});
            Object o = constructor.newInstance(new Object[]{principalName});
            return (Principal) o;
        } catch(Exception ex) {
            RuntimeException e = new RuntimeException();
            e.initCause(ex);
            throw e;
        }
    }

    public String toString() {
        return "principal-name " + principalName + "; className " + getClassName();
    }
}
