/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deployment.node.runtime.common.DescriptorPrincipalName;

import java.lang.reflect.Constructor;
import java.security.Principal;

import org.glassfish.deployment.common.Descriptor;

/**
 * This is an in memory representation of the principal-name with its name of
 * the implementation class.
 *
 * @author deployment dev team
 */
public class PrincipalNameDescriptor extends Descriptor {

    private static final long serialVersionUID = 1L;
    private String principalName;
    private String className;
    private transient ClassLoader cLoader;


    public PrincipalNameDescriptor(String name) {
        this.principalName = name;
    }


    @Override
    public String getName() {
        return principalName;
    }


    /**
     * @return never null.
     */
    public String getClassName() {
        if (className == null) {
            return DescriptorPrincipalName.class.getName();
        }
        return className;
    }


    @Override
    public void setName(String name) {
        principalName = name;
    }


    public void setClassName(String name) {
        className = name;
    }


    public void setClassLoader(ClassLoader c) {
        cLoader = c;
    }


    public final Principal toPrincipal() {
        try {
            if (cLoader == null) {
                cLoader = Thread.currentThread().getContextClassLoader();
            }
            Class<?> clazz = Class.forName(getClassName(), true, cLoader);
            Constructor<?> constructor = clazz.getConstructor(String.class);
            return (Principal) constructor.newInstance(principalName);
        } catch (Exception ex) {
            throw new RuntimeException("Invalid principal settings: " + this, ex);
        }
    }


    @Override
    public String toString() {
        return "principal-name: " + principalName + "; className: " + getClassName();
    }
}
