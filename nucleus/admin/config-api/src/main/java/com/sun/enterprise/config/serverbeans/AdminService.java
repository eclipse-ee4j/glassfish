/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.serverbeans;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Admin Service exists in every instance. It is the configuration for either a normal server,
 * DAS or PE instance.
 */
@Configured
public interface AdminService extends ConfigBeanProxy, PropertyBag {

    String PATTERN_TYPE = "(das|das-and-server|server)";

    /**
     * Gets the value of the {@code type} property. An instance can either be of type das
     * Domain Administration Server in SE/EE or the PE instance das-and-server same as
     * das server Any non-DAS instance in SE/EE. Not valid for PE.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "server")
    @Pattern(regexp = PATTERN_TYPE, message = "Pattern: " + PATTERN_TYPE)
    String getType();

    /**
     * Sets the value of the {@code type} property.
     *
     * @param type allowed object is {@link String}
     */
    void setType(String type) throws PropertyVetoException;

    /**
     * Gets the value of the {@code systemJmxConnectorName} property.
     * The name of the internal jmx connector.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getSystemJmxConnectorName();

    /**
     * Sets the value of the {@code systemJmxConnectorName} property.
     *
     * @param systemJmxConnectorName allowed object is {@link String}
     */
    void setSystemJmxConnectorName(String systemJmxConnectorName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code jmxConnector} property. The {@code jmx-connector}
     * element defines the configuration of a JSR 160 compliant remote JMX Connector.
     * Objects of the following type(s) are allowed in the list {@link JmxConnector}
     */
    @Element("jmx-connector")
    List<JmxConnector> getJmxConnector();

    /**
     * Gets the value of the {@code dasConfig} property.
     *
     * @return possible object is {@link DasConfig}
     */
    @Element("das-config")
    @NotNull
    DasConfig getDasConfig();

    /**
     * Sets the value of the {@code dasConfig} property.
     *
     * @param dasConfig allowed object is {@link DasConfig}
     */
    void setDasConfig(DasConfig dasConfig) throws PropertyVetoException;

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    /**
     * Gets the name of the auth realm to be used for administration. This obsoletes/deprecates
     * the similarly named attribute on JmxConnector. Note that this is of essence where admin
     * access is done outside the containers. Container managed security is still applicable
     * and is handled via security annotations and deployment descriptors of the admin
     * applications (aka admin GUI application, MEjb application).
     *
     * @return name of the auth realm to be used for admin access
     */
    @Attribute(defaultValue = "admin-realm")
    @NotNull
    String getAuthRealmName();

    void setAuthRealmName(String authRealmName);

    default JmxConnector getSystemJmxConnector() {
        for (JmxConnector connector : getJmxConnector()) {
            if (getSystemJmxConnectorName().equals(connector.getName())) {
                return connector;
            }
        }
        return null;
    }

    /**
     * This is the place where the iteration for the {@link AuthRealm} for administration
     * should be carried out in server. A convenience method for the same.
     *
     * @return {@link AuthRealm} instance for which the name is same as {@link #getAuthRealmName()},
     * {@code null} otherwise.
     */
    default AuthRealm getAssociatedAuthRealm() {
        //this is the name of admin-service@auth-realm-name
        String realmName = getAuthRealmName();
        //assumes the structure where <admin-service> resides directly under <config>
        Config config = getParent(Config.class);
        List<AuthRealm> realms = config.getSecurityService().getAuthRealm();
        for (AuthRealm realm : realms) {
            if (realmName.equals(realm.getName())) {
                return realm;
            }
        }
        return null;
    }

    /**
     * Returns true if the classname of associated authrealm is same as fully qualified
     * FileRealm classname.
     *
     * @return {@code true} if associated authrealm is non-null and its classname equals
     * {@code com.sun.enterprise.security.auth.realm.file.FileRealm}, {@code false} otherwise
     */
    default boolean usesFileRealm() {
        boolean usesFileRealm = false;
        AuthRealm authRealm = getAssociatedAuthRealm();
        // Note: This is type unsafe.
        if (authRealm != null && "com.sun.enterprise.security.auth.realm.file.FileRealm".equals(authRealm.getClassname())) {
            usesFileRealm = true;
        }
        return usesFileRealm;
    }
}
