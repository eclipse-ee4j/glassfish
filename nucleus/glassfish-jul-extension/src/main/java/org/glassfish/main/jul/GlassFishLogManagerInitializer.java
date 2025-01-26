/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul;

import java.util.Properties;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.glassfish.main.jul.cfg.GlassFishLoggingConstants;

import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.CLASS_LOG_MANAGER_GLASSFISH;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_MANAGER;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.stacktrace;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;

/**
 * This class tries to set the {@link GlassFishLogManager} as the default {@link LogManager}
 * implementation.
 * <p>
 * The result is <b>not guaranteed</b>, because the first access to any {@link Logger} instance
 * in the JVM starts the initialization.
 * <p>
 * Simply said - this must be the first thing application must execute.
 * <p>
 * As an example, when you enable GC logging, it will be always faster than this class.
 * That is why is this class deprecated and we recommend to use the
 * {@link GlassFishLoggingConstants#JVM_OPT_LOGGING_MANAGER} and other related options
 * which guarantee that the log manager will be set.
 *
 * @author David Matejcek
 */
@Deprecated
public final class GlassFishLogManagerInitializer {

    private GlassFishLogManagerInitializer() {
        // hidden
    }


    /**
     * Tries to set the {@link GlassFishLogManager}as the JVM's {@link LogManager} implementation.
     * This must be done before any JUL component is used and remains set until JVM shutdown.
     * The {@link GlassFishLogManager} will try to find the configuration automatically, use defaults,
     * or will throw an exception, which depends on JVM options.
     *
     * @return true if the operation was successful
     */
    public static synchronized boolean tryToSetAsDefault() {
        return tryToSetAsDefault(null);
    }


    /**
     * Tries to set the {@link GlassFishLogManager}as the JVM's {@link LogManager} implementation.
     * This must be done before any JUL component is used and remains set until JVM shutdown.
     *
     * @param configuration - logging.properties file content, if null, {@link GlassFishLogManager}
     *            will try to resolve it, the solution depends on JVM options.
     * @return true if the operation was successful
     */
    public static synchronized boolean tryToSetAsDefault(final Properties configuration) {
        stacktrace(GlassFishLogManagerInitializer.class, "tryToSetAsDefault(" + configuration + ")");
        if (System.getProperty(JVM_OPT_LOGGING_MANAGER) != null) {
            trace(GlassFishLogManagerInitializer.class, "The Log Manager implementation is already configured.");
            return false;
        }
        // will not work if anyone already called LogManager.getLogManager in the same context!
        final Thread currentThread = Thread.currentThread();
        final ClassLoader originalContectClassLoader = currentThread.getContextClassLoader();
        try {
            // context classloader is used to load the class if not found by system cl.
            final ClassLoader newClassLoader = GlassFishLogManagerInitializer.class.getClassLoader();
            currentThread.setContextClassLoader(newClassLoader);

            // avoid any direct references to prevent static initializer of LogManager class
            // until everything is set.
            System.setProperty(JVM_OPT_LOGGING_MANAGER, CLASS_LOG_MANAGER_GLASSFISH);
            final Class<?> logManagerClass = newClassLoader.loadClass(CLASS_LOG_MANAGER_GLASSFISH);
            trace(GlassFishLogManagerInitializer.class, () -> "Will initialize log manager " + logManagerClass);

            return GlassFishLogManager.initialize(configuration);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Could not initialize logging system.", e);
        } finally {
            currentThread.setContextClassLoader(originalContectClassLoader);
        }
    }
}
