/*
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

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.api.admin.config.PropertiesDesc;
import static org.glassfish.config.support.Constants.NAME_REGEX;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

/**
 * The provider-config element defines the configuration of an authentication provider
 *
 * A provider-config with no contained request-policy or response-policy sub-elements, is a null provider. The container
 * will not instantiate or invoke the methods of a null provider, and as such the implementation class of a null
 * provider need not exist
 */

/* @XmlType(name = "", propOrder = {
    "requestPolicy",
    "responsePolicy",
    "property"
}) */

@Configured
public interface ProviderConfig extends ConfigBeanProxy, PropertyBag {

    /**
     * Gets the value of the providerId property.
     *
     * Identifier used to uniquely identify this provider-config element
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @Pattern(regexp = NAME_REGEX)
    @NotNull
    public String getProviderId();

    /**
     * Sets the value of the providerId property.
     *
     * @param value allowed object is {@link String }
     */
    public void setProviderId(String value) throws PropertyVetoException;

    /**
     * Gets the value of the providerType property.
     *
     * Defines whether the provider is a client authentication provider or a server authentication provider.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @NotNull
    @Pattern(regexp = "(client|server|client-server)")
    public String getProviderType();

    /**
     * Sets the value of the providerType property.
     *
     * @param value allowed object is {@link String }
     */
    public void setProviderType(String value) throws PropertyVetoException;

    /**
     * Gets the value of the className property.
     *
     * Defines the java implementation class of the provider.
     *
     * Client authentication providers must implement the com.sun.enterprise.security.jauth.ClientAuthModule interface.
     *
     * Server-side providers must implement the com.sun.enterprise.security.jauth.ServerAuthModule interface.
     *
     * A provider may implement both interfaces, but it must implement the interface corresponding to its provider type.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @NotNull
    @JavaClassName
    public String getClassName();

    /**
     * Sets the value of the className property.
     *
     * @param value allowed object is {@link String }
     */
    public void setClassName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the requestPolicy property.
     *
     * Defines the authentication policy requirements associated with request processing performed by the authentication
     * provider
     *
     * @return possible object is {@link RequestPolicy }
     */
    @Element
    public RequestPolicy getRequestPolicy();

    /**
     * Sets the value of the requestPolicy property.
     *
     * @param value allowed object is {@link RequestPolicy }
     */
    public void setRequestPolicy(RequestPolicy value) throws PropertyVetoException;

    /**
     * Gets the value of the responsePolicy property.
     *
     * Defines the authentication policy requirements associated with the response processing performed by the
     * authentication provider.
     *
     * @return possible object is {@link ResponsePolicy }
     */
    @Element
    public ResponsePolicy getResponsePolicy();

    /**
     * Sets the value of the responsePolicy property.
     *
     * @param value allowed object is {@link ResponsePolicy }
     */
    public void setResponsePolicy(ResponsePolicy value) throws PropertyVetoException;

    /**
     * Properties.
     */
    @PropertiesDesc(props = {
            @PropertyDesc(name = "security.config", defaultValue = "${com.sun.aas.instanceRoot}/config/wss-server-config-1.0.xml", description = "Specifies the location of the message security configuration file"),

            @PropertyDesc(name = "debug", defaultValue = "false", dataType = Boolean.class, description = "Enables dumping of server provider debug messages to the server log"),

            @PropertyDesc(name = "dynamic.username.password", defaultValue = "false", dataType = Boolean.class, description = "Signals the provider runtime to collect the user name and password from the "
                    + "CallbackHandler for each request. If false, the user name and password for wsse:UsernameToken(s) is "
                    + "collected once, during module initialization. Applicable only for a ClientAuthModule"),

            @PropertyDesc(name = "encryption.key.alias", defaultValue = "s1as", description = "Specifies the encryption key used by the provider. The key is identified by its keystore alias"),

            @PropertyDesc(name = "signature.key.alias", defaultValue = "s1as", description = "Specifies the signature key used by the provider. The key is identified by its keystore alias") })
    @Element
    List<Property> getProperty();
}
