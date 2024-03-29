/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.bootstrap.ContextDuplicatePostProcessor;
import com.sun.enterprise.module.bootstrap.Main;
import com.sun.enterprise.module.bootstrap.ModuleStartup;
import com.sun.enterprise.module.bootstrap.StartupContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.glassfish.hk2.api.ServiceLocator;

import static java.util.logging.Level.FINER;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * The {@link GlassFishRuntime} implementation for non-OSGi environments.
 *
 * @author bhavanishankar@dev.java.net
 */
public class StaticGlassFishRuntime extends GlassFishRuntime {

    private static final Logger LOG = Util.getLogger();

    private static final String AUTO_DELETE = "org.glassfish.embeddable.autoDelete";

    private final Map<String, GlassFish> glassFishInstances = new HashMap<>();
    private final Main main;

    public StaticGlassFishRuntime(Main main) {
        this.main = main;
    }

    /**
     * Creates a new GlassFish instance and add it to a Map of instances created by this runtime.
     *
     * @param glassFishProperties properties required to create a new GlassFish instance
     * @return newly instantiated {@link GlassFish} object
     * @throws GlassFishException if an error occurred
     */
    @Override
    public synchronized GlassFish newGlassFish(GlassFishProperties glassFishProperties) throws GlassFishException {
        // Set env props before updating config, because configuration update may actually trigger
        // some code to be executed which may be depending on the environment variable values.
        try {
            // Don't set temporarily created instanceRoot in the user supplied GlassFishProperties,
            // hence clone it.
            Properties properties = new Properties();
            properties.putAll(glassFishProperties.getProperties());

            final GlassFishProperties gfProps = new GlassFishProperties(properties);
            setEnv(gfProps);

            final StartupContext startupContext = new StartupContext(gfProps.getProperties());

            ModulesRegistry modulesRegistry = SingleHK2Factory.getInstance().createModulesRegistry();

            ServiceLocator serviceLocator = main.createServiceLocator(modulesRegistry, startupContext,
                    List.of(new EmbeddedInhabitantsParser(), new ContextDuplicatePostProcessor()), null);

            final ModuleStartup gfKernel = main.findStartupService(modulesRegistry, serviceLocator, null, startupContext);

            // Create a new GlassFish instance
            GlassFish glassFish = new GlassFishImpl(gfKernel, serviceLocator, gfProps.getProperties()) {
                @Override
                public void dispose() throws GlassFishException {
                    try {
                        super.dispose();
                    } finally {
                        glassFishInstances.remove(gfProps.getInstanceRoot());
                        if (Boolean.parseBoolean(gfProps.getProperties().getProperty(AUTO_DELETE)) && gfProps.getInstanceRoot() != null) {
                            File instanceRoot = new File(gfProps.getInstanceRoot());
                            // Might have been deleted already.
                            if (instanceRoot.exists()) {
                                Util.deleteRecursive(instanceRoot);
                            }
                        }
                    }
                }
            };

            // Add this newly created instance to a Map
            glassFishInstances.put(gfProps.getInstanceRoot(), glassFish);

            return glassFish;
        } catch (GlassFishException e) {
            throw e;
        } catch (Exception e) {
            throw new GlassFishException(e);
        }
    }

    @Override
    public synchronized void shutdown() throws GlassFishException {
        for (GlassFish glassFish : glassFishInstances.values()) {
            try {
                glassFish.dispose();
            } catch (IllegalStateException ex) {
                LOG.log(FINER, "GlassFish dispose failed: " + ex.getMessage());
            }
        }

        glassFishInstances.clear();

        try {
            shutdownInternal();
        } catch (GlassFishException ex) {
            LOG.log(WARNING, LogFacade.CAUGHT_EXCEPTION, ex.getMessage());
        }
    }

    private void setEnv(GlassFishProperties gfProps) throws Exception {
        String instanceRootValue = gfProps.getInstanceRoot();
        if (instanceRootValue == null) {
            instanceRootValue = createTempInstanceRoot(gfProps);
            gfProps.setInstanceRoot(instanceRootValue);
        }

        File instanceRoot = new File(instanceRootValue);
        System.setProperty(Constants.INSTANCE_ROOT_PROP_NAME, instanceRoot.getAbsolutePath());
        System.setProperty(Constants.INSTANCE_ROOT_URI_PROP_NAME, instanceRoot.toURI().toString());

        String installRootValue = System.getProperty("org.glassfish.embeddable.installRoot");
        if (installRootValue == null) {
            installRootValue = instanceRoot.getAbsolutePath();
            gfProps.setProperty("-type", "EMBEDDED");
            JarUtil.extractRars(installRootValue);
        }
        JarUtil.setEnv(installRootValue);

        File installRoot = new File(installRootValue);

        // Some legacy code might depend on setting installRoot as system property.
        // Ideally everyone should depend only on StartupContext.
        System.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
        System.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());

        // StartupContext requires the installRoot to be set in the GlassFishProperties.
        gfProps.setProperty(Constants.INSTALL_ROOT_PROP_NAME, installRoot.getAbsolutePath());
        gfProps.setProperty(Constants.INSTALL_ROOT_URI_PROP_NAME, installRoot.toURI().toString());
    }

    private String createTempInstanceRoot(GlassFishProperties gfProps) throws Exception {
        String tmpDir =  gfProps.getProperties().getProperty("glassfish.embedded.tmpdir");
        if (tmpDir == null) {
            tmpDir = System.getProperty("glassfish.embedded.tmpdir", System.getProperty("java.io.tmpdir"));
        } else {
            new File(tmpDir).mkdirs();
        }

        File instanceRoot = File.createTempFile("gfembed", "tmp", new File(tmpDir));
        // Convert the file into a directory.
        if (!instanceRoot.delete() || !instanceRoot.mkdir()) {
            throw new Exception("cannot create directory: " + instanceRoot.getAbsolutePath());
        }

        try {
            String[] configFiles = new String[] {
                "config/keyfile",
                "config/server.policy",
                "config/cacerts.jks",
                "config/keystore.jks",
                "config/login.conf",
                "config/admin-keyfile",
                "org/glassfish/web/embed/default-web.xml",
                "org/glassfish/embed/domain.xml"
            };

            // Create instance config directory.
            File instanceConfigDir = new File(instanceRoot, "config");
            instanceConfigDir.mkdirs();

            // Create instance docroot directory.
            new File(instanceRoot, "docroot").mkdirs();

            // Copy all the config files from uber jar to the instanceConfigDir.
            ClassLoader cl = getClass().getClassLoader();
            for (String configFile : configFiles) {
                copy(cl.getResource(configFile), new File(instanceConfigDir,
                    configFile.substring(configFile.lastIndexOf('/') + 1)), false);
            }

            // If the user has specified a custom domain.xml then copy it.
            String configFileURI = gfProps.getConfigFileURI();
            if(configFileURI != null) {
                copy(URI.create(configFileURI).toURL(), new File(instanceConfigDir, "domain.xml"), true);
            }
        } catch (Exception ex) {
            LOG.log(SEVERE, "Failed to create instanceRoot", ex);
        }

        String autoDelete = gfProps.getProperties().getProperty(AUTO_DELETE, "true");
        gfProps.setProperty(AUTO_DELETE, autoDelete);
        return instanceRoot.getAbsolutePath();
    }

    public static void copy(URL url, File destFile, boolean overwrite) {
        if (url == null || destFile == null) return;
        try {
            if (!destFile.exists() || overwrite) {
                if (!destFile.toURI().equals(url.toURI())) {
                    InputStream stream = url.openStream();
                    destFile.getParentFile().mkdirs();
                    Util.copy(stream, new FileOutputStream(destFile), stream.available());
                    LOG.log(FINER, () -> "Copied " + url + " to " + destFile.getAbsolutePath());
                }
            }
        } catch (Exception ex) {
            LOG.log(FINER, ex, () -> "Failed to copy " + url + " to " + destFile.getAbsolutePath());
        }
    }
}
