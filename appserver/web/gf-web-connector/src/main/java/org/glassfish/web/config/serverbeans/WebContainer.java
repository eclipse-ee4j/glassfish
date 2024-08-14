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

package org.glassfish.web.config.serverbeans;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;
import java.util.List;

import org.glassfish.api.admin.config.ConfigExtension;
import org.glassfish.api.admin.config.PropertiesDesc;
import org.glassfish.api.admin.config.PropertyDesc;
import org.glassfish.config.support.datatypes.PositiveInteger;
import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;


@Configured
public interface WebContainer extends ConfigBeanProxy, PropertyBag, ConfigExtension {

    /**
     * Gets the value of the sessionConfig property.
     *
     * @return possible object is
     *         {@link SessionConfig }
     */
    @Element
    @NotNull
    SessionConfig getSessionConfig();

    /**
     * Sets the value of the sessionConfig property.
     *
     * @param value allowed object is
     *              {@link SessionConfig }
     */
    void setSessionConfig(SessionConfig value) throws PropertyVetoException;

    @Attribute(defaultValue = "false")
    String getJspCachingEnabled();

    void setJspCachingEnabled(String value) throws PropertyVetoException;

    /**
        Properties.
     */
@PropertiesDesc(
    props={
        @PropertyDesc(name="dispatcher-max-depth", defaultValue="20", dataType=PositiveInteger.class,
            description="Prevents recursive include or forward statements from creating an infinite loop by setting a maximum " +
                "nested dispatch level. If this level is exceeded, the following message is written to the server log: " +
                "Exceeded maximum depth for nested request dispatches")
    }
    )
    @Element
    List<Property> getProperty();
}
