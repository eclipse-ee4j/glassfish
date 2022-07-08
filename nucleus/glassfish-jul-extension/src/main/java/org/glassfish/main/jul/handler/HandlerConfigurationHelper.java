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

package org.glassfish.main.jul.handler;

import java.lang.reflect.Constructor;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;

import org.glassfish.main.jul.cfg.ConfigurationHelper;
import org.glassfish.main.jul.cfg.LogProperty;
import org.glassfish.main.jul.formatter.HandlerId;

import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.error;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;


/**
 * This is a tool to help with parsing the logging.properties file to configure handlers.
 * <p>
 * It respects JUL configuration standards, so ie. each formatter knows best how to configure itself,
 * but still can use this helper to parse properties directly to objects instead of plain strings.
 * Helper also supports custom error handlers.
 *
 * @author David Matejcek
 */
public class HandlerConfigurationHelper extends ConfigurationHelper {

    /**
     * Handler's property for formatter
     */
    public static final LogProperty FORMATTER = () -> "formatter";
    /**
     * Handler's property for filter
     */
    public static final LogProperty FILTER = () -> "filter";
    private final HandlerId handlerId;



    /**
     * Creates a helper based on {@link HandlerId}. For the id uses the handlerClass parameter.
     *
     * @param handlerClass
     * @return {@link HandlerConfigurationHelper}
     */
    public static HandlerConfigurationHelper forHandlerClass(final Class<? extends Handler> handlerClass) {
        return new HandlerConfigurationHelper(HandlerId.forHandlerClass(handlerClass));
    }


    /**
     * Creates a helper based on {@link HandlerId}
     *
     * @param handlerId
     */
    public HandlerConfigurationHelper(final HandlerId handlerId) {
        super(handlerId.getPropertyPrefix(), ERROR_HANDLER_PRINT_TO_STDERR);
        this.handlerId = handlerId;
    }


    /**
     * @param defaultFormatterClass
     * @return preconfigured {@link Formatter}, defaults are defined by the formatter and properties
     */
    public Formatter getFormatter(final Class<? extends Formatter> defaultFormatterClass) {
        final Supplier<Formatter> defaultSupplier = () -> createNewFormatter(defaultFormatterClass);
        final Function<String, Formatter> converter = value -> createNewFormatter(value);
        return parseOrSupply(FORMATTER, defaultSupplier, converter);
    }


    /**
     * @return preconfigured {@link Filter}, default is null.
     */
    public Filter getFilter() {
        final Function<String, Filter> converter = value -> {
            final Class<Filter> filterClass = findClass(value);
            return createNewFilter(filterClass);
        };
        return parseOrSupply(FILTER, () -> null, converter);
    }


    @SuppressWarnings("unchecked")
    private <F extends Formatter> F createNewFormatter(final String className) {
        final Class<Formatter> formatterClass = findClass(className);
        return (F) createNewFormatter(formatterClass);
    }


    @SuppressWarnings("unchecked")
    private  <F> Class<F> findClass(final String className) {
        if (className == null) {
            return null;
        }
        final ClassLoader classLoader = getClassLoader();
        try {
            return (Class<F>) classLoader.loadClass(className);
        } catch (ClassCastException | ClassNotFoundException | NoClassDefFoundError e) {
            error(ConfigurationHelper.class, "Classloader: " + classLoader, e);
            throw new IllegalStateException("Formatter instantiation failed! ClassLoader used: " + classLoader, e);
        }
    }


    private ClassLoader getClassLoader() {
        final ClassLoader threadCL = Thread.currentThread().getContextClassLoader();
        if (threadCL != null) {
            return threadCL;
        }
        return getClass().getClassLoader();
    }


    private <F extends Formatter> F createNewFormatter(final Class<F> clazz) {
        if (clazz == null) {
            return null;
        }
        final Constructor<F> constructor = getFormatterConstructorForHandler(clazz);
        try {
            if (constructor == null) {
                // All formatters must have default constructor
                return clazz.getDeclaredConstructor().newInstance();
            }
            return constructor.newInstance(handlerId);
        } catch (ReflectiveOperationException | RuntimeException e) {
            handleError(e, FORMATTER.getPropertyFullName(handlerId.getPropertyPrefix()), clazz);
            return null;
        }
    }

    private <F extends Formatter> Constructor<F> getFormatterConstructorForHandler(final Class<F> formatterClass) {
        try {
            return formatterClass.getConstructor(HandlerId.class);
        } catch (NoSuchMethodException | SecurityException e) {
            trace(getClass(), "This formatter doesn't support configuration by handler's formatter properties subset: "
                + formatterClass + ", so we will use formatter's default constructor");
            return null;
        }
    }


    private <F extends Filter> F createNewFilter(final Class<F> clazz) {
        try {
            return clazz.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException | RuntimeException e) {
            handleError(e, FILTER.getPropertyFullName(handlerId.getPropertyPrefix()), clazz);
            return null;
        }
    }
}
