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
package org.glassfish.microprofile.health.service;

import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.spi.JsonProvider;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.microprofile.health.HealthReport;
import org.glassfish.microprofile.health.HealthReporter;
import org.glassfish.microprofile.health.HealthReporter.ReportKind;

/**
 * Serves the MicroProfile Health endpoints directly from Grizzly, so that they are available
 * also when no application is deployed and the web container is not started.
 */
class HealthHttpHandler extends HttpHandler {

    private static final Logger LOGGER = Logger.getLogger(HealthHttpHandler.class.getName());

    private final HealthReporter healthReporter;
    private final String contextPath;
    private final ClassLoader classLoader;
    private volatile JsonProvider jsonProvider;

    /**
     * @param healthReporter provides the health reports
     * @param contextPath context path of the endpoint, starting with a slash
     * @param classLoader context class loader used to evaluate the health checks and to serialize
     *            the report, so that the JSON-P and MicroProfile Config implementations are found
     */
    HealthHttpHandler(HealthReporter healthReporter, String contextPath, ClassLoader classLoader) {
        super("MicroProfile Health");
        this.healthReporter = healthReporter;
        this.contextPath = contextPath;
        this.classLoader = classLoader;
    }

    @Override
    public void service(Request request, Response response) throws Exception {
        ReportKind reportKind = getReportKind(request.getDecodedRequestURI());
        if (reportKind == null) {
            response.sendError(HttpStatus.NOT_FOUND_404.getStatusCode());
            return;
        }
        Method method = request.getMethod();
        if (!Method.GET.equals(method) && !Method.HEAD.equals(method)) {
            response.setHeader(Header.Allow, "GET, HEAD");
            response.sendError(HttpStatus.METHOD_NOT_ALLOWED_405.getStatusCode());
            return;
        }

        Thread thread = Thread.currentThread();
        ClassLoader originalClassLoader = thread.getContextClassLoader();
        thread.setContextClassLoader(classLoader);
        try {
            HealthReport healthReport;
            try {
                healthReport = healthReporter.getReport(reportKind);
                response.setStatus(switch (healthReport.status()) {
                    case UP -> HttpStatus.OK_200;
                    case DOWN -> HttpStatus.SERVICE_UNAVAILABLE_503;
                });
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unable to fetch health check status", e);
                healthReport = new HealthReport(HealthCheckResponse.Status.DOWN, List.of());
                response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
            }
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.getWriter().write(toJson(healthReport).toString());
        } finally {
            thread.setContextClassLoader(originalClassLoader);
        }
    }

    /**
     * Serializes the report with JSON-P. JSON-B is not used, because its implementation looks up
     * a CDI container, which initializes Weld before the web container starts it.
     */
    JsonObject toJson(HealthReport healthReport) {
        JsonProvider json = getJsonProvider();
        JsonArrayBuilder checks = json.createArrayBuilder();
        for (HealthCheckResponse check : healthReport.checks()) {
            JsonObjectBuilder checkJson = json.createObjectBuilder()
                .add("name", String.valueOf(check.getName()))
                .add("status", String.valueOf(check.getStatus()));
            check.getData().filter(data -> !data.isEmpty()).ifPresent(data -> {
                JsonObjectBuilder dataJson = json.createObjectBuilder();
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    Object value = entry.getValue();
                    if (value instanceof Boolean booleanValue) {
                        dataJson.add(entry.getKey(), booleanValue);
                    } else if (value instanceof Long || value instanceof Integer) {
                        dataJson.add(entry.getKey(), ((Number) value).longValue());
                    } else {
                        dataJson.add(entry.getKey(), String.valueOf(value));
                    }
                }
                checkJson.add("data", dataJson);
            });
            checks.add(checkJson);
        }
        return json.createObjectBuilder()
            .add("checks", checks)
            .add("status", String.valueOf(healthReport.status()))
            .build();
    }

    private JsonProvider getJsonProvider() {
        if (jsonProvider == null) {
            jsonProvider = JsonProvider.provider();
        }
        return jsonProvider;
    }

    /**
     * @param path decoded request path
     * @return the kind of the report, or {@code null} if the path is not a health endpoint
     */
    ReportKind getReportKind(String path) {
        if (!path.startsWith(contextPath)) {
            return null;
        }
        return switch (path.substring(contextPath.length())) {
            case "" -> ReportKind.ALL;
            case "/live" -> ReportKind.LIVE;
            case "/ready" -> ReportKind.READY;
            case "/started" -> ReportKind.STARTED;
            default -> null;
        };
    }
}
