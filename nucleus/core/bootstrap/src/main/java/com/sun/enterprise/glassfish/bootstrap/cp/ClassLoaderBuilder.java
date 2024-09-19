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
import com.sun.enterprise.glassfish.bootstrap.osgi.OSGiGlassFishRuntimeBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import static org.osgi.framework.Constants.EXPORT_PACKAGE;
import static org.osgi.framework.Constants.FRAMEWORK_SYSTEMPACKAGES;

public class ClassLoaderBuilder {

    private final ClassPathBuilder cpBuilder;
    private final StartupContextCfg cfg;

    private ClassLoaderBuilder(StartupContextCfg cfg) {
        this.cfg = cfg;
        this.cpBuilder = new ClassPathBuilder();
    }

    private void addPlatformDependencies() throws IOException {
        getOsgiPlatformAdapter(cfg).addFrameworkJars(cpBuilder);
    }

    private ClassLoader build(ClassLoader delegate) {
        return cpBuilder.build(delegate);
    }

    private void addLauncherDependencies() throws IOException {
        cpBuilder.addJar(cfg.getFileUnderInstallRoot(Path.of("modules", "glassfish.jar")));
    }

    private void addServerBootstrapDependencies() throws IOException {
        cpBuilder.addJar(cfg.getFileUnderInstallRoot(Path.of("modules", "simple-glassfish-api.jar")));
        cpBuilder.addJar(cfg.getFileUnderInstallRoot(Path.of("lib", "bootstrap", "glassfish-jul-extension.jar")));
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
     * @see #createOSGiFrameworkLauncherCL(StartupContextCfg, ClassLoader)
     * @param delegate Parent class loader for the launcher class loader.
     */
    public static ClassLoader createLauncherCL(StartupContextCfg cfg, ClassLoader delegate) {
        try {
            ClassLoader osgiFWLauncherCL = createOSGiFrameworkLauncherCL(cfg, delegate);
            ClassLoaderBuilder clb = new ClassLoaderBuilder(cfg);
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
     * {@link com.sun.enterprise.glassfish.bootstrap.Launcher} won't be able to see the same copy that's
     * used by rest of the system.
     * tools.jar is needed because its packages, which are exported via system bundle, are consumed by EJBC.
     * This class loader is configured to be the delegate for all bundle class loaders by setting
     * org.osgi.framework.bundle.parent=framework in OSGi configuration. Since this is the delegate for all bundle
     * class loaders, one should be very careful about adding stuff here, as it not only affects performance, it also
     * affects functionality as explained in GlassFish issue 13287.
     *
     * @param delegate Parent class loader for this class loader.
     */
    private static ClassLoader createOSGiFrameworkLauncherCL(StartupContextCfg cfg, ClassLoader delegate) {
        try {
            ClassLoaderBuilder clb = new ClassLoaderBuilder(cfg);
            clb.addPlatformDependencies();
            clb.addServerBootstrapDependencies();
            ClassLoader classLoader = clb.build(delegate);
            String osgiPackages = classLoader.resources("META-INF/MANIFEST.MF").map(ClassLoaderBuilder::loadExports)
                .collect(Collectors.joining(", "));
            // FIXME: This will not be printed anywhere after failure, because logging could not be configured.
//            BOOTSTRAP_LOGGER.log(INFO, "OSGI framework packages:\n{0}", osgiPackages);
            System.err.println("OSGI framework packages:\n" + osgiPackages);
            String javaPackages = detectJavaPackages();
            System.err.println("JDK provided packages:\n" + javaPackages);
            cfg.setProperty(FRAMEWORK_SYSTEMPACKAGES, osgiPackages +  ", " + javaPackages);
            return classLoader;
        } catch (IOException e) {
            throw new Error(e);
        }
    }


    private static OsgiPlatformAdapter getOsgiPlatformAdapter(StartupContextCfg cfg) {
        OsgiPlatformAdapter osgiPlatformAdapter;
        switch (cfg.getPlatform()) {
            case Felix:
                osgiPlatformAdapter = new FelixAdapter(cfg);
                break;
            case Knopflerfish:
                osgiPlatformAdapter = new KnopflerfishAdapter(cfg);
                break;
            case Equinox:
                osgiPlatformAdapter = new EquinoxAdapter(cfg);
                break;
            case Embedded:
            case Static:
                osgiPlatformAdapter = new EmbeddedAdapter();
                break;
            default:
                throw new RuntimeException("Unsupported platform " + cfg.getPlatform());
        }
        return osgiPlatformAdapter;
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
