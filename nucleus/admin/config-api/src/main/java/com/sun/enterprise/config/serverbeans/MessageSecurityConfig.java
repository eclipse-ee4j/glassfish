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
import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;
import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 * Defines the message layer specific provider configurations of the application server. All of the providers within a
 * message-security-config element must be able to perform authentication processing at the message layer defined by the
 * value of the auth-layer attribute.
 */

/* @XmlType(name = "", propOrder = {
    "providerConfig"
}) */

@Configured
@RestRedirects({ @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-message-security-provider"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-message-security-provider") })
public interface MessageSecurityConfig extends ConfigBeanProxy {

    /**
     * Gets the value of the authLayer property. Values: "SOAP" or "HttpServlet".
     *
     * All of the providers within a message-security-config element must be able to perform authentication processing at
     * the message layer defined by the value of the auth-layer attribute.
     *
     * @return possible object is {@link String }
     */
    @Attribute(key = true)
    @NotNull
    public String getAuthLayer();

    /**
     * Sets the value of the authLayer property.
     *
     * @param value allowed object is {@link String }
     */
    public void setAuthLayer(String value) throws PropertyVetoException;

    /**
     * Gets the value of the defaultProvider property.
     *
     * Used to identify the server provider to be invoked for any application for which a specific server provider has not
     * been bound.
     *
     * When a default provider of a type is not defined for a message layer, the container will only invoke a provider of
     * the type (at the layer) for those applications for which a specific provider has been bound.
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Pattern(regexp = NAME_REGEX)
    public String getDefaultProvider();

    /**
     * Sets the value of the defaultProvider property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDefaultProvider(String value) throws PropertyVetoException;

    /**
     * Gets the value of the defaultClientProvider property.
     *
     * Used to identify the client provider to be invoked for any application for which a specific client provider has not
     * been bound
     *
     * @return possible object is {@link String }
     */
    @Attribute
    @Pattern(regexp = NAME_REGEX)
    public String getDefaultClientProvider();

    /**
     * Sets the value of the defaultClientProvider property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDefaultClientProvider(String value) throws PropertyVetoException;

    /**
     * Gets the value of the providerConfig property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for the
     * providerConfig property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     *
     * <pre>
     * getProviderConfig().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list {@link ProviderConfig }
     */
    @Element(required = true)
    public List<ProviderConfig> getProviderConfig();
}
