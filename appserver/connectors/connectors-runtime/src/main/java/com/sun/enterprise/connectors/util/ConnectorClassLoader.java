/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.loader.ASURLClassLoader;
import com.sun.logging.LogDomains;

import java.io.File;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class loader is responsible for loading standalone
 * RAR files. This class loader is the parent of JarClassLoader
 * (the application class loader)
 *
 * @author Tony Ng, Sivakumar Thyagarajan
 */
public class ConnectorClassLoader extends ASURLClassLoader {

    private static final Logger _logger = LogDomains.getLogger(ConnectorClassLoader.class, LogDomains.RSR_LOGGER);

    private volatile static ConnectorClassLoader classLoader;

    /**
     * A linked list of URL classloaders representing each deployed connector
     * module
     */
    private final List<ASURLClassLoader> classLoaderChain = new LinkedList<>();

    /**
     * The parent class loader for the connector Class Loader [ie the common
     * Classloader]
     */
    private ClassLoader parent;

    /**
     * Maintains a mapping between rar name and a classloader that has services
     * that RAR module.
     */
    private final Map<String, ASURLClassLoader> rarModuleClassLoaders = new HashMap<>();

    public static synchronized ConnectorClassLoader getInstance() {
        if (classLoader == null) {
            PrivilegedAction<ConnectorClassLoader> action = ConnectorClassLoader::new;
            classLoader = AccessController.doPrivileged(action);
        }
        return classLoader;
    }

    private ConnectorClassLoader() {
    }

    private ConnectorClassLoader(ClassLoader parent) {
        super(parent);
        this.parent = parent;
    }

    /**
     * Initializes this singleton with the given parent class loader
     * if not already created.
     *
     * @param parent parent class loader
     * @return the instance
     */
    public static ConnectorClassLoader getInstance(final ClassLoader parent) {
        if (classLoader == null) {
            synchronized (ConnectorClassLoader.class) {
                if (classLoader == null) {
                    PrivilegedAction<ConnectorClassLoader> action = () -> new ConnectorClassLoader(parent);
                    classLoader = AccessController.doPrivileged(action);
                }
            }
        }
        return classLoader;
    }

    /**
     * Adds the requested resource adapter to the ConnectorClassLoader. A
     * ConnectorClassLoader is created with the moduleDir as its search path
     * and this classloader is added to the classloader chain.
     *
     * @param rarName   the resourceAdapter module name to add
     * @param moduleDir the directory location where the RAR contents are exploded
     */
    public void addResourceAdapter(String rarName, String moduleDir) {

        try {
            File file = new File(moduleDir);
            PrivilegedAction<ASURLClassLoader> action = () -> new ASURLClassLoader(parent);
            ASURLClassLoader cl = AccessController.doPrivileged(action);

            cl.appendURL(file.toURI().toURL());
            appendJars(file, cl);
            classLoaderChain.add(cl);
            rarModuleClassLoaders.put(rarName, cl);
        } catch (MalformedURLException ex) {
            _logger.log(Level.SEVERE, "enterprise_util.connector_malformed_url", ex);
        }
    }

    //TODO V3 handling "unexploded jars" for now, V2 deployment module used to explode the jars also
    private void appendJars(File moduleDir, ASURLClassLoader cl) throws MalformedURLException {
        if (moduleDir.isDirectory()) {
            File[] files = moduleDir.listFiles();
            if(files != null) {
                for (File file : files) {
                    if (file.getName().toUpperCase(Locale.getDefault()).endsWith(".JAR")) {
                        cl.appendURL(file.toURI().toURL());
                    } else if (file.isDirectory()) {
                        appendJars(file, cl); //recursive add
                    }
                }
            }
        }
    }

    /**
     * Removes the resource adapter's class loader from the classloader linked
     * list
     *
     * @param moduleName the connector module that needs to be removed.
     */
    public void removeResourceAdapter(String moduleName) {
        ASURLClassLoader classLoaderToRemove = rarModuleClassLoaders.get(moduleName);
        if (classLoaderToRemove != null) {
            classLoaderChain.remove(classLoaderToRemove);
            rarModuleClassLoaders.remove(moduleName);
            _logger.log(Level.WARNING, "enterprise_util.remove_connector", moduleName);
        }
    }


    /**
     * Loads the class with the specified name and resolves it if specified.
     *
     * @see java.lang.ClassLoader#loadClass(java.lang.String, boolean)
     */
    @Override
    public synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Use the delegation model to service class requests that could be
        // satisfied by parent [common class loader].

        if (parent != null) {
            try {
                Class<?> clz = parent.loadClass(name);
                if (clz != null) {
                    if (resolve) {
                        resolveClass(clz);
                    }
                    return clz;
                }
            } catch (ClassNotFoundException e) {
                // ignore and try the connector modules classloader
                // chain.
            }
        } else {
            return super.loadClass(name, resolve);
        }

        // Going through the connector module classloader chain to find
        // class and return the first match.
        for (ASURLClassLoader ccl : classLoaderChain) {
            try {
                Class<?> clz = ccl.loadClass(name);
                if (clz != null) {
                    if (resolve) {
                        resolveClass(clz);
                    }
                    return clz;
                }
            } catch (ClassNotFoundException cnfe) {
                // ignore this exception and continue with next classloader in chain
                continue;
            }
        }

        //Can't find requested class in parent and in our classloader chain
        throw new ClassNotFoundException(name);
    }

    /**
     * Returns all the resources of the connector classloaders in the chain,
     * concatenated to a classpath string.
     * <p/>
     * Notice that this method is called by the setClassPath() method of
     * org.apache.catalina.loader.WebappLoader, since the ConnectorClassLoader does
     * not extend off of URLClassLoader.
     *
     * @return Classpath string containing all the resources of the connectors
     *         in the chain. An empty string if there exists no connectors in the chain.
     */
    @Override
    public String getClasspath() {
        StringBuilder strBuf = new StringBuilder();
        boolean first = true;
        for (ASURLClassLoader loader : classLoaderChain) {
            String eclClasspath = loader.getClasspath();
            if (eclClasspath != null) {
                if (!first) {
                    strBuf.append(File.pathSeparator);
                    first = false;
                }
                strBuf.append(eclClasspath);
            }
        }
        return strBuf.toString();
    }
}
