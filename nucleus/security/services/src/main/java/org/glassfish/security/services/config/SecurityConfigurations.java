/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.modularity.annotation.HasNoDefaultConfiguration;
import com.sun.enterprise.config.serverbeans.DomainExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

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
    default <T extends SecurityConfiguration> List<T> getSecurityServicesByType(Class<T> type) {
        List<T> typedServices = new ArrayList<>();
        for (SecurityConfiguration securityServiceConfiguration : getSecurityServices()) {
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
    default <T extends SecurityConfiguration> T getDefaultSecurityServiceByType(Class<T> type) {
        for (SecurityConfiguration securityServiceConfiguration : getSecurityServices()) {
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
    default <T extends SecurityConfiguration> T getSecurityServiceByName(String name, Class<T> type) {
        for (SecurityConfiguration securityServiceConfiguration : getSecurityServices()) {
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
    default SecurityConfiguration getSecurityServiceByName(String name) {
        for (SecurityConfiguration securityServiceConfiguration : getSecurityServices()) {
            if (securityServiceConfiguration.getName().equals(name)) {
                return securityServiceConfiguration;
            }
        }
        return null;
    }
}
