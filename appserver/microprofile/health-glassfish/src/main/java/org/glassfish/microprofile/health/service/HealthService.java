/*
 * Copyright (c) 2024, 2026 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.health.service;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.VirtualServer;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.microprofile.health.GlassFishHealthCheckResponse;
import org.glassfish.microprofile.health.HealthCheckInfo;
import org.glassfish.microprofile.health.HealthReporter;
import org.jvnet.hk2.annotations.Service;

/**
 * Registers the server-wide {@link HealthReporter} and the MicroProfile Health endpoint at startup,
 * so that the endpoint works also when no application is deployed or no deployed application uses
 * CDI, and removes health checks of undeployed applications.
 * <p>
 * Until the server is started, the startup and readiness reports contain a health check with the
 * status defined by the {@code mp.health.default.startup.empty.response} and
 * {@code mp.health.default.readiness.empty.response} properties ({@code DOWN} by default). After
 * that, the readiness report contains such a health check while no application is deployed.
 */
@Service(name = "healthcheck-service")
@RunLevel(StartupRunLevel.VAL)
public class HealthService implements EventListener, PostConstruct, PreDestroy {

    static final String DEFAULT_CONTEXT_PATH = "/health";

    static final String STARTUP_CHECK_NAME = "glassfish-server-started";
    static final String READINESS_CHECK_NAME = "glassfish-applications-ready";

    private static final String MICROPROFILE_HEALTH_ENABLED = "org.glassfish.microprofile.health.enabled";
    private static final String MICROPROFILE_HEALTH_CONTEXT_PATH = "org.glassfish.microprofile.health.context-path";
    private static final String MP_DEFAULT_STARTUP_EMPTY_RESPONSE = "mp.health.default.startup.empty.response";
    private static final String MP_DEFAULT_READINESS_EMPTY_RESPONSE = "mp.health.default.readiness.empty.response";
    private static final String ADMIN_VIRTUAL_SERVER = "__asadmin";
    private static final String REASON_KEY = "reason";

    private static final Logger LOGGER = Logger.getLogger(HealthService.class.getName());

    @Inject
    Events events;

    @Inject
    ServiceLocator serviceLocator;

    @Inject
    RequestDispatcher requestDispatcher;

    @Inject
    ClassLoaderHierarchy classLoaderHierarchy;

    @Inject
    ApplicationRegistry applicationRegistry;

    @Inject
    Domain domain;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    private HealthReporter healthReporter;
    private HealthHttpHandler httpHandler;
    private String contextPath;
    private volatile boolean serverReady;
    private volatile String lastInvalidValue;

    @Override
    public void postConstruct() {
        healthReporter = serviceLocator.getService(HealthReporter.class);
        if (healthReporter == null) {
            ServiceLocatorUtilities.addClasses(serviceLocator, true, HealthReporter.class);
            healthReporter = serviceLocator.getService(HealthReporter.class);
        }
        healthReporter.setServerHealthChecks(this::getServerHealthChecks);
        events.register(this);

        if (Boolean.parseBoolean(System.getProperty(MICROPROFILE_HEALTH_ENABLED, "true"))) {
            contextPath = toContextPath(System.getProperty(MICROPROFILE_HEALTH_CONTEXT_PATH));
            httpHandler = new HealthHttpHandler(healthReporter, contextPath, classLoaderHierarchy.getCommonClassLoader());
            registerEndpoint();
        } else {
            LOGGER.info("MicroProfile Health is disabled");
        }
    }

    @Override
    public void preDestroy() {
        events.unregister(this);
        if (httpHandler != null) {
            try {
                requestDispatcher.unregisterEndpoint(contextPath);
            } catch (EndpointRegistrationException e) {
                LOGGER.log(Level.WARNING, "Unable to unregister the MicroProfile Health endpoint", e);
            }
        }
    }

    @Override
    public void event(Event<?> event) {
        if (event.is(EventTypes.SERVER_READY)) {
            serverReady = true;
        } else if (event.is(Deployment.APPLICATION_STARTED)) {
            // An application deployed to the context path of the endpoint replaced it
            registerEndpoint();
        } else if (event.is(Deployment.APPLICATION_UNLOADED) && event.hook() instanceof ApplicationInfo appInfo) {
            healthReporter.removeAllHealthChecksFrom(appInfo.getName());
            // An application deployed to the context path of the endpoint removed it on undeployment
            registerEndpoint();
        }
    }

    /**
     * Converts the value of the {@value #MICROPROFILE_HEALTH_CONTEXT_PATH} system property
     * to a context path.
     *
     * @param value the property value, may be {@code null}
     * @return context path starting with a slash and not ending with a slash
     */
    static String toContextPath(String value) {
        if (value == null) {
            return DEFAULT_CONTEXT_PATH;
        }
        String path = value.strip();
        while (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            LOGGER.log(Level.WARNING, "Invalid value {0} of the {1} system property, using {2}",
                new Object[] {value, MICROPROFILE_HEALTH_CONTEXT_PATH, DEFAULT_CONTEXT_PATH});
            return DEFAULT_CONTEXT_PATH;
        }
        return path.startsWith("/") ? path : "/" + path;
    }

    private void registerEndpoint() {
        if (httpHandler == null) {
            return;
        }
        List<String> virtualServers = new ArrayList<>();
        for (VirtualServer virtualServer : config.getHttpService().getVirtualServer()) {
            if (!ADMIN_VIRTUAL_SERVER.equals(virtualServer.getId())) {
                virtualServers.add(virtualServer.getId());
            }
        }
        try {
            requestDispatcher.registerEndpoint(contextPath, virtualServers, httpHandler, null);
        } catch (EndpointRegistrationException e) {
            LOGGER.log(Level.WARNING, "Unable to register the MicroProfile Health endpoint " + contextPath, e);
        }
    }

    private Collection<HealthCheckInfo> getServerHealthChecks() {
        List<HealthCheckInfo> healthChecks = new ArrayList<>(2);
        if (!serverReady) {
            addEmptyResponseCheck(healthChecks, HealthCheckInfo.Kind.STARTUP, STARTUP_CHECK_NAME,
                MP_DEFAULT_STARTUP_EMPTY_RESPONSE, "The server is starting");
            addEmptyResponseCheck(healthChecks, HealthCheckInfo.Kind.READY, READINESS_CHECK_NAME,
                MP_DEFAULT_READINESS_EMPTY_RESPONSE, "The server is starting");
        } else if (!isAnyApplicationLoaded()) {
            addEmptyResponseCheck(healthChecks, HealthCheckInfo.Kind.READY, READINESS_CHECK_NAME,
                MP_DEFAULT_READINESS_EMPTY_RESPONSE, "No application is deployed");
        }
        return healthChecks;
    }

    private void addEmptyResponseCheck(List<HealthCheckInfo> healthChecks, HealthCheckInfo.Kind kind, String name,
        String propertyName, String reason) {
        HealthCheckResponse.Status status = getEmptyResponseStatus(propertyName);
        if (status == HealthCheckResponse.Status.UP) {
            return;
        }
        HealthCheckResponse response = new GlassFishHealthCheckResponse(name, status,
            Optional.of(Map.of(REASON_KEY, reason)));
        healthChecks.add(new HealthCheckInfo(() -> response, EnumSet.of(kind)));
    }

    private HealthCheckResponse.Status getEmptyResponseStatus(String propertyName) {
        Optional<String> value = getProperty(propertyName).map(String::strip);
        if (value.isEmpty()) {
            return HealthCheckResponse.Status.DOWN;
        }
        try {
            return HealthCheckResponse.Status.valueOf(value.get());
        } catch (IllegalArgumentException e) {
            if (!value.get().equals(lastInvalidValue)) {
                lastInvalidValue = value.get();
                LOGGER.log(Level.WARNING, "Invalid value {0} of the {1} property, using DOWN",
                    new Object[] {value.get(), propertyName});
            }
            return HealthCheckResponse.Status.DOWN;
        }
    }

    /**
     * Reads the property from MicroProfile Config. MicroProfile Config is initialized only when
     * the first application using it is deployed. Until then, the property is read from its
     * default configuration sources, system properties and environment variables.
     */
    private Optional<String> getProperty(String propertyName) {
        try {
            return ConfigProvider.getConfig(classLoaderHierarchy.getCommonClassLoader())
                .getOptionalValue(propertyName, String.class);
        } catch (IllegalStateException e) {
            LOGGER.log(Level.FINEST, "MicroProfile Config is not available", e);
        }
        String value = System.getProperty(propertyName);
        if (value == null) {
            value = getEnvironmentVariable(propertyName);
        }
        return Optional.ofNullable(value);
    }

    /**
     * Looks up the environment variable as defined by MicroProfile Config: the exact name, then
     * the name with all non-alphanumeric characters replaced by {@code _}, then the same in upper case.
     */
    static String getEnvironmentVariable(String propertyName) {
        String value = System.getenv(propertyName);
        if (value == null) {
            String sanitizedName = propertyName.replaceAll("[^a-zA-Z0-9_]", "_");
            value = System.getenv(sanitizedName);
            if (value == null) {
                value = System.getenv(sanitizedName.toUpperCase(Locale.ROOT));
            }
        }
        return value;
    }

    private boolean isAnyApplicationLoaded() {
        Applications applications = domain.getApplications();
        if (applications == null) {
            return false;
        }
        for (Application application : applications.getApplications()) {
            if (application.isLifecycleModule()) {
                continue;
            }
            ApplicationInfo applicationInfo = applicationRegistry.get(application.getName());
            if (applicationInfo != null && applicationInfo.isLoaded()) {
                return true;
            }
        }
        return false;
    }
}
