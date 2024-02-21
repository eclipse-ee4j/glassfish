/*
 * Copyright (c) 2021, 2024 Contributors to Eclipse Foundation.
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

import static org.glassfish.appclient.common.ClassPathUtils.getJavaClassPathForAppClient;

import com.sun.enterprise.loader.ResourceLocator;
import com.sun.enterprise.util.io.FileUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Consumer;
import org.glassfish.common.util.GlassfishUrlClassLoader;

/**
 * Application client classloader
 *
 * @author tjquinn
 */
public class ACCClassLoader extends GlassfishUrlClassLoader {

    private static final String AGENT_LOADER_CLASS_NAME = "org.glassfish.appclient.client.acc.agent.ACCAgentClassLoader";
    private static ACCClassLoader instance;

    private ACCClassLoader shadow;
    private boolean shouldTransform;

    private final List<ClassFileTransformer> transformers = Collections.synchronizedList(new ArrayList<ClassFileTransformer>());

    public static synchronized ACCClassLoader newInstance(ClassLoader parent, boolean shouldTransform) {
        if (instance != null) {
            throw new IllegalStateException("already set");
        }

        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        boolean currentCLWasAgentCL = currentClassLoader.getClass().getName().equals(AGENT_LOADER_CLASS_NAME);
        ClassLoader parentForACCCL = currentCLWasAgentCL ? currentClassLoader.getParent() : currentClassLoader;

        instance = new ACCClassLoader(getJavaClassPathForAppClient(), parentForACCCL, shouldTransform);

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


    public ACCClassLoader(ClassLoader parent, final boolean shouldTransform) {
        super(new URL[0], parent);
        this.shouldTransform = shouldTransform;
    }

    public ACCClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
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
            shadow = new ACCClassLoader(getURLs(), getParent());
        }

        return shadow;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!shouldTransform) {
            return findClassUnshadowed(name);
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
        try (InputStream is = getResourceAsStream(resourceName)) {
            if (is == null) {
                throw new ClassNotFoundException(className);
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileUtils.copy(is, baos);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new ClassNotFoundException(className, e);
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        return new ResourceLocator(this, getParentClassLoader(), true).getResources(name);
    }

    private ClassLoader getParentClassLoader() {
        final ClassLoader parent = getParent();
        if (parent == null) {
            return getSystemClassLoader();
        }

        return parent;
    }
}
