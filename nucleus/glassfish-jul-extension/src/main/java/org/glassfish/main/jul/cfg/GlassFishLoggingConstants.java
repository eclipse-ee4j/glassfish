/*
 * Copyright (c) 2022, 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.jul.cfg;

import java.util.logging.LogRecord;

/**
 * Constants used to configure the Java Util Logging.
 * <p>
 * <b>Always remember - you cannot switch the LogManager used in the JVM once any runtime
 * touches the Logger class or the LogManager class.</b>
 * If you want to use GlassFishLogManager, you need to set the system property
 * {@value #JVM_OPT_LOGGING_MANAGER} to {@value #CLASS_LOG_MANAGER_GLASSFISH} before it happens.
 *
 * @author David Matejcek
 */
// do not reference JUL classes from here, they can be initialized when you don't want it.
public class GlassFishLoggingConstants {

    /** Default JUL LogManager class name */
    public static final String CLASS_LOG_MANAGER_JUL = "java.util.logging.LogManager";
    /** GlassFish's JUL LogManager implementation class name */
    public static final String CLASS_LOG_MANAGER_GLASSFISH = "org.glassfish.main.jul.GlassFishLogManager";
    /** GlassFish's JUL LogManager initializer class name */
    public static final String CLASS_INITIALIZER = "org.glassfish.main.jul.GlassFishLogManagerInitializer";
    /** Handler blocking processing of records until the log manager is reconfigured.*/
    public static final String CLASS_HANDLER_BLOCKING = "org.glassfish.main.jul.handler.BlockingExternallyManagedLogHandler";

    /**
     * System property name defining LogManager implementation for the rest of the JVM runtime
     * existence.
     */
    public static final String JVM_OPT_LOGGING_MANAGER = "java.util.logging.manager";

    /**
     * System property name to ask the starting log manager to block until application finishes
     * the configuration.
     */
    public static final String JVM_OPT_LOGGING_CFG_BLOCK = "java.util.logging.config.block";

    /**
     * System property name defining property file which will be automatically loaded on startup.
     * Usually it is named <code>logging.properties</code>
     */
    public static final String JVM_OPT_LOGGING_CFG_FILE = "java.util.logging.config.file";
    /**
     * System property telling the GlassFishLogManager to use defaults if there would not be any
     * logging.properties set by {@value #JVM_OPT_LOGGING_CFG_FILE}.
     * <p>
     * Defaults use the SimpleLogHandler and level INFO or level set by
     * {@value #JVM_OPT_LOGGING_CFG_DEFAULT_LEVEL}
     */
    public static final String JVM_OPT_LOGGING_CFG_USE_DEFAULTS = "java.util.logging.config.useDefaults";
    /**
     * If the GlassFishLogManager would use defaults as configured by
     * the {@value #JVM_OPT_LOGGING_CFG_USE_DEFAULTS}, this system property tells him to use this
     * level and not the default INFO.
     */
    public static final String JVM_OPT_LOGGING_CFG_DEFAULT_LEVEL = "java.util.logging.config.defaultLevel";

    /** If this key is set to true, GJULE will print really detailed tracing info to the standard output */
    public static final String KEY_TRACING_ENABLED = "org.glassfish.main.jul.tracing.enabled";

    /**
     * If this key is set to true, GJULE will detect the caller class and method from stacktrace,
     * which is quite expensive operation affecting logging throughput.
     * <p>
     * If it is set to false, GJULE will not perform such detection.
     * <p>
     * If the property is not set, GJULE makes the decision based on the (<code>*.printSource</code>
     * property) - if any formatter requires this feature, the feature is enabled.
     * This applies just when the formatter is set from the supplied log manager configuration,
     * not when you configure formatter from your code.
     *
     * <p>
     * It is disabled otherwise.
     */
    public static final String KEY_CLASS_AND_METHOD_DETECTION_ENABLED = "org.glassfish.main.jul.classAndMethodDetection.enabled";
    /**
     * Enable printing the source class and method of the LogRecord.
     * See {@link LogRecord#getSourceClassName()} and {@link LogRecord#getSourceMethodName()}
     */
    public static final String KEY_FORMATTER_PRINT_SOURCE_SUFFIX = "printSource";

    /** 1 000 000 */
    public static final long BYTES_PER_MEGABYTES = 1_000_000;


    private GlassFishLoggingConstants() {
        // hidden constructor
    }
}
