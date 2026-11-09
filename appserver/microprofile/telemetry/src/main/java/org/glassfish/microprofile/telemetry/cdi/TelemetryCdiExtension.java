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
package org.glassfish.microprofile.telemetry.cdi;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.instrumentation.annotations.WithSpan;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.AfterDeploymentValidation;
import jakarta.enterprise.inject.spi.BeforeShutdown;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.inject.spi.ProcessAnnotatedType;
import jakarta.enterprise.inject.spi.WithAnnotations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.microprofile.telemetry.TelemetryInstances;
import org.glassfish.microprofile.telemetry.runtime.OpenTelemetryLogHandler;

import static org.glassfish.microprofile.telemetry.TelemetryInstances.INSTRUMENTATION_NAME;

/**
 * Registers the OpenTelemetry instance of the application, configured from its MicroProfile
 * Config, provides the OpenTelemetry beans and binds the {@link WithSpan} interceptor.
 * <p>
 * The beans are synthetic beans rather than annotated types of this module, so that no bean
 * archive is created for the module in the application.
 */
public class TelemetryCdiExtension implements Extension {

    private static final String OTEL_PREFIX = "otel.";

    /** Set up for the applications using MicroProfile Config or MicroProfile REST Client. */
    private static final String CONFIG_CONTAINER = "org.glassfish.microprofile.config.ConfigContainer";

    private volatile ClassLoader applicationClassLoader;
    private volatile Supplier<OpenTelemetry> openTelemetry = OpenTelemetry::noop;

    <T> void bindWithSpan(@Observes @WithAnnotations(WithSpan.class) ProcessAnnotatedType<T> event) {
        event.configureAnnotatedType()
            .filterMethods(method -> method.isAnnotationPresent(WithSpan.class))
            .forEach(method -> method.add(WithSpanBinding.Literal.INSTANCE));
    }

    void registerBeans(@Observes AfterBeanDiscovery event) {
        event.addBean()
            .types(Object.class, OpenTelemetry.class)
            .scope(ApplicationScoped.class)
            .beanClass(TelemetryCdiExtension.class)
            .id(TelemetryCdiExtension.class.getName() + ".OpenTelemetry")
            .createWith(context -> openTelemetry.get());
        event.addBean()
            .types(Object.class, Tracer.class)
            .scope(ApplicationScoped.class)
            .beanClass(TelemetryCdiExtension.class)
            .id(TelemetryCdiExtension.class.getName() + ".Tracer")
            .createWith(context -> openTelemetry.get().getTracer(INSTRUMENTATION_NAME));
        event.addBean()
            .types(Object.class, Meter.class)
            .scope(ApplicationScoped.class)
            .beanClass(TelemetryCdiExtension.class)
            .id(TelemetryCdiExtension.class.getName() + ".Meter")
            .createWith(context -> openTelemetry.get().getMeter(INSTRUMENTATION_NAME));
        // Always delegate to the current span and baggage, like Span.current() and Baggage.current()
        event.addBean()
            .types(Object.class, Span.class)
            .scope(Dependent.class)
            .beanClass(TelemetryCdiExtension.class)
            .id(TelemetryCdiExtension.class.getName() + ".Span")
            .createWith(context -> CurrentSpan.INSTANCE);
        event.addBean()
            .types(Object.class, Baggage.class)
            .scope(Dependent.class)
            .beanClass(TelemetryCdiExtension.class)
            .id(TelemetryCdiExtension.class.getName() + ".Baggage")
            .createWith(context -> CurrentBaggage.INSTANCE);
        event.addBean(new WithSpanInterceptor(() -> openTelemetry.get().getTracer(INSTRUMENTATION_NAME)));
    }

    void registerOpenTelemetry(@Observes AfterDeploymentValidation event) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        applicationClassLoader = classLoader;
        // MicroProfile Config is set up for the application only if it uses it. Otherwise the otel.*
        // properties can only come from system properties and environment variables, visible to the
        // runtime: then the runtime instance is used. The configuration is read when the SDK is
        // created, not during the bootstrap of the application.
        String applicationName = getApplicationName();
        Supplier<Map<String, String>> properties = isMicroProfileConfigEnabled(applicationName)
            ? () -> readOtelProperties(classLoader)
            : Map::of;
        openTelemetry = TelemetryInstances.registerApplication(classLoader, properties, applicationName);
        OpenTelemetryLogHandler.install();
    }

    /**
     * Creates the SDK when the application starts, so that the application telemetry, like logs,
     * does not depend on the first use of the OpenTelemetry beans.
     */
    void createOpenTelemetry(@Observes @Initialized(ApplicationScoped.class) Object event) {
        openTelemetry.get();
    }

    void unregisterOpenTelemetry(@Observes BeforeShutdown event) {
        ClassLoader classLoader = applicationClassLoader;
        if (classLoader != null) {
            TelemetryInstances.unregisterApplication(classLoader);
        }
        openTelemetry = OpenTelemetry::noop;
    }

    private static boolean isMicroProfileConfigEnabled(String applicationName) {
        ApplicationRegistry registry = applicationName == null ? null : Globals.get(ApplicationRegistry.class);
        ApplicationInfo application = registry == null ? null : registry.get(applicationName);
        if (application == null) {
            return false;
        }
        for (Sniffer sniffer : application.getSniffers()) {
            String[] containers = sniffer.getContainersNames();
            if (containers != null && Arrays.asList(containers).contains(CONFIG_CONTAINER)) {
                return true;
            }
        }
        return false;
    }

    private static Map<String, String> readOtelProperties(ClassLoader classLoader) {
        Config config = ConfigProvider.getConfig(classLoader);
        Map<String, String> properties = new HashMap<>();
        for (String name : config.getPropertyNames()) {
            if (name.startsWith(OTEL_PREFIX)) {
                config.getOptionalValue(name, String.class).ifPresent(value -> properties.put(name, value));
            }
        }
        return properties;
    }

    private static String getApplicationName() {
        InvocationManager invocationManager = Globals.get(InvocationManager.class);
        ComponentInvocation invocation = invocationManager == null ? null : invocationManager.getCurrentInvocation();
        return invocation == null ? null : invocation.getAppName();
    }
}
