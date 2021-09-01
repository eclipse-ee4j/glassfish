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

import com.sun.enterprise.glassfish.bootstrap.Constants;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.tests.utils.ConfigApiTest.TestDocument;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Transactions;
import org.jvnet.hk2.testing.junit.annotations.Classes;
import org.jvnet.hk2.testing.junit.annotations.InhabitantFiles;
import org.jvnet.hk2.testing.junit.annotations.Packages;
import org.jvnet.hk2.testing.junit.internal.ClassVisitorImpl;
import org.jvnet.hk2.testing.junit.internal.TestServiceLocator;
import org.objectweb.asm.ClassReader;

import static org.glassfish.hk2.utilities.BuilderHelper.createConstantDescriptor;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneConstant;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.createAndPopulateServiceLocator;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * This JUnit5 extension is based on {@link TestServiceLocator} implementaion, which served for JUnit4.
 * In the future it will probably move to HK2, but first it needs to gain some maturity in practice.
 * The hk2-junitrunner will be refactored, so both usages will be possible.
 *
 * @author David Matejcek
 */
public class HK2Extension
    implements BeforeAllCallback, TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    private static final Logger LOG = Logger.getLogger(HK2Extension.class.getName());
    private static final String CLASS_PATH_PROP = "java.class.path";
    private static final String DOT_CLASS = ".class";
    private static final String START_TIME_METHOD = "start time method";
    private ServiceLocator locator;
    private DynamicConfiguration config;
    private Namespace namespaceMethod;


    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        // TODO: make the extension extensible, so projects can change defaults
        final Class<?> testClass = context.getRequiredTestClass();
        final ClassLoader loader = testClass.getClassLoader();

        locator = createAndPopulateServiceLocator(testClass.getSimpleName() + "ServiceLocator");
        assertNotNull(locator.getService(Transactions.class),
            "Transactions service from Configuration subsystem is null");

        addOneConstant(locator, Logger.getLogger(testClass.getName()));

        final Properties startupContextProperties = new Properties();
        final String rootPath = testClass.getResource("/").getPath();
        startupContextProperties.put(Constants.INSTALL_ROOT_PROP_NAME, rootPath);
        startupContextProperties.put(Constants.INSTANCE_ROOT_PROP_NAME, rootPath);
        final StartupContext startupContext = new StartupContext(startupContextProperties);
        addOneConstant(locator, startupContext);
        addOneConstant(locator, new StaticModulesRegistry(loader, startupContext));

        addOneConstant(locator, new TestDocument(locator));
        final CustomConfiguration configAnnotation = testClass.getAnnotation(CustomConfiguration.class);
        if (configAnnotation != null) {
            addConfigFromResource(loader, configAnnotation.value());
        }

        final Packages packagesAnnotation = testClass.getAnnotation(Packages.class);
        final List<String> packages = packagesAnnotation == null ? List.of(testClass.getPackageName())
            : Arrays.asList(packagesAnnotation.value());
        final Classes classesAnnotation = testClass.getAnnotation(Classes.class);
        final List<Class<?>> classes = classesAnnotation == null ? List.of() : Arrays.asList(classesAnnotation.value());
        final ExcludeClasses excludeClassesAnnotation = testClass.getAnnotation(ExcludeClasses.class);
        final Set<String> excludedClasses = excludeClassesAnnotation == null ? Set.of()
            : Stream.of(excludeClassesAnnotation.value()).map(Class::getName).collect(Collectors.toSet());

        config = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();
        addServicesFromDefault(loader, excludedClasses, getDefaultLocatorPaths(context));
        addServicesFromPackage(packages, excludedClasses);
        addServices(classes, excludedClasses);

        config.addActiveDescriptor(ServerEnvironmentImpl.class);

        try {
            config.commit();
        } catch (Exception e) {
            // if it failed, dump everything
            ServiceLocatorUtilities.dumpAllDescriptors(locator);
            throw e;
        }
    }



    private void addConfigFromResource(final ClassLoader loader, final String resourcePath) {
        URL url = Objects.requireNonNull(loader.getResource(resourcePath),
            "The resourcePath doesn't exist: " + resourcePath);
        ConfigParser configParser = new ConfigParser(locator);
        DomDocument document = configParser.parse(url, locator.getService(TestDocument.class));
        ServiceLocatorUtilities.addOneConstant(locator, document);
    }


    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        LOG.log(Level.INFO, "Injecting attributes to the test instance: {0}", testInstance);
        locator.inject(testInstance);
    }


    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        LOG.log(Level.INFO, "beforeEach. Test name: {0}", context.getRequiredTestMethod());
        this.namespaceMethod = Namespace.create(context.getRequiredTestClass(), context.getRequiredTestMethod());
        context.getStore(this.namespaceMethod).put(START_TIME_METHOD, LocalDateTime.now());
    }


    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        final LocalDateTime startTime = context.getStore(this.namespaceMethod).remove(START_TIME_METHOD,
            LocalDateTime.class);
        LOG.log(Level.INFO, "afterEach(). Test name: {0}, started at {1}, test time: {2} ms", //
            new Object[] {
            context.getRequiredTestMethod().getName(), //
            DateTimeFormatter.ISO_LOCAL_TIME.format(startTime), //
            startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS)});
    }


    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        if (locator != null) {
            locator.shutdown();
        }
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


    private void addServicesFromDefault(final ClassLoader loader, final Set<String> excludedClasses,
        final Set<String> locatorFiles) {
        for (final String locatorFile : locatorFiles) {
            Enumeration<URL> resources;
            try {
                resources = loader.getResources(locatorFile);
            } catch (IOException e) {
                throw new IllegalStateException("addServicesFromDefault failed.", e);
            }
            readResources(resources, excludedClasses);
        }
    }


    private void addServicesFromPackage(final List<String> packages, final Set<String> excludedClasses) {
        if (packages.isEmpty()) {
            return;
        }
        final String classPath = System.getProperty(CLASS_PATH_PROP);
        final StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            addServicesFromPathElement(packages, st.nextToken(), excludedClasses);
        }
    }


    private void addServices(final List<Class<?>> classes, final Set<String> exclusions) {
        for (Class<?> clazz : classes) {
            if (exclusions.contains(clazz.getName()) || exclusions.contains(clazz.getPackageName())) {
                continue;
            }
            config.addActiveDescriptor(clazz);
        }
    }


    private void addServicesFromPathElement(final List<String> packages, final String path,
        final Set<String> excludedClasses) {
        final File fileElement = new File(path);
        if (!fileElement.exists()) {
            return;
        }

        if (fileElement.isDirectory()) {
            addServicesFromPathDirectory(packages, fileElement, excludedClasses);
        } else {
            addServicesFromPathJar(packages, fileElement, excludedClasses);
        }
    }


    private void addServicesFromPathDirectory(final List<String> packages, final File directory,
        final Set<String> excludedClasses) {
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
                    addClassIfService(fis, excludedClasses);
                } catch (IOException e) {
                    throw new IllegalStateException("addServicesFromPathDirectory failed.", e);
                }
            }
        }
    }


    private void addServicesFromPathJar(final List<String> packages, final File jar, final Set<String> excludes) {
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
        } catch (IOException e) {
            throw new IllegalStateException("addServicesFromPathJar failed.", e);
        }
    }


    private void readResources(final Enumeration<URL> resources, final Set<String> excludedClasses) {
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()))) {
                while (true) {
                    final DescriptorImpl descriptor = new DescriptorImpl();
                    final boolean goOn = descriptor.readObject(reader);
                    if (!goOn) {
                        break;
                    }
                    if (excludedClasses.contains(descriptor.getImplementation())) {
                        continue;
                    }
                    config.bind(descriptor);
                }
            } catch (IOException e) {
                throw new IllegalStateException("readResources failed.", e);
            }
        }
    }


    private static String convertToFileFormat(final String clazzFormat) {
        return clazzFormat.replaceAll("\\.", "/");
    }


    private void addClassIfService(final InputStream is, final Set<String> excludedClasses) throws IOException {
        final ClassReader reader = new ClassReader(is);
        final ClassVisitorImpl cvi = new ClassVisitorImpl(locator, true, excludedClasses);
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
}
