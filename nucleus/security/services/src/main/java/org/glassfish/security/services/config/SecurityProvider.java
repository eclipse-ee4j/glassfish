/*
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
 * The configuration that is used by a security service to identify the
 * set of security providers which are created and consumed by the service.
 */
@Configured
public interface SecurityProvider extends ConfigBeanProxy {
    /**
     * Gets the name of the security provider.
     * The name represents the qualifier @Named given to the security provider.
     */
    @Attribute
    @NotNull
    public String getName();
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the type of the security provider.
     * The type represents a String used by the security service for the security provider interface.
     */
    @Attribute(required=true)
    @NotNull
    String getType();
    void setType(String type) throws PropertyVetoException;

    /**
     * Gets the name of the security provider configuration instance.
     * The provider name is used to reference specific provider configuration objects.
     */
    @Attribute(required=true, key=true)
    @NotNull
    String getProviderName();
    void setProviderName(String name) throws PropertyVetoException;

    /**
     * Gets configuration object specific to the security provider implementation.
     * Security provider configuration must extend the SecurityProviderConfig interface.
     */
    @Element("*")
    List<SecurityProviderConfig> getSecurityProviderConfig();
}
