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

package org.glassfish.main.jul.cfg;

import java.io.File;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;

import org.glassfish.main.jul.tracing.GlassFishLoggingTracer;

/**
 * This is a tool to help with parsing the logging.properties file to configure JUL business objects.
 * <p>
 * It respects JUL configuration standards, so ie. each formatter knows best how to configure itself,
 * but still can use this helper to parse properties directly to objects instead of plain strings.
 * Helper also supports custom error handlers.
 *
 * @author David Matejcek
 */
public class ConfigurationHelper {

    /**
     * Logs an error via the {@link GlassFishLoggingTracer}
     */
    public static final LoggingPropertyErrorHandler ERROR_HANDLER_PRINT_TO_STDERR = (k, v, e) -> {
        GlassFishLoggingTracer.error(ConfigurationHelper.class, "Invalid value for the key: " + k + ": " + v, e);
    };

    private static final Function<String, Character> STR_TO_CHAR = v -> v == null || v.isEmpty() ? null : v.charAt(0);

    private static final Function<String, Boolean> STR_TO_BOOL = v -> {
        if (v == null || v.isEmpty()) {
            return null;
        }
        if ("true".equals(v)) {
            return true;
        }
        if ("false".equals(v)) {
            return false;
        }
        throw new IllegalArgumentException("Value is not a boolean: " + v);
    };


    private static final Function<String, Integer> STR_TO_POSITIVE_INT = v -> {
        final Integer value = Integer.valueOf(v);
        if (value >= 0) {
            return value;
        }
        throw new NumberFormatException("Value must be higher or equal to zero!");
    };


    private static final Function<String, DateTimeFormatter> STR_TO_DF = v -> {
        final DateTimeFormatter df = DateTimeFormatter.ofPattern(v);
        // test that it is able to format this type.
        df.format(OffsetDateTime.now());
        return df;
    };


    private static final Function<String, List<String>> STR_TO_LIST = v -> {
        if (v == null || v.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(v.split("[\\s]*,[\\s]*"));
    };


    private final LogManager manager;
    private final String prefix;
    private final LoggingPropertyErrorHandler errorHandler;

    /**
     * @param prefix Usually a canonical class name without dot.
     * @param errorHandler
     */
    public ConfigurationHelper(final String prefix, final LoggingPropertyErrorHandler errorHandler) {
        this.manager = LogManager.getLogManager();
        this.prefix = prefix == null ? "" : prefix;
        this.errorHandler = errorHandler;
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link String} or defaultValue
     */
    public String getString(final LogProperty key, final String defaultValue) {
        return parse(key, defaultValue, Function.identity());
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link Character} or defaultValue
     */
    public Character getCharacter(final LogProperty key, final Character defaultValue) {
        return parse(key, defaultValue, STR_TO_CHAR);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link Integer} or defaultValue
     */
    public Integer getInteger(final LogProperty key, final Integer defaultValue) {
        return parse(key, defaultValue, Integer::valueOf);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link Integer} or defaultValue
     */
    public Integer getNonNegativeInteger(final LogProperty key, final Integer defaultValue) {
        return parse(key, defaultValue, STR_TO_POSITIVE_INT);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link Boolean} or defaultValue
     */
    public Boolean getBoolean(final LogProperty key, final Boolean defaultValue) {
        return parse(key, defaultValue, STR_TO_BOOL);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link Level} or defaultValue
     */
    public Level getLevel(final LogProperty key, final Level defaultValue) {
        return parse(key, defaultValue, Level::parse);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link File} or defaultValue
     */
    public File getFile(final LogProperty key, final File defaultValue) {
        return parse(key, defaultValue, File::new);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link DateTimeFormatter} or defaultValue
     */
    public DateTimeFormatter getDateTimeFormatter(final LogProperty key, final DateTimeFormatter defaultValue) {
        return parse(key, defaultValue, STR_TO_DF);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link Charset} or defaultValue
     */
    public Charset getCharset(final LogProperty key, final Charset defaultValue) {
        return parse(key, defaultValue, Charset::forName);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @return parsed {@link List} of {@link String} instances or empty list
     */
    public List<String> getList(final LogProperty key, final String defaultValue) {
        return parseOrSupply(key, () -> STR_TO_LIST.apply(defaultValue), STR_TO_LIST);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     * @param <T>
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValue
     * @param converter - function used to parse the value or the defaultValue
     * @return parsed value or defaultValue
     */
    protected <T> T parse(final LogProperty key, final T defaultValue, final Function<String, T> converter) {
        final Supplier<T> defaultValueSupplier = () -> defaultValue;
        return parseOrSupply(key, defaultValueSupplier, converter);
    }


    /**
     * Joins <code>prefix.key</code>, uses the result to call the
     * {@link LogManager#getProperty(String)} and parses the retrieved value.
     * Parsing exceptions are consumed by the {@link LoggingPropertyErrorHandler}
     * given in constructor.
     * @param <T>
     *
     * @param key key relative to the prefix given in constructor
     * @param defaultValueSupplier
     * @param converter - function used to parse the value or the defaultValue
     * @return parsed value or defaultValue
     */
    protected <T> T parseOrSupply(final LogProperty key, final Supplier<T> defaultValueSupplier,
        final Function<String, T> converter) {
        final String realKey = key.getPropertyFullName(prefix);
        final String property = getProperty(realKey);
        if (property == null) {
            return defaultValueSupplier.get();
        }
        try {
            return converter.apply(property);
        } catch (final Exception e) {
            handleError(e, realKey, property);
            return defaultValueSupplier.get();
        }
    }


    /**
     * Calls the {@link LoggingPropertyErrorHandler} set in constructor.
     *
     * @param cause
     * @param key
     * @param property
     * @throws RuntimeException - depends on the implementation of the error handler
     */
    protected void handleError(final Exception cause, final String key, final Object property) {
        if (errorHandler != null) {
            errorHandler.handle(key, property, cause);
        }
    }


    /**
     * Note: if you want to use untrimmed value, use the {@link LogManager#getProperty(String)}
     * directly.
     *
     * @param key
     * @return trimmed value for the key or null
     */
    private String getProperty(final String key) {
        final String value = manager.getProperty(key);
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }


    /**
     * Allows custom error handling (ie. throwing a runtime exception or collecting errors)
     */
    @FunctionalInterface
    public interface LoggingPropertyErrorHandler {
        /**
         * @param key the whole key used
         * @param value found string value
         * @param e exception thrown
         */
        void handle(String key, Object value, Exception e);
    }
}