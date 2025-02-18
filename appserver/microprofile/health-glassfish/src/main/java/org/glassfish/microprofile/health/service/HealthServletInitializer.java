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
package org.glassfish.microprofile.health.service;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;

import java.util.Set;
import java.util.logging.Logger;

public class HealthServletInitializer implements ServletContainerInitializer {

    private static final String MICROPROFILE_HEALTH_SERVLET = "microprofile-health-servlet";
    private static final String MICROPROFILE_HEALTH_ENABLED = "org.glassfish.microprofile.health.enabled";
    private static final Logger LOGGER = Logger.getLogger(HealthServletInitializer.class.getName());

    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) {
         if (!servletContext.getContextPath().isEmpty()) {
            return;
        }

        if (!Boolean.parseBoolean(System.getProperty(MICROPROFILE_HEALTH_ENABLED, "true"))) {
            LOGGER.info("MicroProfile Health is disabled");
            return;
        }

        servletContext.addServlet(MICROPROFILE_HEALTH_SERVLET, HealthServlet.class)
            .addMapping("/health", "/health/live", "/health/ready", "/health/started");
    }
}
