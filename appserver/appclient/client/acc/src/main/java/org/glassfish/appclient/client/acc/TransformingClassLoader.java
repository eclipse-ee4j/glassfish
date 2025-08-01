/*
 * Copyright (c) 2021, 2025 Contributors to Eclipse Foundation.
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

import com.sun.enterprise.loader.ResourceLocator;
import com.sun.enterprise.util.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.glassfish.embeddable.client.ApplicationClientClassLoader;
import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;


/**
 * Application client classloader
 *
 * @author tjquinn
 */
public class TransformingClassLoader extends GlassfishUrlClassLoader {

    static {
        registerAsParallelCapable();
    }

    private static final Function<Path, URL> PATH_TO_URL = p -> {
        try {
            return p.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not convert path to url: " + p, e);
        }
    };

    private static TransformingClassLoader instance;

    private TransformingClassLoader shadow;
    private final boolean shouldTransform;
    private final List<ClassFileTransformer> transformers = Collections.synchronizedList(new ArrayList<>());


    /**
     * @param parent
     * @param shouldTransform
     * @return new class loader
     * @throws IllegalStateException
     * @throws IllegalArgumentException
     */
    public static synchronized TransformingClassLoader newInstance(ClassLoader parent, boolean shouldTransform) {
        if (instance != null) {
            throw new IllegalStateException("Already set");
        }
        instance = parent instanceof ApplicationClientClassLoader
            // Parent already comes with user dependencies
            ? new TransformingClassLoader(new URL[0], parent, shouldTransform)
            // Otherwise adopt system class path, environment options, whatever.
            : new TransformingClassLoader(createClassPath(), parent, shouldTransform);

        return instance;
    }

    public static TransformingClassLoader instance() {
        return instance;
    }

    private TransformingClassLoader(URL[] classpath, ClassLoader parent, boolean shouldTransform) {
        super("Transformer", classpath, parent);
        this.shouldTransform = shouldTransform;
    }

    public TransformingClassLoader(URL[] urls, ClassLoader parent) {
        super("Transformer", urls, parent);
        this.shouldTransform = false;
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

    synchronized TransformingClassLoader shadow() {
        if (shadow == null) {
            shadow = new TransformingClassLoader(getURLs(), getParent());
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
        final ResourceLocator locator = new ResourceLocator(this, getParent(), true);
        return locator.getResources(name);
    }

    private static URL[] createClassPath() {
        final Stream<Path> cpPaths = convertClassPathToPaths(System.getProperty("java.class.path"));
        final Stream<Path> envPaths = convertClassPathToPaths(System.getenv("APPCPATH"));
        final Predicate<Path> filterOutGfClient = f -> !f.endsWith(Path.of("gf-client.jar"));
        return Stream.concat(cpPaths, envPaths).map(Path::toAbsolutePath).map(Path::normalize).distinct()
            .filter(filterOutGfClient).map(PATH_TO_URL).toArray(URL[]::new);
    }

    private static Stream<Path> convertClassPathToPaths(final String classPath) {
        if (classPath == null || classPath.isBlank()) {
            return Stream.empty();
        }
        try {
            return Stream.of(classPath.split(File.pathSeparator)).map(File::new).map(File::toPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not parse the classpath: " + classPath, e);
        }
    }

}
