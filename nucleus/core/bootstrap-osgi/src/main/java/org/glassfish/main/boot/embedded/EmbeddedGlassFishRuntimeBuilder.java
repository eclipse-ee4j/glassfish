/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.main.boot.embedded;

import com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys;
import com.sun.enterprise.glassfish.bootstrap.cfg.OsgiPlatform;
import com.sun.enterprise.glassfish.bootstrap.cfg.ServerFiles;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.AbstractFactory;

import java.io.File;
import java.io.FileFilter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.embeddable.spi.RuntimeBuilder;
import org.glassfish.main.boot.log.LogFacade;
import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;

import static org.glassfish.embeddable.GlassFishVariable.OSGI_PLATFORM;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * @author bhavanishankar@dev.java.net
 */
// Note: Used in a service file!
public class EmbeddedGlassFishRuntimeBuilder implements RuntimeBuilder {

    private static final Logger LOG = LogFacade.BOOTSTRAP_LOGGER;
    private static final String JAR_EXT = ".jar";
    private static final Set<String> MODULE_EXCLUDES = Set.of("jsftemplating.jar", "gf-client-module.jar");
    public static boolean addModuleJars = true;

    @Override
    public GlassFishRuntime build(BootstrapProperties bsProps) throws GlassFishException {
        String installRoot = getInstallRoot(bsProps);
        // Required to add moduleJarURLs to support 'java -jar glassfish.jar case'
        List<URL> moduleJarURLs = getModuleJarURLs(installRoot);
        final ClassLoader cl;
        if (moduleJarURLs.isEmpty()) {
            cl = getClass().getClassLoader();
        } else {
            cl = new EmbeddedClassLoader(getClass().getClassLoader(), moduleJarURLs);
        }
        return build(bsProps, cl, installRoot);
    }

    @Override
    public GlassFishRuntime build(BootstrapProperties bsProps, ClassLoader cl) throws GlassFishException {
        String installRoot = getInstallRoot(bsProps);
        return build(bsProps, cl, installRoot);
    }


    private GlassFishRuntime build(BootstrapProperties bsProps, ClassLoader cl, String installRoot) {
        if (installRoot != null) {
            setProperty("org.glassfish.embeddable.installRoot", installRoot, true);
        }
        SingleHK2Factory.initialize(cl);
        ModulesRegistry modulesRegistry = AbstractFactory.getInstance().createModulesRegistry();
        modulesRegistry.setParentClassLoader(cl);
        return new EmbeddedGlassFishRuntime(new EmbeddedMain());
    }


    @Override
    public boolean handles(BootstrapProperties bsProps) {
        // See GLASSFISH-16743 for the reason behind additional check
        final String builderName = bsProps.getProperty(BootstrapKeys.BUILDER_NAME_PROPERTY);
        if (builderName != null && !builderName.equals(getClass().getName())) {
            return false;
        }
        String platform = bsProps.getProperty(OSGI_PLATFORM.getPropertyName());
        return platform == null || OsgiPlatform.Embedded.name().equalsIgnoreCase(platform)
            || OsgiPlatform.Static.name().equalsIgnoreCase(platform);
    }

    private String getInstallRoot(BootstrapProperties props) {
        String installRootProp = props.getInstallRoot();
        if (installRootProp == null) {
            File installRoot = ServerFiles.detectInstallRoot();
            if (isValidInstallRoot(installRoot)) {
                installRootProp = installRoot.getAbsolutePath();
            }
        }
        return installRootProp;
    }

    private List<URL> getModuleJarURLs(String installRoot) {
        if(installRoot == null || !addModuleJars) {
            return new ArrayList<>();
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

    private boolean isValidInstallRoot(File installRoot) {
        if (installRoot == null || !installRoot.exists()) {
            return false;
        }
        if (!new File(installRoot, "modules").exists()) {
            return false;
        }
        if (!new File(installRoot, "lib/dtds").exists()) {
            return false;
        }
        return true;
    }

    private static class EmbeddedClassLoader extends GlassfishUrlClassLoader {
        static {
            registerAsParallelCapable();
        }

        private EmbeddedClassLoader(ClassLoader parent, List<URL> moduleJarURLs) {
            super("EmbeddedCL", moduleJarURLs.toArray(URL[]::new), parent);
        }
    }

}
