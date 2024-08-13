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

package org.glassfish.webservices.config;

import jakarta.validation.constraints.NotNull;

import java.beans.PropertyVetoException;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;

/**
 * Specifies configuration for a XSLT transformation rule
 */

/* @XmlType(name = "") */

@Configured
public interface TransformationRule extends ConfigBeanProxy  {

    /**
     * Gets the value of the name property.
     *
     * Name of the transformation rule
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute(key=true)
    @NotNull
    public String getName();

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) throws PropertyVetoException;

    /**
     * Gets the value of the enabled property.
     *
     * If false, this transformation rule is disabled
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="true",dataType=Boolean.class)
    public String getEnabled();

    /**
     * Sets the value of the enabled property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setEnabled(String value) throws PropertyVetoException;

    /**
     * Gets the value of the applyTo property.
     *
     * - "request": transformations are applied to request in the order
     *   in which they are specified.
     * - "response": transformation is applied to response in the order in
         which they are specified.
     * - "both": transformation rule is applied to request and response. The
     *   order is reversed for response.
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute (defaultValue="request")
    public String getApplyTo();

    /**
     * Sets the value of the applyTo property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setApplyTo(String value) throws PropertyVetoException;

    /**
     * Gets the value of the ruleFileLocation property.
     *
     * Location of rule file to do transformation. Only XSLT files are allowed.
     * Default is:
     * ${com.sun.aas.instanceRoot}/generated/xml/<appOrModule>/<xslt-ilename>/
     * Absolute paths can also be specified
     *
     * @return possible object is
     *         {@link String }
     */
    @Attribute
    @NotNull
    public String getRuleFileLocation();

    /**
     * Sets the value of the ruleFileLocation property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setRuleFileLocation(String value) throws PropertyVetoException;

}
