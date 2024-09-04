/*
 * Copyright (c) 2023, 2024 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;
import com.sun.enterprise.glassfish.bootstrap.osgi.impl.OsgiPlatform;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.Which;
import com.sun.enterprise.module.common_impl.AbstractFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.common.util.GlassfishUrlClassLoader;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.spi.RuntimeBuilder;

/**
 * @author bhavanishankar@dev.java.net
 */
// Note: Used in a service file!
public class StaticGlassFishRuntimeBuilder implements RuntimeBuilder {

    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;
    private static final String JAR_EXT = ".jar";
    private static final List<String> MODULE_EXCLUDES = Arrays.asList("jsftemplating.jar", "gf-client-module.jar");

    @Override
    public GlassFishRuntime build(BootstrapProperties bsProps) throws GlassFishException {
        /* Step 1. Build the classloader. */
        // The classloader should contain installRoot/modules/**/*.jar files.
        String installRoot = getInstallRoot(bsProps);
        if (installRoot != null) {
            System.setProperty("org.glassfish.embeddable.installRoot", installRoot);
        }
        // Required to add moduleJarURLs to support 'java -jar modules/glassfish.jar case'
        List<URL> moduleJarURLs = getModuleJarURLs(installRoot);
        ClassLoader cl = getClass().getClassLoader();
        if (!moduleJarURLs.isEmpty()) {
            cl = new StaticClassLoader(getClass().getClassLoader(), moduleJarURLs);
        }

        // Step 2. Setup the module subsystem.
        Main main = new EmbeddedMain(cl);
        SingleHK2Factory.initialize(cl);
        ModulesRegistry modulesRegistry = AbstractFactory.getInstance().createModulesRegistry();
        modulesRegistry.setParentClassLoader(cl);

        // Step 3. Create NonOSGIGlassFishRuntime
        GlassFishRuntime glassFishRuntime = new StaticGlassFishRuntime(main);
        LOG.logp(Level.FINER, getClass().getName(), "build",
                "Created GlassFishRuntime {0} with InstallRoot {1}, Bootstrap Options {2}",
                new Object[]{glassFishRuntime, installRoot, bsProps});
        return glassFishRuntime;
    }

    @Override
    public boolean handles(BootstrapProperties bsProps) {
        // See GLASSFISH-16743 for the reason behind additional check
        final String builderName = bsProps.getProperty(BootstrapKeys.BUILDER_NAME_PROPERTY);
        if (builderName != null && !builderName.equals(getClass().getName())) {
            return false;
        }
        String platform = bsProps.getProperty(BootstrapKeys.PLATFORM_PROPERTY_KEY);
        return platform == null || OsgiPlatform.Static.toString().equalsIgnoreCase(platform);
    }

    private String getInstallRoot(BootstrapProperties props) {
        String installRootProp = props.getInstallRoot();
        if (installRootProp == null) {
            File installRoot = GlassFishMain.getInstallRoot();
            if (isValidInstallRoot(installRoot)) {
                installRootProp = installRoot.getAbsolutePath();
            }
        }
        return installRootProp;
    }

    private boolean isValidInstallRoot(File installRoot) {
        return installRoot == null ? false : isValidInstallRoot(installRoot.getAbsolutePath());
    }

    private List<URL> getModuleJarURLs(String installRoot) {
        if(installRoot == null) {
            return new ArrayList<>();
        }
        JarFile jarfile = null;
        try {
            // When running off the uber jar don't add extras module URLs to classpath.
            jarfile = new JarFile(Which.jarFile(getClass()));
            String mainClassName = jarfile.getManifest().
                    getMainAttributes().getValue("Main-Class");
            if (UberMain.class.getName().equals(mainClassName)) {
                return new ArrayList<>();
            }
        } catch (Exception ex) {
            LOG.log(Level.WARNING, LogFacade.CAUGHT_EXCEPTION, ex);
        } finally {
            if (jarfile != null) {
                try {
                    jarfile.close();
                } catch (IOException ex) {
                    // ignored
                }
            }
        }

        File modulesDir = new File(installRoot, "modules/");
        final File autostartModulesDir = new File(modulesDir, "autostart/");
        final List<URL> moduleJarURLs = new ArrayList<>();
        modulesDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if (pathname.isDirectory() && !pathname.equals(autostartModulesDir)) {
                    pathname.listFiles(this);
                } else if (pathname.getName().endsWith(JAR_EXT) && !MODULE_EXCLUDES.contains(pathname.getName())) {
                    try {
                        moduleJarURLs.add(pathname.toURI().toURL());
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, LogFacade.CAUGHT_EXCEPTION, ex);
                    }
                }
                return false;
            }
        });
        return moduleJarURLs;
    }

    private boolean isValidInstallRoot(String installRootPath) {
        if(installRootPath == null || !new File(installRootPath).exists()) {
            return false;
        }
        if(!new File(installRootPath, "modules").exists()) {
            return false;
        }
        if(!new File(installRootPath, "lib/dtds").exists()) {
            return false;
        }
        return true;
    }

    private static class StaticClassLoader extends GlassfishUrlClassLoader {

        StaticClassLoader(ClassLoader parent, List<URL> moduleJarURLs) {
            super(moduleJarURLs.toArray(new URL[moduleJarURLs.size()]), parent);
        }
    }

}
