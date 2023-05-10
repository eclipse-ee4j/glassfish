/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.concurrent.config;

import java.beans.PropertyVetoException;
import java.util.List;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.types.Property;
import org.jvnet.hk2.config.types.PropertyBag;

/**
 * Concurrency resource base class
 */
@Configured
public interface ConcurrencyResource extends PropertyBag, ConfigBeanProxy {

    /**
     * Gets the value of the contextInfoEnabled property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "true", dataType = Boolean.class)
    String getContextInfoEnabled();

    /**
     * Sets the value of the contextInfoEnabled property.
     *
     * @param value allowed object is {@link String}
     */
    void setContextInfoEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the contextInfo property.
     *
     * @return possible object is {@link String}
     */
    @Attribute(defaultValue = "Classloader,JNDI,Security,WorkArea")
    String getContextInfo();

    /**
     * Sets the value of the contextInfo property.
     *
     * @param value allowed object is {@link String}
     */
    void setContextInfo(String value) throws PropertyVetoException;

    /**
     * Gets the value of the description property.
     *
     * @return possible object is {@link String}
     */
    @Attribute
    String getDescription();

    /**
     * Sets the value of the description property.
     *
     * @param value allowed object is {@link String}
     */
    void setDescription(String value) throws PropertyVetoException;

    /**
     * Properties as per {@link PropertyBag}
     */
    @Override
    @Element
    List<Property> getProperty();
}
