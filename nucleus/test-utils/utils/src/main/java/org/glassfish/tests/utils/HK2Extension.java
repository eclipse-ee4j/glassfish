/*
 * Copyright (c) 2021 Eclipse Foundation and/or its affiliates. All rights reserved.
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils;

import com.sun.enterprise.module.bootstrap.StartupContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.server.ServerEnvironmentImpl;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.jvnet.hk2.testing.junit.annotations.InhabitantFiles;
import org.jvnet.hk2.testing.junit.annotations.Packages;
import org.jvnet.hk2.testing.junit.internal.ClassVisitorImpl;
import org.jvnet.hk2.testing.junit.internal.TestServiceLocator;
import org.objectweb.asm.ClassReader;


/**
 * This JUnit5 extension is based on {@link TestServiceLocator} implementaion, which served for JUnit4.
 * In the future it will probably move to HK2, but first it needs to gain some maturity in practice.
 * The hk2-junitrunner will be refactored, so both usages will be possible.
 *
 * @author David Matejcek
 */
public class HK2Extension implements BeforeAllCallback, TestInstancePostProcessor, AfterAllCallback {
    private static final String CLASS_PATH_PROP = "java.class.path";
    private final static String DOT_CLASS = ".class";
    private final ServiceLocator locator;
    private DynamicConfiguration config;


    /**
     * Creates {@link ServiceLocator} with a name of this extension.
     */
    public HK2Extension() {
        locator = ServiceLocatorUtilities.createAndPopulateServiceLocator(getClass().getSimpleName());
    }


    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final Class<?> testClass = context.getRequiredTestClass();
        final ClassLoader loader = testClass.getClassLoader();
        final Packages packagesAnnotation = testClass.getAnnotation(Packages.class);
        final List<String> packages = packagesAnnotation == null ? List.of(testClass.getPackageName())
            : Arrays.asList(packagesAnnotation.value());

        config = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();
        addServicesFromDefault(loader, Set.of(), getDefaultLocatorPaths(context));
        addServicesFromPackage(packages, Set.of());
        config.addActiveDescriptor(ServerEnvironmentImpl.class);
        config.addActiveDescriptor(StartupContext.class);
        config.commit();
    }


    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        locator.inject(testInstance);
    }


    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        locator.shutdown();
    }


    private Set<String> getDefaultLocatorPaths(final ExtensionContext context) {
        final HashSet<String> paths = new HashSet<>();
        final InhabitantFiles iFiles = context.getRequiredTestClass().getAnnotation(InhabitantFiles.class);
        if (iFiles == null) {
            paths.add("META-INF/hk2-locator/default");
            return paths;
        }
        for (final String iFile : iFiles.value()) {
            paths.add(iFile);
        }
        return paths;
    }


    private void addServicesFromDefault(final ClassLoader loader, final Set<String> excludes, final Set<String> locatorFiles)
        throws IOException {
        for (final String locatorFile : locatorFiles) {
            final Enumeration<URL> resources = loader.getResources(locatorFile);
            readResources(resources, excludes);
        }
    }


    private void readResources(final Enumeration<URL> resources, final Set<String> excludes) throws IOException {
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                while (true) {
                    final DescriptorImpl bindMe = new DescriptorImpl();
                    final boolean goOn = bindMe.readObject(reader);
                    if (!goOn) {
                        break;
                    }
                    if (!excludes.contains(bindMe.getImplementation())) {
                        config.bind(bindMe);
                    }
                }
            }
        }
    }


    private void addServicesFromPackage(final List<String> packages, final Set<String> excludes) throws IOException {
        if (packages.isEmpty()) {
            return;
        }
        final String classPath = System.getProperty(CLASS_PATH_PROP);
        final StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            addServicesFromPathElement(packages, st.nextToken(), excludes);
        }
    }


    private void addServicesFromPathElement(final List<String> packages, final String path, final Set<String> excludes) throws IOException {
        final File fileElement = new File(path);
        if (!fileElement.exists()) {
            return;
        }

        if (fileElement.isDirectory()) {
            addServicesFromPathDirectory(packages, fileElement, excludes);
        } else {
            addServicesFromPathJar(packages, fileElement, excludes);
        }
    }


    private void addServicesFromPathDirectory(final List<String> packages, final File directory, final Set<String> excludes) throws IOException {
        for (final String pack : packages) {
            final File searchDir = new File(directory, convertToFileFormat(pack));
            if (!searchDir.exists()) {
                continue;
            }
            if (!searchDir.isDirectory()) {
                continue;
            }

            final File candidates[] = searchDir.listFiles((FilenameFilter) (dir, name) -> {
                if (name == null) {
                    return false;
                }
                if (name.endsWith(DOT_CLASS)) {
                    return true;
                }
                return false;
            });

            if (candidates == null) {
                continue;
            }

            for (final File candidate : candidates) {
                try (FileInputStream fis = new FileInputStream(candidate)) {
                    addClassIfService(fis, excludes);
                }
            }
        }
    }


    private void addServicesFromPathJar(final List<String> packages, final File jar, final Set<String> excludes) throws IOException {
        try (JarFile jarFile = new JarFile(jar)) {
            for (final String pack : packages) {
                final String packAsFile = convertToFileFormat(pack);
                final int packAsFileLen = packAsFile.length() + 1;

                final Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    final JarEntry entry = entries.nextElement();

                    final String entryName = entry.getName();
                    if (!entryName.startsWith(packAsFile)) {
                        // Not in the correct directory
                        continue;
                    }
                    if (entryName.substring(packAsFileLen).contains("/")) {
                        // Next directory down
                        continue;
                    }
                    if (!entryName.endsWith(DOT_CLASS)) {
                        // Not a class
                        continue;
                    }

                    try {
                        addClassIfService(jarFile.getInputStream(entry), excludes);
                    } catch (final IOException ioe) {
                        // Simply don't add it if we can't read it
                    }
                }
            }
        }
    }


    private static String convertToFileFormat(final String clazzFormat) {
        return clazzFormat.replaceAll("\\.", "/");
    }


    private void addClassIfService(final InputStream is, final Set<String> excludes) throws IOException {
        final ClassReader reader = new ClassReader(is);
        final ClassVisitorImpl cvi = new ClassVisitorImpl(locator, true, excludes);
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
}
