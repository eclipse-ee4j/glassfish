/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.glassfish.bootstrap;

import com.sun.enterprise.glassfish.bootstrap.osgi.OSGiGlassFishRuntimeBuilder;
import com.sun.enterprise.module.bootstrap.ArgumentManager;
import com.sun.enterprise.module.bootstrap.StartupContext;
import com.sun.enterprise.module.bootstrap.Which;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.LineNumberReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

// WARNING: There must not be any references to java.util.logging.* instances in static initialization except levels.
//          They would cause initialization of the JVM logging which we do AFTER this.
import static com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.Constants.INSTALL_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.Constants.INSTANCE_ROOT_URI_PROP_NAME;
import static com.sun.enterprise.glassfish.bootstrap.Constants.PLATFORM_PROPERTY_KEY;
import static com.sun.enterprise.glassfish.bootstrap.LogFacade.BOOTSTRAP_LOGGER;
import static com.sun.enterprise.module.bootstrap.ArgumentManager.argsToMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.FRAMEWORK_STORAGE;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES;

/**
 * Utility class used by bootstrap module.
 * Most of the code is moved from {@link ASMain} or {@link GlassFishMain}to this class to keep them
 * as small as possible and to improve reusability when GlassFish is launched in other modes (e.g., karaf).
 *
 * @author Sanjeeb.Sahoo@Sun.COM
 */
public class MainHelper {

    static void checkJdkVersion() {
        int version = Runtime.version().feature();
        if (version < 11) {
            BOOTSTRAP_LOGGER.log(SEVERE, LogFacade.BOOTSTRAP_INCORRECT_JDKVERSION, new Object[] {11, version});
            System.exit(1);
        }
    }

    static String whichPlatform() {
        final String platformSysOption = System.getProperty(PLATFORM_PROPERTY_KEY);
        if (platformSysOption != null && !platformSysOption.isBlank()) {
            return platformSysOption.trim();
        }
        final String platformEnvOption = System.getenv(PLATFORM_PROPERTY_KEY);
        if (platformEnvOption != null && !platformEnvOption.isBlank()) {
            return platformEnvOption.trim();
        }
        return Constants.Platform.Felix.toString();
    }

    public static Properties parseAsEnv(File installRoot) {

        Properties asenvProps = new Properties();

        // let's read the asenv.conf
        File configDir = new File(installRoot, "config");
        File asenv = getAsEnvConf(configDir);

        if (!asenv.exists()) {
            BOOTSTRAP_LOGGER.log(FINE, "{0} not found, ignoring", asenv.getAbsolutePath());
            return asenvProps;
        }
        try (LineNumberReader lnReader = new LineNumberReader(new FileReader(asenv))) {
            String line = lnReader.readLine();
            // most of the asenv.conf values have surrounding "", remove them
            // and on Windows, they start with SET XXX=YYY
            Pattern p = Pattern.compile("(?i)(set +)?([^=]*)=\"?([^\"]*)\"?");
            while (line != null) {
                Matcher m = p.matcher(line);
                if (m.matches()) {
                    File f = new File(m.group(3));
                    if (!f.isAbsolute()) {
                        f = new File(configDir, m.group(3));
                        if (f.exists()) {
                            asenvProps.put(m.group(2), f.getAbsolutePath());
                        } else {
                            asenvProps.put(m.group(2), m.group(3));
                        }
                    } else {
                        asenvProps.put(m.group(2), m.group(3));
                    }
                }
                line = lnReader.readLine();
            }
            return asenvProps;
        } catch (IOException ioe) {
            throw new RuntimeException("Error opening asenv.conf : ", ioe);
        }
    }

    void addPaths(File dir, String[] jarPrefixes, List<URL> urls) throws MalformedURLException {
        File[] jars = dir.listFiles();
        if (jars == null) {
            return;
        }
        for (File f : jars) {
            for (String prefix : jarPrefixes) {
                String name = f.getName();
                if (name.startsWith(prefix) && name.endsWith(".jar")) {
                    urls.add(f.toURI().toURL());
                }
            }
        }
    }

    /**
     * Figures out the asenv.conf file to load.
     */
    private static File getAsEnvConf(File configDir) {
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            return new File(configDir, "asenv.bat");
        }
        return new File(configDir, "asenv.conf");
    }

    /**
     * Determines the root directory of the domain that we'll start.
     */
    static File getDomainRoot(Properties args, Properties asEnv) {
        // first see if it is specified directly

        String domainDir = getParam(args, "domaindir");

        if (isSet(domainDir)) {
            return new File(domainDir);
        }

        // now see if they specified the domain name -- we will look in the
        // default domains-dir

        File defDomainsRoot = getDefaultDomainsDir(asEnv);
        String domainName = getParam(args, "domain");

        if (isSet(domainName)) {
            return new File(defDomainsRoot, domainName);
        }

        // OK -- they specified nothing.  Get the one-and-only domain in the
        // domains-dir
        return getDefaultDomain(defDomainsRoot);
    }

    /**
     * Verifies correctness of the root directory of the domain that we'll start and
     * sets the system property called {@link com.sun.enterprise.glassfish.bootstrap.Constants#INSTANCE_ROOT_PROP_NAME}.
     */
    void verifyAndSetDomainRoot(File domainRoot) {
        verifyDomainRoot(domainRoot);
        System.setProperty(INSTANCE_ROOT_PROP_NAME, absolutize(domainRoot).getPath());
    }

    /**
     * Verifies correctness of the root directory of the domain that we'll start.
     *
     * @param domainRoot
     */
    static void verifyDomainRoot(File domainRoot) {
        String msg = null;

        if (domainRoot == null) {
            msg = "Internal Error: The domain dir is null.";
        } else if (!domainRoot.exists()) {
            msg = "the domain directory does not exist";
        } else if (!domainRoot.isDirectory()) {
            msg = "the domain directory is not a directory.";
        } else if (!domainRoot.canWrite()) {
            msg = "the domain directory is not writable.";
        } else if (!new File(domainRoot, "config").isDirectory()) {
            msg = "the domain directory is corrupt - there is no config subdirectory.";
        }

        if (msg != null) {
            throw new RuntimeException(msg);
        }
    }

    private static File getDefaultDomainsDir(Properties asEnv) {
        // note: 99% error detection!
        String dirname = asEnv.getProperty(Constants.DEFAULT_DOMAINS_DIR_PROPNAME);
        if (!isSet(dirname)) {
            throw new RuntimeException(Constants.DEFAULT_DOMAINS_DIR_PROPNAME + " is not set.");
        }

        File domainsDir = absolutize(new File(dirname));
        if (!domainsDir.isDirectory()) {
            throw new RuntimeException(Constants.DEFAULT_DOMAINS_DIR_PROPNAME + "[" + dirname + "]"
                + " is specifying a file that is NOT a directory.");
        }
        return domainsDir;
    }


    private static File getDefaultDomain(File domainsDir) {
        File[] domains = domainsDir.listFiles(File::isDirectory);

        // By default we will start an unspecified domain iff it is the only
        // domain in the default domains dir

        if (domains == null || domains.length == 0) {
            throw new RuntimeException("no domain directories found under " + domainsDir);
        }

        if (domains.length > 1) {
            throw new RuntimeException("Multiple domains[" + domains.length + "] found under "
                    + domainsDir + " -- you must specify a domain name as -domain <name>");
        }

        return domains[0];
    }


    private static boolean isSet(String s) {
        return s != null && !s.isEmpty();
    }

    private static String getParam(Properties map, String name) {
        // allow both "-" and "--"
        String val = map.getProperty("-" + name);
        if (val == null) {
            val = map.getProperty("--" + name);
        }
        return val;
    }

    private static File absolutize(File f) {
        try {
            return f.getCanonicalFile();
        } catch (Exception e) {
            return f.getAbsoluteFile();
        }
    }

    /**
     * CLI or any other client needs to ALWAYS pass in the instanceDir for
     * instances.
     *
     * @param args
     * @param asEnv
     * @return
     */
    static File getInstanceRoot(Properties args, Properties asEnv) {
        String instanceDir = getParam(args, "instancedir");
        if (isSet(instanceDir)) {
            return new File(instanceDir);
        }
        return null;
    }

    static File findInstallRoot() {
        // glassfish/modules/glassfish.jar
        File bootstrapFile = findBootstrapFile();
        // glassfish/
        return bootstrapFile.getParentFile().getParentFile();
    }

    static File findInstanceRoot(File installRoot, Properties args) {
        Properties asEnv = parseAsEnv(installRoot);

        // IMPORTANT - check for instance BEFORE domain.  We will always come up
        // with a default domain but there is no such thing sa a default instance

        File instanceDir = getInstanceRoot(args, asEnv);
        if (instanceDir == null) {
            // that means that this is a DAS.
            instanceDir = getDomainRoot(args, asEnv);
        }
        verifyDomainRoot(instanceDir);
        return instanceDir;
    }

    static File findInstanceRoot(File installRoot, String[] args) {
        return findInstanceRoot(installRoot, ArgumentManager.argsToMap(args));
    }

    private static File findBootstrapFile() {
        try {
            return Which.jarFile(ASMain.class);
        } catch (IOException e) {
            throw new RuntimeException("Cannot get bootstrap path from " + ASMain.class + " class location, aborting");
        }
    }

    static Properties buildStartupContext(String platform, File installRoot, File instanceRoot, String[] args) {
        Properties ctx = argsToMap(args);
        ctx.setProperty(StartupContext.TIME_ZERO_NAME, Long.toString(System.currentTimeMillis()));

        ctx.setProperty(PLATFORM_PROPERTY_KEY, platform);

        ctx.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
        ctx.setProperty(INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());

        ctx.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
        ctx.setProperty(INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());

        if (ctx.getProperty(StartupContext.STARTUP_MODULE_NAME) == null) {
            ctx.setProperty(StartupContext.STARTUP_MODULE_NAME, Constants.GF_KERNEL);
        }

        // temporary hack until CLI does that for us.
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-upgrade")) {
                if (i + 1 < args.length && !args[i + 1].equals("false")) {
                    ctx.setProperty(StartupContext.STARTUP_MODULESTARTUP_NAME, "upgrade");
                }
            }
        }

        addRawStartupInfo(args, ctx);

        mergePlatformConfiguration(ctx);
        return ctx;
    }

    public static Properties buildStaticStartupContext(long timeZero, String... args) {
        checkJdkVersion();
        Properties ctx = argsToMap(args);
        ctx.setProperty(PLATFORM_PROPERTY_KEY, "Static");
        buildStartupContext(ctx);
        addRawStartupInfo(args, ctx);

        // Reset time zero with the real value. We can't do this before buildStartupContext()
        // because it has an optimization that is triggered by this key.
        ctx.setProperty(StartupContext.TIME_ZERO_NAME, Long.toString(timeZero));

        // Config variable substitution (and maybe other downstream code) relies on
        // some values in System properties, so copy them in.
        for (String key : new String[]{INSTALL_ROOT_PROP_NAME,
                                       INSTANCE_ROOT_URI_PROP_NAME,
                                       INSTALL_ROOT_PROP_NAME,
                                       INSTALL_ROOT_URI_PROP_NAME}) {
            System.setProperty(key, ctx.getProperty(key));
        }
        return ctx;
    }

    public static void buildStartupContext(Properties ctx) {
        if (ctx.getProperty(StartupContext.TIME_ZERO_NAME) == null) {
            ctx.setProperty(StartupContext.TIME_ZERO_NAME, Long.toString(System.currentTimeMillis()));
        } else {
            // Optimisation
            // Skip the rest of the code. We assume that we are called from GlassFishMain
            // which already passes a properly populated properties object.
            return;
        }

        if (ctx.getProperty(PLATFORM_PROPERTY_KEY) == null) {
            ctx.setProperty(PLATFORM_PROPERTY_KEY, Constants.Platform.Felix.name());
        }

        if (ctx.getProperty(INSTALL_ROOT_PROP_NAME) == null) {
            File installRoot = findInstallRoot();
            ctx.setProperty(INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
            ctx.setProperty(INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
        }

        if (ctx.getProperty(INSTANCE_ROOT_PROP_NAME) == null) {
            File installRoot = new File(ctx.getProperty(INSTALL_ROOT_PROP_NAME));
            File instanceRoot = findInstanceRoot(installRoot, ctx);
            ctx.setProperty(INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
            ctx.setProperty(INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());
        }

        if (ctx.getProperty(StartupContext.STARTUP_MODULE_NAME) == null) {
            ctx.setProperty(StartupContext.STARTUP_MODULE_NAME, Constants.GF_KERNEL);
        }

        if (!ctx.contains(Constants.NO_FORCED_SHUTDOWN)) {
            // Since we are in non-embedded mode, we set this property to false unless user has specified it
            // When set to false, the VM will exit when server fails to startup for whatever reason.
            // See AppServerStartup.java
            ctx.setProperty(Constants.NO_FORCED_SHUTDOWN, Boolean.FALSE.toString());
        }
        mergePlatformConfiguration(ctx);
    }

    /**
     * Need the raw unprocessed args for RestartDomainCommand in case we were NOT started
     * by CLI
     *
     * @param args raw args to this main()
     * @param p    the properties to save as a system property
     */
    private static void addRawStartupInfo(final String[] args, final Properties p) {
        //package the args...
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                sb.append(Constants.ARG_SEP);
            }

            sb.append(args[i]);
        }

        if (!wasStartedByCLI(p)) {
            // no sense doing this if we were started by CLI...
            p.put(Constants.ORIGINAL_CP, System.getProperty("java.class.path"));
            p.put(Constants.ORIGINAL_CN, ASMain.class.getName());
            p.put(Constants.ORIGINAL_ARGS, sb.toString());
        }
    }

    private static boolean wasStartedByCLI(final Properties props) {
        // if we were started by CLI there will be some special args set...
        return props.getProperty("-asadmin-classpath") != null
            && props.getProperty("-asadmin-classname") != null
            && props.getProperty("-asadmin-args") != null;
    }

    /**
     * This method is responsible setting up launcher class loader which is then used while calling
     * {@link org.glassfish.embeddable.GlassFishRuntime#bootstrap(org.glassfish.embeddable.BootstrapProperties, ClassLoader)}.
     *
     * This launcher class loader's delegation hierarchy looks like this:
     * launcher class loader
     *       -> OSGi framework launcher class loader
     *             -> extension class loader
     *                   -> null (bootstrap loader)
     * We first create what we call "OSGi framework launcher class loader," that has
     * classes that we want to be visible via system bundle.
     * Then we create launcher class loader which has {@link OSGiGlassFishRuntimeBuilder} and its dependencies in
     * its search path. We set the former one as the parent of this, there by sharing the same copy of
     * GlassFish API classes and also making OSGi classes visible to OSGiGlassFishRuntimeBuilder.
     *
     * We could have merged all the jars into one class loader and called it the launcher class loader, but
     * then such a loader, when set as the bundle parent loader for all OSGi classloading delegations, would make
     * more things visible than desired. Please note, glassfish.jar has a very long dependency chain. See
     * glassfish issue 13287 for the kinds of problems it can create.
     *
     * @see #createOSGiFrameworkLauncherCL(java.util.Properties, ClassLoader)
     * @param delegate Parent class loader for the launcher class loader.
     */
    static ClassLoader createLauncherCL(Properties ctx, ClassLoader delegate) {
        try {
            ClassLoader osgiFWLauncherCL = createOSGiFrameworkLauncherCL(ctx, delegate);
            ClassLoaderBuilder clb = new ClassLoaderBuilder(ctx);
            clb.addLauncherDependencies();
            return clb.build(osgiFWLauncherCL);
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    /**
     * This method is responsible for setting up the what we call "OSGi framework launcher class loader." It has
     * the following classes/jars in its search path:
     *  - OSGi framework classes,
     *  - GlassFish bootstrap apis (simple-glassfish-api.jar)
     *  - jdk tools.jar classpath.
     * OSGi framework classes are there because we want to launch the framework.
     * simple-glassfish-api.jar is needed, because we need those classes higher up in the class loader chain otherwise
     * {@link com.sun.enterprise.glassfish.bootstrap.GlassFishMain.Launcher} won't be able to see the same copy that's
     * used by rest of the system.
     * tools.jar is needed because its packages, which are exported via system bundle, are consumed by EJBC.
     * This class loader is configured to be the delegate for all bundle class loaders by setting
     * org.osgi.framework.bundle.parent=framework in OSGi configuration. Since this is the delegate for all bundle
     * class loaders, one should be very careful about adding stuff here, as it not only affects performance, it also
     * affects functionality as explained in GlassFish issue 13287.
     *
     * @param delegate Parent class loader for this class loader.
     */
    private static ClassLoader createOSGiFrameworkLauncherCL(Properties ctx, ClassLoader delegate) {
        try {
            ClassLoaderBuilder clb = new ClassLoaderBuilder(ctx);
            clb.addPlatformDependencies();
            clb.addServerBootstrapDependencies();
            ClassLoader classLoader = clb.build(delegate);
            String osgiPackages = classLoader.resources("META-INF/MANIFEST.MF").map(MainHelper::loadExports)
                .collect(Collectors.joining(", "));
            // FIXME: This will not be printed anywhere after failure, because logging could not be configured.
//            BOOTSTRAP_LOGGER.log(INFO, "OSGI framework packages:\n{0}", osgiPackages);
            System.err.println("OSGI framework packages:\n" + osgiPackages);
            String javaPackages = detectJavaPackages();
            System.err.println("JDK provided packages:\n" + javaPackages);
            ctx.setProperty(FRAMEWORK_SYSTEMPACKAGES, osgiPackages +  ", " + javaPackages);
            return classLoader;
        } catch (IOException e) {
            throw new Error(e);
        }
    }


    private static String detectJavaPackages() {
        Set<String> packages = new HashSet<>();
        for (Module module : ModuleLayer.boot().modules()) {
            addAllExportedPackages(module, packages);
        }
        return packages.stream().sorted().collect(Collectors.joining(", "));
    }


    private static void addAllExportedPackages(Module module, Set<String> packages) {
        for (String pkg : module.getPackages()) {
            if (module.isExported(pkg) || module.isOpen(pkg)) {
                packages.add(pkg);
            }
        }
    }


    private static String loadExports(final URL url) {
        try (InputStream is = url.openStream()) {
            Manifest manifest = new Manifest(is);
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue(EXPORT_PACKAGE);
        } catch (IOException e) {
            throw new IllegalStateException("Could not parse manifest from " + url, e);
        }
    }


    /**
     * Store relevant information in system properties.
     *
     * @param ctx
     */
    static void setSystemProperties(Properties ctx) {
        // Set the system property if downstream code wants to know about it
        System.setProperty(PLATFORM_PROPERTY_KEY, ctx.getProperty(PLATFORM_PROPERTY_KEY));
    }

    static void mergePlatformConfiguration(Properties ctx) {
        final Properties osgiConf;
        try {
            osgiConf = PlatformHelper.getPlatformHelper(ctx).readPlatformConfiguration();
        } catch (IOException e) {
            throw new IllegalStateException("The OSGI configuration could not be loaded!", e);
        }
        osgiConf.putAll(ctx);
        Util.substVars(osgiConf);
        // Starting with GlassFish 3.1.2, we allow user to overrride values specified in OSGi config file by
        // corresponding values as set via System properties. There are two properties that we must always read
        // from OSGi config file. They are felix.fileinstall.dir and felix.fileinstall.log.level, as their values have
        // changed incompatibly from 3.1 to 3.1.1, but we are not able to change domain.xml in 3.1.1 for
        // compatibility reasons.
        Util.overrideBySystemProps(osgiConf, Arrays.asList("felix.fileinstall.dir", "felix.fileinstall.log.level"));
        ctx.clear();
        ctx.putAll(osgiConf);
    }

    static boolean isOSGiPlatform(String platform) {
        Constants.Platform p = Constants.Platform.valueOf(platform);
        switch (p) {
            case Felix:
            case Knopflerfish:
            case Equinox:
                return true;
            case Static:
            default:
                return false;
        }
    }

    static class ClassLoaderBuilder {

        private final ClassPathBuilder cpb;
        private final File glassfishDir;
        private final Properties ctx;

        ClassLoaderBuilder(Properties ctx) {
            this.ctx = ctx;
            cpb = new ClassPathBuilder();
            glassfishDir = new File(ctx.getProperty(INSTALL_ROOT_PROP_NAME));
        }

        void addPlatformDependencies() throws IOException {
            PlatformHelper.getPlatformHelper(ctx).addFrameworkJars(cpb);
        }

        /**
         * Adds JDK tools.jar to classpath.
         */
        void addJDKToolsJar() {
            File jdkToolsJar = Util.getJDKToolsJar();
            try {
                cpb.addJar(jdkToolsJar);
            } catch (IOException ioe) {
                // on the mac, it happens all the time
                BOOTSTRAP_LOGGER.log(FINE, "JDK tools.jar does not exist at {0}", jdkToolsJar);
            }
        }

        public ClassLoader build(ClassLoader delegate) {
            return cpb.create(delegate);
        }

        public void addLauncherDependencies() throws IOException {
            cpb.addJar(new File(glassfishDir, "modules/glassfish.jar"));
        }

        public void addServerBootstrapDependencies() throws IOException {
            cpb.addJar(new File(glassfishDir, "modules/simple-glassfish-api.jar"));
            cpb.addJar(new File(glassfishDir, "lib/bootstrap/glassfish-jul-extension.jar"));
        }
    }

    static abstract class PlatformHelper {
        /**
         * Location of the unified config properties file relative to the domain directory
         */
        public static final String CONFIG_PROPERTIES = "config/osgi.properties";

        protected Properties properties;
        protected File glassfishDir;
        protected File domainDir;
        protected File fwDir;

        static synchronized PlatformHelper getPlatformHelper(Properties properties) {
            Constants.Platform platform = Constants.Platform
                .valueOf(properties.getProperty(PLATFORM_PROPERTY_KEY));
            PlatformHelper platformHelper;
            switch (platform) {
                case Felix:
                    platformHelper = new FelixHelper();
                    break;
                case Knopflerfish:
                    platformHelper = new KnopflerfishHelper();
                    break;
                case Equinox:
                    platformHelper = new EquinoxHelper();
                    break;
                case Static:
                    platformHelper = new StaticHelper();
                    break;
                default:
                    throw new RuntimeException("Unsupported platform " + platform);
            }
            platformHelper.init(properties);
            return platformHelper;
        }


        /**
         * @param properties Initial properties
         */
        void init(Properties properties) {
            this.properties = properties;
            glassfishDir = StartupContextUtil.getInstallRoot(properties);
            domainDir = StartupContextUtil.getInstanceRoot(properties);
            setFwDir();
        }

        protected abstract void setFwDir();

        /**
         * Adds the jar files of the OSGi platform to the given {@link ClassPathBuilder}
         */
        protected abstract void addFrameworkJars(ClassPathBuilder cpb) throws IOException;

        /**
         * @return platform specific configuration information
         * @throws IOException if the configuration could not be loaded
         */
        protected Properties readPlatformConfiguration() throws IOException {
            Properties platformConfig = new Properties();
            final File configFile = getFrameworkConfigFile();
            if (configFile == null) {
                return platformConfig;
            }
            try (InputStream in = new FileInputStream(configFile)) {
                platformConfig.load(in);
            }
            return platformConfig;
        }


        protected File getFrameworkConfigFile() {
            String fileName = CONFIG_PROPERTIES;
            // First we search in domainDir. If it's not found there, we fall back on installDir
            File f = new File(domainDir, fileName);
            if (f.exists()) {
                BOOTSTRAP_LOGGER.log(INFO, LogFacade.BOOTSTRAP_FMWCONF, f.getAbsolutePath());
            } else {
                f = new File(glassfishDir, fileName);
            }
            return f;
        }
    }

    static class FelixHelper extends PlatformHelper {
        private static final String FELIX_HOME = "FELIX_HOME";

        /**
         * Home of FW installation relative to Glassfish root installation.
         */
        public static final String GF_FELIX_HOME = "osgi/felix";

        /**
         * Location of the config properties file relative to the domain directory
         */
        public static final String CONFIG_PROPERTIES = "config/osgi.properties";

        @Override
        protected void setFwDir() {
            String fwPath = System.getenv(FELIX_HOME);
            if (fwPath == null) {
                // try system property, which comes from asenv.conf
                fwPath = System.getProperty(FELIX_HOME, new File(glassfishDir, GF_FELIX_HOME).getAbsolutePath());
            }
            fwDir = new File(fwPath);
            if (!fwDir.exists()) {
                throw new RuntimeException("Can't locate Felix at " + fwPath);
            }
        }

        @Override
        protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
            File felixJar = new File(fwDir, "bin/felix.jar");
            cpb.addJar(felixJar);
        }

        @Override
        protected Properties readPlatformConfiguration() throws IOException {
            // GlassFish filesystem layout does not recommend use of upper case char in file names.
            // So, we can't use ${GlassFish_Platform} to generically set the cache dir.
            // Hence, we set it here.
            Properties platformConfig = super.readPlatformConfiguration();
            platformConfig.setProperty(FRAMEWORK_STORAGE, new File(domainDir, "osgi-cache/felix/").getAbsolutePath());
            return platformConfig;
        }
    }

    static class EquinoxHelper extends PlatformHelper {

        /**
         * If equinox is installed under glassfish/eclipse this would be the
         * glassfish/eclipse/plugins dir that contains the equinox jars can be null
         */
        private static File pluginsDir;

        @Override
        protected void setFwDir() {
            String fwPath = System.getenv("EQUINOX_HOME");
            if (fwPath == null) {
                fwPath = new File(glassfishDir, "osgi/equinox").getAbsolutePath();
            }
            fwDir = new File(fwPath);
            if (!fwDir.exists()) {
                throw new RuntimeException("Can't locate Equinox at " + fwPath);
            }
        }

        @Override
        protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
            // Add all the jars to classpath for the moment, since the jar name
            // is not a constant.
            if (pluginsDir == null) {
                cpb.addJarFolder(fwDir);
            } else {
                cpb.addGlob(pluginsDir, "org.eclipse.osgi_*.jar");
            }
        }

        @Override
        protected Properties readPlatformConfiguration() throws IOException {
            // GlassFish filesystem layout does not recommend use of upper case char in file names.
            // So, we can't use ${GlassFish_Platform} to generically set the cache dir.
            // Hence, we set it here.
            Properties platformConfig = super.readPlatformConfiguration();
            platformConfig.setProperty(FRAMEWORK_STORAGE, new File(domainDir, "osgi-cache/equinox/").getAbsolutePath());
            return platformConfig;
        }
    }

    static class KnopflerfishHelper extends PlatformHelper {

        private static final String KF_HOME = "KNOPFLERFISH_HOME";

        /**
         * Home of fw installation relative to Glassfish root installation.
         */
        public static final String GF_KF_HOME = "osgi/knopflerfish.org/osgi/";

        @Override
        protected void setFwDir() {
            String fwPath = System.getenv(KF_HOME);
            if (fwPath == null) {
                fwPath = new File(glassfishDir, GF_KF_HOME).getAbsolutePath();
            }
            fwDir = new File(fwPath);
            if (!fwDir.exists()) {
                throw new RuntimeException("Can't locate KnopflerFish at " + fwPath);
            }
        }

        @Override
        protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
            cpb.addJar(new File(fwDir, "framework.jar"));
        }

        @Override
        protected Properties readPlatformConfiguration() throws IOException {
            // GlassFish filesystem layout does not recommend use of upper case char in file names.
            // So, we can't use ${GlassFish_Platform} to generically set the cache dir.
            // Hence, we set it here.
            Properties platformConfig = super.readPlatformConfiguration();
            platformConfig.setProperty(FRAMEWORK_STORAGE,
                new File(domainDir, "osgi-cache/knopflerfish/").getAbsolutePath());
            return platformConfig;
        }
    }

    static class StaticHelper extends PlatformHelper {
        @Override
        protected void setFwDir() {
            // nothing to do
        }

        @Override
        protected void addFrameworkJars(ClassPathBuilder cpb) throws IOException {
            // nothing to do
        }

        @Override
        protected File getFrameworkConfigFile() {
            // no config file for this platform.
            return null;
        }
    }
}

