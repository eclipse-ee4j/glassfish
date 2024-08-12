/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.jms.config;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-jms-host"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-jms-host")
})
public interface JmsHost extends ConfigExtension, PropertyBag, Payload {

    String PORT_PATTERN = "\\$\\{[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#]*\\}"
            + "|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]"
            + "|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]"
            + "|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5]";

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the host property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    String getHost();

    /**
     * Sets the value of the host property.
     *
     * ip V6 or V4 address or hostname
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setHost(String value) throws PropertyVetoException;

    /**
     * Gets the value of the port property.
     *
     * Port number used by the JMS service
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue = "7676")
    @Pattern(regexp = PORT_PATTERN, message = "{port-pattern}", payload = JmsHost.class)
    String getPort();

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of lazyInit property
     *
     * if false, this listener is started during server startup
     *
     * @return true or false
     */
    @Attribute(defaultValue="true", dataType=Boolean.class)
    String getLazyInit();

    /**
     * Sets the value of lazyInit property
     *
     * Specify is this listener should be started as part of server startup or not
     *
     * @param value true if the listener is to be started lazily; false otherwise
     */
    void setLazyInit(String value);

    /**
     * Gets the value of the adminUserName property.
     *
     * Specifies the admin username
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="admin")
    String getAdminUserName();

    /**
     * Sets the value of the adminUserName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAdminUserName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the adminPassword property.
     *
     * Attribute specifies the admin password
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="admin")
    String getAdminPassword();

    /**
     * Sets the value of the adminPassword property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAdminPassword(String value) throws PropertyVetoException;

    /**
        Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @Override
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
