/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.v3.server;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.api.DelegatingClassLoader;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * This class is responsible for constructing class loader that has visibility
 * to deploy time libraries (--libraries and EXTENSION_LIST of MANIFEST.MF,
 * provided the library is available in 'applibs' directory) for an application.
 * It is different from CommonClassLoader in a sense that the libraries that are part of
 * common class loader are shared by all applications, where as this class
 * loader adds a scope to a library.
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
@Singleton
public class AppLibClassLoaderServiceImpl {
    /*
     * TODO(Sahoo): Not Yet Properly Implemented, as we have to bring in
     * all the changes from
     * http://fisheye5.cenqua.com/browse/glassfish/appserv-core/src/java/com/sun/enterprise/loader/EJBClassPathUtils.java
     * To be specific, we have to bring in createApplicationLibrariesClassLoader().
     */

    @Inject
    ServiceLocator habitat;

    @Inject
    CommonClassLoaderServiceImpl commonCLS;

    /**
     * @see org.glassfish.internal.api.ClassLoaderHierarchy#getAppLibClassLoader(String, List<URI>)
     */
    public ClassLoader getAppLibClassLoader(String application, List<URI> libURIs) throws MalformedURLException {
        ClassLoaderHierarchy clh = habitat.getService(ClassLoaderHierarchy.class);
        DelegatingClassLoader connectorCL = clh.getConnectorClassLoader(application);

        if (libURIs == null || libURIs.isEmpty()) {
            // Optimization: when there are no libraries, why create an empty
            // class loader in the hierarchy? Instead return the parent.
            return connectorCL;
        }

        final ClassLoader commonCL = commonCLS.getCommonClassLoader();
        PrivilegedAction<DelegatingClassLoader> action = () -> new AppLibClassLoader(commonCL);
        DelegatingClassLoader applibCL = AccessController.doPrivileged(action);

        // order of classfinders is important here :
        // connector's classfinders should be added before libraries' classfinders
        // as the delegation hierarchy is appCL->app-libsCL->connectorCL->commonCL->API-CL
        // since we are merging connector and applib classfinders to be at same level,
        // connector classfinders need to be be before applib classfinders in the horizontal
        // search path
        for (DelegatingClassLoader.ClassFinder cf : connectorCL.getDelegates()) {
            applibCL.addDelegate(cf);
        }
        addDelegates(libURIs, applibCL);

        return applibCL;
    }

    private void addDelegates(Collection<URI> libURIs, DelegatingClassLoader holder)
            throws MalformedURLException {

        ClassLoader commonCL = commonCLS.getCommonClassLoader();
        for (URI libURI : libURIs) {
            DelegatingClassLoader.ClassFinder libCF = new URLClassFinder(new URL[]{libURI.toURL()}, commonCL);
            holder.addDelegate(libCF);
        }
    }

    /**
     * @see org.glassfish.internal.api.ClassLoaderHierarchy#getAppLibClassFinder(List<URI>)
     */
    public DelegatingClassLoader.ClassFinder getAppLibClassFinder(Collection<URI> libURIs)
            throws MalformedURLException {
        final ClassLoader commonCL = commonCLS.getCommonClassLoader();
        DelegatingClassLoader appLibClassFinder = AccessController.doPrivileged(new PrivilegedAction<DelegatingClassLoader>() {
            @Override
            public DelegatingClassLoader run() {
                return new AppLibClassFinder(commonCL);
            }
        });
        addDelegates(libURIs, appLibClassFinder);
        return (DelegatingClassLoader.ClassFinder)appLibClassFinder;
    }

    private static class AppLibClassLoader extends DelegatingClassLoader implements Closeable {

        public AppLibClassLoader(ClassLoader parent) {
            super(parent);
        }

        @Override
        public void close() throws IOException {
            for (ClassFinder classFinder : getDelegates()) {
                if (classFinder instanceof URLClassFinder) {
                    ((GlassfishUrlClassLoader) classFinder).close();
                }
            }
        }
    }

    private static class URLClassFinder extends GlassfishUrlClassLoader
            implements DelegatingClassLoader.ClassFinder {

        URLClassFinder(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        public Class<?> findClass(String name) throws ClassNotFoundException {
            Class<?> c = this.findLoadedClass(name);
            if (c!=null) {
                return c;
            }
            return super.findClass(name);
        }

        @Override
        public Class<?> findExistingClass(String name) {
            return super.findLoadedClass(name);
        }
    }

    private static class AppLibClassFinder extends DelegatingClassLoader implements DelegatingClassLoader.ClassFinder {

        AppLibClassFinder(ClassLoader parent, List<DelegatingClassLoader.ClassFinder> delegates)
                throws IllegalArgumentException {
            super(parent, delegates);
        }

        AppLibClassFinder(ClassLoader parent) {
            super(parent);
        }

        @Override
        public Class<?> findExistingClass(String name) {
            // no action needed as parent is delegating classloader which will never be a defining classloader
            return null;
        }

        @Override
        public URL findResource(String name) {
            return super.findResource(name);
        }

        @Override
        public Enumeration<URL> findResources(String name) throws IOException {
            return super.findResources(name);
        }
    }
}
