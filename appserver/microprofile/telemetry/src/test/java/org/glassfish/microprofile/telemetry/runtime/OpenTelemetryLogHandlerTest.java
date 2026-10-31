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
package org.glassfish.microprofile.telemetry.runtime;

import io.opentelemetry.api.logs.Severity;

import java.util.logging.Level;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class OpenTelemetryLogHandlerTest {

    @Test
    void severityOfLevels() {
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.SEVERE), equalTo(Severity.ERROR));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.WARNING), equalTo(Severity.WARN));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.INFO), equalTo(Severity.INFO));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.CONFIG), equalTo(Severity.DEBUG4));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.FINE), equalTo(Severity.DEBUG));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.FINER), equalTo(Severity.TRACE4));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.FINEST), equalTo(Severity.TRACE));
        assertThat(OpenTelemetryLogHandler.toSeverity(Level.ALL), equalTo(Severity.UNDEFINED_SEVERITY_NUMBER));
    }
}
