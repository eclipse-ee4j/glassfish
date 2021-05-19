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

package org.glassfish.security.services.config;

import com.sun.enterprise.config.serverbeans.DomainExtension;
import com.sun.enterprise.config.modularity.annotation.HasNoDefaultConfiguration;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The top level security configuration which holds the list of configured security services.
 */
@Configured
@HasNoDefaultConfiguration
public interface SecurityConfigurations extends ConfigBeanProxy, DomainExtension {
    /**
     * Gets the list of configured security services.
     */
    @Element("*")
    List<SecurityConfiguration> getSecurityServices();

    /**
     * Gets the list of configured security services by security service type.
     */
    @DuckTyped
    <T extends SecurityConfiguration> List<T> getSecurityServicesByType(Class<T> type);

    /**
     * Gets the default configured security service by security service type.
     */
    @DuckTyped
    <T extends SecurityConfiguration> T getDefaultSecurityServiceByType(Class<T> type);

    /**
     * Gets a named security service configuration by specific security type.
     */
    @DuckTyped
    <T extends SecurityConfiguration> T getSecurityServiceByName(String name, Class<T> type);

    /**
     * Gets a named security service configuration.
     */
    @DuckTyped
    SecurityConfiguration getSecurityServiceByName(String name);

    class Duck {
        /**
         * Gets the list of configured security services by security service type.
         */
        public static <T extends SecurityConfiguration> List<T> getSecurityServicesByType(SecurityConfigurations services, Class<T> type) {
            List<T> typedServices = new ArrayList<T>();
            for (SecurityConfiguration securityServiceConfiguration : services.getSecurityServices()) {
                try {
                    if (type.isAssignableFrom(securityServiceConfiguration.getClass())) {
                        typedServices.add(type.cast(securityServiceConfiguration));
                    }
                } catch (Exception e) {
                    // ignore, not the right type.
                }
            }
                        return Collections.unmodifiableList(typedServices);
        }

        /**
         * Gets the default configured security service by security service type.
         */
            public static <T extends SecurityConfiguration> T getDefaultSecurityServiceByType(SecurityConfigurations services, Class<T> type) {
            for (SecurityConfiguration securityServiceConfiguration : services.getSecurityServices()) {
                try {
                    if (securityServiceConfiguration.getDefault()) {
                        return type.cast(securityServiceConfiguration);
                    }
                } catch (Exception e) {
                    // ignore, not the right type.
                }
            }
            return null;
        }

        /**
         * Gets a named security service configuration by specific security type.
         */
            public static <T extends SecurityConfiguration> T getSecurityServiceByName(SecurityConfigurations services, String name, Class<T> type) {
            for (SecurityConfiguration securityServiceConfiguration : services.getSecurityServices()) {
                try {
                    if (securityServiceConfiguration.getName().equals(name)) {
                        return type.cast(securityServiceConfiguration);
                    }
                } catch (Exception e) {
                    // ignore, not the right type.
                }
            }
            return null;
        }

        /**
         * Gets a named security service configuration.
         */
            public static SecurityConfiguration getSecurityServiceByName(SecurityConfigurations services, String name) {
            for (SecurityConfiguration securityServiceConfiguration : services.getSecurityServices()) {
                if (securityServiceConfiguration.getName().equals(name)) {
                    return securityServiceConfiguration;
                }
            }
            return null;
        }
    }
}
