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

import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

@Singleton
public class HealthReporter {

    private static final String MP_DEFAULT_STARTUP_EMPTY_RESPONSE = "mp.health.default.startup.empty.response";
    private static final String MP_DEFAULT_READINESS_EMPTY_RESPONSE = "mp.health.default.readiness.empty.response";
    private static final Logger LOGGER = Logger.getLogger(HealthReporter.class.getName());

    private final Map<String, List<HealthCheck>> applicationHealthChecks = new ConcurrentHashMap<>();

    private static HealthCheckResponse callHealthCheck(HealthCheck healthCheck) {
        try {
            return healthCheck.call();
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Health check failed", e);
            return buildHealthCheckResponse(healthCheck.getClass().getName(), e);
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
        LIVE,
        READY,
        STARTED,
        ALL;

        private Stream<HealthCheck> healthChecks() {
            Instance<HealthCheck> select = CDI.current().select(HealthCheck.class);
            return switch (this) {
                case LIVE -> select.select(Liveness.Literal.INSTANCE).stream();
                case READY -> select.select(Readiness.Literal.INSTANCE).stream();
                case STARTED -> select.select(Startup.Literal.INSTANCE).stream();
                case ALL -> Stream.of(
                        select.select(Liveness.Literal.INSTANCE).stream(),
                        select.select(Readiness.Literal.INSTANCE).stream(),
                        select.select(Startup.Literal.INSTANCE).stream()
                ).flatMap(s -> s);
            };
        }

        private HealthCheckResponse.Status getEmptyResponse() {
            return switch (this) {
                case LIVE -> getValue(MP_DEFAULT_STARTUP_EMPTY_RESPONSE)
                        .orElse(HealthCheckResponse.Status.UP);
                case READY -> getValue(MP_DEFAULT_READINESS_EMPTY_RESPONSE)
                        .orElse(HealthCheckResponse.Status.UP);
                case STARTED, ALL -> HealthCheckResponse.Status.UP;
            };
        }
    }

    public HealthReport getReport(ReportKind reportKind) {
        HealthCheckResponse.Status emptyResponse = HealthCheckResponse.Status.UP;

        Stream<HealthCheck> healthChecks = applicationHealthChecks.values()
                .stream()
                .flatMap(Collection::stream);
//                Stream.concat(reportKind.healthChecks(), applicationHealthChecks.stream());

        List<HealthCheckResponse> healthCheckResults = healthChecks
                .map(HealthReporter::callHealthCheck)
                .toList();

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


    public void addHealthCheck(String contextPath, HealthCheck healthCheck) {
        applicationHealthChecks.computeIfAbsent(contextPath, k -> new CopyOnWriteArrayList<>())
                .add(healthCheck);
//        applicationHealthChecks.add(healthCheck);
    }

    public void removeHealthCheck(String contextPath, HealthCheck healthCheck) {
        applicationHealthChecks.get(contextPath).remove(healthCheck);
    }

    public void removeAllHealthChecksFrom(String contextPath) {
        applicationHealthChecks.remove(contextPath);
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
