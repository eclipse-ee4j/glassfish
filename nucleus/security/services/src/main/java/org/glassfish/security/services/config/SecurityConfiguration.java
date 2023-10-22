/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;

/**
 * Base interface for all security service configurations.
 *
 * <p>Each security service configuration has a name, indication of the service
 * configuration being the default when multiple service configurations are
 * present and an optional list of the specific security provider plugins.
 */
@Configured
public interface SecurityConfiguration extends ConfigBeanProxy {

    /**
     * Gets the {@code name} of the security service instance.
     */
    @Attribute(required = true, key = true)
    @NotNull
    String getName();

    void setName(String name) throws PropertyVetoException;

    /**
     * Determine if this is the default instance.
     */
    @Attribute(defaultValue = "false")
    boolean getDefault();

    void setDefault(boolean defaultValue) throws PropertyVetoException;

    /**
     * Gets the list of the security provider plugins used by the security service.
     */
    @Element("security-provider")
    List<SecurityProvider> getSecurityProviders();

    /**
     * Gets a named security provider.
     */
    default SecurityProvider getSecurityProviderByName(String name) {
        for (SecurityProvider config : getSecurityProviders()) {
            if (config.getProviderName().equals(name)) {
                return config;
            }
        }
        return null;
    }
}
