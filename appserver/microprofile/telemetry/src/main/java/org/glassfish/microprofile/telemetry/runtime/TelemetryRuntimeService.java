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

import io.opentelemetry.api.OpenTelemetry;

import jakarta.inject.Inject;

import org.glassfish.api.StartupRunLevel;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.microprofile.telemetry.TelemetryInstances;
import org.jvnet.hk2.annotations.Service;

/**
 * Creates the runtime OpenTelemetry SDK instance at startup when {@code otel.sdk.disabled=false}
 * is set as a system property or as the {@code OTEL_SDK_DISABLED} environment variable, and
 * provides the runtime telemetry: JVM metrics and logs.
 */
@Service(name = "microprofile-telemetry-runtime")
@RunLevel(StartupRunLevel.VAL)
public class TelemetryRuntimeService implements PostConstruct, PreDestroy {

    @Inject
    private ClassLoaderHierarchy classLoaderHierarchy;

    private JvmMetrics jvmMetrics;

    @Override
    public void postConstruct() {
        if (!TelemetryInstances.isRuntimeEnabled()) {
            return;
        }
        // The common class loader sees the service providers of all modules and of domain/lib
        OpenTelemetry openTelemetry = TelemetryInstances.runtime(classLoaderHierarchy.getCommonClassLoader());
        jvmMetrics = JvmMetrics.register(openTelemetry);
        OpenTelemetryLogHandler.install();
    }

    @Override
    public void preDestroy() {
        if (jvmMetrics != null) {
            jvmMetrics.close();
            jvmMetrics = null;
        }
        OpenTelemetryLogHandler.uninstall();
        TelemetryInstances.shutdownRuntime();
    }
}
