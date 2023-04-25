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

package org.glassfish.security.services.provider.authorization;

import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.glassfish.security.services.config.SecurityProviderConfig;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

@Configured
public interface AuthorizationProviderConfig extends SecurityProviderConfig, PropertyBag {

    /**
     * Gets the class name of the authorization provider.
     */
    @Attribute
    @NotNull
    @JavaClassName
    String getProviderClass();

    void setProviderClass(String providerClass) throws PropertyVetoException;

    /**
     * Configuration parameter indicating if the provider support policy deploy or not.
     *
     * @return {@code true} if supports policy deploy, {@code false} otherwise
     */
    @Attribute(defaultValue = "true")
    boolean getSupportPolicyDeploy();

    void setSupportPolicyDeploy(boolean supportPolicyDeploy) throws PropertyVetoException;

    /**
     * Configuration parameter to indicate the {@code version} of the provider.
     *
     * @return version of the provider
     */
    @Attribute
    String getVersion();

    void setVersion(String version) throws PropertyVetoException;

    /**
     * Gets the properties of the LoginModule.
     */
    @Override
    @Element
    List<Property> getProperty();

    /**
     * Gets the options of the LoginModule for use with JAAS Configuration.
     */
    default Map<String,?> getProviderOptions() {
        Map<String,String> providerOptions = new HashMap<>();
        for (Property property : getProperty()) {
            providerOptions.put(property.getName(), property.getValue());
        }
        return providerOptions;
    }
}
