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

import jakarta.validation.constraints.Max;

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
 *
 */

/* @XmlType(name = "", propOrder = {
    "property"
}) */

@Configured
public interface ManagerProperties extends ConfigBeanProxy, PropertyBag {


    /**
     * Gets the value of the sessionFileName property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSessionFileName();

    /**
     * Sets the value of the sessionFileName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSessionFileName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the reapIntervalInSeconds property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="60",dataType=Integer.class)
    public String getReapIntervalInSeconds();

    /**
     * Sets the value of the reapIntervalInSeconds property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setReapIntervalInSeconds(String value) throws PropertyVetoException;

    /**
     * Gets the value of the maxSessions property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(defaultValue="-1")
    @Max(value=Integer.MAX_VALUE)
    public String getMaxSessions();

    /**
     * Sets the value of the maxSessions property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setMaxSessions(String value) throws PropertyVetoException;

    /**
     * Gets the value of the sessionIdGeneratorClassname property.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    public String getSessionIdGeneratorClassname();

    /**
     * Sets the value of the sessionIdGeneratorClassname property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setSessionIdGeneratorClassname(String value) throws PropertyVetoException;


    /**
        Properties as per {@link org.jvnet.hk2.config.types.PropertyBag}
     */
    @ToDo(priority=ToDo.Priority.IMPORTANT, details="Provide PropertyDesc for legal props" )
    @PropertiesDesc(props={})
    @Element
    List<Property> getProperty();
}
