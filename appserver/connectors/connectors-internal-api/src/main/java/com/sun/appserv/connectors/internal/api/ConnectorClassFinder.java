/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.appserv.connectors.internal.api;

import com.sun.enterprise.loader.ASURLClassLoader;

import org.glassfish.internal.api.DelegatingClassLoader;

/**
 * connector-class-finder to provide a class from its .rar
 *
 * @author Jagadish Ramu
 */
public class ConnectorClassFinder extends ASURLClassLoader implements DelegatingClassLoader.ClassFinder {

    static {
        registerAsParallelCapable();
    }

    private final DelegatingClassLoader.ClassFinder librariesClassFinder;
    private volatile String raName;

    public ConnectorClassFinder(ClassLoader parent, String raName, DelegatingClassLoader.ClassFinder finder) {
        super("Connector(" + raName + ')', parent);
        this.raName = raName;

        // There should be better approach to skip libraries Classloader when none specified.
        // casting to DelegatingClassLoader is not a clean approach
        DelegatingClassLoader.ClassFinder libcf = null;
        if (finder != null && (finder instanceof DelegatingClassLoader)) {
            if (((DelegatingClassLoader) finder).getDelegates().size() > 0) {
                libcf = finder;
            }
        }
        this.librariesClassFinder = libcf;
    }


    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        Class<?> c = null;

        if (librariesClassFinder != null) {
            try {
                c = librariesClassFinder.findClass(name);
            } catch (ClassNotFoundException cnfe) {
                // ignore
            }
            if (c != null) {
                return c;
            }
        }
        return super.findClass(name);
    }


    @Override
    public Class<?> findExistingClass(String name) {
        if (librariesClassFinder != null) {
            Class<?> claz = librariesClassFinder.findExistingClass(name);
            if (claz != null) {
                return claz;
            }
        }
        return super.findLoadedClass(name);
    }


    public String getResourceAdapterName() {
        return raName;
    }


    public void setResourceAdapterName(String raName) {
        this.raName = raName;
    }
}
