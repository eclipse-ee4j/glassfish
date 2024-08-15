/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config.types;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlID;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Property type definition.
 *
 * @author Jerome Dochez
 */
@Configured
public interface Property extends ConfigBeanProxy  {

    /**
     * Gets the value of the {@code name} property.
     *
     * @return possible object is {@link String}
     */
    @XmlAttribute(required = true)
    @XmlID
    @Attribute(required = true, key = true)
    String getName();

    /**
     * Sets the value of the {@code name} property.
     *
     * @param name allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setName(String name) throws PropertyVetoException;

    /**
     * Gets the value of the {@code value} property.
     *
     * @return possible object is {@link String}
     */
    @XmlAttribute(required = true)
    @Attribute(required = true)
    String getValue();

    /**
     * Sets the value of the {@code value} property.
     *
     * @param value allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setValue(String value) throws PropertyVetoException;

    /**
     * Gets the value of the {@code description} property.
     *
     * @return possible object is {@link String}
     */
    @XmlAttribute
    @Attribute
    String getDescription();

    /**
     * Sets the value of the {@code description} property.
     *
     * @param description allowed object is {@link String}
     * @throws PropertyVetoException if a listener vetoes the change
     */
    void setDescription(String description) throws PropertyVetoException;
}
