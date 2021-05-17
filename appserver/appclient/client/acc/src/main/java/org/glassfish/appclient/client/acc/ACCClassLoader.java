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
package org.glassfish.appclient.client.acc;

import static java.security.AccessController.doPrivileged;
import static java.util.Collections.emptyList;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Consumer;

import org.glassfish.appclient.common.ClientClassLoaderDelegate;

/**
 *
 * @author tjquinn
 */
public class ACCClassLoader extends URLClassLoader {

    private static final String AGENT_LOADER_CLASS_NAME = "org.glassfish.appclient.client.acc.agent.ACCAgentClassLoader";
    private static ACCClassLoader instance;

    private ACCClassLoader shadow;
    private boolean shouldTransform;

    private final List<ClassFileTransformer> transformers = Collections.synchronizedList(new ArrayList<ClassFileTransformer>());

    private ClientClassLoaderDelegate clientCLDelegate;

    public static synchronized ACCClassLoader newInstance(ClassLoader parent, boolean shouldTransform) {
        if (instance != null) {
            throw new IllegalStateException("already set");
        }

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        boolean currentCLWasAgentCL = currentClassLoader.getClass().getName().equals(AGENT_LOADER_CLASS_NAME);
        ClassLoader parentForACCCL = currentCLWasAgentCL ? currentClassLoader.getParent() : currentClassLoader;

        instance = doPrivileged(new PrivilegedAction<ACCClassLoader>() {

            @Override
            public ACCClassLoader run() {
                return new ACCClassLoader(userClassPath(), parentForACCCL, shouldTransform);
            }

        });

        if (currentCLWasAgentCL) {
            try {
                adjustACCAgentClassLoaderParent(instance);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return instance;
    }

    public static ACCClassLoader instance() {
        return instance;
    }

    private static void adjustACCAgentClassLoaderParent(ACCClassLoader instance) throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();

        if (systemClassLoader.getClass().getName().equals(AGENT_LOADER_CLASS_NAME)) {
            if (systemClassLoader instanceof Consumer<?>) {

                @SuppressWarnings("unchecked")
                Consumer<ClassLoader> consumerOfClassLoader = (Consumer<ClassLoader>) systemClassLoader;

                consumerOfClassLoader.accept(instance);

                System.setProperty("org.glassfish.appclient.acc.agentLoaderDone", "true");
            }
        }
    }

    private static URL[] userClassPath() {
        URI GFSystemURI = GFSystemURI();
        List<URL> result = classPathToURLs(System.getProperty("java.class.path"));

        for (ListIterator<URL> it = result.listIterator(); it.hasNext();) {
            URL url = it.next();
            try {
                if (url.toURI().equals(GFSystemURI)) {
                    it.remove();
                }
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            }
        }

        result.addAll(classPathToURLs(System.getenv("APPCPATH")));

        return result.toArray(new URL[result.size()]);
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

        List<URL> result = new ArrayList<>();
        try {
            for (String classPathElement : classPath.split(File.pathSeparator)) {
                result.add(new File(classPathElement).toURI().normalize().toURL());
            }
            return result;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ACCClassLoader(ClassLoader parent, final boolean shouldTransform) {
        super(new URL[0], parent);
        this.shouldTransform = shouldTransform;

        clientCLDelegate = new ClientClassLoaderDelegate(this);
    }

    public ACCClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);

        clientCLDelegate = new ClientClassLoaderDelegate(this);
    }


    private ACCClassLoader(URL[] urls, ClassLoader parent, boolean shouldTransform) {
        this(urls, parent);
        this.shouldTransform = shouldTransform;
    }

    public synchronized void appendURL(final URL url) {
        addURL(url);
        if (shadow != null) {
            shadow.addURL(url);
        }
    }

    public void addTransformer(final ClassFileTransformer xf) {
        transformers.add(xf);
    }

    public void setShouldTransform(final boolean shouldTransform) {
        this.shouldTransform = shouldTransform;
    }

    synchronized ACCClassLoader shadow() {
        if (shadow == null) {
            shadow = doPrivileged(new PrivilegedAction<ACCClassLoader>() {
                @Override
                public ACCClassLoader run() {
                    return new ACCClassLoader(getURLs(), getParent());
                }

            });
        }

        return shadow;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!shouldTransform) {
            return super.findClass(name);
        }

        return copyClass(shadow().findClassUnshadowed(name));
    }

    private Class<?> copyClass(Class<?> sourceClass) throws ClassNotFoundException {
        String name = sourceClass.getName();
        ProtectionDomain pd = sourceClass.getProtectionDomain();
        byte[] bytecode = readByteCode(name);

        for (ClassFileTransformer xf : transformers) {
            try {
                bytecode = xf.transform(this, name, null, pd, bytecode);
            } catch (IllegalClassFormatException ex) {
                throw new ClassNotFoundException(name, ex);
            }
        }
        return defineClass(name, bytecode, 0, bytecode.length, pd);
    }

    private Class<?> findClassUnshadowed(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    private byte[] readByteCode(final String className) throws ClassNotFoundException {
        String resourceName = className.replace('.', '/') + ".class";
        InputStream is = getResourceAsStream(resourceName);
        if (is == null) {
            throw new ClassNotFoundException(className);
        }

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8196];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(className, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new ClassNotFoundException(className, e);
            }
        }
    }

    @Override
    protected PermissionCollection getPermissions(CodeSource codesource) {
        if (System.getSecurityManager() == null) {
            return super.getPermissions(codesource);
        }

        // When security manager is enabled, find the declared permissions
        if (clientCLDelegate.getCachedPerms(codesource) != null) {
            return clientCLDelegate.getCachedPerms(codesource);
        }

        return clientCLDelegate.getPermissions(codesource, super.getPermissions(codesource));
    }

    public void processDeclaredPermissions() throws IOException {
        if (clientCLDelegate == null) {
            clientCLDelegate = new ClientClassLoaderDelegate(this);
        }
    }

}
