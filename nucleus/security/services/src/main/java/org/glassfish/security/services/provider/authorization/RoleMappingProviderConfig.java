/*
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.NotNull;

import org.glassfish.security.services.config.SecurityProviderConfig;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

@Configured
public interface RoleMappingProviderConfig extends SecurityProviderConfig, PropertyBag {

    /**
     * Gets the class name of the role provider.
     */
    @Attribute(required=false)
    @NotNull
    @JavaClassName String getProviderClass();
    void setProviderClass(String value) throws PropertyVetoException;

    /**
     * Indicates if the provider supports role deployment.
     */
    @Attribute(defaultValue = "true")
    boolean getSupportRoleDeploy();
    void setSupportRoleDeploy(boolean value) throws PropertyVetoException;

    /**
     * Gets the version of the provider.
     */
    @Attribute(required=false)
    String getVersion();
    void setVersion(String value) throws PropertyVetoException;

    /**
     * Gets the properties of the provider.
     */
    @Override
    @Element
    List<Property> getProperty();

    /**
     * Gets the options of the provider.
     */
    @DuckTyped
    Map<String,?> getProviderOptions();

    class Duck {
        /**
         * Gets the options of the provider by looking at the properties.
         */
        public static Map<String,?> getProviderOptions(RoleMappingProviderConfig config) {
            Map<String,String> providerOptions = new HashMap<>();
            for (Property prop : config.getProperty()) {
                providerOptions.put(prop.getName(), prop.getValue());
            }
            return providerOptions;
        }
    }
}
