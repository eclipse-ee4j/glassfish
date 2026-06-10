/*
 * Copyright (c) 2024, 2025, 2026 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.module.bootstrap.ModuleStartup;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandRunner;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.main.boot.impl.GlassFishImpl;
import org.glassfish.main.jdke.props.SystemProperties;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_DELETE;

/**
 * Embedded glassfish which can delete its directories on dispose.
 */
class AutoDisposableGlassFish extends GlassFishImpl {

    private static final Logger LOG = System.getLogger(AutoDisposableGlassFish.class.getName());
    private static final String GENERAL_CONFIG_PROP_PREFIX = "embedded-glassfish-config.";

    private final File instanceRoot;
    private final boolean autoDelete;
    private final Consumer<GlassFish> onDisposeAction;

    /**
     * Deletes the temporary instance root if {@link #dispose()} was never called before the JVM
     * exits. See issue #25545.
     */
    private final Thread cleanupHook = new Thread(this::cleanupOnShutdown, "gfembed-cleanup");
    private boolean cleanupHookRegistered;

    AutoDisposableGlassFish(ModuleStartup gfKernel, ServiceLocator serviceLocator, GlassFishProperties gfProps,
        Consumer<GlassFish> onDispose) throws GlassFishException {
        super(gfKernel, serviceLocator);
        this.instanceRoot = new File(gfProps.getInstanceRoot());
        this.autoDelete = Boolean.parseBoolean(gfProps.getProperty(AUTO_DELETE));
        this.onDisposeAction = onDispose;

        // If there are custom configurations like http.port, https.port, jmx.port then configure them.
        CommandRunner commandRunner = null;
        Set<String> knownPropertyPrefixes = new HashSet<>();
        ArrayList<String> configPropertiesToSet = new ArrayList<>();
        for (String key : gfProps.getPropertyNames()) {
            String propertyName = key;
            if (key.startsWith(GENERAL_CONFIG_PROP_PREFIX)) {
                propertyName = key.substring(GENERAL_CONFIG_PROP_PREFIX.length());
            }
            String propertyValue = gfProps.getProperty(key);
            if (commandRunner == null) {
                // only create the CommandRunner if needed
                commandRunner = serviceLocator.getService(CommandRunner.class);
                if (commandRunner == null) {
                    throw new GlassFishException("Service locator failed to resolve the CommandRunner");
                }
            }
            String propertyPrefix = propertyName.split("\\.")[0];
            if (!knownPropertyPrefixes.contains(propertyPrefix)) {
                CommandResult resultList = commandRunner.run("list", propertyPrefix);
                if (resultList.getExitStatus() == CommandResult.ExitStatus.SUCCESS
                        && resultList.getOutput().contains(propertyPrefix)) {
                    knownPropertyPrefixes.add(propertyPrefix);
                } else {
                    if (!key.startsWith("com.sun.aas.") && !key.startsWith("-")) {
                        // unknown property, set as system property
                        LOG.log(Level.INFO, "Setting system property " + key + " from GlassFish properties, it doesn't match any known property");
                        SystemProperties.setProperty(key, gfProps.getProperty(key), false);
                    }
                    // not a dotted name, doesn't start with a supported prefix, do not set it later
                    continue;
                }
            }
            configPropertiesToSet.add(propertyName + "=" + propertyValue);
        }

        CommandResult result = commandRunner.run("set", configPropertiesToSet.toArray(String[]::new));
        if (result.getExitStatus() != CommandResult.ExitStatus.SUCCESS) {
            throw new GlassFishException(result.getOutput(), result.getFailureCause());
        }
    }

    @Override
    public synchronized void start() throws GlassFishException {
        super.start();
        // The instance may be started more than once (start/stop/start), but the hook must be
        // registered only once - addShutdownHook rejects an already registered thread.
        if (autoDelete && instanceRoot != null && !cleanupHookRegistered) {
            try {
                Runtime.getRuntime().addShutdownHook(cleanupHook);
                cleanupHookRegistered = true;
            } catch (IllegalStateException e) {
                // The JVM is already shutting down, nothing more we can do here.
                LOG.log(Level.DEBUG, "Could not register the temp directory cleanup hook.", e);
            }
        }
    }

    @Override
    public synchronized void dispose() throws GlassFishException {
        // The directory is deleted here, so the shutdown hook is no longer needed.
        // It can't be removed if the JVM is already shutting down (i.e. we are called from the hook).
        if (cleanupHookRegistered) {
            try {
                Runtime.getRuntime().removeShutdownHook(cleanupHook);
                cleanupHookRegistered = false;
            } catch (IllegalStateException e) {
                LOG.log(Level.DEBUG, "Could not remove the temp directory cleanup hook.", e);
            }
        }
        onDisposeAction.accept(this);
        try {
            super.dispose();
        } finally {
            deleteInstanceRoot();
        }
    }

    /**
     * Invoked by the JVM shutdown hook when {@link #dispose()} was not called. Disposes the instance
     * so the server stops cleanly and releases its files, then deletes the temporary directory.
     */
    private void cleanupOnShutdown() {
        try {
            dispose();
        } catch (IllegalStateException e) {
            // Already disposed - the directory has already been deleted.
            LOG.log(Level.DEBUG, "GlassFish already disposed on shutdown.", e);
        } catch (GlassFishException e) {
            // Best effort: dispose failed, still try to remove the temporary directory.
            LOG.log(Level.WARNING, "GlassFish dispose failed on shutdown, deleting temp directory anyway.", e);
            deleteInstanceRoot();
        }
    }

    private void deleteInstanceRoot() {
        if (autoDelete && instanceRoot != null && instanceRoot.exists()) {
            // Might have been deleted already.
            deleteRecursive(instanceRoot);
        }
    }

    private static boolean deleteRecursive(File dir) {
        try (Stream<Path> paths = Files.walk(dir.toPath())) {
            paths.sorted(Comparator.reverseOrder()).forEach(AutoDisposableGlassFish::delete);
        } catch (Exception e) {
            LOG.log(Level.ERROR, "Could not delete: " + dir, e);
        }
        return Files.exists(dir.toPath());
    }


    private static void delete(Path path) {
        try {
            Files.delete(path);
        } catch (IOException e) {
            LOG.log(Level.ERROR, "Could not delete: " + path, e);
        }
    }
}
