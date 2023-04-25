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

package org.glassfish.resources.mail.config;

import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.customvalidators.JavaClassName;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.admin.cli.resources.ResourceConfigCreator;
import org.glassfish.admin.cli.resources.UniqueResourceNameConstraint;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.RestRedirect;
import org.glassfish.quality.ToDo;
import org.glassfish.resourcebase.resources.ResourceDeploymentOrder;
import org.glassfish.resourcebase.resources.ResourceTypeOrder;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * The mail-resource element describes a {@link jakarta.mail.Session} resource.
 */
@Configured
@ResourceConfigCreator(commandName="create-mail-resource")
@RestRedirects({
        @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-mail-resource"),
        @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-mail-resource")
})
@ResourceTypeOrder(deploymentOrder = ResourceDeploymentOrder.MAIL_RESOURCE)
@UniqueResourceNameConstraint(message = "{resourcename.isnot.unique}", payload = MailResource.class)
public interface MailResource extends ConfigBeanProxy, Resource, PropertyBag, BindableResource {

    /**
     * Gets the value of the {@code storeProtocol} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "imap")
    String getStoreProtocol();

    /**
     * Sets the value of the {@code storeProtocol} property.
     *
     * @param protocol allowed object is {@link String}
     */
    void setStoreProtocol(String protocol) throws PropertyVetoException;

    /**
     * Gets the value of the {@code storeProtocolClass} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "com.sun.mail.imap.IMAPStore")
    @JavaClassName
    String getStoreProtocolClass();

    /**
     * Sets the value of the {@code storeProtocolClass} property.
     *
     * @param protocolClass allowed object is {@link String}
     */
    void setStoreProtocolClass(String protocolClass) throws PropertyVetoException;

    /**
     * Gets the value of the {@code transportProtocol} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "smtp")
    String getTransportProtocol();

    /**
     * Sets the value of the {@code transportProtocol} property.
     *
     * @param protocol allowed object is {@link String}
     */
    void setTransportProtocol(String protocol) throws PropertyVetoException;

    /**
     * Gets the value of the {@code transportProtocolClass} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "com.sun.mail.smtp.SMTPTransport")
    @JavaClassName
    String getTransportProtocolClass();

    /**
     * Sets the value of the {@code transportProtocolClass} property.
     *
     * @param protocolClass allowed object is {@link String}
     */
    void setTransportProtocolClass(String protocolClass) throws PropertyVetoException;

    /**
     * Gets the value of the {@code host} property.
     *
     * <p>IP V6 or V4 address or hostname
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getHost();

    /**
     * Sets the value of the {@code host} property.
     *
     * @param host allowed object is {@link String}
     */
    void setHost(String host) throws PropertyVetoException;

    /**
     * Gets the value of the {@code user} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getUser();

    /**
     * Sets the value of the {@code user} property.
     *
     * @param user allowed object is {@link String}
     */
    void setUser(String user) throws PropertyVetoException;

    /**
     * Gets the value of the {@code from} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    @NotNull
    String getFrom();

    /**
     * Sets the value of the {@code from} property.
     *
     * @param from allowed object is {@link String}
     */
    void setFrom(String from) throws PropertyVetoException;

    /**
     * Gets the value of the {@code debug} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "false", dataType = Boolean.class)
    String getDebug();

    /**
     * Sets the value of the {@code debug} property.
     *
     * @param debug allowed object is {@link String}
     */
    void setDebug(String debug) throws PropertyVetoException;

    /**
     * Gets the value of the {@code enabled} property.
     *
     * @return possible object is {@link String}
     */
    @Override
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the {@code enabled} property.
     *
     * @param enabled allowed object is {@link String}
     */
    @Override
    void setEnabled(String enabled) throws PropertyVetoException;

    /**
     * Gets the value of the {@code description} property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the {@code description} property.
     *
     * @param description allowed object is {@link String}
     */
    void setDescription(String description) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @ToDo(priority = ToDo.Priority.IMPORTANT, details = "Provide PropertyDesc for legal props")
    @PropertiesDesc(props = {})
    @Element
    List<Property> getProperty();

    @Override
    default String getIdentity() {
        return getJndiName();
    }
}
