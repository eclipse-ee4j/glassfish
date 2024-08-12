/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.Named;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 * The jmx-connector element defines the configuration of a JSR 160 compliant remote JMX Connector.
 */
@Configured
@ReferenceConstraint(skipDuringCreation = true, payload = JmxConnector.class)
public interface JmxConnector extends ConfigBeanProxy, Named, PropertyBag, Payload {

    String PORT_PATTERN = "\\$\\{[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#]*}"
                          + "|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]"
                          + "|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]"
                          + "|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5]";

    /**
     * Gets the value of the {@code enabled} property.
     *
     * <p>Defines if this connector is enabled. For EE this must be enabled.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code protocol} property.
     *
     * <p>Defines the protocol that this jmx-connector should support.
     * Supported protocols are defined by Entity rjmx-protocol.
     * Other protocols can be used by user applications independently.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "rmi_jrmp")
    String getProtocol();

    /**
     * Sets the value of the {@code protocol} property.
     *
     * @param protocol allowed object is {@link String}
     */
    void setProtocol(String protocol) throws PropertyVetoException;

    /**
     * Gets the value of the {@code address} property.
     *
     * <p>Specifies the IP address or host-name.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getAddress();

    /**
     * Sets the value of the {@code address} property.
     *
     * @param address allowed object is {@link String}
     */
    void setAddress(String address) throws PropertyVetoException;

    /**
     * Gets the value of the {@code port} property.
     *
     * <p>Specifies the port of the jmx-connector-server. Note that jmx-service-url
     * is a function of protocol, port and address as defined by the JSR 160 1.0 Specification.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @Pattern(regexp = PORT_PATTERN, message = "{port-pattern}", payload = JmxConnector.class)
    String getPort();

    /**
     * Sets the value of the {@code port} property.
     *
     * @param port allowed object is {@link String}
     */
    void setPort(String port) throws PropertyVetoException;

    /**
     * Gets the value of the {@code acceptAll} property.
     *
     * <p>Determines whether the connection can be made on all the network interfaces.
     * A value of false implies that the connections only for this specific
     * address will be selected.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getAcceptAll();

    /**
     * Sets the value of the {@code acceptAll} property.
     *
     * @param acceptAll allowed object is {@link String}
     */
    void setAcceptAll(String acceptAll) throws PropertyVetoException;

    /**
     * Gets the value of the {@code authRealmName} property.
     *
     * <p>The name of the auth-realm in this config element that represents
     * the special administrative realm. All authentication (from administrative
     * GUI and CLI) will be handled by this realm.
     *
     * <p><strong>Note:</strong> This is deprecated since GlassFish v3 FCS.
     * Use similarly named attribute on admin-service. This will be used
     * only when the admin-service attribute in missing.
     *
     * @return String representing the name of auth realm.
     */
    @Deprecated
    @Attribute
    @NotNull
    @Pattern(regexp = NAME_REGEX)
    @ReferenceConstraint.RemoteKey(message = "{resourceref.invalid.auth-realm-name}", type = AuthRealm.class)
    String getAuthRealmName();

    /**
     * Sets the value of the {@code authRealmName} property.
     *
     * @param authRealmName allowed object is {@link String}
     */
    void setAuthRealmName(String authRealmName) throws PropertyVetoException;

    /**
     * Gets the value of the {@code securityEnabled} property.
     *
     * <p>Decides whether the transport layer security be used in jmx-connector.
     * If {@code true}, configure the {@code ssl} element.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getSecurityEnabled();

    /**
     * Sets the value of the {@code securityEnabled} property.
     *
     * @param securityEnabled allowed object is {@link String}
     */
    void setSecurityEnabled(String securityEnabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code ssl} property.
     *
     * @return possible object is {@link Ssl}
     */
    @Element
    Ssl getSsl();

    /**
     * Sets the value of the {@code ssl} property.
     *
     * @param ssl allowed object is {@link Ssl}
     */
    void setSsl(Ssl ssl) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();
}
