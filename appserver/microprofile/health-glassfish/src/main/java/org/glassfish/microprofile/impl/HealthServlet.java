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
package org.glassfish.microprofile.impl;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.health.HealthCheckResponse;
import org.glassfish.internal.api.Globals;
import org.glassfish.microprofile.health.HealthReport;
import org.glassfish.microprofile.health.HealthReporter;

public class HealthServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(HealthServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HealthReport healthReport;
        try {
            healthReport = Globals.getDefaultHabitat().getService(HealthReporter.class)
                    .getReport(getReportKind(req.getRequestURI()));

            int httpStatus = switch (healthReport.status()) {
                case UP -> HttpServletResponse.SC_OK;
                case DOWN -> HttpServletResponse.SC_SERVICE_UNAVAILABLE;
            };
            resp.setStatus(httpStatus);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to fetch health check status", e);
            healthReport = new HealthReport(HealthCheckResponse.Status.DOWN, List.of());
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        Jsonb jsonb = JsonbBuilder.create();
        String json = jsonb.toJson(healthReport);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");
        resp.getWriter().println(json);
    }

    private static HealthReporter.ReportKind getReportKind(String path) {
        if (path.endsWith("health/live")) {
            return HealthReporter.ReportKind.LIVE;
        } else if (path.endsWith("health/ready")) {
            return HealthReporter.ReportKind.READY;
        } else if (path.endsWith("health/started")) {
            return HealthReporter.ReportKind.STARTED;
        } else {
            return HealthReporter.ReportKind.ALL;
        }
    }
}
