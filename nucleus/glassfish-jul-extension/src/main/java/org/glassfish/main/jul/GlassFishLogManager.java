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

package org.glassfish.main.jul;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.glassfish.main.jul.cfg.ConfigurationHelper;
import org.glassfish.main.jul.cfg.GlassFishLogManagerConfiguration;
import org.glassfish.main.jul.cfg.GlassFishLogManagerProperty;
import org.glassfish.main.jul.cfg.LoggingProperties;
import org.glassfish.main.jul.env.LoggingSystemEnvironment;
import org.glassfish.main.jul.handler.ExternallyManagedLogHandler;
import org.glassfish.main.jul.handler.SimpleLogHandler;
import org.glassfish.main.jul.handler.SimpleLogHandler.SimpleLogHandlerProperty;

import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_RELEASE_PARAMETERS_EARLY;
import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_RESOLVE_LEVEL_WITH_INCOMPLETE_CONFIGURATION;
import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_ROOT_HANDLERS;
import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_SYS_ROOT_LOGGER_LEVEL;
import static org.glassfish.main.jul.cfg.GlassFishLogManagerProperty.KEY_USR_ROOT_LOGGER_LEVEL;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_CFG_DEFAULT_LEVEL;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_CFG_FILE;
import static org.glassfish.main.jul.cfg.GlassFishLoggingConstants.JVM_OPT_LOGGING_CFG_USE_DEFAULTS;
import static org.glassfish.main.jul.env.LoggingSystemEnvironment.isReleaseParametersEarly;
import static org.glassfish.main.jul.env.LoggingSystemEnvironment.isResolveLevelWithIncompleteConfiguration;
import static org.glassfish.main.jul.env.LoggingSystemEnvironment.setReleaseParametersEarly;
import static org.glassfish.main.jul.env.LoggingSystemEnvironment.setResolveLevelWithIncompleteConfiguration;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.error;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.isTracingEnabled;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.setTracingEnabled;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.stacktrace;
import static org.glassfish.main.jul.tracing.GlassFishLoggingTracer.trace;

/**
 * The custom {@link LogManager} implementation.
 * Main differences:
 * <ul>
 * <li>Customized lifecycle allowing deferred or phased configuration, see {@link GlassFishLoggingStatus}
 * <li>{@link #reset()} method is not available except internal usage.
 * <li>You can use {@link #reconfigure(GlassFishLogManagerConfiguration)} method instead.
 * <li>Or you can use {@link #reconfigure(GlassFishLogManagerConfiguration, Action, Action)} method,
 * which provides a way to add programatical extension points.
 * </ul>
 * <p>
 * Note: Some methods have complicated implementation, the reason is that JDK {@link LogManager} is
 * not an example of well usable and well extensible class at all.
 *
 * @author David Matejcek
 */
public class GlassFishLogManager extends LogManager {
    /** Empty string - standard root logger name */
    public static final String ROOT_LOGGER_NAME = "";

    private static final ReentrantLock LOCK = new ReentrantLock();

    private static final AtomicBoolean RESET_PROTECTION = new AtomicBoolean(true);
    private static volatile GlassFishLoggingStatus status = GlassFishLoggingStatus.UNINITIALIZED;
    private static GlassFishLogManager glassfishLogManager;

    private volatile GlassFishLogger systemRootLogger;
    private volatile GlassFishLogger userRootLogger;
    private volatile GlassFishLogger globalLogger;

    private GlassFishLogManagerConfiguration configuration;


    static boolean initialize(final Properties configuration) {
        trace(GlassFishLogManager.class, "initialize(configuration)");
        if (status.ordinal() > GlassFishLoggingStatus.UNINITIALIZED.ordinal()) {
            error(GlassFishLogManager.class, "Initialization of the logging system failed - it was already executed");
            return false;
        }
        LOCK.lock();
        try {
            // We must respect that LogManager.getLogManager()
            // - creates final root and global loggers,
            // - calls also addLogger.
            // - calls setLevel if the level was not set in addLogger.
            // OR something already configured another log manager implementation
            // Therefore status is now moved directly to UNCONFIGURED
            status = GlassFishLoggingStatus.UNCONFIGURED;
            final GlassFishLogManager logManager = getLogManager();
            if (logManager == null) {
                // oh no, another LogManager implementation is already used.
                return false;
            }
            logManager.doFirstInitialization(ensureSortedProperties(configuration));
            return true;
        } finally {
            LOCK.unlock();
        }
    }


    /**
     * @return true if {@link GlassFishLogManager} is configured as the JVM log manager.
     */
    public static boolean isGlassFishLogManager() {
        if (glassfishLogManager == null) {
            return getLogManager() != null;
        }
        return true;
    }


    /**
     * Returns current {@link GlassFishLogManager} instance.
     * <p>
     * If it is not initialized yet, starts the initialization.
     *
     * @return null if the current {@link LogManager} is not an instance of this class.
     */
    public static GlassFishLogManager getLogManager() {
        if (glassfishLogManager != null) {
            return glassfishLogManager;
        }
        LOCK.lock();
        try {
            final LogManager logManager = LogManager.getLogManager();
            if (logManager instanceof GlassFishLogManager) {
                glassfishLogManager = (GlassFishLogManager) logManager;
                return glassfishLogManager;
            }
            // If the tracing is off and another LogManager implementation is used,
            // we don't need to spam stderr so much
            // But if the tracing is on, do spam a lot, because even tracing is not much useful
            // except this message.
            if (isTracingEnabled()) {
                stacktrace(GlassFishLogManager.class,
                    "GlassFishLogManager not available, using " + logManager + ". Classloader used:" //
                        + "\n here:  " + GlassFishLogManager.class.getClassLoader() //
                        + "\n there: " + logManager.getClass().getClassLoader());
            }
            return null;
        } finally {
            LOCK.unlock();
        }
    }


    /**
     * @deprecated Don't call this constructor directly. Use {@link LogManager#getLogManager()} instead.
     * See {@link LogManager} javadoc for more.
     */
    @Deprecated
    public GlassFishLogManager() {
        trace(getClass(), "new GlassFishLogManager()");
        LoggingSystemEnvironment.initialize();
    }


    @Override
    public String getProperty(final String name) {
        return this.configuration == null ? null : this.configuration.getProperty(name);
    }


    /**
     * @return clone of internal configuration properties
     */
    public GlassFishLogManagerConfiguration getConfiguration() {
        return this.configuration.clone();
    }


    /**
     * {@inheritDoc}
     * @return false to force caller to refind the new logger, true to inform him that we did not add it.
     */
    @Override
    public boolean addLogger(final Logger logger) {
        Objects.requireNonNull(logger, "logger is null");
        Objects.requireNonNull(logger.getName(), "logger.name is null");
        trace(getClass(), () -> "addLogger(logger.name=" + logger.getName() + ")");

        if (getLoggingStatus().ordinal() < GlassFishLoggingStatus.CONFIGURING.ordinal()) {
            try {
                // initialization of system loggers in LogManager.ensureLogManagerInitialized
                // ignores output of addLogger. That's why we use wrappers.
                if (ROOT_LOGGER_NAME.equals(logger.getName())) {
                    trace(getClass(), () -> "System root logger catched: " + logger + ")");
                    this.systemRootLogger = new GlassFishLoggerWrapper(logger);
                    // do not add system logger to user context. Create own root instead.
                    // reason: LogManager.ensureLogManagerInitialized ignores result of addLogger,
                    // so there is no way to override it. So leave it alone.
                    this.userRootLogger = new GlassFishLogger(ROOT_LOGGER_NAME);
                    return callJULAddLogger(userRootLogger);
                }
                if (Logger.GLOBAL_LOGGER_NAME.equals(logger.getName())) {
                    trace(getClass(), () -> "System global logger catched: " + logger + ")");
                    this.globalLogger = new GlassFishLoggerWrapper(Logger.getGlobal());
                    return callJULAddLogger(globalLogger);
                }
            } finally {
                // if we go directly through constructor without initialize(cfg)
                if (this.systemRootLogger != null && this.globalLogger != null
                    && getLoggingStatus() == GlassFishLoggingStatus.UNINITIALIZED) {
                    doFirstInitialization(provideProperties());
                }
            }
        }

        final GlassFishLogger replacementLogger = replaceWithGlassFishLogger(logger);
        final boolean loggerAdded = callJULAddLogger(replacementLogger);
        if (loggerAdded && replacementLogger.getParent() == null
            && !ROOT_LOGGER_NAME.equals(replacementLogger.getName())) {
            replacementLogger.setParent(getRootLogger());
        }
        // getLogger must refetch if we wrapped the original instance.
        // note: JUL ignores output for system loggers
        return loggerAdded && replacementLogger == logger;
    }


    @Override
    public GlassFishLogger getLogger(final String name) {
        trace(getClass(), "getLogger(name=" + name + ")");
        Objects.requireNonNull(name, "logger name is null");
        // we are hiding the real root and global loggers, because they cannot be overriden
        // directly by GlassFishLogger
        if (ROOT_LOGGER_NAME.equals(name)) {
            return getRootLogger();
        }
        if (Logger.GLOBAL_LOGGER_NAME.equals(name)) {
            return this.globalLogger;
        }
        final Logger logger = super.getLogger(name);
        if (logger == null) {
            return null;
        }
        if (logger instanceof GlassFishLogger) {
            return (GlassFishLogger) logger;
        }
        // First request to Logger.getLogger calls LogManager.demandLogger which calls
        // addLogger, which caches the logger and can be overriden, but returns unwrapped
        // logger.
        // Second request is from the cache OR is a special logger like the global logger.
        return ensureGlassFishLoggerOrWrap(super.getLogger(name));
    }


    /**
     * Don't use this method, it will not do anything in most cases.
     * It is used just by the {@link LogManager} on startup and removes all handlers.
     */
    @Override
    @Deprecated
    public void reset() {
        LOCK.lock();
        try {
            // reset causes closing of current handlers
            // reset is invoked automatically also in the begining of super.readConfiguration(is).
            if (RESET_PROTECTION.get()) {
                stacktrace(GlassFishLogManager.class, "reset() ignored.");
                return;
            }
            super.reset();
            trace(getClass(), "reset() done.");
        } finally {
            LOCK.unlock();
        }
    }


    /**
     * Does nothing! (Except it can lock caller thread)
     */
    @Override
    @Deprecated
    public void readConfiguration() throws SecurityException, IOException {
        LOCK.lock();
        try {
            trace(getClass(), "readConfiguration() ignored.");
        } finally {
            LOCK.unlock();
        }
    }


    /**
     * Don't use this method, it is here just for the {@link LogManager}.
     * Use {@link #reconfigure(GlassFishLogManagerConfiguration)} instead.
     */
    @Override
    @Deprecated
    public void readConfiguration(final InputStream input) throws SecurityException, IOException {
        LOCK.lock();
        try {
            trace(getClass(), () -> "readConfiguration(ins=" + input + ")");
            this.configuration = GlassFishLogManagerConfiguration.parse(input);
            trace(getClass(), "readConfiguration(input) done.");
        } finally {
            LOCK.unlock();
        }
    }


    /**
     * Does nothing!
     */
    @Override
    @Deprecated
    public void updateConfiguration(Function<String, BiFunction<String, String, String>> mapper) throws IOException {
        trace(getClass(), "updateConfiguration(mapper) ignored.");
    }


    /**
     * Does nothing!
     */
    @Override
    @Deprecated
    public void updateConfiguration(InputStream ins, Function<String, BiFunction<String, String, String>> mapper)
        throws IOException {
        trace(getClass(), "updateConfiguration(ins, mapper) ignored.");
    }


    /**
     * @return {@link GlassFishLoggingStatus}, never null
     */
    public GlassFishLoggingStatus getLoggingStatus() {
        return status;
    }


    /**
     * @return all loggers currently managed by thus log manager (only from user context)
     */
    public List<GlassFishLogger> getAllLoggers() {
        return Collections.list(getLoggerNames()).stream().map(this::getLogger).filter(Objects::nonNull)
            .collect(Collectors.toList());
    }


    /**
     * @return all handlers currently managed by this log manager (only from user context)
     */
    public List<Handler> getAllHandlers() {
        final Function<GlassFishLogger, Stream<Handler>> toHandler = logger -> Arrays.stream(logger.getHandlers());
        return Collections.list(getLoggerNames()).stream().map(this::getLogger).filter(Objects::nonNull)
            .flatMap(toHandler).collect(Collectors.toList());
    }


    /**
     * @return can be null only when {@link LogManager} does initialization.
     */
    public GlassFishLogger getRootLogger() {
        return this.userRootLogger;
    }


    /**
     * Reconfigures the logging system.
     *
     * @param cfg
     */
    public void reconfigure(final GlassFishLogManagerConfiguration cfg) {
        reconfigure(cfg, null, null);
    }


    /**
     * Reconfigures the logging system.
     *
     * @param cfg
     * @param reconfigureAction - a callback executed after the reconfiguration of logger levels is
     *            finished. This action may perform some programmatic configuration.
     * @param flushAction - a callback executed after reconfigureAction to flush program's
     *            {@link LogRecord} buffers waiting until the reconfiguration is completed.
     */
    public void reconfigure(final GlassFishLogManagerConfiguration cfg, final Action reconfigureAction,
        final Action flushAction) {
        LOCK.lock();
        final long start = System.nanoTime();
        try {
            trace(getClass(), () -> "reconfigure(cfg, action, action); Configuration:\n" + cfg
                + "\n reconfigureAction: " + reconfigureAction + "\n flushAction: " + flushAction);
            if (cfg.isTracingEnabled()) {
                // if enabled, start immediately. If not, don't change it yet, it could be set by
                // JVM option.
                setTracingEnabled(cfg.isTracingEnabled());
            }
            setStatus(GlassFishLoggingStatus.CONFIGURING);
            this.configuration = cfg;
            final ConfigurationHelper configurationHelper = getConfigurationHelper();
            setReleaseParametersEarly(
                configurationHelper.getBoolean(KEY_RELEASE_PARAMETERS_EARLY, isReleaseParametersEarly()));
            setResolveLevelWithIncompleteConfiguration(configurationHelper.getBoolean(
                KEY_RESOLVE_LEVEL_WITH_INCOMPLETE_CONFIGURATION, isResolveLevelWithIncompleteConfiguration()));
            // it is used to configure new objects in LogManager class
            final Thread currentThread = Thread.currentThread();
            final ClassLoader originalCL = currentThread.getContextClassLoader();
            trace(GlassFishLogManager.class, "Reconfiguring logger levels...");
            final Enumeration<String> existingLoggerNames = getLoggerNames();
            while (existingLoggerNames.hasMoreElements()) {
                final String existingLoggerName = existingLoggerNames.nextElement();
                if (ROOT_LOGGER_NAME.equals(existingLoggerName)) {
                    this.systemRootLogger.setLevel(getLevel(KEY_SYS_ROOT_LOGGER_LEVEL, Level.INFO));
                    this.userRootLogger.setLevel(getLevel(KEY_USR_ROOT_LOGGER_LEVEL, Level.INFO));
                    continue;
                }
                final GlassFishLogger logger = getLogger(existingLoggerName);
                if (logger != null) {
                    final Level level = getLevel(existingLoggerName + ".level", null);
                    trace(getClass(), "Configuring logger level for '" + existingLoggerName + "' to '" + level + "'");
                    // null means inherit from parent
                    logger.setLevel(level);
                }
            }
            trace(getClass(), "Updated logger levels successfully.");

            initializeRootLoggers();
            if (reconfigureAction != null) {
                try {
                    currentThread.setContextClassLoader(reconfigureAction.getClassLoader());
                    reconfigureAction.run();
                } finally {
                    currentThread.setContextClassLoader(originalCL);
                }
            }

            final Predicate<Handler> isReadyPredicate = h -> !ExternallyManagedLogHandler.class.isInstance(h)
                || ExternallyManagedLogHandler.class.cast(h).isReady();
            final List<Handler> handlers = getAllHandlers();
            if (handlers.isEmpty() || handlers.stream().allMatch(isReadyPredicate)) {
                setStatus(GlassFishLoggingStatus.FLUSHING_BUFFERS);
                if (flushAction != null) {
                    try {
                        currentThread.setContextClassLoader(flushAction.getClassLoader());
                        flushAction.run();
                    } finally {
                        currentThread.setContextClassLoader(originalCL);
                    }
                }
                final StartupQueue queue = StartupQueue.getInstance();
                trace(getClass(), () -> "Count of records waiting in the queue: " + queue.getSize());
                queue.toStream().forEach(o -> o.getLogger().checkAndLog(o.getRecord()));
                queue.reset();
                setStatus(GlassFishLoggingStatus.FULL_SERVICE);
            }
        } finally {
            trace(getClass(), "Reconfiguration finished in " + (System.nanoTime() - start) + " ns");
            // regardless of the result, set tracing.
            setTracingEnabled(cfg.isTracingEnabled());
            LOCK.unlock();
        }
    }


    /**
     * Closes all {@link ExternallyManagedLogHandler} instances managed by this log manager.
     * Should be called ie. by shutdown hooks to release all injected dependencies.
     * Handlers must stop processing records after that.
     */
    public void closeAllExternallyManagedLogHandlers() {
        trace(GlassFishLogManager.class, "closeAllExternallyManagedLogHandlers()");
        final List<GlassFishLogger> loggers = getAllLoggers();
        // single handler instance can be used by more loggers
        final Set<Handler> handlersToClose = new HashSet<>();
        final Consumer<GlassFishLogger> remover = logger -> {
            final List<Handler> handlers = logger.getHandlers(ExternallyManagedLogHandler.class);
            handlersToClose.addAll(handlers);
            handlers.forEach(logger::removeHandler);
        };
        loggers.forEach(remover);
        trace(getClass(), () -> "Handlers to be closed: " + handlersToClose);
        handlersToClose.forEach(Handler::close);
    }


    private boolean callJULAddLogger(final GlassFishLogger newLogger) {
        if (System.getSecurityManager() == null) {
            return super.addLogger(newLogger);
        }
        // CORBA and IMQ stand aside
        PrivilegedAction<Boolean> action = () -> super.addLogger(newLogger);
        return AccessController.doPrivileged(action);
    }


    private void setStatus(final GlassFishLoggingStatus status) {
        trace(getClass(), () -> "setLoggingStatus(status=" + status + ")");
        GlassFishLogManager.status = status;
    }


    private static GlassFishLogger replaceWithGlassFishLogger(final Logger logger) {
        trace(GlassFishLogManager.class, "replaceWithGlassFishLogger(" + logger.getName() + ")");
        if (logger instanceof GlassFishLogger) {
            return (GlassFishLogger) logger;
        }
        return new GlassFishLogger(logger);
    }


    /**
     * This is a failsafe method to wrapp any logger which would miss standard mechanisms.
     * Invocation of this method would mean that something changed in JDK implementation
     * and this module must be updated.
     * <p>
     * Prints error to STDERR if the logger is not a {@link GlassFishLogger} instance and wraps
     * it to {@link GlassFishLoggerWrapper}.
     *
     * @param logger
     * @return {@link GlassFishLogger} or {@link GlassFishLoggerWrapper}
     */
    private GlassFishLogger ensureGlassFishLoggerOrWrap(final Logger logger) {
        if (logger instanceof GlassFishLogger) {
            return (GlassFishLogger) logger;
        }
        error(getClass(), "Emergency wrapping logger!", new RuntimeException());
        return new GlassFishLoggerWrapper(logger);
    }


    private void doFirstInitialization(final LoggingProperties properties) {
        trace(getClass(), () -> "Initializing logManager: " + this);
        try {
            RESET_PROTECTION.set(false);
            setStatus(GlassFishLoggingStatus.UNCONFIGURED);
            this.configuration = new GlassFishLogManagerConfiguration(properties);
            this.globalLogger.setParent(this.userRootLogger);
            initializeRootLoggers();
            reconfigure(this.configuration);
        } catch (final Exception e) {
            error(getClass(), "Initialization of " + this + " failed!", e);
            throw e;
        } finally {
            RESET_PROTECTION.set(true);
        }
    }


    private void initializeRootLoggers() {
        trace(getClass(), "initializeRootLoggers()");
        final GlassFishLogger referenceLogger = getRootLogger();
        final List<String> requestedHandlerNames = getConfigurationHelper().getList(KEY_ROOT_HANDLERS, null);
        final List<Handler> currentHandlers = Arrays.asList(referenceLogger.getHandlers());

        final List<Handler> handlersToAdd = new ArrayList<>();
        final List<Handler> handlersToRemove = new ArrayList<>();
        for (final String handlerClass : requestedHandlerNames) {
            if (currentHandlers.stream().noneMatch(h -> h.getClass().getName().equals(handlerClass))) {
                final Handler newHandler = create(handlerClass);
                if (newHandler != null) {
                    handlersToAdd.add(newHandler);
                }
            }
            final List<Handler> existingToReinstantiate = currentHandlers.stream()
                .filter(h -> h.getClass().getName().equals(handlerClass)
                    && !ExternallyManagedLogHandler.class.isAssignableFrom(h.getClass()))
                .collect(Collectors.toList());
            handlersToRemove.addAll(existingToReinstantiate);
            final Function<Handler, Handler> mapper = h -> create(h.getClass().getName());
            handlersToAdd.addAll(
                existingToReinstantiate.stream().map(mapper).filter(Objects::nonNull).collect(Collectors.toList()));
        }

        final Level systemRootLevel = getLevel(KEY_SYS_ROOT_LOGGER_LEVEL, Level.INFO);
        final Level rootLoggerLevel = getLevel(KEY_USR_ROOT_LOGGER_LEVEL, Level.INFO);
        // both loggers use same handler set.
        configureRootLogger(systemRootLogger, systemRootLevel, requestedHandlerNames, handlersToRemove, handlersToAdd);
        configureRootLogger(userRootLogger, rootLoggerLevel, requestedHandlerNames, handlersToRemove, handlersToAdd);
        setMissingParentToRootLogger(userRootLogger);
    }


    private ConfigurationHelper getConfigurationHelper() {
        return new ConfigurationHelper(null,  ConfigurationHelper.ERROR_HANDLER_PRINT_TO_STDERR);
    }


    private void setMissingParentToRootLogger(final GlassFishLogger rootParentLogger) {
        final Enumeration<String> names = getLoggerNames();
        while (names.hasMoreElements()) {
            final String name = names.nextElement();
            final GlassFishLogger logger = getLogger(name);
            if (logger != null && logger.getParent() == null && !ROOT_LOGGER_NAME.equals(logger.getName())) {
                error(getClass(), "Setting parent to logger: " + logger.getName() + "/" + logger);
                logger.setParent(rootParentLogger);
            }
        }
    }


    private void configureRootLogger(final GlassFishLogger rootLogger, final Level level,
        final List<String> requestedHandlers, final List<Handler> handlersToRemove, final List<Handler> handlersToAdd) {
        trace(getClass(), () -> "configureRootLogger(rootLogger=" + rootLogger
            + ", level=" + level + ", requestedHandlers=" + requestedHandlers + ")");
        rootLogger.setLevel(level);
        final List<Handler> currentHandlers = Arrays.asList(rootLogger.getHandlers());
        if (requestedHandlers == null || requestedHandlers.isEmpty()) {
            error(getClass(), "No handlers set for the root logger!");
            return;
        }
        for (final Handler handler : handlersToRemove) {
            rootLogger.removeHandler(handler);
            handler.close();
        }
        for (final Handler handler : currentHandlers) {
            if (requestedHandlers.stream().noneMatch(name -> name.equals(handler.getClass().getName()))) {
                rootLogger.removeHandler(handler);
                handler.close();
            }
        }
        for (final Handler handler : handlersToAdd) {
            rootLogger.addHandler(handler);
        }
    }


    private Level getLevel(final GlassFishLogManagerProperty property, final Level defaultLevel) {
        return getLevel(property.getPropertyName(), defaultLevel);
    }


    private Level getLevel(final String property, final Level defaultLevel) {
        final String levelProperty = getProperty(property);
        if (levelProperty == null || levelProperty.isEmpty()) {
            return defaultLevel;
        }
        try {
            return Level.parse(levelProperty);
        } catch (final IllegalArgumentException e) {
            error(getClass(), "Could not parse level " + levelProperty + ", returning " + defaultLevel + ".", e);
            return defaultLevel;
        }
    }


    @SuppressWarnings("unchecked") // always safe
    private static <T> T create(final String clazz) {
        trace(GlassFishLogManager.class, () -> "create(clazz=" + clazz + ")");
        try {
            // JUL uses SystemClassloader, so with custom formatters always fallbacks to defaults
            // Don't use ConsoleHandler with custom formatters, use SimpleLogHandler instead
            return (T) Class.forName(clazz).getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            error(GlassFishLogManager.class, "Could not create " + clazz, e);
            return null;
        }
    }


    private static LoggingProperties ensureSortedProperties(final Properties properties) {
        if (properties == null) {
            return provideProperties();
        }
        if (properties instanceof LoggingProperties) {
            return (LoggingProperties) properties;
        }
        return new LoggingProperties(properties);
    }


    private static LoggingProperties provideProperties() {
        try {
            final LoggingProperties propertiesFromJvmOption = toProperties(System.getProperty(JVM_OPT_LOGGING_CFG_FILE));
            if (propertiesFromJvmOption != null) {
                return propertiesFromJvmOption;
            }
            final LoggingProperties propertiesFromClasspath = loadFromClasspath();
            if (propertiesFromClasspath != null) {
                return propertiesFromClasspath;
            }
            if (Boolean.getBoolean(JVM_OPT_LOGGING_CFG_USE_DEFAULTS)) {
                return createDefaultProperties();
            }
            throw new IllegalStateException(
                "Could not find any logging.properties configuration file neither from JVM option ("
                    + JVM_OPT_LOGGING_CFG_FILE + ") nor from classpath and even " + JVM_OPT_LOGGING_CFG_USE_DEFAULTS
                    + " wasn't set to true.");
        } catch (final IOException e) {
            throw new IllegalStateException("Could not load logging configuration file.", e);
        }
    }


    private static LoggingProperties createDefaultProperties() {
        final LoggingProperties cfg = new LoggingProperties();
        final String level = System.getProperty(JVM_OPT_LOGGING_CFG_DEFAULT_LEVEL, Level.INFO.getName());
        cfg.setProperty(KEY_SYS_ROOT_LOGGER_LEVEL.getPropertyName(), level);
        cfg.setProperty(KEY_USR_ROOT_LOGGER_LEVEL.getPropertyName(), level);
        cfg.setProperty(KEY_ROOT_HANDLERS.getPropertyName(), SimpleLogHandler.class.getName());
        cfg.setProperty(SimpleLogHandlerProperty.LEVEL.getPropertyFullName(), level);
        return cfg;
    }


    private static LoggingProperties toProperties(final String absolutePath) throws IOException {
        if (absolutePath == null) {
            return null;
        }
        final File file = new File(absolutePath);
        if (!file.canRead()) {
            return null;
        }
        return LoggingProperties.loadFrom(file);
    }


    private static LoggingProperties loadFromClasspath() throws IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        trace(GlassFishLogManager.class, () -> "loadFromClasspath(); classloader: " + classLoader);
        try (InputStream input = classLoader.getResourceAsStream("logging.properties")) {
            if (input == null) {
                return null;
            }
            return LoggingProperties.loadFrom(input);
        }
    }


    /**
     * Action to be performed when client calls
     * {@link GlassFishLogManager#reconfigure(GlassFishLogManagerConfiguration, Action, Action)}
     */
    @FunctionalInterface
    public interface Action {

        /**
         * Custom action to be performed when executing the reconfiguration.
         */
        void run();


        /**
         * @return thread context classloader; can be overriden.
         */
        default ClassLoader getClassLoader() {
            return Thread.currentThread().getContextClassLoader();
        }
    }
}
