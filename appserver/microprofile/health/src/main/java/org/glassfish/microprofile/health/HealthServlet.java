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
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.health.Readiness;
import org.eclipse.microprofile.health.Startup;

public class HealthServlet extends HttpServlet {

    private static final String MP_DEFAULT_STARTUP_EMPTY_RESPONSE = "mp.health.default.startup.empty.response";
    private static final String MP_DEFAULT_READINESS_EMPTY_RESPONSE = "mp.health.default.readiness.empty.response";
    private static final Logger LOGGER = Logger.getLogger(HealthServlet.class.getName());

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

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Output output;

        try {
            HealthCheckResponse.Status emptyResponse = HealthCheckResponse.Status.UP;

            Instance<HealthCheck> select = CDI.current().select(HealthCheck.class);
            Stream<HealthCheck> healthChecks;
            String requestURI = req.getRequestURI();
            if (requestURI.endsWith("health/live")) {
                healthChecks = select.select(Liveness.Literal.INSTANCE).stream();
            } else if (requestURI.endsWith("health/ready")) {
                healthChecks = select.select(Readiness.Literal.INSTANCE).stream();
                emptyResponse = getValue(MP_DEFAULT_READINESS_EMPTY_RESPONSE)
                        .orElse(HealthCheckResponse.Status.DOWN);

            } else if (requestURI.endsWith("health/started")) {
                healthChecks = select.select(Startup.Literal.INSTANCE).stream();
                emptyResponse = getValue(MP_DEFAULT_STARTUP_EMPTY_RESPONSE)
                        .orElse(HealthCheckResponse.Status.DOWN);
            } else {
                healthChecks = Stream.of(
                        select.select(Liveness.Literal.INSTANCE).stream(),
                        select.select(Readiness.Literal.INSTANCE).stream(),
                        select.select(Startup.Literal.INSTANCE).stream()
                ).flatMap(s -> s);
            }


            List<HealthCheckResponse> healthCheckResults = healthChecks
                    .map(HealthServlet::callHealthCheck)
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
            output = new Output(overallStatus, healthCheckResults);

            int httpStatus = switch (overallStatus) {
                case UP -> HttpServletResponse.SC_OK;
                case DOWN -> HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            };
            resp.setStatus(httpStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to fetch health check status", e);
            output = new Output(HealthCheckResponse.Status.DOWN, List.of(buildHealthCheckResponse("error", e)));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(output);
        resp.getWriter().println(json);
    }

    private Optional<HealthCheckResponse.Status> getValue(String value) {
        try {
            return ConfigProvider.getConfig(getServletContext().getClassLoader())
                    .getOptionalValue(value, String.class)
                    .map(HealthCheckResponse.Status::valueOf);
        } catch (IllegalStateException e) {
            // Microprofile Config is not enabled for this application
            return Optional.empty();
        }
    }
}
