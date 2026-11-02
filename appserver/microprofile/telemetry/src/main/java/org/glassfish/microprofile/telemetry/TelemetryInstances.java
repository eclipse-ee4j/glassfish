/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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
package org.glassfish.microprofile.telemetry;

import com.sun.enterprise.security.ssl.SSLUtils;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.lang.System.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.glassfish.internal.api.Globals;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.INFO;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Holds the OpenTelemetry SDK instances used by the runtime and by the applications.
 * <p>
 * MicroProfile Telemetry defines two mutually exclusive modes:
 * <ul>
 * <li>If {@code otel.sdk.disabled=false} is visible to the runtime at startup (system property
 * or {@code OTEL_SDK_DISABLED} environment variable), a single SDK instance is created for the
 * runtime and used by all applications. Application configuration is ignored.
 * <li>Otherwise the runtime telemetry is disabled and every application gets its own SDK
 * instance configured from its MicroProfile Config, disabled unless the application sets
 * {@code otel.sdk.disabled=false}.
 * </ul>
 * Application instances are looked up by class loader, walking up the parent chain, so that
 * a web module of an EAR finds the instance registered for the application.
 */
public final class TelemetryInstances {

    private static final Logger LOG = System.getLogger(TelemetryInstances.class.getName());

    /** Instrumentation scope name of the telemetry created by GlassFish. */
    public static final String INSTRUMENTATION_NAME = "org.glassfish.microprofile.telemetry";

    static final String SDK_DISABLED = "otel.sdk.disabled";
    static final String SDK_DISABLED_ENV = "OTEL_SDK_DISABLED";
    static final String SERVICE_NAME = "otel.service.name";
    static final String OTLP_PROTOCOL = "otel.exporter.otlp.protocol";

    private static final Map<ClassLoader, ApplicationInstance> APPLICATIONS = new ConcurrentHashMap<>();
    private static final Object RUNTIME_LOCK = new Object();

    private static volatile Boolean runtimeEnabled;
    private static volatile OpenTelemetry runtime;

    private TelemetryInstances() {
    }

    /**
     * @return {@code true} if {@code otel.sdk.disabled=false} is visible to the runtime, so
     *         that a single SDK instance is shared by the runtime and all applications.
     */
    public static boolean isRuntimeEnabled() {
        Boolean enabled = runtimeEnabled;
        if (enabled == null) {
            enabled = isFalse(System.getProperty(SDK_DISABLED, System.getenv(SDK_DISABLED_ENV)));
            runtimeEnabled = enabled;
        }
        return enabled;
    }

    /**
     * Returns the runtime SDK instance, creating it on the first call.
     *
     * @param serviceClassLoader class loader used to discover the SDK service providers
     * @return the runtime instance, or a no-op instance if the runtime telemetry is disabled.
     */
    public static OpenTelemetry runtime(ClassLoader serviceClassLoader) {
        if (!isRuntimeEnabled()) {
            return OpenTelemetry.noop();
        }
        OpenTelemetry instance = runtime;
        if (instance == null) {
            synchronized (RUNTIME_LOCK) {
                instance = runtime;
                if (instance == null) {
                    instance = createSdk(Map.of(), serviceClassLoader);
                    LOG.log(INFO, () -> "Created the runtime OpenTelemetry SDK instance shared by all applications.");
                    runtime = instance;
                }
            }
        }
        return instance;
    }

    /**
     * Finds the instance to use for code running with the given class loader.
     *
     * @param classLoader the application class loader, or any of its descendants
     * @return the runtime instance if enabled, the application instance if registered,
     *         otherwise a no-op instance.
     */
    public static OpenTelemetry forClassLoader(ClassLoader classLoader) {
        if (isRuntimeEnabled()) {
            OpenTelemetry instance = runtime;
            return instance == null ? runtime(classLoader) : instance;
        }
        if (APPLICATIONS.isEmpty()) {
            return OpenTelemetry.noop();
        }
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            ApplicationInstance instance = APPLICATIONS.get(loader);
            if (instance != null) {
                return instance.get();
            }
        }
        return OpenTelemetry.noop();
    }

    /**
     * Like {@link #forClassLoader(ClassLoader)}, but never creates an SDK instance.
     *
     * @param classLoader the application class loader, or any of its descendants
     * @return the instance if it was already created, otherwise a no-op instance.
     */
    public static OpenTelemetry forClassLoaderIfCreated(ClassLoader classLoader) {
        if (isRuntimeEnabled()) {
            OpenTelemetry instance = runtime;
            return instance == null ? OpenTelemetry.noop() : instance;
        }
        if (APPLICATIONS.isEmpty()) {
            return OpenTelemetry.noop();
        }
        for (ClassLoader loader = classLoader; loader != null; loader = loader.getParent()) {
            ApplicationInstance instance = APPLICATIONS.get(loader);
            if (instance != null) {
                OpenTelemetry created = instance.instance;
                return created == null ? OpenTelemetry.noop() : created;
            }
        }
        return OpenTelemetry.noop();
    }

    /**
     * @return the instance to use for code running in the current thread.
     */
    public static OpenTelemetry current() {
        return forClassLoader(Thread.currentThread().getContextClassLoader());
    }

    /**
     * @param openTelemetry an instance returned by this class
     * @return {@code false} for the no-op instance.
     */
    public static boolean isEnabled(OpenTelemetry openTelemetry) {
        return openTelemetry != OpenTelemetry.noop();
    }

    /**
     * Registers the SDK instance of an application, unless the runtime instance is enabled.
     * <p>
     * The SDK is created on the first use, typically in a request thread of the application: SDK
     * components provided by the application, like exporters looking up CDI beans, can only be
     * created there, and the SDK worker threads inherit the context class loader of that thread.
     *
     * @param classLoader the application class loader
     * @param properties supplier of the {@code otel.*} properties visible to the application
     * @param defaultServiceName the service name used when the application does not set one
     * @return the supplier of the instance the application must use
     */
    public static Supplier<OpenTelemetry> registerApplication(ClassLoader classLoader,
            Supplier<Map<String, String>> properties, String defaultServiceName) {
        if (isRuntimeEnabled()) {
            return () -> runtime(classLoader);
        }
        ApplicationInstance instance = APPLICATIONS.computeIfAbsent(classLoader,
            loader -> new ApplicationInstance(properties, defaultServiceName, loader));
        return instance::get;
    }

    /**
     * Removes and shuts down the SDK instance registered for the class loader.
     *
     * @param classLoader the application class loader
     */
    public static void unregisterApplication(ClassLoader classLoader) {
        ApplicationInstance instance = APPLICATIONS.remove(classLoader);
        if (instance != null) {
            instance.shutdown();
        }
    }

    /**
     * Shuts down the runtime SDK instance, if it was created.
     */
    public static void shutdownRuntime() {
        synchronized (RUNTIME_LOCK) {
            if (runtime instanceof OpenTelemetrySdk sdk) {
                shutdown(sdk);
            }
            runtime = null;
        }
    }

    private static OpenTelemetry createSdk(Map<String, String> properties, ClassLoader serviceClassLoader) {
        // GlassFish installs the default SSLContext, used by the OTLP HTTP sender, when SSLUtils is created
        if (Globals.getDefaultHabitat() != null) {
            Globals.get(SSLUtils.class);
        }
        return AutoConfiguredOpenTelemetrySdk.builder()
            // Only the HTTP sender is provided, the SDK defaults to gRPC
            .addPropertiesSupplier(() -> Map.of(OTLP_PROTOCOL, "http/protobuf"))
            .addPropertiesSupplier(() -> properties)
            .setServiceClassLoader(serviceClassLoader)
            .addSpanExporterCustomizer(
                (exporter, config) -> ContextClassLoaderExporters.wrap(exporter, serviceClassLoader))
            .addMetricExporterCustomizer(
                (exporter, config) -> ContextClassLoaderExporters.wrap(exporter, serviceClassLoader))
            .addLogRecordExporterCustomizer(
                (exporter, config) -> ContextClassLoaderExporters.wrap(exporter, serviceClassLoader))
            .disableShutdownHook()
            .build()
            .getOpenTelemetrySdk();
    }

    private static void shutdown(OpenTelemetrySdk sdk) {
        try {
            sdk.close();
        } catch (RuntimeException e) {
            LOG.log(WARNING, () -> "Failed to shut down the OpenTelemetry SDK instance.", e);
        }
    }

    /**
     * The SDK instance of an application, created on the first use.
     */
    private static final class ApplicationInstance {

        private final Supplier<Map<String, String>> properties;
        private final String defaultServiceName;
        private final ClassLoader classLoader;
        private volatile OpenTelemetry instance;
        private boolean shutdown;

        ApplicationInstance(Supplier<Map<String, String>> properties, String defaultServiceName,
            ClassLoader classLoader) {
            this.properties = properties;
            this.defaultServiceName = defaultServiceName;
            this.classLoader = classLoader;
        }

        OpenTelemetry get() {
            OpenTelemetry current = instance;
            if (current != null) {
                return current;
            }
            synchronized (this) {
                if (shutdown) {
                    return OpenTelemetry.noop();
                }
                if (instance == null) {
                    instance = create();
                }
                return instance;
            }
        }

        private OpenTelemetry create() {
            Map<String, String> config = new HashMap<>(properties.get());
            if (!isFalse(config.get(SDK_DISABLED))) {
                LOG.log(DEBUG, () -> "OpenTelemetry is disabled for application " + defaultServiceName);
                return OpenTelemetry.noop();
            }
            if (defaultServiceName != null) {
                config.putIfAbsent(SERVICE_NAME, defaultServiceName);
            }
            LOG.log(DEBUG, () -> "Creating OpenTelemetry SDK instance for application " + defaultServiceName);
            return createSdk(config, classLoader);
        }

        synchronized void shutdown() {
            shutdown = true;
            if (instance instanceof OpenTelemetrySdk sdk) {
                TelemetryInstances.shutdown(sdk);
            }
            instance = OpenTelemetry.noop();
        }
    }

    private static boolean isFalse(String value) {
        return value != null && "false".equalsIgnoreCase(value.trim());
    }
}
