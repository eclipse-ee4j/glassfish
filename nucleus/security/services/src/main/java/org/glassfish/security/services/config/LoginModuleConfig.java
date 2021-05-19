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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.beans.PropertyVetoException;
import jakarta.validation.constraints.NotNull;
import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

/**
 * The LoginModule configuration used for a security provider plugin.
 *
 * Defines setup for standard JAAS LoginModule Configuration.
 */
@Configured
public interface LoginModuleConfig extends SecurityProviderConfig, PropertyBag {
    /**
     * Gets the class name of the LoginModule.
     */
    @Attribute(required=true)
    @NotNull
    @JavaClassName
    public String getModuleClass();
    public void setModuleClass(String value) throws PropertyVetoException;

    /**
     * Gets the JAAS control flag of the LoginModule.
     */
    @Attribute(required=true)
    @NotNull
    public String getControlFlag();
    public void setControlFlag(String value) throws PropertyVetoException;

    /**
     * Gets the properties of the LoginModule.
     */
    @Element
    List<Property> getProperty();

    /**
     * Gets the options of the LoginModule for use with JAAS Configuration.
     */
    @DuckTyped
    Map<String,?> getModuleOptions();

    class Duck {
        /**
         * Gets the options of the LoginModule for use with JAAS Configuration.
         */
        public static Map<String,?> getModuleOptions(LoginModuleConfig config) {
                Map<String,String> moduleOptions = new HashMap<String,String>();
            for (Property prop : config.getProperty()) {
                moduleOptions.put(prop.getName(), prop.getValue());
            }
            return moduleOptions;
        }
    }
}
