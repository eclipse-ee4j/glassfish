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

package com.sun.enterprise.glassfish.bootstrap.embedded;

import com.sun.enterprise.glassfish.bootstrap.GlassFishImpl;
import com.sun.enterprise.module.bootstrap.ModuleStartup;

import java.io.File;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.hk2.api.ServiceLocator;

import static com.sun.enterprise.glassfish.bootstrap.cfg.BootstrapKeys.AUTO_DELETE;

/**
 * Embedded glassfish which can delete its directories on dispose.
 */
class AutoDisposableGlassFish extends GlassFishImpl {

    private static final Logger LOG = System.getLogger(AutoDisposableGlassFish.class.getName());

    private final File instanceRoot;
    private final boolean autoDelete;
    private final Consumer<GlassFish> onDisposeAction;

    AutoDisposableGlassFish(ModuleStartup gfKernel, ServiceLocator serviceLocator, GlassFishProperties gfProps,
        Consumer<GlassFish> onDispose) throws GlassFishException {
        super(gfKernel, serviceLocator, gfProps.getProperties());
        this.instanceRoot = new File(gfProps.getInstanceRoot());
        this.autoDelete = Boolean.parseBoolean(gfProps.getProperties().getProperty(AUTO_DELETE));
        this.onDisposeAction = onDispose;
    }

    @Override
    public void dispose() throws GlassFishException {
        try {
            super.dispose();
        } finally {
            if (autoDelete && instanceRoot != null) {
                // Might have been deleted already.
                if (instanceRoot.exists()) {
                    deleteRecursive(instanceRoot);
                }
            }
            onDisposeAction.accept(this);
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
