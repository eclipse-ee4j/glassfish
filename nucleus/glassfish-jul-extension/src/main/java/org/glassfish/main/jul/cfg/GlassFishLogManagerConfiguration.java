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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.SortedSet;
import java.util.stream.Stream;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.KEY_CLASS_AND_METHOD_DETECTION_ENABLED;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.KEY_FORMATTER_PRINT_SOURCE_SUFFIX;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.KEY_TRACING_ENABLED;

/**
 * Replacement (wrapper) for {@link Properties} used in JUL.
 *
 * @author David Matejcek
 */
public class GlassFishLogManagerConfiguration implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    private final LoggingProperties properties;
    private final boolean tracingEnabled;
    private final boolean classAndMethodDetectionEnabled;

    /**
     * @param properties configuration to clone
     */
    public GlassFishLogManagerConfiguration(final LoggingProperties properties) {
        this.properties = properties.clone();
        this.tracingEnabled = Boolean.parseBoolean(this.properties.getProperty(KEY_TRACING_ENABLED));
        this.classAndMethodDetectionEnabled = resolveClassAndMethodDetectionEnabled(this.properties);
    }


    /**
     * @return all property names used in the current configuration.
     */
    public SortedSet<String> getPropertyNames() {
        return properties.getPropertyNames();
    }

    /**
     * @param name proeprty name
     * @return null or configured value
     */
    public String getProperty(final String name) {
        GlassFishLoggingTracer.trace(GlassFishLogManagerConfiguration.class, () -> "getProperty(" + name + ")");
        return this.properties.getProperty(name, null);
    }


    /**
     * @return {@link Stream} of configuration entries (key and value)
     */
    public Stream<ConfigurationEntry> toStream() {
        return this.properties.entrySet().stream().map(ConfigurationEntry::new);
    }


    /**
     * @return cloned {@link Properties}
     */
    public LoggingProperties toProperties() {
        return this.properties.clone();
    }


    /**
     * @return true if the logging of logging is enabled in this configuration. Doesn't affect error
     *         reporting, which is always enabled.
     */
    public boolean isTracingEnabled() {
        return this.tracingEnabled;
    }


    /**
     * If {@link GlassFishLoggingConstants#KEY_CLASS_AND_METHOD_DETECTION_ENABLED} is set to true,
     * GJULE will detect the caller class and method from stacktrace,
     * which is quite expensive operation affecting logging throughput.
     * <p>
     * If it is set to null, GJULE will not perform such detection.
     * <p>
     * If the property is not set, GJULE makes the decision based on known handlers
     * (<code>*.printSource</code> value) - if any handler requires this feature, it is enabled.
     * It is disabled otherwise or you can forcibly enable it by setting the value to true or false.
     *
     * @return true if handlers can use source class and method in formatters.
     */
    public boolean isClassAndMethodDetectionEnabled() {
        return classAndMethodDetectionEnabled;
    }


    /**
     * Creates clone of this instance.
     */
    @Override
    public GlassFishLogManagerConfiguration clone() {
        try {
            return (GlassFishLogManagerConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Clone failed!", e);
        }
    }


    /**
     * Returns this configuration formatted as a {@link Properties}
     */
    @Override
    public String toString() {
        return this.properties.toString();
    }


    /**
     * Parses the configuration from a {@link File}.
     *
     * @param file
     * @return {@link GlassFishLogManagerConfiguration}
     * @throws IOException
     */
    public static GlassFishLogManagerConfiguration parse(final File file) throws IOException {
        return new GlassFishLogManagerConfiguration(LoggingProperties.loadFrom(file));
    }

    /**
     * Parses the configuration from an {@link InputStream}.
     *
     * @param inputStream
     * @return {@link GlassFishLogManagerConfiguration}
     * @throws IOException
     */
    public static GlassFishLogManagerConfiguration parse(final InputStream inputStream) throws IOException {
        return new GlassFishLogManagerConfiguration(LoggingProperties.loadFrom(inputStream));
    }


    private static boolean resolveClassAndMethodDetectionEnabled(final LoggingProperties properties) {
        String value = properties.getProperty(KEY_CLASS_AND_METHOD_DETECTION_ENABLED);
        if (Boolean.TRUE.toString().equalsIgnoreCase(value)) {
            return true;
        }
        if (Boolean.FALSE.toString().equalsIgnoreCase(value)) {
            return false;
        }
        return properties.stringPropertyNames().stream()
            .filter(name -> name.endsWith(KEY_FORMATTER_PRINT_SOURCE_SUFFIX))
            .anyMatch(name -> properties.getProperty(name, Boolean.FALSE.toString())
                .equalsIgnoreCase(Boolean.TRUE.toString()));
    }


    /**
     * Configuration entry, pair of a key and a value, both can be null (but it is not very useful).
     */
    public static final class ConfigurationEntry {

        private final String key;
        private final String value;

        ConfigurationEntry(final Entry<Object, Object> entry) {
            this.key = entry.getKey() == null ? null : entry.getKey().toString();
            this.value = entry.getValue() == null ? null : entry.getValue().toString();
        }

        /**
         * @return property key
         */
        public String getKey() {
            return key;
        }


        /**
         * @return property value
         */
        public String getValue() {
            return value;
        }


        /**
         * Returns key:value
         */
        @Override
        public String toString() {
            return getKey() + ":" + getValue();
        }
    }
}
