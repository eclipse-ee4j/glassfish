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

package org.glassfish.apf.impl;

import java.io.File;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashSet;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import org.glassfish.apf.Scanner;

/**
 * Implements the scanner interface on a jar file.
 *
 * @author Jerome Dochez
 */
public class JarScanner extends JavaEEScanner implements Scanner<Object> {

    File jarFile;
    Set<JarEntry> entries = new HashSet<JarEntry>();
    ClassLoader classLoader = null;


    public  void process(File jarFile, Object bundleDesc, ClassLoader loader) throws java.io.IOException {
        this.jarFile = jarFile;
        JarFile jf = new JarFile(jarFile);

        try {
            Enumeration<JarEntry> entriesEnum = jf.entries();
            while(entriesEnum.hasMoreElements()) {
                JarEntry je = entriesEnum.nextElement();
                if (je.getName().endsWith(".class")) {
                    entries.add(je);
                }
            }
        } finally {
            jf.close();
        }
        initTypes(jarFile);
    }

    public ClassLoader getClassLoader() {
        if (classLoader==null) {
            final URL[] urls = new URL[1];
            try {
                if (jarFile == null) throw new IllegalStateException("jarFile must first be set with the process method.");
                urls[0] = jarFile.getAbsoluteFile().toURL();
                classLoader = new PrivilegedAction<URLClassLoader>() {
                  @Override
                  public URLClassLoader run() {
                    return new URLClassLoader(urls);
                  }
                }.run();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        return classLoader;
    }

    public Set<Class> getElements() {


        Set<Class> elements = new HashSet<Class>();
        if (getClassLoader()==null) {
            AnnotationUtils.getLogger().severe("Class loader null");
            return elements;
        }
        for (JarEntry je : entries) {
            String fileName = je.getName();
            // convert to a class name...
            String className = fileName.replace(File.separatorChar, '.');
            className = className.substring(0, className.length()-6);
            try {
                elements.add(classLoader.loadClass(className));

            } catch(ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            }
        }
        return elements;
    }



}
