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
import java.io.FileFilter;
import java.io.IOException;
import java.security.PrivilegedAction;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashSet;
import java.net.URL;
import java.net.URLClassLoader;

import org.glassfish.apf.Scanner;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.glassfish.hk2.classmodel.reflect.Types;

/**
 * Implementation of the Scanner interface for a directory
 *
 * @author Jerome Dochez
 */
public class DirectoryScanner extends JavaEEScanner implements Scanner {

    File directory;
    Set<String> entries = new HashSet<String>();
    ClassLoader classLoader = null;

    public void process(File directory, Object bundleDesc, ClassLoader classLoader)
            throws IOException {
        AnnotationUtils.getLogger().finer("dir is " + directory);
        AnnotationUtils.getLogger().finer("classLoader is " + classLoader);
        this.directory = directory;
        this.classLoader = classLoader;
        init(directory);
    }

    private void init(File directory) throws java.io.IOException {
        init(directory, directory);
        initTypes(directory);
    }

    private void init(File top, File directory) throws java.io.IOException {

        File[] dirFiles = directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getAbsolutePath().endsWith(".class");
                }
        });
        if (dirFiles != null) {
            for (File file : dirFiles) {
                entries.add(file.getPath().substring(top.getPath().length() + 1));
            }
        }

        File[] subDirs = directory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
        });
        if (subDirs != null) {
            for (File subDir : subDirs) {
                init(top, subDir);
            }
        }
    }

    protected Set<String> getEntries() {
        return entries;
    }

    public ClassLoader getClassLoader() {
        if (classLoader==null) {
            final URL[] urls = new URL[1];
            try {
                if (directory == null) throw new IllegalStateException("directory must first be set by calling the process method.");
                urls[0] = directory.getAbsoluteFile().toURL();
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
        for (String fileName : entries) {
            // convert to a class name...
            String className = fileName.replace(File.separatorChar, '.');
            className = className.substring(0, className.length()-6);
            System.out.println("Getting " + className);
            try {
                elements.add(classLoader.loadClass(className));

            } catch(Throwable cnfe) {
                AnnotationUtils.getLogger().severe("cannot load " + className + " reason : " + cnfe.getMessage());
            }
        }
        return elements;
    }
}
