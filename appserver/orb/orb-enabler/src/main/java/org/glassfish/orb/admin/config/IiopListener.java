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

package org.glassfish.orb.admin.config;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.RestRedirect;
import org.glassfish.api.admin.RestRedirects;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.quality.ToDo;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import static org.glassfish.config.support.Constants.NAME_REGEX;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "ssl",
    "property"
}) */

@Configured
@RestRedirects({
 @RestRedirect(opType = RestRedirect.OpType.POST, commandName = "create-iiop-listener"),
 @RestRedirect(opType = RestRedirect.OpType.DELETE, commandName = "delete-iiop-listener")
})
public interface IiopListener extends ConfigBeanProxy, PropertyBag, Payload {

    String PORT_PATTERN = "\\$\\{[\\p{L}\\p{N}_][\\p{L}\\p{N}\\-_./;#]*\\}"
            + "|[1-9]|[1-9][0-9]|[1-9][0-9][0-9]|[1-9][0-9][0-9][0-9]"
            + "|[1-5][0-9][0-9][0-9][0-9]|6[0-4][0-9][0-9][0-9]"
            + "|65[0-4][0-9][0-9]|655[0-2][0-9]|6553[0-5]";


    /**
     * Gets the value of the id property.
     *
     * if false, a configured listener, is disabled
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    @Pattern(regexp=NAME_REGEX)
    String getId();

    /**
     * Sets the value of the id property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setId(String value) throws PropertyVetoException;

    /**
     * Gets the value of the address property.
     *
     * ip V6 or V4 address or hostname
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    String getAddress();

    /**
     * Sets the value of the address property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setAddress(String value) throws PropertyVetoException;

    /**
     * Gets the value of the port property.
     *
     * Port number
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="1072")
    @Pattern(regexp=PORT_PATTERN,
            message="{port-pattern}",
            payload=IiopListener.class)
    String getPort();

    /**
     * Sets the value of the port property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setPort(String value) throws PropertyVetoException;

    /**
     * Gets the value of the securityEnabled property.
     *
     * Determines whether the iiop listener runs SSL. You can turn
     * SSL2 or SSL3 on or off and set ciphers using an ssl element
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="false")
    String getSecurityEnabled();

    /**
     * Sets the value of the securityEnabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setSecurityEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * if false, a configured listener, is disabled
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ssl property.
     *
     * Specifies optional SSL configuration. Note that the ssl2 ciphers are not
     * supported for iiop, and therefore must be disabled
     *
     * @return possible object is
     *         {@link Ssl }
     */
    @Element
    Ssl getSsl();

    /**
     * Sets the value of the ssl property.
     *
     * @param value allowed object is
     *              {@link Ssl }
     */
    void setSsl(Ssl value) throws PropertyVetoException;

    /**
     * Gets the value of lazyInit property
     *
     * if false, this listener is started during server startup
     *
     * @return true or false
     */
    @Attribute(defaultValue="false", dataType=Boolean.class)
    String getLazyInit();

    /**
     * Sets the value of lazyInit property
     *
     * Specify is this listener should be started as part of server startup or not
     *
     * @param value true if the listener is to be started lazily; false otherwise
     */
    void String(boolean value);

    /**
     * Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @Override
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
