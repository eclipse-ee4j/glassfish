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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.microprofile.health.HealthCheckResponse.Status;
import org.glassfish.microprofile.health.GlassFishHealthCheckResponse;
import org.glassfish.microprofile.health.HealthReport;
import org.junit.jupiter.api.Test;

import static org.glassfish.microprofile.health.HealthReporter.ReportKind.ALL;
import static org.glassfish.microprofile.health.HealthReporter.ReportKind.LIVE;
import static org.glassfish.microprofile.health.HealthReporter.ReportKind.READY;
import static org.glassfish.microprofile.health.HealthReporter.ReportKind.STARTED;
import static org.glassfish.microprofile.health.service.HealthService.DEFAULT_CONTEXT_PATH;
import static org.glassfish.microprofile.health.service.HealthService.toContextPath;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class HealthHttpHandlerTest {

    @Test
    public void reportKind() {
        HealthHttpHandler handler = new HealthHttpHandler(null, "/health", null);
        assertAll(
            () -> assertEquals(ALL, handler.getReportKind("/health")),
            () -> assertEquals(LIVE, handler.getReportKind("/health/live")),
            () -> assertEquals(READY, handler.getReportKind("/health/ready")),
            () -> assertEquals(STARTED, handler.getReportKind("/health/started")),
            () -> assertNull(handler.getReportKind("/health/")),
            () -> assertNull(handler.getReportKind("/health/other")),
            () -> assertNull(handler.getReportKind("/health/other/live")),
            () -> assertNull(handler.getReportKind("/healthy")),
            () -> assertNull(handler.getReportKind("/app/health/live")));
    }

    @Test
    public void reportKindWithCustomContextPath() {
        HealthHttpHandler handler = new HealthHttpHandler(null, "/mp/health", null);
        assertAll(
            () -> assertEquals(ALL, handler.getReportKind("/mp/health")),
            () -> assertEquals(READY, handler.getReportKind("/mp/health/ready")),
            () -> assertNull(handler.getReportKind("/health/ready")));
    }

    @Test
    public void contextPath() {
        assertAll(
            () -> assertEquals(DEFAULT_CONTEXT_PATH, toContextPath(null)),
            () -> assertEquals(DEFAULT_CONTEXT_PATH, toContextPath("")),
            () -> assertEquals(DEFAULT_CONTEXT_PATH, toContextPath("/")),
            () -> assertEquals(DEFAULT_CONTEXT_PATH, toContextPath(" // ")),
            () -> assertEquals("/status", toContextPath("status")),
            () -> assertEquals("/status", toContextPath("/status/")),
            () -> assertEquals("/mp/health", toContextPath(" mp/health ")));
    }

    @Test
    public void json() {
        HealthHttpHandler handler = new HealthHttpHandler(null, "/health", null);
        assertAll(
            () -> assertEquals("{\"checks\":[],\"status\":\"UP\"}",
                handler.toJson(new HealthReport(Status.UP, List.of())).toString()),
            () -> assertEquals("{\"checks\":[{\"name\":\"check\",\"status\":\"DOWN\",\"data\":{\"reason\":\"text\"}},"
                + "{\"name\":\"other\",\"status\":\"UP\"}],\"status\":\"DOWN\"}",
                handler.toJson(new HealthReport(Status.DOWN, List.of(
                    new GlassFishHealthCheckResponse("check", Status.DOWN, Optional.of(Map.of("reason", "text"))),
                    new GlassFishHealthCheckResponse("other", Status.UP, Optional.empty())))).toString()));
    }
}
