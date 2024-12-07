/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap.cp;

import com.sun.enterprise.glassfish.bootstrap.cfg.StartupContextCfg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES;

public class ClassLoaderBuilder {

    /**
     * This method is responsible for setting up the what we call "OSGi framework launcher class loader." It has
     * the following classes/jars in its search path:
     *  - OSGi framework classes,
     *  - GlassFish bootstrap apis (simple-glassfish-api.jar)
     *  - jdk tools.jar classpath.
     * OSGi framework classes are there because we want to launch the framework.
     * simple-glassfish-api.jar is needed, because we need those classes higher up in the class loader chain otherwise
     * {@link com.sun.enterprise.glassfish.bootstrap.launch.Launcher} won't be able to see the same copy that's
     * used by rest of the system.
     * tools.jar is needed because its packages, which are exported via system bundle, are consumed by EJBC.
     * This class loader is configured to be the delegate for all bundle class loaders by setting
     * org.osgi.framework.bundle.parent=framework in OSGi configuration. Since this is the delegate for all bundle
     * class loaders, one should be very careful about adding stuff here, as it not only affects performance, it also
     * affects functionality as explained in GlassFish issue 13287.
     *
     * @param parent Parent class loader for this class loader.
     * @throws IOException
     */
    public static ClassLoader createOSGiFrameworkLauncherCL(StartupContextCfg cfg, ClassLoader parent)
        throws IOException {
        ClassLoader classLoader = getOsgiPlatformAdapter(cfg).addFrameworkJars(new ClassPathBuilder()).build(parent);
        String osgiPackages = classLoader.resources("META-INF/MANIFEST.MF").map(ClassLoaderBuilder::loadExports)
            .collect(Collectors.joining(", "));
        // FIXME: This will not be printed anywhere after failure, because logging could not be configured.
        // BOOTSTRAP_LOGGER.log(INFO, "OSGI framework packages:\n{0}", osgiPackages);
        System.err.println("OSGI framework packages:\n" + osgiPackages);
        String javaPackages = detectJavaPackages();
        System.err.println("JDK provided packages:\n" + javaPackages);
        cfg.setProperty(FRAMEWORK_SYSTEMPACKAGES, osgiPackages + ", " + javaPackages);
        return classLoader;
    }


    private static OsgiPlatformAdapter getOsgiPlatformAdapter(StartupContextCfg cfg) {
        switch (cfg.getPlatform()) {
            case Felix:
                return new FelixAdapter(cfg);
            case Knopflerfish:
                return new KnopflerfishAdapter(cfg);
            case Equinox:
                return new EquinoxAdapter(cfg);
            case Embedded:
            case Static:
                return new EmbeddedAdapter();
            default:
                throw new RuntimeException("Unsupported platform " + cfg.getPlatform());
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
}
