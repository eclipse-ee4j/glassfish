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
package org.glassfish.microprofile.telemetry.rest;

import jakarta.annotation.Priority;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.core.Configuration;
import jakarta.ws.rs.core.FeatureContext;

import org.glassfish.jersey.internal.spi.AutoDiscoverable;
import org.glassfish.jersey.internal.spi.ForcedAutoDiscoverable;

/**
 * Registers the telemetry of Jakarta REST servers and clients, including MicroProfile REST
 * Clients, in every Jersey runtime.
 * <p>
 * The runtimes are configured by the application threads, so the context class loader
 * identifies the application whose OpenTelemetry instance is used.
 */
@Priority(AutoDiscoverable.DEFAULT_PRIORITY)
public final class TelemetryAutoDiscoverable implements ForcedAutoDiscoverable {

    @Override
    public void configure(FeatureContext context) {
        Configuration configuration = context.getConfiguration();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (configuration.getRuntimeType() == RuntimeType.SERVER) {
            if (!configuration.isRegistered(ServerTelemetryListener.class)) {
                context.register(new ServerTelemetryListener(classLoader));
            }
        } else if (!configuration.isRegistered(ClientTelemetryFilter.class)) {
            context.register(new ClientTelemetryFilter(classLoader));
        }
    }
}
