/*
 * Copyright (c) 2021, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.tests.utils.junit;

import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.single.StaticModulesRegistry;
import com.sun.enterprise.util.io.FileUtils;

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
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.server.ServerEnvironmentImpl;
import org.glassfish.tests.utils.mock.MockGenerator;
import org.glassfish.tests.utils.mock.TestDocument;
import org.glassfish.tests.utils.mock.TestServerEnvironment;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.jvnet.hk2.config.ConfigParser;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.DomDocument;
import org.jvnet.hk2.config.Transactions;
import org.objectweb.asm.ClassReader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;
import static org.glassfish.embeddable.GlassFishVariable.INSTALL_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.INSTANCE_ROOT;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneConstant;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.addOneDescriptor;
import static org.glassfish.hk2.utilities.ServiceLocatorUtilities.createAndPopulateServiceLocator;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This JUnit5 extension allows to use HK2 services in tests.
 * You can also override methods in your own {@link Extension} and add features.
 * <p>
 * Injectable services:
 * <ul>
 * <li><code>@Inject {@link Logger}</code> - named after the test.
 * <li><code>@Inject {@link StartupContext}</code> - install root and instance root are set
 * to the root of test classpath; see {@link #getStartupContextProperties(ExtensionContext)}.
 * <li><code>@Inject {@link StaticModulesRegistry}</code>
 * <li><code>@Inject {@link TestDocument}</code>
 * <li>services configured by the {@link DomainXml} annotation.
 * </ul>
 *
 * @author David Matejcek
 */
public class HK2JUnit5Extension
    implements BeforeAllCallback, TestInstancePostProcessor, BeforeEachCallback, AfterEachCallback, AfterAllCallback {

    /**
     * One second timeout set as system property before tests start.
     * @see ConfigSupport
     */
    public static final Integer HK2_CONFIG_LOCK_TIME_OUT_IN_SECONDS = 1;
    private static final Logger LOG = Logger.getLogger(HK2JUnit5Extension.class.getName());
    private static final String CLASS_PATH_PROP = "java.class.path";
    private static final String DOT_CLASS = ".class";
    private static final String START_TIME_METHOD = "start time method";

    private MockGenerator mockGenerator;
    private ServiceLocator locator;
    private DynamicConfiguration config;
    private StartupContext startupContext;
    private StaticModulesRegistry modulesRegistry;

    private Namespace namespaceMethod;

    static {
        System.setProperty("org.glassfish.hk2.config.locktimeout", HK2_CONFIG_LOCK_TIME_OUT_IN_SECONDS.toString());
    }


    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final Class<?> testClass = context.getRequiredTestClass();
        final ClassLoader loader = getClassLoader(context);

        mockGenerator = new MockGenerator();
        final Properties startupContextProperties = getStartupContextProperties(context);
        LOG.log(Level.CONFIG, "startupContextProperties set to {0}", startupContextProperties);
        startupContext = new StartupContext(startupContextProperties);
        modulesRegistry = new StaticModulesRegistry(getClassLoader(context), startupContext);
        // note: locator created from the registry would not support transactions.
        locator = createLocator(context);

        addConstantServices(context);

        final String domainXml = getDomainXml(testClass);
        Class<? extends DomDocument<?>> domainXmlDomClass = getDomainXmlDomClass(testClass);
        addOneConstant(locator, domainXmlDomClass.getConstructor(ServiceLocator.class).newInstance(locator));
        if (domainXml != null) {
            addConfigFromResource(loader, domainXml, domainXmlDomClass);
        }

        // lists keep ordering
        final List<String> packages = getPackages(testClass);
        final List<Class<?>> classes = getClasses(testClass);
        final Set<Class<?>> excludedClasses = getExcludedClasses(testClass);

        config = locator.getService(DynamicConfigurationService.class).createDynamicConfiguration();
        AbstractActiveDescriptor<ExecutorService> descriptor = BuilderHelper.createConstantDescriptor(Executors.newCachedThreadPool());
        descriptor.addContractType(ExecutorService.class);
        config.addActiveDescriptor(descriptor);

        addServicesFromLocatorFiles(loader, excludedClasses, getLocatorFilePaths(context));
        addServicesFromPackage(packages, excludedClasses);
        addServices(classes, excludedClasses);

        try {
            config.commit();
        } catch (Exception e) {
            // if it failed, dump everything
            ServiceLocatorUtilities.dumpAllDescriptors(locator, System.err);
            throw e;
        }

        ExtrasUtilities.enableTopicDistribution(getLocator());
    }


    @Override
    public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
        LOG.log(Level.FINE, "Injecting attributes to the test instance: {0}", testInstance);
        locator.inject(testInstance);
    }


    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        LOG.log(Level.FINE, "beforeEach. Test name: {0}", context.getRequiredTestMethod());
        this.namespaceMethod = Namespace.create(context.getRequiredTestClass(), context.getRequiredTestMethod());
        context.getStore(this.namespaceMethod).put(START_TIME_METHOD, LocalDateTime.now());
    }


    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        final LocalDateTime startTime = context.getStore(this.namespaceMethod).remove(START_TIME_METHOD,
            LocalDateTime.class);
        LOG.log(Level.INFO, "Test: {0}.{1}, started at {2}, test time: {3} ms",
            new Object[] {
                context.getRequiredTestClass().getName(), context.getRequiredTestMethod().getName(),
                DateTimeFormatter.ISO_LOCAL_TIME.format(startTime),
                startTime.until(LocalDateTime.now(), ChronoUnit.MILLIS)});
    }


    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        if (modulesRegistry != null) {
            modulesRegistry.shutdown();
        }
        if (locator != null) {
            locator.shutdown();
        }
    }


    /**
     * @return locator with a name set by {@link #getLocatorName(ExtensionContext)}
     */
    protected ServiceLocator getLocator() {
        return locator;
    }


    /**
     * @return simple name of the test class + ServiceLocator
     */
    protected String getLocatorName(final ExtensionContext context) {
        return context.getRequiredTestClass().getSimpleName() + "ServiceLocator";
    }


    /**
     * @return {@link ServiceLocator} named by {@link #getLocatorName(ExtensionContext)}
     */
    protected ServiceLocator createLocator(final ExtensionContext context) {
        final ServiceLocator newLocator = createAndPopulateServiceLocator(getLocatorName(context));
        assertNotNull(newLocator.getService(Transactions.class),
            "Transactions service from Configuration subsystem is not available!");
        return newLocator;
    }


    /**
     * @return {@link ClassLoader} of the test class
     */
    protected ClassLoader getClassLoader(final ExtensionContext context) {
        return context.getRequiredTestClass().getClassLoader();
    }


    /**
     * @return properties for the {@link StartupContext} instance.
     */
    protected Properties getStartupContextProperties(final ExtensionContext context) {
        final Properties startupContextProperties = new Properties();
        final String rootPath = FileUtils.toFile(context.getRequiredTestClass().getResource("/")).getPath();
        startupContextProperties.put(INSTALL_ROOT.getPropertyName(), rootPath);
        startupContextProperties.put(INSTANCE_ROOT.getPropertyName(), rootPath);
        return startupContextProperties;
    }


    /**
     * Uses {@link ServiceLocatorUtilities#addOneConstant(ServiceLocator, Object)} calls to set
     * some useful implicit services:
     * <li><code>@Inject {@link Logger}</code> - named after the test.
     * <li><code>@Inject {@link MockGenerator}</code>
     * <li><code>@Inject {@link StartupContext}</code> - install root and instance root are set
     * to the root of test classpath; see {@link #getStartupContextProperties(ExtensionContext)}.
     * <li><code>@Inject {@link StaticModulesRegistry}</code>
     * <li><code>@Inject {@link TestDocument}</code>
     */
    protected void addConstantServices(final ExtensionContext context) {
        addOneConstant(locator, Logger.getLogger(context.getRequiredTestClass().getName()), "testLogger", Logger.class);
        addOneConstant(locator, mockGenerator);
        addOneConstant(locator, startupContext);
        addOneConstant(locator, modulesRegistry);
        addOneConstant(locator, new TestServerEnvironment(startupContext), "TestServerEnvironment",
            ServerEnvironment.class, ServerEnvironmentImpl.class);
    }


    /**
     * @param testClass
     * @return path obtained from test's {@link DomainXml} annotation
     */
    protected String getDomainXml(final Class<?> testClass) {
        final DomainXml domainXmlAnnotation = testClass.getAnnotation(DomainXml.class);
        return domainXmlAnnotation == null ? null : domainXmlAnnotation.value();
    }


    /**
     * @param testClass
     * @return a {@link DomDocument} class obtained from test's {@link DomainXml} annotation
     */
    protected Class<? extends DomDocument<?>> getDomainXmlDomClass(final Class<?> testClass) {
        final DomainXml domainXmlAnnotation = testClass.getAnnotation(DomainXml.class);
        return domainXmlAnnotation == null ? TestDocument.class : domainXmlAnnotation.domDocumentClass();
    }


    /**
     * @param testClass
     * @return packages obtained from test's {@link Packages} annotation
     */
    protected List<String> getPackages(final Class<?> testClass) {
        final Packages packagesAnnotation = testClass.getAnnotation(Packages.class);
        final List<String> packages = packagesAnnotation == null ? List.of(testClass.getPackageName())
            : Arrays.asList(packagesAnnotation.value());
        return packages;
    }


    /**
     * @param testClass
     * @return classes obtained from test's {@link Classes} annotation
     */
    protected List<Class<?>> getClasses(final Class<?> testClass) {
        final Classes classesAnnotation = testClass.getAnnotation(Classes.class);
        final List<Class<?>> classes = classesAnnotation == null ? List.of() : List.of(classesAnnotation.value());
        return classes;
    }


    /**
     * @param testClass
     * @return classes obtained from test's {@link ExcludeClasses} annotation
     */
    protected Set<Class<?>> getExcludedClasses(final Class<?> testClass) {
        final ExcludeClasses excludeClassesAnnotation = testClass.getAnnotation(ExcludeClasses.class);
        final Set<Class<?>> excludedClasses = excludeClassesAnnotation == null ? Set.of()
            : Set.of(excludeClassesAnnotation.value());
        return excludedClasses;
    }


    /**
     * @param context
     * @return paths obtained from test's {@link LocatorFiles} annotation
     */
    protected Set<String> getLocatorFilePaths(final ExtensionContext context) {
        final HashSet<String> paths = new HashSet<>();
        final LocatorFiles locatorFilePaths = context.getRequiredTestClass().getAnnotation(LocatorFiles.class);
        if (locatorFilePaths == null) {
            paths.add("META-INF/hk2-locator/default");
            return paths;
        }
        for (final String path : locatorFilePaths.value()) {
            paths.add(path);
        }
        return paths;
    }


    /**
     * Creates a mock for the interface.
     * All it's methods will throw {@link UnsupportedOperationException}.
     * The service then can be injected in cases when it is required as a dependency, but then unused.
     *
     * @param iface
     */
    protected void addMockDescriptor(final Class<?> iface) {
        addOneDescriptor(locator, mockGenerator.createMockDescriptor(iface));
    }


    private void addConfigFromResource(final ClassLoader loader, final String resourcePath,
        final Class<? extends DomDocument<?>> domClass) {
        URL url = requireNonNull(loader.getResource(resourcePath), "The resourcePath doesn't exist: " + resourcePath);
        DomDocument<?> testDocumentService = requireNonNull(locator.getService(domClass), "service " + domClass);
        ConfigParser configParser = new ConfigParser(locator);
        DomDocument<?> document = configParser.parse(url, testDocumentService);
        addOneConstant(locator, document, null, DomDocument.class, domClass);
    }


    private void addServicesFromLocatorFiles(final ClassLoader loader, final Set<Class<?>> excludedClasses,
        final Set<String> locatorFiles) {
        for (final String locatorFile : locatorFiles) {
            Enumeration<URL> resources;
            try {
                resources = loader.getResources(locatorFile);
            } catch (IOException e) {
                throw new IllegalStateException("Resource could not be loaded: " + locatorFile, e);
            }
            readResources(resources, excludedClasses);
        }
    }


    private void addServicesFromPackage(final List<String> packages, final Set<Class<?>> excludedClasses) {
        if (packages.isEmpty()) {
            return;
        }
        final String classPath = System.getProperty(CLASS_PATH_PROP);
        final StringTokenizer st = new StringTokenizer(classPath, File.pathSeparator);
        while (st.hasMoreTokens()) {
            addServicesFromPathElement(packages, st.nextToken(), excludedClasses);
        }
    }


    private void addServices(final List<Class<?>> classes, final Set<Class<?>> excludedClasses) {
        for (Class<?> clazz : classes) {
            if (excludedClasses.contains(clazz)) {
                continue;
            }
            config.addActiveDescriptor(clazz);
        }
    }


    private void addServicesFromPathElement(final List<String> packages, final String path,
        final Set<Class<?>> excludedClasses) {
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
        final Set<Class<?>> excludedClasses) {
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


    private void addServicesFromPathJar(final List<String> packages, final File jar,
        final Set<Class<?>> excludedClasses) {
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
                        addClassIfService(jarFile.getInputStream(entry), excludedClasses);
                    } catch (final IOException ioe) {
                        // Simply don't add it if we can't read it
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("addServicesFromPathJar failed.", e);
        }
    }


    private void readResources(final Enumeration<URL> resources, final Set<Class<?>> excludedClasses) {
        final Set<String> exclude = excludedClasses.stream().map(Class::getName).collect(Collectors.toSet());
        while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), UTF_8))) {
                while (true) {
                    final DescriptorImpl descriptor = new DescriptorImpl();
                    final boolean goOn = descriptor.readObject(reader);
                    if (!goOn) {
                        break;
                    }
                    if (exclude.contains(descriptor.getImplementation())) {
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


    private void addClassIfService(final InputStream is, final Set<Class<?>> excludedClasses) throws IOException {
        final ClassReader reader = new ClassReader(is);
        final HK2ClasssVisitor cvi = new HK2ClasssVisitor(locator, excludedClasses);
        reader.accept(cvi, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
    }
}
