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

import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * The {@link javax.security.auth.spi.LoginModule} configuration used for a
 * security provider plugin.
 *
 * <p>Defines setup for standard JAAS Login Module Configuration.
 */
@Configured
public interface LoginModuleConfig extends SecurityProviderConfig, PropertyBag {

    /**
     * Gets the class name of the LoginModule.
     */
    @Attribute(required = true)
    @NotNull
    @JavaClassName
    String getModuleClass();

    void setModuleClass(String moduleClass) throws PropertyVetoException;

    /**
     * Gets the JAAS control flag of the LoginModule.
     */
    @Attribute(required = true)
    @NotNull
    String getControlFlag();

    void setControlFlag(String controlFlag) throws PropertyVetoException;

    /**
     * Gets the properties of the LoginModule.
     */
    @Element
    List<Property> getProperty();

    /**
     * Gets the options of the LoginModule for use with JAAS Configuration.
     */
    default Map<String,?> getModuleOptions() {
        Map<String,String> moduleOptions = new HashMap<>();
        for (Property property : getProperty()) {
            moduleOptions.put(property.getName(), property.getValue());
        }
        return moduleOptions;
    }
}
