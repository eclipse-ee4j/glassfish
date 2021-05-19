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

import com.sun.enterprise.config.serverbeans.customvalidators.ReferenceConstraint;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.ConfigBeanProxy;
import static org.glassfish.config.support.Constants.NAME_SERVER_REGEX;

import java.beans.PropertyVetoException;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 *
 */

/* @XmlType(name = "", propOrder = {
    "healthChecker"
}) */

@Configured
@ReferenceConstraint(skipDuringCreation = true, payload = ServerRef.class)
public interface ServerRef extends ConfigBeanProxy, Ref, Payload {

    /**
     * Gets the value of the ref property.
     *
     * A reference to the name of a server defined elsewhere
     *
     * @return possible object is {@link String }
     */
    @Override
    @Attribute(key = true)
    @NotNull
    @Pattern(regexp = NAME_SERVER_REGEX, message = "{server.invalid.name}", payload = ServerRef.class)
    @ReferenceConstraint.RemoteKey(message = "{resourceref.invalid.server-ref}", type = Server.class)
    public String getRef();

    /**
     * Sets the value of the ref property.
     *
     * @param value allowed object is {@link String }
     */
    @Override
    public void setRef(String value) throws PropertyVetoException;

    /**
     * Gets the value of the disableTimeoutInMinutes property.
     *
     * The time, in minutes, that it takes this server to reach a quiescent state after having been disabled
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "30")
    public String getDisableTimeoutInMinutes();

    /**
     * Sets the value of the disableTimeoutInMinutes property.
     *
     * @param value allowed object is {@link String }
     */
    public void setDisableTimeoutInMinutes(String value) throws PropertyVetoException;

    /**
     * Gets the value of the lbEnabled property.
     *
     * Causes any and all load-balancers using this server to consider this server available to them. Defaults to
     * available(true)
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = LBENABLED_DEFAULT_VALUE, dataType = Boolean.class)
    public String getLbEnabled();

    /**
     * Sets the value of the lbEnabled property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLbEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * A boolean flag that causes the server to be enabled to serve end-users, or not. Default is to be enabled (true)
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the healthChecker property.
     *
     * @return possible object is {@link HealthChecker }
     */
    @Element("health-checker")
    public HealthChecker getHealthChecker();

    /**
     * Sets the value of the healthChecker property.
     *
     * @param value allowed object is {@link HealthChecker }
     */
    public void setHealthChecker(HealthChecker value) throws PropertyVetoException;

    //defines the default value for lb-enabled attribute
    public String LBENABLED_DEFAULT_VALUE = "true";

}
