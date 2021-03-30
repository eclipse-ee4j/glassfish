/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.cli;

import java.io.*;
import java.net.*;
import java.util.*;

import com.sun.enterprise.universal.i18n.LocalStringsImpl;

/**
 * A class loader that loads classes from all jar files in a specified directory.
 */
public class DirectoryClassLoader extends URLClassLoader {
    private static final LocalStringsImpl strings = new LocalStringsImpl(DirectoryClassLoader.class);

    /**
     * Create a DirectoryClassLoader to load from jar files in the specified directory, with the specified parent class
     * loader.
     *
     * @param dir the directory of jar files to load from
     * @param parent the parent class loader
     * @throws IOException if the directory can't be accessed
     */
    public DirectoryClassLoader(String dir, ClassLoader parent) throws IOException {
        super(getJars(new File(dir)), parent);
    }

    public DirectoryClassLoader(Set<File> jarsAndDirs, ClassLoader parent) throws IOException {
        super(getJars(jarsAndDirs), parent);
    }

    /**
     * Create a DirectoryClassLoader to load from jar files in the specified directory, with the specified parent class
     * loader.
     *
     * @param dir the directory of jar files to load from
     * @param parent the parent class loader
     * @throws IOException if the directory can't be accessed
     */
    public DirectoryClassLoader(File dir, ClassLoader parent) throws IOException {
        super(getJars(dir), parent);
    }

    private static URL[] getJars(Set<File> jarsAndDirs) throws IOException {
        if (jarsAndDirs == null) {
            throw new IOException(strings.get("DirError", ""));
        }
        Collection<URL> result = new ArrayList<URL>();
        for (File jd : jarsAndDirs) {
            if (jd.isDirectory()) {
                result.addAll(Arrays.asList(getJars(jd)));
            } else {
                result.add(jd.toURI().toURL());
            }
        }
        return result.toArray(new URL[result.size()]);
    }

    private static URL[] getJars(File dir) throws IOException {
        File[] fjars = dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
        if (fjars == null)
            throw new IOException(strings.get("DirError", dir));
        URL[] jars = new URL[fjars.length];
        for (int i = 0; i < fjars.length; i++)
            jars[i] = fjars[i].toURI().toURL();
        return jars;
    }
}
