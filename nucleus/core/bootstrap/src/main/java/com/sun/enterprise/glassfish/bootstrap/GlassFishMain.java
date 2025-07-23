/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.cfg.ServerFiles;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;
import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfgFactory;
import com.sun.enterprise.glassfish.bootstrap.launch.GlassfishOsgiBootstrapClassLoader;
import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Path;
import java.util.Properties;

import org.glassfish.main.jdke.props.EnvToPropsConverter;

import static com.sun.enterprise.glassfish.bootstrap.cp.ClassLoaderBuilder.createOSGiFrameworkLauncherCL;
import static com.sun.enterprise.glassfish.bootstrap.log.LogFacade.BOOTSTRAP_LOGGER;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.util.logging.Level.SEVERE;
import static org.glassfish.embeddable.GlassFishVariable.DOMAINS_ROOT;
import static org.glassfish.embeddable.GlassFishVariable.OSGI_PLATFORM;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * @author Sanjeeb.Sahoo@Sun.COM
 * @author David Matejcek
 */
public class GlassFishMain {

    // logging system may override original output streams.
    private static final PrintStream STDOUT = System.out;

    public static void main(final String[] args) {
        try {
            checkJdkVersion();

            final String platformName = whichPlatform();
            // Set the system property to allow downstream code to know the platform on which GlassFish runs.
            setProperty(OSGI_PLATFORM.getPropertyName(), platformName, true);
            final OsgiPlatform platform = OsgiPlatform.valueOf(platformName);
            STDOUT.println("Launching GlassFish on " + platform + " platform");

            // FIXME: move to serverfiles
            final File installRoot = ServerFiles.detectInstallRoot();
            STDOUT.println("Resolved GlassFish install root: " + installRoot);

            final Properties properties = initProperties(args);
            STDOUT.println("Resolved properties: " + properties);
            final Path instanceRoot = findInstanceRoot(installRoot, properties);
            final ServerFiles files = new ServerFiles(installRoot.toPath(), instanceRoot);
            final StartupContextCfg cfg = StartupContextCfgFactory.createStartupContextCfg(platform, files, properties);
            final ClassLoader osgiCL = createOSGiFrameworkLauncherCL(cfg, getSystemClassLoader());
            try (GlassfishOsgiBootstrapClassLoader launcherCL = new GlassfishOsgiBootstrapClassLoader(installRoot, osgiCL)) {
                launcherCL.launchGlassFishServer(cfg.toProperties());
            }

            // Note: debugging is not possible until the debug port is open.
        } catch (Throwable t) {
            throw new Error("Could not start the server!", t);
        }
    }


    private static void checkJdkVersion() {
        int version = Runtime.version().feature();
        if (version < 17) {
            BOOTSTRAP_LOGGER.log(SEVERE, LogFacade.BOOTSTRAP_INCORRECT_JDKVERSION, new Object[] {17, version});
            System.exit(1);
        }
    }

    private static Properties initProperties(String[] args) {
        Properties map = new Properties();
        if (args.length == 0) {
            return map;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String name = args[i];
            if (name.startsWith("-")) {
                // throw it away if there is no value left
                if (i + 1 < args.length) {
                    map.put(name, args[++i]);
                }
            } else {
                // default --> last one wins!
                map.put("default", args[i]);
            }
        }
        // no sense doing this if we were started by CLI...
        if (!wasStartedByCLI(map)) {
            for (int i = 0; i < args.length; i++) {
                if (i > 0) {
                    sb.append(BootstrapKeys.ARG_SEP);
                }
                sb.append(args[i]);
            }
            map.setProperty(BootstrapKeys.ORIGINAL_ARGS, sb.toString());
            map.setProperty(BootstrapKeys.ORIGINAL_CP, System.getProperty("java.class.path"));
            map.setProperty(BootstrapKeys.ORIGINAL_CN, GlassFishMain.class.getName());
            map.setProperty(BootstrapKeys.ORIGINAL_MP, System.getProperty("jdk.module.path"));
        }
        return map;
    }


    private static boolean wasStartedByCLI(final Properties properties) {
        // if we were started by CLI there will be some special args set...
        return properties.getProperty("-asadmin-classpath") != null
            && properties.getProperty("-asadmin-classname") != null
            && properties.getProperty("-asadmin-args") != null;
    }


    private static String whichPlatform() {
        final String platformSysOption = System.getProperty(OSGI_PLATFORM.getSystemPropertyName());
        if (platformSysOption != null && !platformSysOption.isBlank()) {
            return platformSysOption.trim();
        }
        final String platformEnvOption = System.getenv(OSGI_PLATFORM.getEnvName());
        if (platformEnvOption != null && !platformEnvOption.isBlank()) {
            return platformEnvOption.trim();
        }
        return OsgiPlatform.Felix.name();
    }


    /**
     * IMPORTANT - check for instance BEFORE domain.  We will always come up
     * with a default domain but there is no such thing as a default instance.
     */
    private static Path findInstanceRoot(File installRoot, Properties argsAsProps) {
        File instanceDir = getInstanceRoot(argsAsProps);
        if (instanceDir == null) {
            // that means that this is a DAS.
            instanceDir = getDomainRoot(argsAsProps, installRoot);
        }
        verifyDomainRoot(instanceDir);
        return instanceDir.toPath();
    }


    private static File getInstanceRoot(Properties argsAsProps) {
        String instanceDir = getParam(argsAsProps, "instancedir");
        if (isSet(instanceDir)) {
            return new File(instanceDir);
        }
        return null;
    }


    /**
     * Determines the root directory of the domain that we'll start.
     *
     * @param installRoot
     */
    private static File getDomainRoot(Properties argsAsProps, File installRoot) {
        // first see if it is specified directly
        String domainDir = getParam(argsAsProps, "domaindir");
        if (isSet(domainDir)) {
            return new File(domainDir);
        }

        // now see if they specified the domain name -- we will look in the default domains-dir
        File defDomainsRoot = getDefaultDomainsDir(installRoot);
        String domainName = getParam(argsAsProps, "domain");

        if (isSet(domainName)) {
            return new File(defDomainsRoot, domainName);
        }

        // OK - they specified nothing.  Get the one-and-only domain in the domains-dir
        return getDefaultDomain(defDomainsRoot);
    }


    private static File getDefaultDomainsDir(File installRoot) {
        String envKey = DOMAINS_ROOT.getEnvName();
        String sysKey = DOMAINS_ROOT.getSystemPropertyName();
        File domainsDir = new EnvToPropsConverter(installRoot.toPath()).convert(envKey, sysKey);
        if (domainsDir == null) {
            throw new RuntimeException(
                "Neither " + envKey + " env property nor " + sysKey + " system property is set.");
        }
        if (!domainsDir.isDirectory()) {
            throw new RuntimeException(
                DOMAINS_ROOT.getPropertyName() + "[" + domainsDir + "]" + " is NOT a directory.");
        }
        return domainsDir;
    }


    private static File getDefaultDomain(File domainsDir) {
        File[] domains = domainsDir.listFiles(File::isDirectory);

        // By default we will start an unspecified domain if it is the only
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


    /**
     * Verifies correctness of the root directory of the domain that we'll start.
     *
     * @param domainRoot
     */
    private static void verifyDomainRoot(File domainRoot) {
        if (domainRoot == null) {
            throw new RuntimeException("Internal Error: The domain dir is null.");
        } else if (!domainRoot.exists()) {
            throw new RuntimeException("the domain directory does not exist");
        } else if (!domainRoot.isDirectory()) {
            throw new RuntimeException("the domain directory is not a directory.");
        } else if (!domainRoot.canWrite()) {
            throw new RuntimeException("the domain directory is not writable.");
        } else if (!new File(domainRoot, "config").isDirectory()) {
            throw new RuntimeException("the domain directory is corrupt - there is no config subdirectory.");
        }
    }


    private static String getParam(Properties argsAsProps, String name) {
        // allow both "-" and "--"
        String val = argsAsProps.getProperty("-" + name);
        if (val == null) {
            val = argsAsProps.getProperty("--" + name);
        }
        return val;
    }


    private static boolean isSet(String s) {
        return s != null && !s.isEmpty();
    }
}
