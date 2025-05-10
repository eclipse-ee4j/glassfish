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

package com.sun.enterprise.connectors.util;

import java.security.SecureClassLoader;

/**
 * ConnectorRARClassLoader finds classes and resources from a
 * JAR file without exploding it. This uses the JarResourceExtractor
 * for finding classes and resources inside the archive
 *
 * This classloader is _used_ only by the admin GUI to retrieve resource
 * adapter class properties without exploding the connector archive.
 *
 * @author Sivakumar Thyagarajan
 */
public class ConnectorRARClassLoader extends SecureClassLoader{

    static {
        registerAsParallelCapable();
    }

    private final JarResourceExtractor jarResources;

    public ConnectorRARClassLoader(String jarName, ClassLoader parent) {
        super(jarName, parent);
        // Create the JarResource and suck in the .jar file.
        jarResources = new JarResourceExtractor(jarName);
    }

    protected byte[] loadClassBytes(String className) {
        className = formatClassName(className);
        return (jarResources.getResource(className));
    }

    @Override
    public Class findClass(String name) throws ClassNotFoundException {
        byte[] b = loadClassBytes(name);
        if (b == null) {
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, b, 0, b.length);
    }

    private String formatClassName(String className) {
        return className.replace('.', '/') + ".class";
    }

    public String getResourceAsString(String raDeploymentDescPath) {
        return new String(jarResources.getResource(raDeploymentDescPath));
    }

}
