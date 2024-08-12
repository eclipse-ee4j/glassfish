/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.security.services.impl;

import com.sun.enterprise.config.serverbeans.Domain;

import java.util.List;

import org.glassfish.security.services.config.SecurityConfiguration;
import org.glassfish.security.services.config.SecurityConfigurations;

/**
 * The base security service factory class.
 */
public class ServiceFactory {
    /**
     * Get the security service configuration for the specified service type.
     *
     * Attempt to obtain the service configuration marked as default
     * otherwise use the first configured service instance.
     *
     * @param domain The current Domain configuration object
     * @param type The type of the security service configuration
     *
     * @return null when no service configurations are found
     */
    public static <T extends SecurityConfiguration> T getSecurityServiceConfiguration(Domain domain, Class<T> type) {
        T config = null;

        // Look for security service configurations
        SecurityConfigurations secConfigs = domain.getExtensionByType(SecurityConfigurations.class);
        if (secConfigs != null) {
            // Look for the service configuration marked default
            config = secConfigs.getDefaultSecurityServiceByType(type);
            if (config == null) {
                // Obtain the first service configuration listed
                List<T> configs = secConfigs.getSecurityServicesByType(type);
                if (!configs.isEmpty()) {
                    config = configs.get(0);
                }
            }
        }

        // Return the service configuration
        return config;
    }
}
