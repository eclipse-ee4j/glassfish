/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.appclient.client.acc.agent;

import static java.io.File.pathSeparator;
import static java.security.AccessController.doPrivileged;
import static java.util.Collections.emptyList;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ListIterator;

/**
 * Used as the system class loader during app client launch.
 * <p>
 * The main role of this class loader is to find a splash screen image that might be specified in the manifest of the
 * app client. Once the ACC begins working it will create an ACCClassLoader for loading client classes and locating
 * client resources.
 *
 * @author tjquinn
 */
public class ACCAgentClassLoader extends URLClassLoader {

    private boolean isActive = true;

    public ACCAgentClassLoader(ClassLoader parent) {
        /*
         * This constructor is used by the VM to create a system class loader (as specified by -Djava.system.class.loader on the
         * java command created from the appclient script).
         *
         * Actually create two new loaders. One will handle the GlassFish
         * system JARs and the second will temporarily handle the application resources - typically for a splash screen.
         */
        super(userClassPath(), prepareLoader(GFSystemClassPath(), new ClassLoaderWrapper(parent.getParent())));
    }

    private static URLClassLoader prepareLoader(URL[] urls, ClassLoader parent) {
        return doPrivileged(new PrivilegedAction<URLClassLoader>() {
            @Override
            public URLClassLoader run() {
                return new URLClassLoader(urls, parent);
            }

        });
    }

    public ACCAgentClassLoader(URL[] urls) {
        super(urls);
    }

    public ACCAgentClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, new ClassLoaderWrapper(parent));
    }

    public ACCAgentClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    // a custom system class loader need to define this method in order to load the java agent.
    public void appendToClassPathForInstrumentation(String path) throws MalformedURLException {
        addURL(new File(path).toURI().toURL());
    }

    @Override
    public synchronized Class<?> loadClass(String name) throws ClassNotFoundException {
        if (isActive && isStillActive()) {
            return super.loadClass(name);
        }

        return getParent().loadClass(name);
    }

    @Override
    public URL getResource(String name) {
        if (isActive && isStillActive()) {
            return super.getResource(name);
        }

        return getParent().getResource(name);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        if (isActive && isStillActive()) {
            return super.getResources(name);
        }

        return getParent().getResources(name);
    }

    private boolean isStillActive() {
        if (isActive) {
            String propValue = System.getProperty("org.glassfish.appclient.acc.agentLoaderDone");
            isActive = (propValue != null);
        }

        return isActive;
    }

    private static URL[] userClassPath() {
        URI GFSystemURI = GFSystemURI();
        List<URL> classPathURLs = classPathToURLs(System.getProperty("java.class.path"));

        for (ListIterator<URL> classPathURLsIterator = classPathURLs.listIterator(); classPathURLsIterator.hasNext();) {
            URL classPathURL = classPathURLsIterator.next();
            try {
                if (classPathURL.toURI().equals(GFSystemURI)) {
                    classPathURLsIterator.remove();
                }
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }

        return classPathURLs.toArray(new URL[classPathURLs.size()]);
    }

    private static URL[] GFSystemClassPath() {
        try {
            return new URL[] { GFSystemURI().normalize().toURL() };
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static URI GFSystemURI() {
        try {
            return Class.forName("org.glassfish.appclient.client.acc.agent.AppClientContainerAgent")
                        .getProtectionDomain()
                        .getCodeSource()
                        .getLocation()
                        .toURI()
                        .normalize();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static List<URL> classPathToURLs(final String classPath) {
        if (classPath == null) {
            return emptyList();
        }

        List<URL> classPathURLs = new ArrayList<>();
        try {
            for (String classPathElement : classPath.split(pathSeparator)) {
                classPathURLs.add(new File(classPathElement).toURI().normalize().toURL());
            }
            return classPathURLs;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
