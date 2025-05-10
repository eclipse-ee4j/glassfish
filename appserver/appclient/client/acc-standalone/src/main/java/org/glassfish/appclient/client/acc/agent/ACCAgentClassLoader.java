/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.security.PrivilegedAction;
import java.util.Enumeration;

import org.glassfish.appclient.common.ClassPathUtils;

import static java.security.AccessController.doPrivileged;

/**
 * Used as the system class loader during app client launch.
 * <p>
 * The main role of this class loader is to find a splash screen image that might be specified in
 * the manifest of the app client.
 * Once the ACC begins working it will create an ACCClassLoader for loading client classes and
 * locating client resources.
 * <p>
 * This class and it's dependencies must not use logging, which could cause recursion in class
 * loading. So don't extend GlassfishUrlClassLoader. Reproducer: TCK tests use this classloader.
 * <p>
 * The name of this class must not be changed - it is explicitly used in the TCK Platform Test
 * package.
 *
 * @author tjquinn
 * @author David Matejcek
 */
public class ACCAgentClassLoader extends URLClassLoader {

    static {
        registerAsParallelCapable();
    }

    private boolean isActive = true;

    /**
     * This constructor is used by the VM to create a system class loader (as specified by -Djava.system.class.loader on the
     * java command created from the appclient script).
     */
    public ACCAgentClassLoader(ClassLoader parent) {
        super("Agent", new URL[] {}, prepareLoader(parent));
    }


    private static URLClassLoader prepareLoader(ClassLoader parent) {
        PrivilegedAction<URLClassLoader> action = () -> new URLClassLoader("User",
            new URL[] {ClassPathUtils.getGFClientJarURL()}, new ClassLoaderWrapper(parent));
        return doPrivileged(action);
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
}
