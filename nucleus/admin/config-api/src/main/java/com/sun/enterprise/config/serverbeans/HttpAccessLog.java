/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.jvnet.hk2.config.ConfigBeanProxy;

import java.beans.PropertyVetoException;
import java.io.Serializable;

/**
 *
 */

/* @XmlType(name = "") */

@Configured
public interface HttpAccessLog extends ConfigBeanProxy {

    /**
     * Gets the value of the logDirectory property.
     *
     * location of the access logs specified as a directory.This defaults to the domain.log-root, which by default is
     * ${INSTANCE_ROOT}/logs. Hence the default value for this attribute is ${INSTANCE_ROOT}/logs/access
     *
     * @return possible object is {@link String }
     */
    @Attribute(defaultValue = "access")
    public String getLogDirectory();

    /**
     * Sets the value of the logDirectory property.
     *
     * @param value allowed object is {@link String }
     */
    public void setLogDirectory(String value) throws PropertyVetoException;

    /**
     * Gets the value of the iponly property.
     *
     * If the IP address of the user agent should be specified or a DNS lookup should be done
     *
     * @return possible object is {@link String }
     */
    @Attribute
    public String getIponly();

    /**
     * Sets the value of the iponly property.
     *
     * @param value allowed object is {@link String }
     */
    public void setIponly(String value) throws PropertyVetoException;

}
