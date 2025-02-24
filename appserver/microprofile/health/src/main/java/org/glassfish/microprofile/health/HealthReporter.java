/*
 * Copyright (c) 2024 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.health;

import jakarta.inject.Singleton;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;

@Singleton
public class HealthReporter {

    private static final String MP_DEFAULT_STARTUP_EMPTY_RESPONSE = "mp.health.default.startup.empty.response";
    private static final String MP_DEFAULT_READINESS_EMPTY_RESPONSE = "mp.health.default.readiness.empty.response";
    private static final String CONTEXT_KEY = "context";

    private static final Logger LOGGER = Logger.getLogger(HealthReporter.class.getName());

    private final Map<String, List<HealthCheckInfo>> applicationHealthChecks = new ConcurrentHashMap<>();

    private static HealthCheckResponse callHealthCheck(HealthCheck healthCheck) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(healthCheck.getClass().getClassLoader());
            return healthCheck.call();
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Health check failed", e);
            return buildHealthCheckResponse(healthCheck.getClass().getName(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }

    private static HealthCheckResponse addContextToResponse(HealthCheckResponse response, String contextName) {
        if (response instanceof GlassFishHealthCheckResponse gfResponse) {
            return gfResponse.addData(CONTEXT_KEY, contextName);
        } else {
            return response;
        }
    }
    private static HealthCheckResponse buildHealthCheckResponse(String name, Exception e) {
        return HealthCheckResponse.builder()
                .down()
                .name(name)
                .withData("rootCause", e.getMessage())
                .build();
    }

    public enum ReportKind {
        /**
         * Return only health checks of kind
         * {@link org.eclipse.microprofile.health.Liveness}
         */
        LIVE,
        /**
         * Return only health checks of kind
         * {@link org.eclipse.microprofile.health.Readiness}
         */
        READY,
        /**
         * Return only health checks of kind
         * {@link org.eclipse.microprofile.health.Startup}
         */
        STARTED,
        /**
         * Return all health checks
         */
        ALL;

        private HealthCheckResponse.Status getEmptyResponse() {
            return switch (this) {
                case LIVE ->
                    getValue(MP_DEFAULT_STARTUP_EMPTY_RESPONSE)
                    .orElse(HealthCheckResponse.Status.UP);
                case READY ->
                    getValue(MP_DEFAULT_READINESS_EMPTY_RESPONSE)
                    .orElse(HealthCheckResponse.Status.UP);
                case STARTED, ALL ->
                    HealthCheckResponse.Status.UP;
            };
        }

        public boolean filter(HealthCheckInfo healthCheck) {
            return switch (this) {
                case LIVE ->
                    healthCheck.kind().contains(HealthCheckInfo.Kind.LIVE);
                case READY ->
                    healthCheck.kind().contains(HealthCheckInfo.Kind.READY);
                case STARTED ->
                    healthCheck.kind().contains(HealthCheckInfo.Kind.STARTUP);
                case ALL ->
                    true;
            };
        }
    }

    public HealthReport getReport(ReportKind reportKind) {
        HealthCheckResponse.Status emptyResponse = reportKind.getEmptyResponse();

        List<HealthCheckResponse> healthCheckResults = applicationHealthChecks.entrySet()
                .stream()
                .flatMap(entry -> {
                    String contextName = entry.getKey();
                    return entry.getValue()
                            .stream()
                            .filter(reportKind::filter)
                            .map(HealthCheckInfo::healthCheck)
                            .map(HealthReporter::callHealthCheck)
                            .map(response -> addContextToResponse(response, entry.getKey()));
                }).toList();

        HealthCheckResponse.Status overallStatus;
        if (healthCheckResults.isEmpty()) {
            overallStatus = emptyResponse;
        } else {
            overallStatus = healthCheckResults.stream()
                    .map(HealthCheckResponse::getStatus)
                    .filter(HealthCheckResponse.Status.DOWN::equals)
                    .findFirst()
                    .orElse(HealthCheckResponse.Status.UP);
        }
        return new HealthReport(overallStatus, healthCheckResults);
    }

    public void addHealthCheck(String contextName, HealthCheckInfo healthCheck) {
        applicationHealthChecks.computeIfAbsent(contextName, k -> new CopyOnWriteArrayList<>())
                .add(healthCheck);
    }

    public void removeAllHealthChecksFrom(String contextName) {
        applicationHealthChecks.remove(contextName);
    }

    private static Optional<HealthCheckResponse.Status> getValue(String value) {
        try {
            return ConfigProvider.getConfig()
                    .getOptionalValue(value, String.class)
                    .map(HealthCheckResponse.Status::valueOf);
        } catch (IllegalStateException e) {
            // Microprofile Config is not enabled for this application
            return Optional.empty();
        }
    }

}
