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

package com.sun.enterprise.glassfish.bootstrap;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Builds up a {@link ClassLoader}.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ClassPathBuilder {
    private final List<URL> urls = new ArrayList<URL>();
    private final ClassLoader parent;

    public ClassPathBuilder(ClassLoader parent) {
        this.parent = parent;
    }

    /**
     * Adds a single jar.
     */
    public void addJar(File jar) throws IOException {
        if(!jar.exists())
            throw new IOException("No such file: "+jar);
        urls.add(jar.toURI().toURL());
    }

    /**
     * Adds a single class folder.
     */
    public void addClassFolder(File classFolder) throws IOException {
        addJar(classFolder);
    }

    /**
     * Adds all jars in the given folder.
     *
     * @param folder
     *      A directory that contains a bunch of jar files.
     * @param excludes
     *      List of jars to be excluded
     */
    public void addJarFolder(File folder, final String... excludes) throws IOException {
        if(!folder.isDirectory())
            throw new IOException("Not a directory "+folder);

        File[] children = folder.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                for (String name : excludes) {
                    if(pathname.getName().equals(name))
                        return false;   // excluded
                }
                return pathname.getPath().endsWith(".jar");
            }
        });

        if(children==null)
            return; // in a very rare race condition, the directory can disappear after we checked.

        for (File child : children) {
            addJar(child);
        }
    }

    /**
     * Looks for the child files/directories in the given folder that matches the specified GLOB patterns
     * (like "foo-*.jar") and adds them to the classpath.
     */
    public void addGlob(File folder, String... masks) throws IOException {
        StringBuilder regexp = new StringBuilder();
        for (String mask : masks) {
            if(regexp.length()>0)   regexp.append('|');
            regexp.append("(\\Q");
            regexp.append(mask.replace("?","\\E.\\Q").replace("*","\\E.*\\Q"));
            regexp.append("\\E)");
        }
        Pattern p = Pattern.compile(regexp.toString());

        File[] children = folder.listFiles();
        if(children==null)  return;
        for (File child : children) {
            if(p.matcher(child.getName()).matches())
                addJar(child);
        }
    }

    public ClassLoader create() {
        return AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {
            @Override
            public ClassLoader run() {
                return new URLClassLoader(urls.toArray(new URL[urls.size()]),parent);
            }
        });
    }

}
